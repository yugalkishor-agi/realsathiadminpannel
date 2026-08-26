function normalizePhoneNumber(rawPhoneNumber) {
    const input = String(rawPhoneNumber || "").trim();
    const digits = input.replace(/\D/g, "");

    if (input.startsWith("+") && /^\+[1-9]\d{9,14}$/.test(input)) {
        return input;
    }

    if (digits.length === 10) {
        return `+91${digits}`;
    }

    if (digits.length === 11 && digits.startsWith("0")) {
        return `+91${digits.slice(1)}`;
    }

    if (digits.length === 12 && digits.startsWith("91")) {
        return `+${digits}`;
    }

    return "";
}

function isValidE164(phoneNumber) {
    return /^\+[1-9]\d{9,14}$/.test(phoneNumber);
}

module.exports = {
    normalizePhoneNumber,
    isValidE164
};
