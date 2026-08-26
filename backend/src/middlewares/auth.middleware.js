const jwt = require("jsonwebtoken");
const env = require("../config/env");
const AppError = require("../utils/app-error");

function getSessionToken(req) {
    const headerValue =
        req.headers["x-session-token"] ||
        req.headers["X-Session-Token"] ||
        req.headers.authorization ||
        req.headers.Authorization ||
        "";

    return String(headerValue).trim().replace(/^Bearer\s+/i, "");
}

function authenticateAccessToken(req, res, next) {
    try {
        const token = getSessionToken(req);
        if (!token) {
            throw new AppError("Missing session token.", 401);
        }

        const payload = jwt.verify(token, env.jwt.accessSecret, {
            issuer: "frndzz-backend"
        });

        if (!payload?.sub) {
            throw new AppError("Access token subject is missing.", 401);
        }

        req.auth = {
            userId: String(payload.sub),
            phoneNumber: String(payload.phoneNumber || "").trim(),
            isHost: Boolean(payload.isHost),
            raw: payload
        };

        next();
    } catch (error) {
        if (error instanceof AppError) {
            next(error);
            return;
        }

        const message = String(error?.message || "Invalid access token.");
        if (message.toLowerCase().includes("jwt expired")) {
            next(new AppError("Access token expired.", 401));
            return;
        }

        next(new AppError(message, 401));
    }
}

module.exports = {
    authenticateAccessToken
};
