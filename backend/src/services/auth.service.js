const crypto = require("crypto");
const env = require("../config/env");
const {
    HOST_STATUSES,
    USER_ROLES,
    mapUserProfile,
    mapUserSession
} = require("../models/user.model");
const { buildDisplayName, generateOtpCode, generatePublicId, isValidPublicId } = require("../utils/auth.util");
const AppError = require("../utils/app-error");

const DEFAULT_LANGUAGE = "All";
const DEFAULT_ACCOUNT_MODE = "customer";
const DEFAULT_HOST_STATUS = HOST_STATUSES.NOT_APPLICABLE;
const DEFAULT_AUDIO_RATE = 35;
const DEFAULT_VIDEO_RATE = 65;
const DEFAULT_AVATAR_ID = 9;
const pendingOtpRequestsById = new Map();
const pendingOtpRequestsByPhone = new Map();

class AuthService {
    constructor({ otpRepository, userRepository, otpService, tokenService }) {
        this.otpRepository = otpRepository;
        this.userRepository = userRepository;
        this.otpService = otpService;
        this.tokenService = tokenService;
    }

    async sendOtp({ phoneNumber }) {
        const normalizedPhone = this.normalizePhoneNumber(phoneNumber);
        const dbPhone = this.toDatabasePhone(normalizedPhone);
        const requestId = crypto.randomUUID();
        const otpCode = env.otp.provider === "mock"
            ? env.otp.mockOtpCode
            : generateOtpCode(6);
        const expiresAt = new Date(
            Date.now() + env.otp.expirySeconds * 1000
        ).toISOString();

        await this.otpService.sendOtp({
            phoneNumber: normalizedPhone,
            otpCode,
            requestId
        });

        await this.otpRepository.upsertOtpCode({
            phone: dbPhone,
            otp: otpCode,
            attempts: 0,
            expires_at: expiresAt
        });

        const pendingRequest = {
            phone: dbPhone,
            otp: otpCode,
            attempts: 0,
            expiresAt
        };
        pendingOtpRequestsById.set(requestId, pendingRequest);
        pendingOtpRequestsByPhone.set(dbPhone, {
            ...pendingRequest,
            requestId
        });

        return {
            requestId,
            message: "OTP sent successfully.",
            retryAfterSeconds: env.otp.retryAfterSeconds
        };
    }

    async verifyOtp({ phoneNumber, otpCode, requestId, deviceId = "", deviceBrand = "", countryCode = "", appBrand = "RealSaathi" }) {
        const normalizedPhone = this.normalizePhoneNumber(phoneNumber);
        const dbPhone = this.toDatabasePhone(normalizedPhone);
        let request = await this.otpRepository.findByPhone(dbPhone);

        if (!request && requestId) {
            request = pendingOtpRequestsById.get(requestId) || null;
        }

        if (!request) {
            request = pendingOtpRequestsByPhone.get(dbPhone) || null;
        }

        if (!request) {
            throw new AppError("OTP request not found for this mobile number. Please request OTP again.", 404);
        }

        const expiresAtValue = request.expires_at || request.expiresAt;
        const expiresAt = this.parseExpiryTimestamp(expiresAtValue);
        if (!Number.isFinite(expiresAt) || expiresAt <= Date.now()) {
            await this.otpRepository.deleteByPhone(dbPhone);
            this.clearPendingOtp(requestId, dbPhone);
            throw new AppError("OTP expired. Please request a new OTP.", 400);
        }

        const currentAttempts = Number(request.attempts || 0);
        if (currentAttempts >= env.otp.maxAttempts) {
            await this.otpRepository.deleteByPhone(dbPhone);
            this.clearPendingOtp(requestId, dbPhone);
            throw new AppError("Too many invalid attempts. Please request a new OTP.", 429);
        }

        const isOtpValid = String(request.otp || "").trim() === String(otpCode || "").trim();

        if (!isOtpValid) {
            const nextAttempts = currentAttempts + 1;
            await this.otpRepository.updateAttempts(dbPhone, nextAttempts);
            this.updatePendingAttempts(requestId, dbPhone, nextAttempts);

            throw new AppError("Invalid OTP code.", 400, {
                remainingAttempts: Math.max(env.otp.maxAttempts - nextAttempts, 0)
            });
        }

        await this.otpRepository.deleteByPhone(dbPhone);
        this.clearPendingOtp(requestId, dbPhone);

        const { userRecord, isNewUser } = await this.findOrCreateUser(normalizedPhone);
        const activeUserRecord = await this.userRepository.updateUser(userRecord.id, {
            last_active: new Date().toISOString(),
            is_online: true,
            device_id: deviceId,
            device_brand: deviceBrand,
            signup_country: countryCode,
            app_brand: appBrand,
            session_version: Number(userRecord.session_version || 0) + 1
        });

        const userSession = mapUserSession(activeUserRecord);
        const profile = mapUserProfile(activeUserRecord);
        const tokens = this.tokenService.issueAuthTokens(userSession);

        return {
            accessToken: tokens.accessToken,
            refreshToken: tokens.refreshToken,
            isNewUser,
            user: userSession,
            profile
        };
    }

