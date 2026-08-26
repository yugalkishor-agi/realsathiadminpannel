const express = require("express");
const authRoutes = require("./auth.routes");
const configRoutes = require("./config.routes");
const discoveryRoutes = require("./discovery.routes");
const hostRoutes = require("./host.routes");
const profileRoutes = require("./profile.routes");
const reportRoutes = require("./report.routes");
const supportRoutes = require("./support.routes");
const walletRoutes = require("./wallet.routes");

const router = express.Router();

router.use("/auth", authRoutes);
router.use("/config", configRoutes);
router.use("/discovery", discoveryRoutes);
router.use("/host", hostRoutes);
router.use("/profile", profileRoutes);
router.use("/report", reportRoutes);
router.use("/support", supportRoutes);
router.use("/wallet", walletRoutes);

module.exports = router;
