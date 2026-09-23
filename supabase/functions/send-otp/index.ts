import {
  corsResponse,
  createAdminClient,
  generateOtpCode,
  jsonResponse,
  normalizeIndianPhone,
} from "../_shared/auth.ts";

const FAST_KEY =
  Deno.env.get("FAST2SMS_AUTH_KEY") ?? Deno.env.get("FAST2SMS_API_KEY");
const FAST_PHONE_NUMBER_ID =
  Deno.env.get("FAST2SMS_WHATSAPP_PHONE_NUMBER_ID") ?? "946624178539491";
const FAST_MESSAGE_ID =
  Deno.env.get("FAST2SMS_WHATSAPP_MESSAGE_ID") ?? "12725";
const FAST_TEMPLATE_NAME =
  Deno.env.get("FAST2SMS_WHATSAPP_TEMPLATE_NAME") ?? "frndzz_otp";
const FAST_TEMPLATE_LANGUAGE =
  Deno.env.get("FAST2SMS_WHATSAPP_TEMPLATE_LANGUAGE") ?? "en";
const OTP_EXPIRY_SECONDS = Number(Deno.env.get("OTP_EXPIRY_SECONDS") ?? "300");
const OTP_RETRY_AFTER_SECONDS = Number(
  Deno.env.get("OTP_RETRY_AFTER_SECONDS") ?? "30"
);

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }

  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const body = await req.json();
    const phoneNumber = normalizeIndianPhone(body?.phoneNumber);

    if (!phoneNumber) {
      return jsonResponse({ message: "Enter a valid mobile number." }, 422);
    }

    if (!FAST_KEY) {
      return jsonResponse({ message: "Fast2SMS key is missing." }, 500);
    }

    const supabase = createAdminClient();
    const otpCode = generateOtpCode(6);
    const requestId = crypto.randomUUID();
    const expiresAt = new Date(
      Date.now() + OTP_EXPIRY_SECONDS * 1000
    ).toISOString();
    const dbPhoneNumber = phoneNumber.replace(/\D/g, "");
    const providerPayload = {
      messaging_product: "whatsapp",
      recipient_type: "individual",
      to: dbPhoneNumber,
      type: "template",
      template: {
        name: FAST_TEMPLATE_NAME,
        language: {
          code: FAST_TEMPLATE_LANGUAGE,
        },
        components: [
          {
            type: "body",
            parameters: [
              {
                type: "text",
                text: otpCode,
              },
            ],
          },
          {
            type: "button",
            sub_type: "url",
            index: "0",
            parameters: [
              {
                type: "text",
                text: otpCode,
              },
            ],
          },
        ],
      },
    };

    const providerResponse = await fetch(
      `https://www.fast2sms.com/dev/whatsapp/v24.0/${encodeURIComponent(
        FAST_PHONE_NUMBER_ID
      )}/messages`,
      {
        method: "POST",
        headers: {
          Accept: "application/json",
          Authorization: FAST_KEY,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(providerPayload),
      }
    );

    const providerRawBody = await providerResponse.text();
    let providerBody: any = null;

    try {
      providerBody = providerRawBody
        ? JSON.parse(providerRawBody)
        : null;
    } catch {
      providerBody = null;
    }

    const providerFailed =
      !providerResponse.ok ||
      providerBody?.return === false ||
      providerBody?.success === false ||
      Boolean(providerBody?.error) ||
      Boolean(providerBody?.errors) ||
      !Array.isArray(providerBody?.messages);

    if (providerFailed) {
      console.error("Fast2SMS WhatsApp OTP failed", {
        status: providerResponse.status,
        response: providerRawBody,
        destinationNumber: dbPhoneNumber,
        messageId: FAST_MESSAGE_ID,
        phoneNumberId: FAST_PHONE_NUMBER_ID,
        templateName: FAST_TEMPLATE_NAME,
      });
      return jsonResponse(
        {
          message:
            (Array.isArray(providerBody?.message)
              ? String(providerBody?.message?.[0] ?? "")
              : String(providerBody?.message ?? "")) ||
            String(providerBody?.error ?? "") ||
            providerRawBody ||
            "Failed to send OTP on WhatsApp.",
        },
        502
      );
    }

    console.log("Fast2SMS WhatsApp OTP accepted", {
      destinationNumber: dbPhoneNumber,
      messageId: FAST_MESSAGE_ID,
      phoneNumberId: FAST_PHONE_NUMBER_ID,
      templateName: FAST_TEMPLATE_NAME,
      response: providerRawBody,
    });

    await supabase
      .from("otp_codes")
      .delete()
      .eq("phone", dbPhoneNumber);

    const { error } = await supabase.from("otp_codes").insert([
      {
        phone: dbPhoneNumber,
        otp: otpCode,
        attempts: 0,
        expires_at: expiresAt,
      },
    ]);

    if (error) {
      return jsonResponse({ message: error.message }, 500);
    }

    return jsonResponse({
      requestId,
      message: "OTP sent successfully.",
      retryAfterSeconds: OTP_RETRY_AFTER_SECONDS,
    });
  } catch (error) {
    console.error("send-otp error", error);
    return jsonResponse({ message: "Server error in send-otp." }, 500);
  }
});
