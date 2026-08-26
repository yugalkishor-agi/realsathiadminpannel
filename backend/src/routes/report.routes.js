const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const asyncHandler = require("../utils/async-handler");
const AppError = require("../utils/app-error");

const router = express.Router();

const ALLOWED_REASONS = new Set([
    "harassment",
    "fake_profile",
    "inappropriate_content",
    "other"
]);

const ALLOWED_CONTEXTS = new Set([
    "call",
    "chat",
    "profile",
    "random_match",
    "discovery"
]);

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

function normalizeChoice(value, allowed, fallback) {
    const normalized = String(value || "").trim().toLowerCase();
    return allowed.has(normalized) ? normalized : fallback;
}

router.post("/", authenticateAccessToken, asyncHandler(async (req, res) => {
    const reportedUserId = String(req.body?.reportedUserId || req.body?.reportedId || "").trim();
    if (!reportedUserId) {
        throw new AppError("Reported user is required.", 422);
    }
    if (reportedUserId === String(req.auth.userId)) {
        throw new AppError("You cannot report yourself.", 422);
    }

    const reason = normalizeChoice(req.body?.reason, ALLOWED_REASONS, "other");
    const context = normalizeChoice(req.body?.context, ALLOWED_CONTEXTS, "profile");
    const note = String(req.body?.note || "").trim().slice(0, 500);
    const shouldBlock = req.body?.block !== false;

    const reportPayload = {
        reporter_id: req.auth.userId,
        reported_id: reportedUserId,
        reason,
        context,
        note,
        status: "pending"
    };

    const reportResult = await supabase
        .from("user_reports")
        .insert(reportPayload)
        .select("*")
        .single();

    if (reportResult.error && !isSchemaError(reportResult.error)) {
        throw new AppError("Failed to submit report.", 500, reportResult.error.message);
    }

    let blocked = false;
    if (shouldBlock) {
        const blockResult = await supabase
            .from("user_blocks")
            .upsert({
                blocker_id: req.auth.userId,
                blocked_id: reportedUserId
            }, { onConflict: "blocker_id,blocked_id" })
            .select("*")
            .single();

        if (blockResult.error && !isSchemaError(blockResult.error)) {
            throw new AppError("Report submitted, but block failed.", 500, blockResult.error.message);
        }

        blocked = !blockResult.error;
    }

    res.status(200).json({
        submitted: !reportResult.error,
        blocked,
        reportId: reportResult.data?.id || null,
        message: shouldBlock
            ? "Report submitted. User blocked for future matching."
            : "Report submitted."
    });
}));

router.post("/unblock", authenticateAccessToken, asyncHandler(async (req, res) => {
    const blockedUserId = String(req.body?.blockedUserId || req.body?.userId || "").trim();
    if (!blockedUserId) {
        throw new AppError("Blocked user is required.", 422);
    }

    const result = await supabase
        .from("user_blocks")
        .delete()
        .eq("blocker_id", req.auth.userId)
        .eq("blocked_id", blockedUserId);

    if (result.error && !isSchemaError(result.error)) {
        throw new AppError("Failed to unblock user.", 500, result.error.message);
    }

    res.status(200).json({
        unblocked: !result.error,
        message: "User unblocked."
    });
}));

module.exports = router;
