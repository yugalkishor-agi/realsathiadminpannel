const jwt = require("jsonwebtoken");
const env = require("../config/env");

class TokenService {
    issueAuthTokens(userSession) {
        const tokenPayload = {
            phoneNumber: userSession.phoneNumber,
            isHost: userSession.isHost,
            sessionVersion: Number(userSession.sessionVersion || 0)
        };

        const accessToken = jwt.sign(tokenPayload, env.jwt.accessSecret, {
            expiresIn: env.jwt.accessExpiresIn,
            issuer: "realsaathi-backend",
            subject: userSession.id
        });

        const refreshToken = jwt.sign(
            { type: "refresh", sessionVersion: Number(userSession.sessionVersion || 0) },
            env.jwt.refreshSecret,
            {
                expiresIn: env.jwt.refreshExpiresIn,
                issuer: "realsaathi-backend",
                subject: userSession.id
            }
        );

        return {
            accessToken,
            refreshToken
        };
    }
}

module.exports = TokenService;
