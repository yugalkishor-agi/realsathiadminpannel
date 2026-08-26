const crypto = require("crypto");

function generateOtpCode(length = 6) {
    let otpCode = "";
    while (otpCode.length < length) {
        otpCode += crypto.randomInt(0, 10).toString();
    }
    return otpCode;
}

function hashOtp(otpCode) {
    return crypto.createHash("sha256").update(otpCode).digest("hex");
}

function buildDisplayName(phoneNumber) {
    return "Frndzz User";
}

function generatePublicId(seed = "") {
    const normalizedSeed = String(seed || "").trim();
    const numericId = normalizedSeed
        ? 10000000 + (
            parseInt(
                crypto.createHash("sha256").update(normalizedSeed).digest("hex").slice(0, 12),
                16
            ) % 90000000
        )
        : crypto.randomInt(10000000, 100000000);

    return String(numericId).padStart(8, "0");
}

function isValidPublicId(publicId) {
    return /^\d{8}$/.test(String(publicId || "").trim());
}

module.exports = {
    buildDisplayName,
    generatePublicId,
    isValidPublicId,
    generateOtpCode,
    hashOtp
};
