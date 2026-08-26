const AppError = require("../utils/app-error");

function sanitizeSupportHistory(rawHistory) {
    if (!Array.isArray(rawHistory)) {
        return [];
    }

    return rawHistory
        .map((entry) => {
            const message = entry && typeof entry === "object" ? entry : {};
            const role = String(message.role || "").trim().toLowerCase();
            const content = String(message.content || "").trim();

            if (!content) {
                return null;
            }

            if (role !== "user" && role !== "assistant") {
                return null;
            }

            return {
                role,
                content
            };
        })
        .filter(Boolean)
        .slice(-8);
}

function validateSupportChatPayload(body) {
    const message = String(body?.message || "").trim();
    const history = sanitizeSupportHistory(body?.history);

    if (!message) {
        throw new AppError("Please type a message.", 422);
    }

    if (message.length > 500) {
        throw new AppError("Message is too long.", 422);
    }

    return {
        message,
        history
    };
}

module.exports = {
    validateSupportChatPayload
};
