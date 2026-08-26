
const cors = require("cors");
const express = require("express");
const env = require("./config/env");
const { errorMiddleware, notFoundMiddleware } = require("./middlewares/error.middleware");
const apiRoutes = require("./routes");

const app = express();

const corsOrigin =
    env.appOrigin === "*"
        ? true
        : env.appOrigin.split(",").map((origin) => origin.trim());

app.use(
    cors({
        origin: corsOrigin,
        credentials: true
    })
);
app.use(express.json());

app.get("/health", (req, res) => {
    res.status(200).json({
        status: "ok",
        service: "frndzz-backend",
        environment: env.nodeEnv
    });
});

app.use("/v1", apiRoutes);

app.use(notFoundMiddleware);
app.use(errorMiddleware);

module.exports = app;
