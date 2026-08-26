const express = require("express");
const supportController = require("../controllers/support.controller");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");

const router = express.Router();

router.post("/chat", authenticateAccessToken, supportController.chat);

module.exports = router;
