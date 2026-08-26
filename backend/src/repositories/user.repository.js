const AppError = require("../utils/app-error");

class UserRepository {
    constructor(supabase) {
        this.supabase = supabase;
        this.userTable = "users";
        this.profileTable = "user_profiles";
    }

    async findByPhoneNumber(phoneNumber) {
        for (const column of ["phone", "phone_number"]) {
            for (const value of this.phoneLookupValues(phoneNumber)) {
                const { data, error } = await this.supabase
                    .from(this.userTable)
                    .select("*")
                    .eq(column, value)
                    .maybeSingle();

                if (error) {
                    if (this.isSchemaError(error)) {
                        break;
                    }
                    throw new AppError("Failed to fetch user account.", 500, error.message);
                }

                if (data) {
                    return data;
                }
            }
        }

        return null;
    }

    async findById(userId) {
        const { data, error } = await this.supabase
            .from(this.userTable)
            .select("*")
            .eq("id", userId)
            .maybeSingle();

        if (error) {
            throw new AppError("Failed to fetch user account.", 500, error.message);
        }

        return data;
    }

    async findByPublicId(publicId) {
        const normalizedPublicId = String(publicId || "").trim();
        if (!normalizedPublicId) {
            return null;
        }

        const { data, error } = await this.supabase
            .from(this.userTable)
            .select("id, public_id")
            .eq("public_id", normalizedPublicId)
            .maybeSingle();

        if (error) {
            if (this.isSchemaError(error)) {
                return null;
            }
            throw new AppError("Failed to check Frndzz ID.", 500, error.message);
        }

        return data;
    }

    async publicIdBelongsToAnotherUser(publicId, userId = "") {
        const existingUser = await this.findByPublicId(publicId);
        if (!existingUser) {
            return false;
        }

        return String(existingUser.id || "") !== String(userId || "");
    }

    async createUser(payload) {
        const primaryResult = await this.supabase
            .from(this.userTable)
            .insert(payload)
            .select("*")
            .single();

        if (!primaryResult.error) {
            return primaryResult.data;
        }

        if (!this.isSchemaError(primaryResult.error)) {
            throw new AppError("Failed to create user account.", 500, primaryResult.error.message);
        }

        const legacyResult = await this.supabase
            .from(this.userTable)
            .insert(this.toLegacyUserPayload(payload))
            .select("*")
            .single();

        if (legacyResult.error) {
            throw new AppError("Failed to create user account.", 500, legacyResult.error.message);
        }

        return legacyResult.data;
    }

    async updateUser(userId, payload) {
        const primaryResult = await this.supabase
            .from(this.userTable)
            .update(payload)
            .eq("id", userId)
            .select("*")
            .single();

        if (!primaryResult.error) {
            return primaryResult.data;
        }

        if (!this.isSchemaError(primaryResult.error)) {
            throw new AppError("Failed to update user account.", 500, primaryResult.error.message);
        }

        const legacyPayload = this.toLegacyUserUpdatePayload(payload);
        if (Object.keys(legacyPayload).length === 0) {
            return this.findById(userId);
        }

        const legacyResult = await this.supabase
            .from(this.userTable)
            .update(legacyPayload)
            .eq("id", userId)
            .select("*")
            .single();

        if (legacyResult.error) {
            throw new AppError("Failed to update user account.", 500, legacyResult.error.message);
        }

        return legacyResult.data;
    }

    async findProfileByUserId(userId) {
        try {
            const { data, error } = await this.supabase
                .from(this.profileTable)
                .select("*")
                .eq("user_id", userId)
                .maybeSingle();

            if (error) {
                if (String(error.code || "").toUpperCase() === "PGRST205") {
                    return null;
                }
                throw new AppError("Failed to fetch user profile.", 500, error.message);
            }

            return data;
        } catch (error) {
            if (String(error?.message || "").includes("schema cache")) {
                return null;
            }
            throw error;
        }
    }

    async upsertProfile(payload) {
        const { data, error } = await this.supabase
            .from(this.profileTable)
            .upsert(payload, { onConflict: "user_id" })
            .select("*")
            .single();

        if (error) {
            if (String(error.code || "").toUpperCase() === "PGRST205") {
                return null;
            }
            if (this.isSchemaError(error)) {
                const legacyPayload = {
                    user_id: payload.user_id,
                    display_name: payload.display_name,
                    gender: payload.gender,
                    preferred_language: payload.preferred_language
                };
                const legacyResult = await this.supabase
                    .from(this.profileTable)
                    .upsert(legacyPayload, { onConflict: "user_id" })
                    .select("*")
                    .single();

                if (legacyResult.error) {
                    if (this.isSchemaError(legacyResult.error)) {
                        return null;
                    }
                    throw new AppError("Failed to save user profile.", 500, legacyResult.error.message);
                }

                return legacyResult.data;
            }
            throw new AppError("Failed to save user profile.", 500, error.message);
        }

        return data;
    }

    async updateLastLogin(userId) {
        const primaryResult = await this.supabase
            .from(this.userTable)
            .update({
                last_active: new Date().toISOString()
            })
            .eq("id", userId);

        if (!primaryResult.error) {
            return;
        }

        if (!this.isSchemaError(primaryResult.error)) {
            throw new AppError("Failed to update last login timestamp.", 500, primaryResult.error.message);
        }

        const legacyResult = await this.supabase
            .from(this.userTable)
            .update({
                last_login_at: new Date().toISOString()
            })
            .eq("id", userId);

        if (legacyResult.error) {
            throw new AppError("Failed to update last login timestamp.", 500, legacyResult.error.message);
        }
    }

    phoneLookupValues(phoneNumber) {
        const raw = String(phoneNumber || "").trim();
        const digits = raw.replace(/\D/g, "");
        const values = [raw];

        if (digits) {
            values.push(digits);
            values.push(`+${digits}`);
        }

        if (digits.length === 10) {
            values.push(`91${digits}`);
            values.push(`+91${digits}`);
        }

        return [...new Set(values.filter(Boolean))];
    }

    toLegacyUserPayload(payload) {
        const phone = this.phoneLookupValues(payload.phone || payload.phone_number)
            .find((value) => value.startsWith("+")) ||
            String(payload.phone || payload.phone_number || "");
        const role = String(payload.role || "user").trim().toLowerCase();
        const now = new Date().toISOString();

        return {
            phone_number: phone,
            role,
            is_host: role === "host",
            host_status: payload.host_status || "not_applicable",
            is_active: payload.account_status !== "disabled",
            last_login_at: payload.last_active || now
        };
    }

    toLegacyUserUpdatePayload(payload) {
        const update = {};

        if (payload.role != null) {
            update.role = payload.role;
            update.is_host = String(payload.role).trim().toLowerCase() === "host";
        }
        if (payload.host_status != null) {
            update.host_status = payload.host_status;
        }
        if (payload.account_status != null) {
            update.is_active = payload.account_status !== "disabled";
        }
        if (payload.is_online != null) {
            update.is_active = Boolean(payload.is_online);
        }
        if (payload.last_active != null) {
            update.last_login_at = payload.last_active;
        }

        return update;
    }

    isSchemaError(error) {
        const code = String(error?.code || "").toUpperCase();
        const message = String(error?.message || "");
        return code === "PGRST204" ||
            code === "PGRST205" ||
            message.includes("schema cache") ||
            message.includes("Could not find") ||
            message.includes("column");
    }
}

module.exports = UserRepository;
