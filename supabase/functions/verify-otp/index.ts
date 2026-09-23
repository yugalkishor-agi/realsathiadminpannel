import {
  DEFAULT_ACCOUNT_MODE,
  DEFAULT_AUDIO_RATE,
  DEFAULT_AVATAR_ID,
  DEFAULT_HOST_STATUS,
  DEFAULT_LANGUAGE,
  DEFAULT_VIDEO_RATE,
  buildDisplayName,
  buildProfilePayload,
  buildUserSession,
  corsResponse,
  createAdminClient,
  digitsOnly,
  issueAuthTokens,
  jsonResponse,
  normalizeIndianPhone,
} from "../_shared/auth.ts";

const OTP_MAX_ATTEMPTS = Number(Deno.env.get("OTP_MAX_ATTEMPTS") ?? "5");

function buildNewUser(phoneNumber: string) {
  const username = buildDisplayName(phoneNumber);
  const now = new Date().toISOString();

  return {
    phone: phoneNumber,
    username,
    gender: "",
    language: DEFAULT_LANGUAGE,
    role: "user",
    username_change_count: 0,
    created_at: now,
    last_active: now,
    is_online: false,
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
    session_version: 0,
  };
}

function buildMissingUserDefaults(userRow: Record<string, unknown>, phoneNumber: string) {
  const updates: Record<string, unknown> = {};

  if (["blocked", "banned", "suspended"].includes(String(userRow.account_status ?? "active").trim().toLowerCase())) {
    return jsonResponse({ message: "This account is not allowed to access the app." }, 403);
  }

  if (!String(userRow.username ?? "").trim()) {
    updates.username = buildDisplayName(phoneNumber);
  }
  if (userRow.gender == null) {
    updates.gender = "";
  }
  if (!String(userRow.language ?? "").trim()) {
    updates.language = DEFAULT_LANGUAGE;
  }
  if (!String(userRow.role ?? "").trim()) {
    updates.role = "user";
  }
  if (userRow.username_change_count == null) {
    updates.username_change_count = 0;
  }
  if (userRow.last_active == null) {
    updates.last_active = new Date().toISOString();
  }
  if (userRow.is_online == null) {
    updates.is_online = false;
  }
  if (userRow.wallet_locked == null) {
    updates.wallet_locked = false;
  }
  if (!String(userRow.account_status ?? "").trim()) {
    updates.account_status = "active";
  }
  if (userRow.avatar_id == null) {
    updates.avatar_id = DEFAULT_AVATAR_ID;
  }
  if (!Array.isArray(userRow.interests)) {
    updates.interests = [];
  }
  if (!String(userRow.account_mode ?? "").trim()) {
    updates.account_mode = DEFAULT_ACCOUNT_MODE;
  }
  if (!String(userRow.host_status ?? "").trim()) {
    updates.host_status =
      String(userRow.role ?? "").trim().toLowerCase() === "host"
        ? "approved"
        : DEFAULT_HOST_STATUS;
  }
  if (userRow.host_audio_live == null) {
    updates.host_audio_live = false;
  }
  if (userRow.host_video_live == null) {
    updates.host_video_live = false;
  }
  if (userRow.host_audio_rate == null) {
    updates.host_audio_rate = DEFAULT_AUDIO_RATE;
  }
  if (userRow.host_video_rate == null) {
    updates.host_video_rate = DEFAULT_VIDEO_RATE;
  }
  if (userRow.host_profile_photo_url == null) {
    updates.host_profile_photo_url = "";
  }
  if (!Array.isArray(userRow.host_story_items)) {
    updates.host_story_items = [];
  }
  if (userRow.community_name == null) {
    updates.community_name = "";
  }
  if (userRow.community_city == null) {
    updates.community_city = "";
  }
  if (userRow.community_about == null) {
    updates.community_about = "";
  }
  if (userRow.community_experience == null) {
    updates.community_experience = "";
  }

  return updates;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }

  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const body = await req.json();
    const phoneNumber = normalizeIndianPhone(body?.phoneNumber);
    const otpCode = String(body?.otpCode ?? "").trim();
    const deviceId = String(body?.deviceId ?? "").trim().slice(0, 200);
    const deviceBrand = String(body?.deviceBrand ?? body?.brandName ?? "").trim().slice(0, 120);
    const signupCountry = String(body?.countryCode ?? "").trim().toUpperCase().slice(0, 8);
    const appBrand = String(body?.appBrand ?? "RealSaathi").trim().slice(0, 80) || "RealSaathi";
    const dbPhoneNumber = digitsOnly(phoneNumber);

    if (!phoneNumber) {
      return jsonResponse({ message: "Enter a valid mobile number." }, 422);
    }

    if (!/^\d{6}$/.test(otpCode)) {
      return jsonResponse({ message: "Enter a valid 6-digit OTP." }, 422);
    }

    const supabase = createAdminClient();
    const { data: otpRequest, error: otpError } = await supabase
      .from("otp_codes")
      .select("*")
      .eq("phone", dbPhoneNumber)
      .maybeSingle();

    if (otpError) {
      return jsonResponse({ message: otpError.message }, 500);
    }

    if (!otpRequest || otpRequest.phone !== dbPhoneNumber) {
      return jsonResponse(
        { message: "OTP request not found for this mobile number." },
        404
      );
    }

    if (new Date(otpRequest.expires_at).getTime() <= Date.now()) {
      await supabase.from("otp_codes").delete().eq("phone", dbPhoneNumber);

      return jsonResponse(
        { message: "OTP expired. Please request a new OTP." },
        400
      );
    }

    if (Number(otpRequest.attempts) >= OTP_MAX_ATTEMPTS) {
      await supabase.from("otp_codes").delete().eq("phone", dbPhoneNumber);

      return jsonResponse(
        { message: "Too many invalid attempts. Please request a new OTP." },
        429
      );
    }

    const isOtpValid = otpCode === String(otpRequest.otp ?? "");

    if (!isOtpValid) {
      const nextAttempts = Number(otpRequest.attempts) + 1;

      await supabase
        .from("otp_codes")
        .update({ attempts: nextAttempts })
        .eq("phone", dbPhoneNumber);

      return jsonResponse(
        {
          message: "Invalid OTP code.",
          remainingAttempts: Math.max(OTP_MAX_ATTEMPTS - nextAttempts, 0),
        },
        400
      );
    }

    await supabase.from("otp_codes").delete().eq("phone", dbPhoneNumber);

    const { data: existingUser, error: userLookupError } = await supabase
      .from("users")
      .select("*")
      .eq("phone", dbPhoneNumber)
      .maybeSingle();

    if (userLookupError) {
      return jsonResponse({ message: userLookupError.message }, 500);
    }

    let userRecord = existingUser;
    let isNewUser = false;

    if (!userRecord) {
      const { data: newUser, error: createUserError } = await supabase
        .from("users")
        .insert([buildNewUser(dbPhoneNumber)])
        .select("*")
        .single();

      if (createUserError) {
        return jsonResponse({ message: createUserError.message }, 500);
      }

      userRecord = newUser;
      isNewUser = true;
    } else {
      const missingDefaults = buildMissingUserDefaults(userRecord, dbPhoneNumber);

      if (Object.keys(missingDefaults).length > 0) {
        const { data: updatedUser, error: updateUserError } = await supabase
          .from("users")
          .update(missingDefaults)
          .eq("id", userRecord.id)
          .select("*")
          .single();

        if (updateUserError) {
          return jsonResponse({ message: updateUserError.message }, 500);
        }

        userRecord = updatedUser;
      }
    }

    const { data: lastActiveUser, error: sessionUpdateError } = await supabase
      .from("users")
      .update({
        last_active: new Date().toISOString(),
        is_online: true,
        device_id: deviceId,
        device_brand: deviceBrand,
        signup_country: String(userRecord.signup_country ?? "").trim() || signupCountry,
        app_brand: appBrand,
        session_version: Number(userRecord.session_version ?? 0) + 1,
      })
      .eq("id", userRecord.id)
      .select("*")
      .single();

    if (sessionUpdateError || !lastActiveUser) {
      return jsonResponse({ message: "Unable to start a secure session." }, 500);
    }
    userRecord = lastActiveUser;

    const userSession = buildUserSession(userRecord);
    const tokens = await issueAuthTokens(userSession);

    return jsonResponse({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      isNewUser,
      user: userSession,
      profile: buildProfilePayload(userRecord),
    });
  } catch (error) {
    console.error("verify-otp error", error);
    return jsonResponse({ message: "Server error in verify-otp." }, 500);
  }
});
