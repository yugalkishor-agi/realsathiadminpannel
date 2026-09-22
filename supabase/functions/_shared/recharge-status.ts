export type RechargeStatus = "pending" | "processing" | "completed" | "failed";

type PaymentAttempt = { payment_status?: string; payment_time?: string; cf_payment_id?: string | number };

export function resolveRechargeStatus(orderStatus: string, payments: PaymentAttempt[]): RechargeStatus {
  if (orderStatus === "PAID") return "completed";
  if (["EXPIRED", "TERMINATED"].includes(orderStatus)) return "failed";
  const latest = [...payments].sort((a, b) =>
    (Date.parse(b.payment_time ?? "") || 0) - (Date.parse(a.payment_time ?? "") || 0) ||
    Number(b.cf_payment_id ?? 0) - Number(a.cf_payment_id ?? 0)
  )[0];
  if (["PENDING", "SUCCESS"].includes(latest?.payment_status ?? "")) return "processing";
  if (["FAILED", "USER_DROPPED", "CANCELLED", "VOID"].includes(latest?.payment_status ?? "")) return "failed";
  return "pending";
}
