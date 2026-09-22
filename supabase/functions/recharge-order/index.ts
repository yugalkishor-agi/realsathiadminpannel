import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";
import { CashfreeRequestError, cashfreeRequest, rechargePacks, verifyAndCreditOrder } from "../_shared/cashfree.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const webhookSignature = req.headers.get("x-webhook-signature") ?? "";
    const webhookTimestamp = req.headers.get("x-webhook-timestamp") ?? "";
    if (webhookSignature || webhookTimestamp) {
      const secret = Deno.env.get("CASHFREE_CLIENT_SECRET");
      if (!secret || !webhookSignature || !/^\d+$/.test(webhookTimestamp))
        return jsonResponse({ message: "Unauthorized." }, 401);
      const rawBody = await req.text();
      const key = await crypto.subtle.importKey("raw", new TextEncoder().encode(secret),
        { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
      const digest = new Uint8Array(await crypto.subtle.sign("HMAC", key,
        new TextEncoder().encode(webhookTimestamp + rawBody)));
      const expected = btoa(String.fromCharCode(...digest));
      const actualBytes = new TextEncoder().encode(webhookSignature);
      const expectedBytes = new TextEncoder().encode(expected);
      let mismatch = actualBytes.length ^ expectedBytes.length;
      for (let i = 0; i < Math.max(actualBytes.length, expectedBytes.length); i++)
        mismatch |= (actualBytes[i] ?? 0) ^ (expectedBytes[i] ?? 0);
      if (mismatch !== 0) return jsonResponse({ message: "Unauthorized." }, 401);
      const event = JSON.parse(rawBody);
      const orderId = String(event?.data?.order?.order_id ?? "");
      if (/^rs_[a-f0-9]{32}$/.test(orderId)) await verifyAndCreditOrder(orderId);
      return jsonResponse({ ok: true });
    }

    const { userId, phoneNumber } = await authenticateRequest(req);
    const body = await req.json();
    const action = String(body?.action ?? "");
    if (action === "verify") {
      const orderId = String(body?.orderId ?? "");
      if (!/^rs_[a-f0-9]{32}$/.test(orderId)) return jsonResponse({ message: "Invalid order." }, 422);
      return jsonResponse(await verifyAndCreditOrder(orderId, userId));
    }
    if (action !== "create") return jsonResponse({ message: "Invalid action." }, 422);

    const coins = Number(body?.coins);
    const amount = rechargePacks.get(coins);
    const phone = phoneNumber.replace(/\D/g, "").slice(-10);
    if (!amount || !/^\d{10}$/.test(phone))
      return jsonResponse({ message: "Invalid recharge pack or phone number." }, 422);

    const orderId = `rs_${crypto.randomUUID().replaceAll("-", "")}`;
    const db = createAdminClient();
    const { error: insertError } = await db.from("recharge_orders").insert({
      order_id: orderId, user_id: userId, coins, amount_rupees: amount,
    });
    if (insertError) throw insertError;

    const remote = await cashfreeRequest("", "POST", {
      order_id: orderId,
      order_amount: amount,
      order_currency: "INR",
      customer_details: { customer_id: userId, customer_phone: phone },
      order_note: `${coins} RealSaathi coins`,
      order_meta: {
        notify_url: `${Deno.env.get("SUPABASE_URL")}/functions/v1/recharge-order`,
      },
    }).catch(async (error) => {
      // A definite rejection means checkout never started. Network timeouts
      // stay pending because Cashfree might still have accepted the order.
      if (error instanceof CashfreeRequestError && [400, 401, 403, 422].includes(error.httpStatus)) {
        const { error: updateError } = await db.from("recharge_orders")
          .update({ status: "failed" }).eq("order_id", orderId).eq("status", "pending");
        if (updateError) throw updateError;
      }
      throw error;
    });
    const paymentSessionId = String(remote.payment_session_id ?? "");
    if (String(remote.order_id ?? "") !== orderId || !paymentSessionId)
      throw new Error("Cashfree did not return a payment session.");
    return jsonResponse({ orderId, paymentSessionId, coins, amountRupees: amount });
  } catch (error) {
    console.error("recharge-order error", error);
    if (error instanceof CashfreeRequestError && error.code === "payment_gateway_inactive") {
      return jsonResponse({ message: "Recharge is temporarily unavailable. Please try again later." }, 503);
    }
    return requestErrorResponse(error, "Unable to start recharge right now.");
  }
});
