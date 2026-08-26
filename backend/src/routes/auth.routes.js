const express = require("express");
const authController = require("../controllers/auth.controller");

const router = express.Router();

router.post("/otp/send", authController.sendOtp);
router.post("/otp/verify", authController.verifyOtp);

module.exports = router;
