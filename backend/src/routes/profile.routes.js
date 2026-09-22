const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const UserRepository = require("../repositories/user.repository");
const { HOST_STATUSES, mapUserProfile, mapUserSession } = require("../models/user.model");
const { generatePublicId, isValidPublicId } = require("../utils/auth.util");
const asyncHandler = require("../utils/async-handler");
const AppError = require("../utils/app-error");

const router = express.Router();
const userRepository = new UserRepository(supabase);

function normalizeNickname(input) {
    const cleaned = String(input || "")
        .replace(/\s+/g, " ")
        .split("")
        .filter((char) => /[\p{L}\p{N}\s_.]/u.test(char))
        .join("")
        .trimStart()
        .slice(0, 24);
    const firstLetterIndex = cleaned.search(/\p{L}/u);
    if (firstLetterIndex < 0) {
        return cleaned;
    }

    return `${cleaned.slice(0, firstLetterIndex)}${cleaned[firstLetterIndex].toLocaleUpperCase()}${cleaned.slice(firstLetterIndex + 1)}`;
}

function hasBlockedNicknameContent(input) {
    const text = String(input || "").toLowerCase();
    const compact = text.replace(/[^a-z0-9]/g, "");
    const digitCount = (text.match(/\d/g) || []).length;
    if (digitCount >= 10 || /\d{10,}/.test(text)) {
        return true;
    }

    if (
        text.includes("@") ||
        text.includes("http") ||
        text.includes("www.") ||
        text.includes("instagram") ||
        text.includes("insta") ||
        text.includes("whatsapp") ||
        text.includes("wa.me") ||
        text.includes("telegram") ||
        text.includes("t.me") ||
        text.includes("snapchat") ||
        text.includes("snap") ||
        text.includes("facebook") ||
        text.includes("youtube") ||
        text.includes("onlyfans") ||
        text.includes("linktree") ||
        /(^|[\s_.])(ig|fb|yt|wa|snap)($|[\s_.])/.test(text)
    ) {
        return true;
    }

    const blockedTokens = [
        "fuck", "fucker", "sex", "sexy", "porn", "porno", "xxx", "nude", "nudes",
        "boobs", "dick", "pussy", "vagina", "penis", "bitch", "asshole",
        "slut", "horny", "hotgirl", "hotboy", "camgirl", "callgirl",
        "chut", "chutiya", "chutia", "bhosdi", "bhosdike", "bhenchod", "behenchod",
        "madarchod", "randi", "rand", "lund", "lauda", "loda",
        "gaand", "gand", "harami", "kamina", "kamine", "suar"
    ];
    return blockedTokens.some((token) => compact.includes(token));
}

function hasDisallowedNicknameSymbols(input) {
    return String(input || "")
        .split("")
        .some((char) => !/[\p{L}\p{N}\s_.]/u.test(char));
}

function validateNickname(input) {
    if (hasBlockedNicknameContent(input)) {
        throw new AppError("Nickname me gaali, sexual words, social ID ya mobile number allowed nahi hai.", 422);
    }
    if (hasDisallowedNicknameSymbols(input)) {
        throw new AppError("Nickname me sirf letters, numbers, space, dot aur underscore allowed hain.", 422);
    }

    const nickname = normalizeNickname(input);
    if (!nickname || nickname.length < 2) {
        throw new AppError("Use at least 2 characters for your nickname.", 422);
    }
    return nickname;
}

function isDefaultNickname(input) {
    const text = String(input || "").trim().toLowerCase();
    return !text ||
        text === "realsaathi user" ||
        text.startsWith("realsaathi_") ||
        text === "realsaathi_user" ||
        text === "realsaathi user" ||
        text === "realsaathi user" ||
        text.startsWith("realsaathi_");
}

function normalizeHostDisplayName(input) {
    const cleaned = String(input || "")
        .replace(/\s+/g, " ")
        .split("")
        .filter((char) => /[\p{L}\s]/u.test(char))
        .join("")
        .trim()
        .slice(0, 32);
    const firstLetterIndex = cleaned.search(/\p{L}/u);
    if (firstLetterIndex < 0) {
        return cleaned;
    }

    return `${cleaned.slice(0, firstLetterIndex)}${cleaned[firstLetterIndex].toLocaleUpperCase()}${cleaned.slice(firstLetterIndex + 1)}`;
}

function validateHostDisplayName(input) {
    if (hasBlockedNicknameContent(input)) {
        throw new AppError("Name me gaali, sexual words, social ID ya mobile number allowed nahi hai.", 422);
    }
    if (String(input || "").split("").some((char) => !/[\p{L}\s]/u.test(char))) {
        throw new AppError("Host name me sirf name aur surname letters allowed hain.", 422);
    }

    const nickname = normalizeHostDisplayName(input);
    if (!nickname || nickname.length < 2) {
        throw new AppError("Use at least 2 characters for your name.", 422);
    }
    return nickname;
}

