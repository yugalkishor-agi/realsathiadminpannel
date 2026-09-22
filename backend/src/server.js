const app = require("./app");
const env = require("./config/env");

const server = app.listen(env.port, "0.0.0.0", () => {
    console.log(`RealSaathi backend listening on 0.0.0.0:${env.port}`);
});

process.on("unhandledRejection", (error) => {
    console.error("Unhandled promise rejection", error);
    server.close(() => process.exit(1));
});
