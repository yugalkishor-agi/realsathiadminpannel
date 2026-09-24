import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

const encoder = new TextEncoder();
const headers = {
  "Content-Type": "application/json",
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Headers": "authorization, x-session-token, x-client-info, apikey, content-type",
  "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
};
const jsonResponse = (body: unknown, status = 200) => new Response(JSON.stringify(body), { status, headers });
const corsResponse = () => new Response("ok", { headers });
const createAdminClient = () => createClient(Deno.env.get("SUPABASE_URL")!, Deno.env.get("SUPABASE_SERVICE_ROLE_KEY")!);

function base64UrlDecode(value: string) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  return atob(normalized + "=".repeat((4 - (normalized.length % 4 || 4)) % 4));
}

async function authenticateRequest(req: Request) {
  const header = req.headers.get("X-Session-Token") || req.headers.get("Authorization") || "";
  const token = header.trim().replace(/^Bearer\s+/i, "");
  if (!token) throw new Error("Missing session token.");
  const [encodedHeader, encodedPayload, signature] = token.split(".");
  if (!encodedHeader || !encodedPayload || !signature) throw new Error("Malformed access token.");
  const key = await crypto.subtle.importKey("raw", encoder.encode(Deno.env.get("JWT_ACCESS_SECRET")!), { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  const expected = new Uint8Array(await crypto.subtle.sign("HMAC", key, encoder.encode(`${encodedHeader}.${encodedPayload}`)));
  let binary = ""; for (const byte of expected) binary += String.fromCharCode(byte);
  const expectedSignature = btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
  if (expectedSignature !== signature) throw new Error("Invalid access token signature.");
  const payload = JSON.parse(base64UrlDecode(encodedPayload)) as Record<string, unknown>;
  const userId = String(payload.sub ?? "").trim();
  if (!userId || Number(payload.exp ?? 0) <= Math.floor(Date.now() / 1000)) throw new Error("Access token expired.");
  const db = createAdminClient();
  const current = await db.from("users").select("session_version,account_status,role").eq("id", userId).maybeSingle();
  if (current.error || !current.data) throw new Error("Unable to validate session.");
  if (["blocked", "banned", "suspended"].includes(String(current.data.account_status ?? "active"))) throw new Error("This account is not allowed to access the app.");
  if (Number(payload.sessionVersion ?? 0) !== Number(current.data.session_version ?? 0)) throw new Error("Access token session replaced by a login on another device.");
  return { userId, db, role: String(current.data.role ?? "user") };
}

function text(value: unknown) { return String(value ?? "").trim(); }

async function assertNotBlocked(db: ReturnType<typeof createAdminClient>, userId: string, otherId: string) {
  const own = await db.from("user_blocks").select("id").eq("blocker_id", userId).eq("blocked_id", otherId).maybeSingle();
  const reverse = await db.from("user_blocks").select("id").eq("blocker_id", otherId).eq("blocked_id", userId).maybeSingle();
  if (own.error || reverse.error) throw new Error("Unable to check chat safety.");
  if (own.data || reverse.data) throw new Error("User is blocked.");
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);

  try {
    const { userId, db, role } = await authenticateRequest(req);
    const body = await req.json();
    const action = text(body?.action || "send").toLowerCase();
    const conversationId = text(body?.conversationId);
    if (!conversationId) return jsonResponse({ message: "Conversation is required." }, 422);
    if (action === "threads") {
      const accessQuery = role === "host"
        ? db.from("chat_access").select("client_id,host_id,expires_at").eq("host_id", userId)
        : db.from("chat_access").select("client_id,host_id,expires_at").eq("client_id", userId);
      const accessResult = await accessQuery.gt("expires_at", new Date().toISOString());
      if (accessResult.error) throw accessResult.error;
      const rows = accessResult.data ?? [];
      const rawParticipantIds = rows.map((row) => role === "host" ? row.client_id : row.host_id);
      const blocked = await Promise.all(rawParticipantIds.map(async (id) => {
        try { await assertNotBlocked(db, userId, id); return null; } catch { return id; }
      }));
      const blockedIds = new Set(blocked.filter(Boolean));
      const safeRows = rows.filter((row) => !blockedIds.has(role === "host" ? row.client_id : row.host_id));
      const participantIds = safeRows.map((row) => role === "host" ? row.client_id : row.host_id);
      if (!participantIds.length) return jsonResponse({ threads: [] });
      const people = await db.from("users").select("id,username").in("id", participantIds);
      if (people.error) throw people.error;
      const conversationIds = role === "host" ? [userId] : participantIds;
      const messageResult = await db.from("direct_chat_messages")
        .select("id,conversation_id,sender_id,recipient_id,body,created_at,read_at")
        .in("conversation_id", conversationIds)
        .order("created_at", { ascending: false }).limit(500);
      if (messageResult.error) throw messageResult.error;
      const latestByConversation = new Map<string, Record<string, unknown>>();
      for (const message of messageResult.data ?? []) {
        if (!latestByConversation.has(String(message.conversation_id))) {
          latestByConversation.set(String(message.conversation_id), message);
        }
      }
      const threads = safeRows.map((row) => {
        const participantId = role === "host" ? row.client_id : row.host_id;
        const conversationId = role === "host" ? userId : row.host_id;
        return {
          conversationId,
          participantId,
          participantName: people.data?.find((person) => person.id === participantId)?.username || "RealSaathi User",
          unlockedUntil: row.expires_at,
          latestMessage: latestByConversation.get(conversationId) ?? null,
        };
      });
      return jsonResponse({ threads });
    }
    if (action === "history") {
      const isUuid = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(conversationId);
      if (role !== "host" && isUuid) {
        const access = await db.from("chat_access").select("expires_at")
          .eq("client_id", userId).eq("host_id", conversationId).gt("expires_at", new Date().toISOString()).maybeSingle();
        if (access.error) throw access.error;
        if (!access.data) return jsonResponse({ message: "Chat is locked. Complete 10 minutes of calls with this host." }, 403);
      }
      if (isUuid) await assertNotBlocked(db, userId, conversationId);
      const result = await db.from("direct_chat_messages")
        .select("id,sender_id,recipient_id,body,created_at,read_at")
        .eq("conversation_id", conversationId)
        .order("created_at", { ascending: true }).limit(100);
      if (result.error) throw result.error;
      await db.from("direct_chat_messages").update({ read_at: new Date().toISOString() })
        .eq("conversation_id", conversationId).neq("sender_id", userId).is("read_at", null);
      return jsonResponse({ messages: result.data ?? [] });
    }

    const message = text(body?.message);
    if (!message) return jsonResponse({ message: "Please type a message." }, 422);
    if (message.length > 2000) return jsonResponse({ message: "Message is too long." }, 422);
    const requestedRecipient = text(body?.recipientId);
    const recipientId = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(requestedRecipient)
      ? requestedRecipient
      : null;
    if (!recipientId) return jsonResponse({ message: "Chat recipient is required." }, 422);
    await assertNotBlocked(db, userId, recipientId);
    const result = await db.rpc("send_direct_chat_message", {
      p_conversation_id: conversationId,
      p_sender_id: userId,
      p_recipient_id: recipientId,
      p_body: message,
    });
    if (result.error) {
      const detail = String(result.error.message ?? "");
      if (detail.includes("Insufficient coins")) return jsonResponse({ message: detail }, 402);
      if (detail.includes("Chat is locked")) return jsonResponse({ message: detail }, 403);
      if (detail.includes("User is blocked")) return jsonResponse({ message: detail }, 403);
      throw result.error;
    }
    const payload = result.data as Record<string, unknown>;
    return jsonResponse({ message: payload.message, billing: payload });
  } catch (error) {
    console.error("direct-chat error", error);
    if (error instanceof Error && error.message.includes("token")) return jsonResponse({ message: error.message }, 401);
    if (error instanceof Error && error.message.includes("User is blocked")) return jsonResponse({ message: error.message }, 403);
    return jsonResponse({ message: "Unable to send chat message." }, 500);
  }
});