function normalizeList(input, fallback = []) {
    const values = Array.isArray(input) ? input : fallback;
    return values
        .map((value) => String(value || "").trim())
        .filter(Boolean)
        .slice(0, 5);
}

async function generateUniquePublicId(userRepository, seed = "", currentUserId = "") {
    for (let attempt = 0; attempt < 100; attempt += 1) {
        const candidate = generatePublicId(`${seed}:${attempt}:${Date.now()}:${Math.random()}`);
        if (!await userRepository.publicIdBelongsToAnotherUser(candidate, currentUserId)) {
            return candidate;
        }
    }

    throw new AppError("Unable to generate unique RealSaathi ID. Please try again.", 500);
}

router.post("/save", authenticateAccessToken, asyncHandler(async (req, res) => {
    const accountMode = String(req.body?.accountMode || "customer").trim().toLowerCase();
    const isHostMode = accountMode === "host" || accountMode === "community";
    const existingUser = await userRepository.findById(req.auth.userId);
    if (!existingUser) {
        throw new AppError("User account not found.", 404);
    }

    const existingIsHost =
        String(existingUser.role || "").trim().toLowerCase() === "host" ||
        Boolean(existingUser.is_host);
    const existingNickname = normalizeNickname(
        existingUser.nickname || existingUser.username || existingUser.display_name || ""
    );
    const nickname = isHostMode ? validateHostDisplayName(req.body?.nickname) : validateNickname(req.body?.nickname);
    if (
        existingIsHost &&
        !isDefaultNickname(existingNickname) &&
        nickname.toLowerCase() !== existingNickname.toLowerCase()
    ) {
        throw new AppError("Host name change ke liye support team ko request bhejo.", 422);
    }
    const topicTags = normalizeList(req.body?.topicTags, req.body?.interests);
    const nativeLanguages = normalizeList(req.body?.nativeLanguages, [req.body?.preferredLanguage || "All"]);
    const publicId = isValidPublicId(existingUser.public_id)
        ? String(existingUser.public_id).trim()
        : await generateUniquePublicId(userRepository, existingUser.phone || existingUser.phone_number || req.auth.userId, req.auth.userId);

    const payload = {
        public_id: publicId,
        nickname,
        username: nickname,
        gender: isHostMode ? "Female" : String(req.body?.gender || "").trim(),
        language: String(req.body?.preferredLanguage || "All").trim() || "All",
        native_languages: nativeLanguages,
        topic_tags: topicTags,
        avatar_id: Number(req.body?.avatarId || 1),
        interests: topicTags,
        account_mode: accountMode || "customer",
        role: isHostMode ? "host" : "user",
        host_status: isHostMode ? HOST_STATUSES.APPROVED : HOST_STATUSES.NOT_APPLICABLE,
        community_name: String(req.body?.communityName || "").trim(),
        community_city: String(req.body?.communityCity || "").trim(),
        community_about: String(req.body?.communityAbout || "").trim(),
        community_experience: String(req.body?.communityExperience || "").trim()
    };

    const userRecord = await userRepository.updateUser(req.auth.userId, payload);
    const savedProfileRecord = await userRepository.upsertProfile({
        user_id: req.auth.userId,
        display_name: nickname,
        gender: payload.gender,
        preferred_language: payload.language,
        interests: topicTags,
        topic_tags: topicTags,
        native_languages: nativeLanguages
    });
    const profileRecord = savedProfileRecord || await userRepository.findProfileByUserId(req.auth.userId);

    res.status(200).json({
        user: mapUserSession(userRecord, profileRecord),
        profile: mapUserProfile(userRecord, profileRecord)
    });
}));

router.post("/delete", authenticateAccessToken, asyncHandler(async (req, res) => {
    const { data: user, error: lookupError } = await supabase
        .from("users")
        .select("*")
        .eq("id", req.auth.userId)
        .single();
    if (lookupError || !user) {
        res.status(404).json({ message: "Account not found." });
        return;
    }
    if (user.role === "host") {
        for (const purpose of ["profile_photo", "story"]) {
            const { data: files, error: listError } = await supabase.storage
                .from("host-media").list(`${req.auth.userId}/${purpose}`, { limit: 100 });
            if (listError && !/not found/i.test(listError.message)) throw listError;
            const paths = (files || []).filter((file) => file.name && file.id)
                .map((file) => `${req.auth.userId}/${purpose}/${file.name}`);
            if (paths.length) {
                const { error: removeError } = await supabase.storage.from("host-media").remove(paths);
                if (removeError) throw removeError;
            }
        }
    }
    const phone = user.phone || user.phone_number;
    if (phone) {
        const { error: otpError } = await supabase.from("otp_codes").delete().eq("phone", phone);
        if (otpError && !/schema cache|could not find|relation/i.test(otpError.message || "")) throw otpError;
    }
    const { error } = await supabase.from("users").delete().eq("id", req.auth.userId);
    if (error) throw error;
    res.status(200).json({ deleted: true });
}));

module.exports = router;
