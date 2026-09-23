import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "GET") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const { data, error } = await db.from("wallet_ledger")
      .select("*").eq("user_id", userId).order("created_at", { ascending: false }).limit(100);
    if (error) throw error;
    const { data: balance, error: balanceError } = await db.rpc("wallet_balance", { p_user_id: userId });
    if (balanceError) throw balanceError;
    const { data: receipts, error: receiptsError } = await db.from("recharge_receipts")
      .select("*").eq("user_id", userId).order("created_at", { ascending: false }).limit(100);
    if (receiptsError) throw receiptsError;
    const rechargeUpdates = (receipts ?? []).map((row) => ({
      id: row.id, orderId: row.order_id, status: row.status,
      coins: row.coins, amountRupees: row.amount_rupees,
      createdAt: row.created_at, orderCreatedAt: row.order_created_at,
    }));
    const transactions = (data ?? []).map((row) => ({
      id: row.id,
      kind: row.kind,
      title: row.title,
      detail: row.detail,
      amountText: row.amount_text,
      coinsDelta: row.coins_delta,
      rupeesDelta: row.rupees_delta,
      rechargeAmountRupees: row.recharge_amount_rupees,
      status: row.status,
      counterpartyName: row.metadata?.counterpartyName ?? null,
      callStatus: row.metadata?.callStatus ?? null,
      durationSeconds: row.metadata?.durationSeconds ?? null,
      messageCount: row.metadata?.messageCount ?? null,
      createdAt: row.created_at,
    }));
    return jsonResponse({ balanceCoins: Math.max(0, Number(balance ?? 0)), transactions, rechargeUpdates, source: "ledger" });
  } catch (error) {
    console.error("wallet-summary error", error);
    return requestErrorResponse(error, "Unable to load wallet right now.");
  }
});
