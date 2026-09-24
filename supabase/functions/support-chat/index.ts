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
  const normalized = encodedPayload.replace(/-/g, "+").replace(/_/g, "/");
  const payload = JSON.parse(atob(normalized + "=".repeat((4 - (normalized.length % 4 || 4)) % 4))) as Record<string, unknown>;
  const userId = String(payload.sub ?? "").trim();
  if (!userId || Number(payload.exp ?? 0) <= Math.floor(Date.now() / 1000)) throw new Error("Access token expired.");
  const db = createAdminClient();
  const current = await db.from("users").select("session_version,account_status").eq("id", userId).maybeSingle();
  if (current.error || !current.data) throw new Error("Unable to validate session.");
  if (["blocked", "banned", "suspended"].includes(String(current.data.account_status ?? "active"))) throw new Error("This account is not allowed to access the app.");
  if (Number(payload.sessionVersion ?? 0) !== Number(current.data.session_version ?? 0)) throw new Error("Access token session replaced by a login on another device.");
  return { userId, db };
}

const SUPPORT_ACKNOWLEDGEMENT = "Aapka message RealSaathi Support tak pahunch gaya hai. Kripya wait karein, hamari team aapko 24 ghante ke andar isi chat me reply karegi.";

async function getOrCreateThread(supabase: ReturnType<typeof createAdminClient>, userId: string) {
  const existing = await supabase
    .from("support_threads")
    .select("id,status,subject")
    .eq("user_id", userId)
    .in("status", ["open", "in_progress"])
    .order("updated_at", { ascending: false })
    .limit(1)
    .maybeSingle();
  if (existing.error) throw existing.error;
  if (existing.data) return existing.data;

  const created = await supabase
    .from("support_threads")
    .insert({ user_id: userId })
    .select("id,status,subject")
    .single();
  if (created.error || !created.data) throw created.error ?? new Error("Unable to create support thread.");
  return created.data;
}

function parseSupportMessages(rows: Array<Record<string, unknown>>) {
  return rows.map((row) => ({
    id: row.id,
    senderType: row.sender_type,
    body: row.body,
    createdAt: row.created_at,
  }));
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }

  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const { userId, db: supabase } = await authenticateRequest(req);
    const body = await req.json();
    const action = String(body?.action ?? "send").trim().toLowerCase();
    const thread = await getOrCreateThread(supabase, userId);

    if (action === "history") {
      const historyResult = await supabase
        .from("support_messages")
        .select("id,sender_type,body,created_at")
        .eq("thread_id", thread.id)
        .order("created_at", { ascending: true })
        .limit(100);
      if (historyResult.error) throw historyResult.error;
      return jsonResponse({ threadId: thread.id, status: thread.status, messages: parseSupportMessages(historyResult.data ?? []) });
    }

    const message = String(body?.message ?? "").trim();
    if (!message) {
      return jsonResponse({ message: "Please type a message." }, 422);
    }

    const { data: currentUser, error: currentUserError } = await supabase
      .from("users")
      .select("*")
      .eq("id", userId)
      .single();

    if (currentUserError || !currentUser) {
      return jsonResponse({ message: "User account not found." }, 404);
    }

    const userMessage = await supabase
      .from("support_messages")
      .insert({ thread_id: thread.id, sender_type: "user", sender_id: userId, body: message })
      .select("id")
      .single();
    if (userMessage.error) throw userMessage.error;
    await supabase.from("support_threads").update({ last_message_at: new Date().toISOString(), updated_at: new Date().toISOString(), status: "open" }).eq("id", thread.id);

    const assistantMessage = await supabase
      .from("support_messages")
      .insert({ thread_id: thread.id, sender_type: "assistant", body: SUPPORT_ACKNOWLEDGEMENT })
      .select("id")
      .single();
    if (assistantMessage.error) throw assistantMessage.error;
    await supabase.from("support_threads").update({ last_message_at: new Date().toISOString(), updated_at: new Date().toISOString() }).eq("id", thread.id);

    return jsonResponse({
      answer: SUPPORT_ACKNOWLEDGEMENT,
      threadId: thread.id,
      status: thread.status,
      escalated: false,
    });
  } catch (error) {
    console.error("support-chat error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in support-chat." }, 500);
  }
});
