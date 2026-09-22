import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const kind = String(body?.kind ?? "").trim().toLowerCase();
    const coinsDelta = Number(body?.coinsDelta);
    // Payment credits must come from a verified payment webhook, never this client endpoint.
    if (!new Set(["audio_call", "video_call", "chat"]).has(kind) ||
      !Number.isInteger(coinsDelta) || coinsDelta >= 0 || coinsDelta < -10000 ||
      Number(body?.rupeesDelta ?? 0) !== 0 || Number(body?.rechargeAmountRupees ?? 0) !== 0) {
      return jsonResponse({ message: "This transaction cannot be recorded from the app." }, 422);
    }
    const { data, error } = await createAdminClient().rpc("record_wallet_debit", {
      p_user_id: userId, p_kind: kind, p_title: String(body?.title ?? "Call"),
      p_detail: String(body?.detail ?? ""), p_coins_delta: coinsDelta,
      p_metadata: {
        counterpartyName: String(body?.counterpartyName ?? "").slice(0, 80),
        durationSeconds: Number(body?.durationSeconds ?? 0),
        messageCount: Number(body?.messageCount ?? 0),
      },
    });
    if (error) {
      if (String(error.message ?? "").includes("Insufficient coins"))
        return jsonResponse({ message: "Insufficient coins." }, 422);
      throw error;
    }
    return jsonResponse({ recorded: true, transaction: {
      id: data.id, kind: data.kind, title: data.title, detail: data.detail,
      amountText: data.amount_text, coinsDelta: data.coins_delta,
      rupeesDelta: 0, rechargeAmountRupees: 0, status: data.status,
      counterpartyName: data.metadata?.counterpartyName ?? null,
      durationSeconds: data.metadata?.durationSeconds ?? null,
      messageCount: data.metadata?.messageCount ?? null, createdAt: data.created_at,
    } });
  } catch (error) {
    console.error("record-wallet-transaction error", error);
    return requestErrorResponse(error, "Unable to record transaction right now.");
  }
});
