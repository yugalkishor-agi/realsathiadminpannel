const AppError = require("../utils/app-error");

class OtpRepository {
    constructor(supabase) {
        this.supabase = supabase;
        this.table = "otp_codes";
    }

    async upsertOtpCode(payload) {
        const { error: deleteError } = await this.supabase
            .from(this.table)
            .delete()
            .eq("phone", payload.phone);

        if (deleteError) {
            throw new AppError("Failed to clear existing OTP request.", 500, deleteError.message);
        }

        const { data, error } = await this.supabase
            .from(this.table)
            .insert(payload)
            .select("*")
            .single();

        if (error) {
            throw new AppError("Failed to create OTP request.", 500, error.message);
        }

        return data;
    }

    async findByPhone(phone) {
        const { data, error } = await this.supabase
            .from(this.table)
            .select("*")
            .eq("phone", phone)
            .maybeSingle();

        if (error) {
            throw new AppError("Failed to fetch OTP request.", 500, error.message);
        }

        return data;
    }

    async updateAttempts(phone, attempts) {
        const { error } = await this.supabase
            .from(this.table)
            .update({ attempts })
            .eq("phone", phone);

        if (error) {
            throw new AppError("Failed to update OTP attempts.", 500, error.message);
        }
    }

    async deleteByPhone(phone) {
        const { error } = await this.supabase
            .from(this.table)
            .delete()
            .eq("phone", phone);

        if (error) {
            throw new AppError("Failed to remove OTP request.", 500, error.message);
        }
    }
}

module.exports = OtpRepository;
