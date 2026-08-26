const dotenv = require("dotenv");

dotenv.config();

const isProduction = process.env.NODE_ENV === "production";

function getEnv(name, fallback) {
    const value = process.env[name] ?? fallback;
    if (value === undefined || value === "") {
        throw new Error(`Missing required environment variable: ${name}`);
    }
    return value;
}

function getNumberEnv(name, fallback) {
    const value = Number(getEnv(name, fallback));
    if (Number.isNaN(value)) {
        throw new Error(`Environment variable ${name} must be a valid number.`);
    }
    return value;
}

module.exports = {
    isProduction,
    nodeEnv: process.env.NODE_ENV || "development",
    port: getNumberEnv("PORT", 4000),
    appOrigin: process.env.APP_ORIGIN || "*",
    supabase: {
        url: getEnv("SUPABASE_URL"),
        serviceRoleKey: getEnv("SUPABASE_SERVICE_ROLE_KEY")
    },
    jwt: {
        accessSecret: getEnv(
            "JWT_ACCESS_SECRET",
            isProduction ? undefined : "frndzz-dev-access-secret"
        ),
        refreshSecret: getEnv(
            "JWT_REFRESH_SECRET",
            isProduction ? undefined : "frndzz-dev-refresh-secret"
        ),
        accessExpiresIn: getEnv("JWT_ACCESS_EXPIRES_IN", "15m"),
        refreshExpiresIn: getEnv("JWT_REFRESH_EXPIRES_IN", "30d")
    },
    otp: {
        provider: getEnv("OTP_PROVIDER", "mock"),
        expirySeconds: getNumberEnv("OTP_EXPIRY_SECONDS", 300),
        retryAfterSeconds: getNumberEnv("OTP_RETRY_AFTER_SECONDS", 30),
        maxAttempts: getNumberEnv("OTP_MAX_ATTEMPTS", 5),
        mockOtpCode: getEnv("MOCK_OTP_CODE", "123456"),
        fast2sms: {
            apiKey: process.env.FAST2SMS_API_KEY || "",
            whatsappPhoneNumberId: process.env.FAST2SMS_WHATSAPP_PHONE_NUMBER_ID || "",
            whatsappMessageId: process.env.FAST2SMS_WHATSAPP_MESSAGE_ID || ""
        }
    },
    groq: {
        apiKey: process.env.GROQ_API_KEY || "",
        model: process.env.GROQ_MODEL || "llama-3.3-70b-versatile"
    }
};
