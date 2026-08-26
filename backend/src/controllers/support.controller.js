const { supabase } = require("../config/supabase");
const UserRepository = require("../repositories/user.repository");
const SupportService = require("../services/support.service");
const asyncHandler = require("../utils/async-handler");
const { validateSupportChatPayload } = require("../validators/support.validator");

const supportService = new SupportService({
    userRepository: new UserRepository(supabase)
});

const chat = asyncHandler(async (req, res) => {
    const payload = validateSupportChatPayload(req.body);
    const response = await supportService.sendSupportChat({
        userId: req.auth.userId,
        message: payload.message,
        history: payload.history
    });

    res.status(200).json(response);
});

module.exports = {
    chat
};
