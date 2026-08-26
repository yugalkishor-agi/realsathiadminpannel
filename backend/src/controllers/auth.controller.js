const { supabase } = require("../config/supabase");
const OtpRepository = require("../repositories/otp.repository");
const UserRepository = require("../repositories/user.repository");
const AuthService = require("../services/auth.service");
const OtpService = require("../services/otp.service");
const TokenService = require("../services/token.service");
const asyncHandler = require("../utils/async-handler");
const {
    validateSendOtpPayload,
    validateVerifyOtpPayload
} = require("../validators/auth.validator");

const authService = new AuthService({
    otpRepository: new OtpRepository(supabase),
    userRepository: new UserRepository(supabase),
    otpService: new OtpService(),
    tokenService: new TokenService()
});

const sendOtp = asyncHandler(async (req, res) => {
    const payload = validateSendOtpPayload(req.body);
    const response = await authService.sendOtp(payload);
    res.status(200).json(response);
});

const verifyOtp = asyncHandler(async (req, res) => {
    const payload = validateVerifyOtpPayload(req.body);
    const response = await authService.verifyOtp(payload);
    res.status(200).json(response);
});

module.exports = {
    sendOtp,
    verifyOtp
};
