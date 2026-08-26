const env = require("../config/env");
const AppError = require("../utils/app-error");

class OtpService {
    async sendOtp({ phoneNumber, otpCode, requestId }) {
        switch (env.otp.provider) {
            case "mock":
                return this.sendMockOtp({ phoneNumber, otpCode, requestId });
            case "fast2sms_whatsapp":
                return this.sendFast2SmsWhatsAppOtp({ phoneNumber, otpCode, requestId });
            default:
                throw new AppError(
                    `Unsupported OTP provider "${env.otp.provider}". Configure a provider adapter before production use.`,
                    500
                );
        }
    }

    async sendMockOtp({ phoneNumber, otpCode, requestId }) {
        console.log(
            `[OTP][mock] requestId=${requestId} phoneNumber=${phoneNumber} otp=${otpCode}`
        );

        return {
            provider: "mock",
            providerReference: `mock-${requestId}`
        };
    }

    async sendFast2SmsWhatsAppOtp({ phoneNumber, otpCode, requestId }) {
        const apiKey = env.otp.fast2sms.apiKey;
        const phoneNumberId = env.otp.fast2sms.whatsappPhoneNumberId;
        const messageId = env.otp.fast2sms.whatsappMessageId;

        if (!apiKey || !phoneNumberId || !messageId) {
            throw new AppError(
                "Fast2SMS WhatsApp is not fully configured. Set FAST2SMS_API_KEY, FAST2SMS_WHATSAPP_PHONE_NUMBER_ID, and FAST2SMS_WHATSAPP_MESSAGE_ID.",
                500
            );
        }

        const destinationNumber = this.toFast2SmsNumber(phoneNumber);
        const url = new URL("https://www.fast2sms.com/dev/whatsapp");
        url.searchParams.set("authorization", apiKey);
        url.searchParams.set("message_id", messageId);
        url.searchParams.set("phone_number_id", phoneNumberId);
        url.searchParams.set("numbers", destinationNumber);
        url.searchParams.set("variables_values", otpCode);

        const response = await fetch(url, {
            method: "GET",
            headers: {
                Accept: "application/json"
            }
        });

        const rawBody = await response.text();
        const parsedBody = this.safeParseJson(rawBody);

        if (!response.ok || this.isProviderError(parsedBody)) {
            throw new AppError(
                this.resolveProviderErrorMessage(parsedBody, response.statusText),
                502,
                {
                    provider: "fast2sms_whatsapp",
                    providerResponse: parsedBody || rawBody
                }
            );
        }

        return {
            provider: "fast2sms_whatsapp",
            providerReference: this.resolveProviderReference(parsedBody, requestId)
        };
    }

    toFast2SmsNumber(phoneNumber) {
        const digits = String(phoneNumber || "").replace(/\D/g, "");
        if (!digits) {
            throw new AppError("Mobile number is missing for Fast2SMS delivery.", 422);
        }
        return digits;
    }

    safeParseJson(rawBody) {
        if (!rawBody) {
            return null;
        }

        try {
            return JSON.parse(rawBody);
        } catch {
            return null;
        }
    }

    isProviderError(parsedBody) {
        if (!parsedBody) {
            return false;
        }

        if (typeof parsedBody.return === "boolean") {
            return parsedBody.return === false;
        }

        if (typeof parsedBody.success === "boolean") {
            return parsedBody.success === false;
        }

        return Boolean(parsedBody.error);
    }

    resolveProviderErrorMessage(parsedBody, fallbackMessage) {
        if (!parsedBody) {
            return fallbackMessage || "Fast2SMS WhatsApp request failed.";
        }

        if (Array.isArray(parsedBody.message) && parsedBody.message.length > 0) {
            return String(parsedBody.message[0]);
        }

        if (typeof parsedBody.message === "string" && parsedBody.message.trim()) {
            return parsedBody.message;
        }

        if (typeof parsedBody.error === "string" && parsedBody.error.trim()) {
            return parsedBody.error;
        }

        if (parsedBody.error && typeof parsedBody.error.message === "string") {
            return parsedBody.error.message;
        }

        return fallbackMessage || "Fast2SMS WhatsApp request failed.";
    }

    resolveProviderReference(parsedBody, requestId) {
        return parsedBody?.request_id ||
            parsedBody?.data?.request_id ||
            parsedBody?.data?.id ||
            parsedBody?.messages?.[0]?.id ||
            `fast2sms-${requestId}`;
    }
}

module.exports = OtpService;
