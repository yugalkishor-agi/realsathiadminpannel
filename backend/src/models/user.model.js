const { isValidPublicId } = require("../utils/auth.util");

const USER_ROLES = Object.freeze({
    USER: "user",
    HOST: "host",
    ADMIN: "admin"
});

const HOST_STATUSES = Object.freeze({
    NOT_APPLICABLE: "not_applicable",
    PENDING: "pending",
    APPROVED: "approved",
    REJECTED: "rejected"
});

function pickString(...values) {
    for (const value of values) {
        const text = String(value ?? "").trim();
        if (text) {
            return text;
        }
    }
    return "";
}

function formatPhoneNumber(phoneNumber) {
    const digits = String(phoneNumber ?? "").replace(/\D/g, "");
    if (!digits) {
        return "";
    }

    if (digits.startsWith("91") && digits.length === 12) {
        return `+${digits}`;
    }

    if (digits.length === 10) {
        return `+91${digits}`;
    }

    return `+${digits}`;
}

function resolvePublicId(userRecord, profileRecord = null) {
    const storedPublicId = pickString(
        userRecord.public_id,
        profileRecord?.publicId
    );

    return isValidPublicId(storedPublicId) ? storedPublicId : null;
}

function mapUserSession(userRecord, profileRecord = null) {
    const phone = formatPhoneNumber(
        pickString(userRecord.phone, userRecord.phone_number, profileRecord?.phone)
    );
    const displayName = pickString(
        userRecord.nickname,
        userRecord.username,
        profileRecord?.display_name,
        profileRecord?.nickname,
        profileRecord?.username
    );

    return {
        id: String(userRecord.id || ""),
        phoneNumber: phone,
        displayName: displayName || null,
        publicId: resolvePublicId(userRecord, profileRecord),
        isHost: String(userRecord.role || "").trim().toLowerCase() === "host" || Boolean(userRecord.is_host)
    };
}

function mapUserProfile(userRecord, profileRecord = null, hostStories = []) {
    const nickname = pickString(
        userRecord.nickname,
        userRecord.username,
        profileRecord?.display_name,
        profileRecord?.nickname,
        profileRecord?.username
    );
    const isHost =
        String(userRecord.role || "").trim().toLowerCase() === "host" ||
        Boolean(userRecord.is_host);

    return {
        nickname: nickname || "Frndzz User",
        username: nickname || "Frndzz User",
        publicId: resolvePublicId(userRecord, profileRecord),
        gender: pickString(userRecord.gender, profileRecord?.gender) || null,
        preferredLanguage: pickString(userRecord.language, profileRecord?.preferred_language) || "All",
        nativeLanguages: Array.isArray(userRecord.native_languages)
            ? userRecord.native_languages
            : (profileRecord?.native_languages || []),
        avatarId: Number(userRecord.avatar_id ?? profileRecord?.avatar_id ?? 1) || 1,
        interests: Array.isArray(userRecord.interests)
            ? userRecord.interests
            : (profileRecord?.interests || []),
        topicTags: Array.isArray(userRecord.topic_tags)
            ? userRecord.topic_tags
            : (profileRecord?.topic_tags || profileRecord?.interests || []),
        accountMode: pickString(userRecord.account_mode, profileRecord?.accountMode) || (isHost ? "host" : "customer"),
        hostStatus: pickString(userRecord.host_status, profileRecord?.hostStatus) || "not_applicable",
        communityName: pickString(userRecord.community_name, profileRecord?.communityName) || null,
        communityCity: pickString(userRecord.community_city, profileRecord?.communityCity) || null,
        communityAbout: pickString(userRecord.community_about, userRecord.bio, profileRecord?.communityAbout) || null,
        communityExperience: pickString(userRecord.community_experience, profileRecord?.communityExperience) || null,
        hostAudioLive: Boolean(userRecord.host_audio_live ?? profileRecord?.hostAudioLive),
        hostVideoLive: Boolean(userRecord.host_video_live ?? profileRecord?.hostVideoLive),
        hostAudioRate: Number(userRecord.host_audio_rate ?? profileRecord?.hostAudioRate ?? 35) || 35,
        hostVideoRate: Number(userRecord.host_video_rate ?? profileRecord?.hostVideoRate ?? 65) || 65,
        hostProfilePhotoUrl: pickString(userRecord.host_profile_photo_url, userRecord.avatar_url, profileRecord?.hostProfilePhotoUrl) || null,
        hostStories
    };
}

module.exports = {
    USER_ROLES,
    HOST_STATUSES,
    mapUserSession,
    mapUserProfile
};
