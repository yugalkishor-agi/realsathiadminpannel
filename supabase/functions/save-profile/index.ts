import {
  DEFAULT_AVATAR_ID,
  DEFAULT_ACCOUNT_MODE,
  DEFAULT_HOST_STATUS,
  DEFAULT_LANGUAGE,
  authenticateRequest,
  buildProfilePayload,
  buildUserSession,
  corsResponse,
  createAdminClient,
  jsonResponse,
  normalizeHostStories,
  normalizeInterests,
  sanitizeUsername,
} from "../_shared/auth.ts";

function normalizeAccountMode(rawValue: unknown) {
  return String(rawValue ?? "").trim().toLowerCase() === "community"
    ? "community"
    : DEFAULT_ACCOUNT_MODE;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }

  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();

    const nickname = sanitizeUsername(
      String(body?.nickname ?? "").trim() ||
      String(body?.username ?? "").trim()
    );
    const gender = String(body?.gender ?? "").trim();
    const preferredLanguage =
      String(body?.preferredLanguage ?? "").trim() || DEFAULT_LANGUAGE;
    const avatarId = Number(body?.avatarId ?? DEFAULT_AVATAR_ID) || DEFAULT_AVATAR_ID;
    const interests = normalizeInterests(body?.interests);
    const accountMode = normalizeAccountMode(body?.accountMode);
    const communityName = String(body?.communityName ?? "").trim();
    const communityCity = String(body?.communityCity ?? "").trim();
    const communityAbout = String(body?.communityAbout ?? "").trim();
    const communityExperience = String(body?.communityExperience ?? "").trim();

    if (nickname.length < 2) {
      return jsonResponse(
        { message: "Use at least 2 characters for your nickname." },
        422
      );
    }

    if (accountMode === "community") {
      if (
        communityName.length < 2 ||
        communityCity.length < 2 ||
        communityAbout.length < 20
      ) {
        return jsonResponse(
          { message: "Please complete your community details." },
          422
        );
      }
    }

    const supabase = createAdminClient();
    const { data: currentUser, error: currentUserError } = await supabase
      .from("users")
      .select("*")
      .eq("id", userId)
      .single();

    if (currentUserError || !currentUser) {
      return jsonResponse({ message: "User account not found." }, 404);
    }

    const updatePayload = {
      username: nickname,
      gender,
      language: preferredLanguage,
      avatar_id: avatarId,
      interests,
      account_mode: accountMode,
      role: accountMode === "community" ? "host" : "user",
      host_status:
        accountMode === "community" ? "approved" : DEFAULT_HOST_STATUS,
      community_name: accountMode === "community" ? communityName : "",
      community_city: accountMode === "community" ? communityCity : "",
      community_about: accountMode === "community" ? communityAbout : "",
      community_experience:
        accountMode === "community" ? communityExperience : "",
      host_profile_photo_url:
        accountMode === "community"
          ? String(currentUser.host_profile_photo_url ?? "").trim()
          : "",
      host_story_items:
        accountMode === "community"
          ? normalizeHostStories(
            currentUser.host_story_items,
            String(currentUser.id ?? userId),
            nickname
          )
          : [],
      host_audio_live:
        accountMode === "community"
          ? Boolean(currentUser.host_audio_live)
          : false,
      host_video_live:
        accountMode === "community"
          ? Boolean(currentUser.host_video_live)
          : false,
      last_active: new Date().toISOString(),
      is_online: Boolean(currentUser.is_online),
      account_status: String(currentUser.account_status ?? "").trim() || "active",
    };

    const { data: updatedUser, error: updateError } = await supabase
      .from("users")
      .update(updatePayload)
      .eq("id", userId)
      .select("*")
      .single();

    if (updateError) {
      return jsonResponse({ message: updateError.message }, 500);
    }

    return jsonResponse({
      user: buildUserSession(updatedUser),
      profile: buildProfilePayload(updatedUser),
    });
  } catch (error) {
    console.error("save-profile error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in save-profile." }, 500);
  }
});
