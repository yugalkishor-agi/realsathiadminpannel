const express = require("express");
const { supabase } = require("../config/supabase");
const { authenticateAccessToken } = require("../middlewares/auth.middleware");
const { mapUserProfile } = require("../models/user.model");
const asyncHandler = require("../utils/async-handler");

const router = express.Router();

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

function normalizeList(input) {
    const values = Array.isArray(input)
        ? input
        : String(input || "")
            .split(",")
            .map((value) => value.trim());

    return values
        .map((value) => String(value || "").trim())
        .filter(Boolean);
}

function hasOverlap(recordValues, requestedValues) {
    if (requestedValues.length === 0) return true;
    const normalizedRecordValues = normalizeList(recordValues).map((value) => value.toLowerCase());
    return requestedValues.some((value) => normalizedRecordValues.includes(value.toLowerCase()));
}

function hostIsLive(host) {
    return Boolean(host.host_audio_live || host.host_video_live);
}

function mapDiscoveryHost(host) {
    const profile = mapUserProfile(host);
    return {
        id: String(host.id || ""),
        nickname: profile.nickname,
        username: profile.username,
        avatarUrl: host.host_profile_photo_url || host.avatar_url || null,
        hostProfilePhotoUrl: profile.hostProfilePhotoUrl,
        topicTags: normalizeList(host.topic_tags || host.interests),
        nativeLanguages: normalizeList(host.native_languages || host.language || profile.preferredLanguage),
        preferredLanguage: profile.preferredLanguage,
        hostAudioLive: profile.hostAudioLive,
        hostVideoLive: profile.hostVideoLive,
        hostAudioRate: profile.hostAudioRate,
        hostVideoRate: profile.hostVideoRate,
        hostStatus: profile.hostStatus
    };
}

async function fetchBlockedIds(userId) {
    const { data, error } = await supabase
        .from("user_blocks")
        .select("blocker_id, blocked_id")
        .or(`blocker_id.eq.${userId},blocked_id.eq.${userId}`);

    if (error) {
        if (isSchemaError(error)) return new Set();
        throw error;
    }

    return new Set(
        (data || [])
            .map((row) => String(row.blocker_id) === String(userId) ? row.blocked_id : row.blocker_id)
            .filter(Boolean)
            .map(String)
    );
}

async function fetchEligibleHosts({ userId, topicTags, languages, liveOnly = true }) {
    const { data, error } = await supabase
        .from("users")
        .select("*")
        .eq("role", "host")
        .eq("host_status", "approved")
        .limit(100);

    if (error) {
        if (isSchemaError(error)) return [];
        throw error;
    }

    const blockedIds = await fetchBlockedIds(userId);
    return (data || [])
        .filter((host) => String(host.id || "") !== String(userId || ""))
        .filter((host) => !blockedIds.has(String(host.id || "")))
        .filter((host) => !liveOnly || hostIsLive(host))
        .filter((host) => hasOverlap(host.topic_tags || host.interests, topicTags))
        .filter((host) => hasOverlap(host.native_languages || host.language, languages))
        .map(mapDiscoveryHost);
}

router.get("/hosts", authenticateAccessToken, asyncHandler(async (req, res) => {
    const topicTags = normalizeList(req.query.topicTags || req.query.topic);
    const languages = normalizeList(req.query.languages || req.query.language);
    const liveOnly = String(req.query.liveOnly ?? "true").toLowerCase() !== "false";
    const hosts = await fetchEligibleHosts({
        userId: req.auth.userId,
        topicTags,
        languages,
        liveOnly
    });

    res.status(200).json({
        hosts,
        filters: {
            topicTags,
            languages,
            liveOnly
        }
    });
}));

router.post("/random-match", authenticateAccessToken, asyncHandler(async (req, res) => {
    const topicTags = normalizeList(req.body?.topicTags || req.body?.topic);
    const languages = normalizeList(req.body?.languages || req.body?.language);
    const hosts = await fetchEligibleHosts({
        userId: req.auth.userId,
        topicTags,
        languages,
        liveOnly: true
    });

    if (hosts.length === 0) {
        res.status(200).json({
            matched: false,
            host: null,
            message: "Abhi matching host available nahi hai. Thodi der baad retry karo."
        });
        return;
    }

    const host = hosts[Math.floor(Math.random() * hosts.length)];
    res.status(200).json({
        matched: true,
        host,
        message: "Host matched successfully."
    });
}));

module.exports = router;