    async findOrCreateUser(phoneNumber) {
        let userRecord = await this.userRepository.findByPhoneNumber(phoneNumber);
        let isNewUser = false;

        if (!userRecord) {
            userRecord = await this.createNewUserWithUniquePublicId(phoneNumber);
            isNewUser = true;
            return { userRecord, isNewUser };
        }

        const missingDefaults = this.buildMissingUserDefaults(userRecord, phoneNumber);
        if (!isValidPublicId(userRecord.public_id)) {
            missingDefaults.public_id = await this.generateUniquePublicId(phoneNumber, userRecord.id);
        }
        if (Object.keys(missingDefaults).length > 0) {
            userRecord = await this.userRepository.updateUser(userRecord.id, missingDefaults);
        }

        return { userRecord, isNewUser };
    }

    async createNewUserWithUniquePublicId(phoneNumber) {
        for (let attempt = 0; attempt < 100; attempt += 1) {
            const publicId = await this.generateUniquePublicId(phoneNumber);
            try {
                return await this.userRepository.createUser(this.buildNewUser(phoneNumber, publicId));
            } catch (error) {
                if (!this.isPublicIdCollisionError(error)) {
                    throw error;
                }
            }
        }

        throw new AppError("Unable to generate unique RealSaathi ID. Please try again.", 500);
    }

    buildNewUser(phoneNumber, publicId) {
        const now = new Date().toISOString();
        return {
            phone: this.toDatabasePhone(phoneNumber),
            public_id: publicId,
            username: buildDisplayName(phoneNumber),
            gender: "",
            language: DEFAULT_LANGUAGE,
            role: USER_ROLES.USER,
            username_change_count: 0,
            created_at: now,
            last_active: now,
            is_online: true,
            wallet_locked: false,
            account_status: "active",
            avatar_id: DEFAULT_AVATAR_ID,
            interests: [],
            account_mode: DEFAULT_ACCOUNT_MODE,
            host_status: DEFAULT_HOST_STATUS,
            host_audio_live: false,
            host_video_live: false,
            host_audio_rate: DEFAULT_AUDIO_RATE,
            host_video_rate: DEFAULT_VIDEO_RATE,
            host_profile_photo_url: "",
            host_story_items: [],
            community_name: "",
            community_city: "",
            community_about: "",
            community_experience: "",
            device_id: "",
            device_brand: "",
            signup_country: "",
            app_brand: "RealSaathi",
            session_version: 0
        };
    }

