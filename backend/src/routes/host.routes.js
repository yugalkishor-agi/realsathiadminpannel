const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const UserRepository = require("../repositories/user.repository");
const { mapUserProfile, mapUserSession } = require("../models/user.model");
const asyncHandler = require("../utils/async-handler");
const AppError = require("../utils/app-error");

const router = express.Router();
const userRepository = new UserRepository(supabase);

function isSchemaError(error) {
    const code = String(error?.code || "").toUpperCase();
    const message = String(error?.message || "");
    return code === "PGRST204" ||
        code === "PGRST205" ||
        message.includes("schema cache") ||
        message.includes("Could not find") ||
        message.includes("column") ||
        message.includes("relation");
}

function toInteger(value, fallback = 0) {
    const parsed = Number.parseInt(String(value ?? ""), 10);
    return Number.isFinite(parsed) ? parsed : fallback;
}

function startOfTodayIso() {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate()).toISOString();
}

function formatDuration(seconds) {
    const safeSeconds = toInteger(seconds);
    if (safeSeconds <= 0) return "Ledger entry";
    const minutes = Math.floor(safeSeconds / 60);
    const remainingSeconds = safeSeconds % 60;
    return `${minutes}:${String(remainingSeconds).padStart(2, "0")}`;
}

function formatLedgerTime(value) {
    const date = new Date(value || Date.now());
    if (Number.isNaN(date.getTime())) return "Recently";
    return new Intl.DateTimeFormat("en-IN", {
        day: "2-digit",
        month: "short",
        hour: "numeric",
        minute: "2-digit"
    }).format(date);
}

function earningAmount(row) {
    return toInteger(row.rupees_delta) || toInteger(row.recharge_amount_rupees);
}

function mapHostLedgerLog(row) {
    const metadata = row.metadata && typeof row.metadata === "object" ? row.metadata : {};
    const kind = String(row.kind || "").toLowerCase();
    const messageCount = toInteger(metadata.messageCount);
    const durationSeconds = toInteger(metadata.durationSeconds);
    return {
        name: String(metadata.counterpartyName || row.title || "Wallet earning"),
        kind,
        isVideo: kind === "video_call",
        duration: messageCount > 0 ? `${messageCount} msg` : formatDuration(durationSeconds),
        durationSeconds,
        messageCount,
        amount: Math.abs(earningAmount(row)),
        time: formatLedgerTime(row.created_at),
        createdAt: String(row.created_at || "")
    };
}

function normalizeDigits(value) {
    return String(value || "").replace(/\D/g, "");
}

function normalizePan(value) {
    return String(value || "").trim().toUpperCase();
}

function normalizeIfsc(value) {
    return String(value || "").trim().toUpperCase();
}

function maskAadhaar(value) {
    const digits = normalizeDigits(value);
    return digits.length >= 4 ? `XXXX XXXX ${digits.slice(-4)}` : "";
}

function maskPan(value) {
    const pan = normalizePan(value);
    return pan.length >= 4 ? `${pan.slice(0, 2)}XXXX${pan.slice(-2)}` : "";
}

function maskBankAccount(value) {
    const digits = normalizeDigits(value);
    return digits.length >= 4 ? `XXXXXX${digits.slice(-4)}` : "";
}

function isValidEmail(value) {
    return /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i.test(String(value || "").trim());
}

function isValidDob(value) {
    const match = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(String(value || "").trim());
    if (!match) return false;
    const day = Number.parseInt(match[1], 10);
    const month = Number.parseInt(match[2], 10);
    const year = Number.parseInt(match[3], 10);
    const now = new Date();
    if (year < 1900 || year > now.getFullYear()) return false;
    const date = new Date(Date.UTC(year, month - 1, day));
    return date.getUTCFullYear() === year &&
        date.getUTCMonth() === month - 1 &&
        date.getUTCDate() === day &&
        date <= now;
}

