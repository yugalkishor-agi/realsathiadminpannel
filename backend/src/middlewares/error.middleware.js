const env = require("../config/env");
const AppError = require("../utils/app-error");

function notFoundMiddleware(req, res, next) {
    next(new AppError(`Route ${req.originalUrl} not found.`, 404));
}

function errorMiddleware(error, req, res, next) {
    const statusCode = error.statusCode || 500;

    const payload = {
        message: error.message || "Internal server error."
    };

    if (error.details) {
        payload.details = error.details;
    }

    if (!env.isProduction && error.stack) {
        payload.stack = error.stack;
    }

    res.status(statusCode).json(payload);
}

module.exports = {
    notFoundMiddleware,
    errorMiddleware
};