    buildMissingUserDefaults(userRecord, phoneNumber) {
        const updates = {};

        if (!String(userRecord.username || "").trim()) {
            updates.username = buildDisplayName(phoneNumber);
        }
        if (userRecord.gender == null) {
            updates.gender = "";
        }
        if (!String(userRecord.language || "").trim()) {
            updates.language = DEFAULT_LANGUAGE;
        }
        if (!String(userRecord.role || "").trim()) {
            updates.role = USER_ROLES.USER;
        }
        if (userRecord.username_change_count == null) {
            updates.username_change_count = 0;
        }
        if (userRecord.last_active == null) {
            updates.last_active = new Date().toISOString();
        }
        if (userRecord.is_online == null) {
            updates.is_online = true;
        }
        if (userRecord.wallet_locked == null) {
            updates.wallet_locked = false;
        }
        if (!String(userRecord.account_status || "").trim()) {
            updates.account_status = "active";
        }
        if (userRecord.avatar_id == null) {
            updates.avatar_id = DEFAULT_AVATAR_ID;
        }
        if (!Array.isArray(userRecord.interests)) {
            updates.interests = [];
        }
        if (!String(userRecord.account_mode || "").trim()) {
            updates.account_mode = DEFAULT_ACCOUNT_MODE;
        }
        if (!String(userRecord.host_status || "").trim()) {
            updates.host_status = String(userRecord.role || "").trim().toLowerCase() === "host"
                ? HOST_STATUSES.APPROVED
                : DEFAULT_HOST_STATUS;
        }
        if (userRecord.host_audio_live == null) {
            updates.host_audio_live = false;
        }
        if (userRecord.host_video_live == null) {
            updates.host_video_live = false;
        }
        if (userRecord.host_audio_rate == null) {
            updates.host_audio_rate = DEFAULT_AUDIO_RATE;
        }
        if (userRecord.host_video_rate == null) {
            updates.host_video_rate = DEFAULT_VIDEO_RATE;
        }
        if (userRecord.host_profile_photo_url == null) {
            updates.host_profile_photo_url = "";
        }
        if (!Array.isArray(userRecord.host_story_items)) {
            updates.host_story_items = [];
        }
        if (userRecord.community_name == null) {
            updates.community_name = "";
        }
        if (userRecord.community_city == null) {
            updates.community_city = "";
        }
        if (userRecord.community_about == null) {
            updates.community_about = "";
        }
        if (userRecord.community_experience == null) {
            updates.community_experience = "";
        }
        if (userRecord.device_id == null) updates.device_id = "";
        if (userRecord.device_brand == null) updates.device_brand = "";
        if (userRecord.signup_country == null) updates.signup_country = "";
        if (userRecord.app_brand == null) updates.app_brand = "RealSaathi";
        if (userRecord.session_version == null) updates.session_version = 0;

        return updates;
    }

    async generateUniquePublicId(seed = "", currentUserId = "") {
        for (let attempt = 0; attempt < 100; attempt += 1) {
            const candidate = generatePublicId(`${seed}:${attempt}:${crypto.randomUUID()}`);
            if (!await this.userRepository.publicIdBelongsToAnotherUser(candidate, currentUserId)) {
                return candidate;
            }
        }

        throw new AppError("Unable to generate unique RealSaathi ID. Please try again.", 500);
    }

    isPublicIdCollisionError(error) {
        const message = String(error?.message || error?.details || "").toLowerCase();
        return message.includes("public_id") &&
            (message.includes("duplicate") || message.includes("unique"));
    }

    normalizePhoneNumber(phoneNumber) {
        const value = String(phoneNumber || "").trim();
        const digits = value.replace(/\D/g, "");

        if (value.startsWith("+") && /^\+[1-9]\d{9,14}$/.test(value)) {
            return value;
        }

        if (digits.length === 10) {
            return `+91${digits}`;
        }

        if (digits.length === 11 && digits.startsWith("0")) {
            return `+91${digits.slice(1)}`;
        }

        if (digits.length === 12 && digits.startsWith("91")) {
            return `+${digits}`;
        }

        throw new AppError("Enter a valid mobile number.", 422);
    }

    toDatabasePhone(phoneNumber) {
        return String(phoneNumber || "").replace(/\D/g, "");
    }

    parseExpiryTimestamp(rawValue) {
        const value = String(rawValue || "").trim();
        if (!value) {
            return Number.NaN;
        }

        const hasTimeZoneSuffix = /(?:z|[+-]\d{2}:?\d{2})$/i.test(value);
        const normalizedValue = hasTimeZoneSuffix ? value : `${value}Z`;
        return new Date(normalizedValue).getTime();
    }

    clearPendingOtp(requestId, dbPhone) {
        if (requestId) {
            pendingOtpRequestsById.delete(requestId);
        }
        if (dbPhone) {
            const cached = pendingOtpRequestsByPhone.get(dbPhone);
            if (cached?.requestId) {
                pendingOtpRequestsById.delete(cached.requestId);
            }
            pendingOtpRequestsByPhone.delete(dbPhone);
        }
    }

    updatePendingAttempts(requestId, dbPhone, attempts) {
        if (requestId && pendingOtpRequestsById.has(requestId)) {
            pendingOtpRequestsById.set(requestId, {
                ...pendingOtpRequestsById.get(requestId),
                attempts
            });
        }

        if (dbPhone && pendingOtpRequestsByPhone.has(dbPhone)) {
            pendingOtpRequestsByPhone.set(dbPhone, {
                ...pendingOtpRequestsByPhone.get(dbPhone),
                attempts
            });
        }
    }
}

module.exports = AuthService;