function verifyVerhoeff(number) {
    const multiplication = [
        [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
        [1, 2, 3, 4, 0, 6, 7, 8, 9, 5],
        [2, 3, 4, 0, 1, 7, 8, 9, 5, 6],
        [3, 4, 0, 1, 2, 8, 9, 5, 6, 7],
        [4, 0, 1, 2, 3, 9, 5, 6, 7, 8],
        [5, 9, 8, 7, 6, 0, 4, 3, 2, 1],
        [6, 5, 9, 8, 7, 1, 0, 4, 3, 2],
        [7, 6, 5, 9, 8, 2, 1, 0, 4, 3],
        [8, 7, 6, 5, 9, 3, 2, 1, 0, 4],
        [9, 8, 7, 6, 5, 4, 3, 2, 1, 0]
    ];
    const permutation = [
        [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
        [1, 5, 7, 6, 2, 8, 3, 0, 9, 4],
        [5, 8, 0, 3, 7, 9, 6, 1, 4, 2],
        [8, 9, 1, 6, 0, 4, 3, 5, 2, 7],
        [9, 4, 5, 3, 1, 2, 6, 8, 7, 0],
        [4, 2, 8, 6, 5, 7, 3, 9, 0, 1],
        [2, 7, 9, 3, 8, 0, 6, 4, 1, 5],
        [7, 0, 4, 6, 9, 1, 3, 2, 5, 8]
    ];
    let checksum = 0;
    String(number).split("").reverse().forEach((char, index) => {
        const digit = Number.parseInt(char, 10);
        checksum = multiplication[checksum][permutation[index % 8][digit]];
    });
    return checksum === 0;
}

function isValidAadhaar(value) {
    const digits = normalizeDigits(value);
    return /^[2-9]\d{11}$/.test(digits) && verifyVerhoeff(digits);
}

function normalizePersonName(value) {
    return String(value || "")
        .toUpperCase()
        .replace(/[^A-Z ]/g, " ")
        .replace(/\s+/g, " ")
        .trim();
}

function isLockedKycStatus(status) {
    return ["pending", "submitted", "under_review", "approved"].includes(String(status || "").toLowerCase());
}

function validateHostKycBody(body = {}) {
    const aadhaarNumber = normalizeDigits(body.aadhaarNumber);
    const panNumber = normalizePan(body.panNumber);
    const fullName = String(body.fullName || "").replace(/\s+/g, " ").trim();
    const emailAddress = String(body.emailAddress || "").trim();
    const whatsappNumber = normalizeDigits(body.whatsappNumber);
    const phoneNumber = normalizeDigits(body.phoneNumber);
    const dateOfBirth = String(body.dateOfBirth || "").trim();
    const accountHolderName = String(body.accountHolderName || "")
        .replace(/\s+/g, " ")
        .trim();
    const bankName = String(body.bankName || "")
        .replace(/\s+/g, " ")
        .trim();
    const bankAccountNumber = normalizeDigits(body.bankAccountNumber);
    const ifscCode = normalizeIfsc(body.ifscCode);

    if (!isValidAadhaar(aadhaarNumber)) {
        throw new AppError("Enter valid 12 digit Aadhaar number.", 422);
    }
    if (!/^[A-Z]{5}[0-9]{4}[A-Z]$/.test(panNumber)) {
        throw new AppError("Enter valid PAN number.", 422);
    }
    if (fullName.length < 2 || /[^a-zA-Z\s.]/.test(fullName)) {
        throw new AppError("Enter valid full name.", 422);
    }
    if (!isValidEmail(emailAddress)) {
        throw new AppError("Enter valid email ID.", 422);
    }
    if (!/^\d{10}$/.test(whatsappNumber)) {
        throw new AppError("Enter valid WhatsApp number.", 422);
    }
    if (!/^\d{10}$/.test(phoneNumber)) {
        throw new AppError("Enter valid phone number.", 422);
    }
    if (!isValidDob(dateOfBirth)) {
        throw new AppError("Enter valid date of birth.", 422);
    }
    if (accountHolderName.length < 2 || /[^a-zA-Z\s.]/.test(accountHolderName)) {
        throw new AppError("Enter valid bank account holder name.", 422);
    }
    if (normalizePersonName(accountHolderName) !== normalizePersonName(fullName)) {
        throw new AppError("Account holder name must match Aadhaar name.", 422);
    }
    if (bankName.length < 2 || /[^a-zA-Z0-9\s&().-]/.test(bankName)) {
        throw new AppError("Select valid bank name.", 422);
    }
    if (!/^\d{6,18}$/.test(bankAccountNumber)) {
        throw new AppError("Enter valid bank account number.", 422);
    }
    if (!/^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifscCode)) {
        throw new AppError("Enter valid IFSC code.", 422);
    }

    return {
        aadhaarNumber,
        panNumber,
        fullName,
        emailAddress,
        whatsappNumber,
        phoneNumber,
        dateOfBirth,
        accountHolderName,
        bankName,
        bankAccountNumber,
        ifscCode,
        aadhaarImageBase64: validateKycImage(body.aadhaarImageBase64, "Aadhaar card photo"),
        panImageBase64: validateKycImage(body.panImageBase64, "PAN card photo"),
        selfieWithAadhaarImageBase64: validateKycImage(body.selfieWithAadhaarImageBase64, "Selfie photo")
    };
}

function validateKycImage(value, label) {
    const text = String(value || "").trim();
    if (!text) {
        throw new AppError(`${label} is required.`, 422);
    }
    if (!/^data:image\/(jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$/.test(text)) {
        throw new AppError(`${label} is not a valid image.`, 422);
    }
    if (text.length > 2_500_000) {
        throw new AppError(`${label} image is too large. Please retake the photo.`, 422);
    }
    return text;
}

function mapKycRow(row = null) {
    if (!row) {
        return {
            status: "not_submitted",
            message: "Complete KYC to enable withdrawals. Review is completed within 48 hours after submission."
        };
    }

    return {
        status: String(row.status || "pending"),
        fullName: String(row.full_name || ""),
        emailAddress: String(row.email_address || ""),
        whatsappNumber: String(row.whatsapp_number || ""),
        phoneNumber: String(row.phone_number || ""),
        dateOfBirth: String(row.date_of_birth || ""),
        aadhaarMasked: String(row.aadhaar_masked || ""),
        panMasked: String(row.pan_masked || ""),
        bankName: String(row.bank_name || ""),
        bankAccountMasked: String(row.bank_account_masked || ""),
        ifscCode: String(row.ifsc_code || ""),
        accountHolderName: String(row.account_holder_name || ""),
        message: String(row.message || "Verification submitted. Review will be completed within 48 hours.")
    };
}

async function fetchHostWallet(userId) {
    const { data, error } = await supabase
        .from("wallet_ledger")
        .select("*")
        .eq("user_id", userId)
        .eq("status", "completed")
        .order("created_at", { ascending: false })
        .limit(100);

    if (error) {
        if (isSchemaError(error)) {
            return {
                wallet: { totalEarnings: 0, todayEarnings: 0 },
                logs: []
            };
        }
        throw error;
    }

    const rows = data || [];
    const todayStart = startOfTodayIso();
    const earningRows = rows.filter((row) => earningAmount(row) > 0);
    return {
        wallet: {
            totalEarnings: earningRows.reduce((total, row) => total + earningAmount(row), 0),
            todayEarnings: earningRows
                .filter((row) => String(row.created_at || "") >= todayStart)
                .reduce((total, row) => total + earningAmount(row), 0)
        },
        logs: earningRows.map(mapHostLedgerLog)
    };
}

const dashboardHandler = asyncHandler(async (req, res) => {
    const userRecord = await userRepository.findById(req.auth.userId);
    const profileRecord = await userRepository.findProfileByUserId(req.auth.userId);
    const activeUser = userRecord || {
        id: req.auth.userId,
        phone_number: req.auth.phoneNumber,
        role: req.auth.isHost ? "host" : "user"
    };
    const hostWallet = await fetchHostWallet(req.auth.userId);

    res.status(200).json({
        user: mapUserSession(activeUser, profileRecord),
        profile: mapUserProfile(activeUser, profileRecord),
        wallet: hostWallet.wallet,
        logs: hostWallet.logs
    });
});

router.get("/dashboard", authenticateAccessToken, dashboardHandler);
router.post("/dashboard", authenticateAccessToken, dashboardHandler);

router.get("/kyc", authenticateAccessToken, asyncHandler(async (req, res) => {
    const { data, error } = await supabase
        .from("host_kyc_details")
        .select("*")
        .eq("user_id", req.auth.userId)
        .maybeSingle();

    if (error) {
        if (isSchemaError(error)) {
            return res.status(200).json(mapKycRow(null));
        }
        throw error;
    }

    return res.status(200).json(mapKycRow(data));
}));

router.post("/kyc", authenticateAccessToken, asyncHandler(async (req, res) => {
    const existingResult = await supabase
        .from("host_kyc_details")
        .select("*")
        .eq("user_id", req.auth.userId)
        .maybeSingle();

    if (existingResult.error && !isSchemaError(existingResult.error)) {
        throw existingResult.error;
    }

    if (existingResult.data && isLockedKycStatus(existingResult.data.status)) {
        return res.status(200).json({
            ...mapKycRow(existingResult.data),
            message: "Verification is already submitted. Review will be completed within 48 hours."
        });
    }

    const kyc = validateHostKycBody(req.body);
    const now = new Date().toISOString();
    const payload = {
        user_id: req.auth.userId,
        full_name: kyc.fullName,
        email_address: kyc.emailAddress,
        whatsapp_number: kyc.whatsappNumber,
        phone_number: kyc.phoneNumber,
        date_of_birth: kyc.dateOfBirth,
        aadhaar_masked: maskAadhaar(kyc.aadhaarNumber),
        pan_masked: maskPan(kyc.panNumber),
        bank_name: kyc.bankName,
        bank_account_masked: maskBankAccount(kyc.bankAccountNumber),
        ifsc_code: kyc.ifscCode,
        account_holder_name: kyc.accountHolderName,
        aadhaar_image_data: kyc.aadhaarImageBase64,
        pan_image_data: kyc.panImageBase64,
        selfie_aadhaar_image_data: kyc.selfieWithAadhaarImageBase64,
        status: "pending",
        message: "Verification submitted. Review will be completed within 48 hours.",
        updated_at: now
    };

    const { data, error } = await supabase
        .from("host_kyc_details")
        .upsert(payload, { onConflict: "user_id" })
        .select("*")
        .single();

    if (error) {
        if (isSchemaError(error)) {
            return res.status(200).json({
                ...mapKycRow(payload),
                message: "Verification validated. Storage setup is pending for team review."
            });
        }
        throw error;
    }

    return res.status(200).json(mapKycRow(data));
}));

router.post("/settings/save", authenticateAccessToken, asyncHandler(async (req, res) => {
    const topicTags = Array.isArray(req.body?.topicTags)
        ? req.body.topicTags.map((value) => String(value || "").trim()).filter(Boolean).slice(0, 5)
        : undefined;
    const nativeLanguages = Array.isArray(req.body?.nativeLanguages)
        ? req.body.nativeLanguages.map((value) => String(value || "").trim()).filter(Boolean).slice(0, 5)
        : undefined;
    const payload = {
        account_mode: "host",
        role: "host",
        host_status: "approved",
        host_audio_live: Boolean(req.body?.hostAudioLive),
        host_video_live: Boolean(req.body?.hostVideoLive),
        host_audio_rate: Number(req.body?.hostAudioRate || 35),
        host_video_rate: Number(req.body?.hostVideoRate || 65),
        host_profile_photo_url: String(req.body?.hostProfilePhotoUrl || ""),
        host_story_items: Array.isArray(req.body?.hostStories) ? req.body.hostStories : []
    };
    if (topicTags) {
        payload.topic_tags = topicTags;
        payload.interests = topicTags;
    }
    if (nativeLanguages) {
        payload.native_languages = nativeLanguages;
        payload.language = nativeLanguages[0] || "All";
    }

    const userRecord = await userRepository.updateUser(req.auth.userId, payload);
    const profileRecord = await userRepository.findProfileByUserId(req.auth.userId);

    res.status(200).json({
        user: mapUserSession(userRecord, profileRecord),
        profile: mapUserProfile(userRecord, profileRecord)
    });
}));

router.post("/media/upload", authenticateAccessToken, asyncHandler(async (req, res) => {
    res.status(200).json({
        publicUrl: "",
        mediaType: "IMAGE"
    });
}));

module.exports = router;
