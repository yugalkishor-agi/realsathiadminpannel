import {
  DEFAULT_AUDIO_RATE,
  DEFAULT_VIDEO_RATE,
  authenticateRequest,
  buildDisplayName,
  buildProfilePayload,
  buildUserSession,
  corsResponse,
  createAdminClient,
  jsonResponse,
  normalizeHostStories,
} from "../_shared/auth.ts";

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

    const supabase = createAdminClient();
    const { data: currentUser, error: currentUserError } = await supabase
      .from("users")
      .select("*")
      .eq("id", userId)
      .single();

    if (currentUserError || !currentUser) {
      return jsonResponse({ message: "Host account not found." }, 404);
    }

    if (String(currentUser.role ?? "").trim().toLowerCase() !== "host") {
      return jsonResponse(
        { message: "Host settings are only available for host accounts." },
        403
      );
    }

    const hostAudioLive = Boolean(body?.hostAudioLive);
    const hostVideoLive = Boolean(body?.hostVideoLive);
    const hostAudioRate =
      Number(body?.hostAudioRate ?? currentUser.host_audio_rate ?? DEFAULT_AUDIO_RATE) ||
      DEFAULT_AUDIO_RATE;
    const hostVideoRate =
      Number(body?.hostVideoRate ?? currentUser.host_video_rate ?? DEFAULT_VIDEO_RATE) ||
      DEFAULT_VIDEO_RATE;
    const hostProfilePhotoUrl = String(
      body?.hostProfilePhotoUrl ?? currentUser.host_profile_photo_url ?? ""
    ).trim();
    const hostDisplayName =
      String(currentUser.username ?? "").trim() ||
      buildDisplayName(String(currentUser.id ?? userId));
    const hostStories = normalizeHostStories(
      body?.hostStories ?? currentUser.host_story_items,
      String(currentUser.id ?? userId),
      hostDisplayName
    );

    if (hostAudioRate < 10 || hostAudioRate > 999) {
      return jsonResponse(
        { message: "Audio rate should stay between 10 and 999." },
        422
      );
    }

    if (hostVideoRate < 10 || hostVideoRate > 999) {
      return jsonResponse(
        { message: "Video rate should stay between 10 and 999." },
        422
      );
    }

    const { data: updatedUser, error: updateError } = await supabase
      .from("users")
      .update({
        host_audio_live: hostAudioLive,
        host_video_live: hostVideoLive,
        host_audio_rate: hostAudioRate,
        host_video_rate: hostVideoRate,
        host_profile_photo_url: hostProfilePhotoUrl,
        host_story_items: hostStories,
        is_online: hostAudioLive || hostVideoLive,
        last_active: new Date().toISOString(),
      })
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
    console.error("save-host-settings error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in save-host-settings." }, 500);
  }
});
