import {
  authenticateRefreshToken,
  buildProfilePayload,
  buildUserSession,
  corsResponse,
  createAdminClient,
  issueAuthTokens,
  jsonResponse,
} from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }

  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const body = await req.json();
    const refreshSession = await authenticateRefreshToken(body?.refreshToken);
    const { userId } = refreshSession;
    const supabase = createAdminClient();

    const { data: currentUser, error: currentUserError } = await supabase
      .from("users")
      .select("*")
      .eq("id", userId)
      .single();

    if (currentUserError || !currentUser) {
      return jsonResponse({ message: "User account not found." }, 404);
    }
    if (["blocked", "banned", "suspended"].includes(String(currentUser.account_status ?? "active").trim().toLowerCase())) {
      return jsonResponse({ message: "This account is not allowed to access the app." }, 403);
    }

    const userSession = buildUserSession(currentUser);
    if (Number(refreshSession.raw?.sessionVersion ?? 0) !== Number(userSession.sessionVersion ?? 0)) {
      return jsonResponse({ message: "Refresh token session replaced by a login on another device." }, 401);
    }
    const tokens = await issueAuthTokens(userSession);

    return jsonResponse({
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
      user: userSession,
      profile: buildProfilePayload(currentUser),
    });
  } catch (error) {
    console.error("refresh-session error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in refresh-session." }, 500);
  }
});
