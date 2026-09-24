import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

const zegoId = (value: string) => value.replace(/[^A-Za-z0-9_]/g, "");
const safeStatus = new Set(["completed", "declined", "canceled", "missed"]);

function billedMinutes(seconds: number) {
  return Math.max(1, Math.ceil(Math.max(0, seconds) / 60));
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const status = String(body?.status ?? "").trim().toLowerCase();
    const kind = String(body?.kind ?? "").trim().toLowerCase();
    const counterpartyId = String(body?.counterpartyId ?? "").trim();
    const counterpartyName = String(body?.counterpartyName ?? "Caller").trim().slice(0, 80);
    const callId = String(body?.callId ?? "").trim().slice(0, 160);
    const durationSeconds = Math.max(0, Number(body?.durationSeconds ?? 0) || 0);
    if (!safeStatus.has(status) || !new Set(["audio_call", "video_call"]).has(kind) || !counterpartyId) {
      return jsonResponse({ message: "Invalid call event." }, 422);
    }

    const db = createAdminClient();
    const participantIds = [userId];
    if (/^[0-9a-f]{8}-[0-9a-f-]{27}$/i.test(counterpartyId)) {
      participantIds.push(counterpartyId);
    }
    const { data: users, error: usersError } = await db.from("users")
      .select("id,username,role,host_audio_rate,host_video_rate")
      .in("id", participantIds);
    if (usersError) throw usersError;
    const current = (users ?? []).find((row) => String(row.id) === userId);
    let other = (users ?? []).find((row) => String(row.id) === counterpartyId);
    if (!other) {
      const { data: candidates, error: candidatesError } = await db.from("users")
        .select("id,username,role,host_audio_rate,host_video_rate");
      if (candidatesError) throw candidatesError;
      other = (candidates ?? []).find((row) => zegoId(String(row.id)) === counterpartyId);
    }
    if (!current || !other) return jsonResponse({ message: "Call participants not found." }, 404);

    const host = String(current.role ?? "") === "host" ? current : other;
    const client = host.id === current.id ? other : current;
    if (String(host.role ?? "") !== "host") return jsonResponse({ message: "A host participant is required." }, 422);

    if (status === "completed" && durationSeconds > 0) {
      const { error: accessError } = await db.rpc("record_chat_call_progress", {
        p_client_id: client.id,
        p_host_id: host.id,
        p_duration_seconds: Math.floor(durationSeconds),
      });
      if (accessError) throw accessError;
    }

    const rate = Math.max(0, Number(kind === "video_call" ? host.host_video_rate : host.host_audio_rate) || 0);
    const earning = status === "completed" ? billedMinutes(durationSeconds) * rate : 0;
    const eventId = callId || `${zegoId(String(host.id))}-${zegoId(String(client.id))}-${kind}-${status}`;
    const { data: existing, error: existingError } = await db.from("wallet_ledger")
      .select("*").contains("metadata", { callId: eventId }).in("user_id", [current.id, other.id]);
    if (existingError) throw existingError;
    const existingUsers = new Set((existing ?? []).map((row) => String(row.user_id)));
    const hostStatus = current.id === host.id ? status : (status === "declined" ? "canceled" : status);
    const clientStatus = current.id === client.id ? status : (status === "declined" ? "declined" : status);
    const rows = [];
    if (!existingUsers.has(String(host.id))) rows.push({
      user_id: host.id, kind, title: counterpartyName || "Caller",
      detail: `${kind === "video_call" ? "Video" : "Audio"} call ${hostStatus}`,
      amount_text: earning > 0 ? `+₹${earning}` : "₹0", coins_delta: 0,
      rupees_delta: earning, metadata: { callId: eventId, callStatus: hostStatus,
        counterpartyName, counterpartyId: client.id, durationSeconds, ratePerMinute: rate }
    });
    if (!existingUsers.has(String(client.id))) rows.push({
      user_id: client.id, kind, title: String(host.username ?? "Host"),
      detail: `${kind === "video_call" ? "Video" : "Audio"} call ${clientStatus}`,
      amount_text: earning > 0 ? `-${earning}` : "₹0", coins_delta: earning > 0 ? -earning : 0,
      rupees_delta: 0, metadata: { callId: eventId, callStatus: clientStatus,
        counterpartyName: String(host.username ?? "Host"), counterpartyId: host.id,
        durationSeconds, ratePerMinute: rate }
    });
    if (rows.length) {
      const { error } = await db.from("wallet_ledger").insert(rows);
      if (error) throw error;
    }
    const currentRow = (await db.from("wallet_ledger").select("*").eq("user_id", userId)
      .contains("metadata", { callId: eventId }).limit(1)).data?.[0] ?? null;
    return jsonResponse({ recorded: true, transaction: currentRow });
  } catch (error) {
    console.error("call-events error", error);
    return requestErrorResponse(error, "Unable to record call activity right now.");
  }
});
