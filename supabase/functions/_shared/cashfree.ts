import { createAdminClient } from "./auth.ts";
import { resolveRechargeStatus } from "./recharge-status.ts";

export const rechargePacks = new Map<number, number>([
  [99, 31], [249, 75], [320, 100], [799, 250], [1499, 455], [2999, 899],
]);

const cashfreeBase = "https://sandbox.cashfree.com/pg/orders";

export class CashfreeRequestError extends Error {
  constructor(public readonly httpStatus: number, public readonly code: string) {
    super(`Cashfree request failed (${httpStatus}).`);
  }
}

export async function cashfreeRequest<T = Record<string, unknown>>(path: string, method = "GET", body?: unknown): Promise<T> {
  const clientId = Deno.env.get("CASHFREE_CLIENT_ID");
  const clientSecret = Deno.env.get("CASHFREE_CLIENT_SECRET");
  if (!clientId || !clientSecret) throw new Error("Cashfree sandbox credentials are missing.");
  const response = await fetch(`${cashfreeBase}${path}`, {
    method,
    headers: {
      "Content-Type": "application/json",
      "x-api-version": "2026-01-01",
      "x-client-id": clientId,
      "x-client-secret": clientSecret,
    },
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  const result = await response.json().catch(() => ({}));
  if (!response.ok) {
    console.error("Cashfree request failed", response.status, result?.code ?? "unknown");
    throw new CashfreeRequestError(response.status, String(result?.code ?? "unknown"));
  }
  return result as T;
}

export async function verifyAndCreditOrder(orderId: string, expectedUserId?: string) {
  const db = createAdminClient();
  const { data: order, error } = await db.from("recharge_orders")
    .select("*").eq("order_id", orderId).maybeSingle();
  if (error) throw error;
  if (!order || (expectedUserId && order.user_id !== expectedUserId))
    throw new Error("Recharge order not found.");

  const result = (status: string) => ({
    orderId, status, coins: Number(order.coins), amountRupees: Number(order.amount_rupees),
  });
  if (order.status === "completed") return result("completed");

  const remote = await cashfreeRequest(`/${encodeURIComponent(orderId)}`);
  if (String(remote.order_id ?? "") !== orderId ||
      String(remote.order_currency ?? "") !== "INR" ||
      Number(remote.order_amount) !== Number(order.amount_rupees)) {
    throw new Error("Cashfree order details do not match.");
  }
  const remoteStatus = String(remote.order_status ?? "");
  if (remoteStatus === "PAID") {
    const { error: creditError } = await db.rpc("complete_recharge_order", { p_order_id: orderId });
    if (creditError) throw creditError;
    return result("completed");
  }
  const payments = await cashfreeRequest<Array<{
    payment_status?: string; payment_time?: string; cf_payment_id?: string | number;
  }>>(`/${encodeURIComponent(orderId)}/payments`);
  if (!Array.isArray(payments)) throw new Error("Invalid Cashfree payment response.");
  const status = resolveRechargeStatus(remoteStatus, payments);
  // A stale pending/failed callback must never overwrite a concurrent credit.
  const { error: updateError } = await db.from("recharge_orders")
    .update({ status }).eq("order_id", orderId).eq("status", order.status);
  if (updateError) throw updateError;
  const { data: current, error: readError } = await db.from("recharge_orders")
    .select("status").eq("order_id", orderId).single();
  if (readError) throw readError;
  return result(current.status);
}
