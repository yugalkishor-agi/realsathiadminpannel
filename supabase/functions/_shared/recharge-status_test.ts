import { resolveRechargeStatus } from "./recharge-status.ts";

function check(actual: string, expected: string) {
  if (actual !== expected) throw new Error(`Expected ${expected}, got ${actual}`);
}

Deno.test("only a paid order is completed", () => {
  check(resolveRechargeStatus("PAID", []), "completed");
  check(resolveRechargeStatus("ACTIVE", [{ payment_status: "SUCCESS" }]), "processing");
  check(resolveRechargeStatus("ACTIVE", []), "pending");
});

Deno.test("pending, expired and abandoned payments remain uncredited", () => {
  check(resolveRechargeStatus("ACTIVE", [{ payment_status: "PENDING" }]), "processing");
  check(resolveRechargeStatus("EXPIRED", []), "failed");
  check(resolveRechargeStatus("ACTIVE", [{ payment_status: "USER_DROPPED" }]), "failed");
});

Deno.test("a newer retry supersedes an older failed attempt regardless of API order", () => {
  check(resolveRechargeStatus("ACTIVE", [
    { payment_status: "FAILED", payment_time: "2026-09-20T10:00:00Z", cf_payment_id: 1 },
    { payment_status: "PENDING", payment_time: "2026-09-20T10:01:00Z", cf_payment_id: 2 },
  ]), "processing");
  check(resolveRechargeStatus("ACTIVE", [
    { payment_status: "PENDING", cf_payment_id: 2 },
    { payment_status: "FAILED", cf_payment_id: 1 },
  ]), "processing");
});
