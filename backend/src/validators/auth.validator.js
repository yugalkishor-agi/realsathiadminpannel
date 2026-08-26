const AppError = require("../utils/app-error");
const { isValidE164, normalizePhoneNumber } = require("../utils/phone.util");

const UUID_REGEX =
    /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

function validateSendOtpPayload(body) {
    const phoneNumber = normalizePhoneNumber(body?.phoneNumber);

    if (!isValidE164(phoneNumber)) {
        throw new AppError("Enter a valid mobile number.", 422);
    }

    return {
        phoneNumber
    };
}

function validateVerifyOtpPayload(body) {
    const phoneNumber = normalizePhoneNumber(body?.phoneNumber);
    const otpCode = String(body?.otpCode || "").trim();
    const requestId = String(body?.requestId || "").trim();

    if (!isValidE164(phoneNumber)) {
        throw new AppError("Enter a valid mobile number.", 422);
    }

    if (!/^\d{6}$/.test(otpCode)) {
        throw new AppError("Enter a valid 6-digit OTP.", 422);
    }

    if (!UUID_REGEX.test(requestId)) {
        throw new AppError("Request ID is missing or invalid.", 422);
    }

    return {
        phoneNumber,
        otpCode,
        requestId
    };
}

module.exports = {
    validateSendOtpPayload,
    validateVerifyOtpPayload
};
