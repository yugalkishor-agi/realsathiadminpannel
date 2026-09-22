const jwt = require("jsonwebtoken");
const env = require("../config/env");
const AppError = require("../utils/app-error");
const { supabase } = require("../config/supabase");

function getSessionToken(req) {
    const headerValue =
        req.headers["x-session-token"] ||
        req.headers["X-Session-Token"] ||
        req.headers.authorization ||
        req.headers.Authorization ||
        "";

    return String(headerValue).trim().replace(/^Bearer\s+/i, "");
}

async function authenticateAccessToken(req, res, next) {
    try {
        const token = getSessionToken(req);
        if (!token) {
            throw new AppError("Missing session token.", 401);
        }

        const payload = jwt.verify(token, env.jwt.accessSecret, {
            issuer: "realsaathi-backend"
        });

        if (!payload?.sub) {
            throw new AppError("Access token subject is missing.", 401);
        }

        const { data: currentUser, error: sessionError } = await supabase
            .from("users")
            .select("session_version")
            .eq("id", payload.sub)
            .maybeSingle();
        if (!sessionError && currentUser && Number(payload.sessionVersion || 0) !== Number(currentUser.session_version || 0)) {
            throw new AppError("Session replaced by a login on another device.", 401);
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
