import { authenticateRequest, corsResponse, createAdminClient, jsonResponse } from "../_shared/auth.ts";

const zegoId = (value: string) => value.replace(/[^A-Za-z0-9_]/g, "");
const safeStatus = new Set(["completed", "declined", "canceled", "missed"]);

function describeError(error: unknown) {
  if (error instanceof Error) return error.message;
  if (error && typeof error === "object") {
    const value = error as Record<string, unknown>;
    const fields = ["message", "details", "hint", "code"]
      .map((key) => value[key] == null ? "" : `${key}=${String(value[key])}`)
      .filter(Boolean);
    if (fields.length > 0) return fields.join("; ");
    try {
      return JSON.stringify(error);
    } catch {
      return "Unknown object error.";
    }
  }
  return String(error || "Unknown call event error.");
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
      .select("id,username,role")
      .in("id", participantIds);
    if (usersError) throw usersError;
    const current = (users ?? []).find((row) => String(row.id) === userId);
    let other = (users ?? []).find((row) => String(row.id) === counterpartyId);
    if (!other) {
      const { data: candidates, error: candidatesError } = await db.from("users")
        .select("id,username,role");
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
      if (accessError) {
        console.error("record_chat_call_progress failed; continuing call billing", accessError);
      }
    }

    const eventId = callId || `${zegoId(String(host.id))}-${zegoId(String(client.id))}-${kind}-${status}`;
    const { data: billing, error: billingError } = await db.rpc("record_call_billing", {
      p_call_id: eventId,
      p_client_id: client.id,
      p_host_id: host.id,
      p_kind: kind,
      p_status: status,
      p_duration_seconds: Math.floor(durationSeconds),
    });
    if (billingError) throw billingError;
    return jsonResponse({ recorded: Boolean(billing?.recorded), billing });
  } catch (error) {
    console.error("call-events error", error);
    const detail = describeError(error);
    return jsonResponse({
      message: `Unable to record call activity: ${detail}`,
      code: "CALL_EVENT_RECORD_FAILED",
    }, 500);
  }
});
