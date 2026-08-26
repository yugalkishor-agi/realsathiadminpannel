const jwt = require("jsonwebtoken");
const env = require("../config/env");

class TokenService {
    issueAuthTokens(userSession) {
        const tokenPayload = {
            phoneNumber: userSession.phoneNumber,
            isHost: userSession.isHost
        };

        const accessToken = jwt.sign(tokenPayload, env.jwt.accessSecret, {
            expiresIn: env.jwt.accessExpiresIn,
            issuer: "frndzz-backend",
            subject: userSession.id
        });

        const refreshToken = jwt.sign(
            { type: "refresh" },
            env.jwt.refreshSecret,
            {
                expiresIn: env.jwt.refreshExpiresIn,
                issuer: "frndzz-backend",
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
