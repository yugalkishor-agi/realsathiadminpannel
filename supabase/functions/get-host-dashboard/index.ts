import {
  authenticateRequest,
  buildDisplayName,
  buildProfilePayload,
  buildUserSession,
  corsResponse,
  createAdminClient,
  jsonResponse,
  normalizeHostStories,
} from "../_shared/auth.ts";

const IST_OFFSET_MS = 330 * 60 * 1000;
const DAY_MS = 24 * 60 * 60 * 1000;

function startOfIndiaDay(now = new Date()) {
  const localTime = now.getTime() + IST_OFFSET_MS;
  return new Date(Math.floor(localTime / DAY_MS) * DAY_MS - IST_OFFSET_MS);
}

function asNumber(value: unknown) {
  const amount = Number(value ?? 0);
  return Number.isFinite(amount) ? amount : 0;
}

function formatDuration(durationSeconds: number) {
  if (durationSeconds <= 0) return "0 min";
  if (durationSeconds < 60) return `${durationSeconds} sec`;
  const minutes = Math.max(1, Math.round(durationSeconds / 60));
  return `${minutes} min`;
}

function formatLogTime(timestamp: string) {
  const date = new Date(timestamp);
  if (Number.isNaN(date.getTime())) {
    return "";
  }

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
    timeZone: "Asia/Calcutta",
  }).format(date);
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
        { message: "Host dashboard is only available for host accounts." },
        403
      );
    }

    const hostDisplayName =
      String(currentUser.username ?? "").trim() ||
      buildDisplayName(String(currentUser.id ?? userId));
    const sanitizedStories = normalizeHostStories(
      currentUser.host_story_items,
      String(currentUser.id ?? userId),
      hostDisplayName
    );

    if (
      JSON.stringify(sanitizedStories) !==
      JSON.stringify(currentUser.host_story_items ?? [])
    ) {
      const { data: refreshedUser, error: storyUpdateError } = await supabase
        .from("users")
        .update({
          host_story_items: sanitizedStories,
          last_active: new Date().toISOString(),
        })
        .eq("id", userId)
        .select("*")
        .single();

      if (storyUpdateError) {
        return jsonResponse({ message: storyUpdateError.message }, 500);
      }

      Object.assign(currentUser, refreshedUser);
    }

    const { data: ledgerRows, error: ledgerError } = await supabase
      .from("wallet_ledger")
      .select("kind, title, rupees_delta, metadata, created_at, status")
      .eq("user_id", userId)
      .eq("status", "completed")
      .order("created_at", { ascending: false })
      .limit(100);

    if (ledgerError) {
      return jsonResponse({ message: ledgerError.message }, 500);
    }

    const indiaDayStart = startOfIndiaDay();
    const earningRows = (ledgerRows ?? []).filter((row) => asNumber(row.rupees_delta) > 0);
    const totalCallEarnings = earningRows.reduce((total, row) => total + asNumber(row.rupees_delta), 0);
    const todayCallEarnings = earningRows.reduce((total, row) => {
      return new Date(String(row.created_at ?? "")).getTime() >=
        indiaDayStart.getTime()
        ? total + asNumber(row.rupees_delta)
        : total;
    }, 0);

    const logs = earningRows.slice(0, 20).map((row) => ({
      name: String(row.metadata?.counterpartyName ?? row.title ?? "Caller"),
      isVideo: String(row.kind ?? "").trim().toLowerCase() === "video_call",
      duration: formatDuration(Number(row.metadata?.durationSeconds ?? 0)),
      amount: Math.round(asNumber(row.rupees_delta)),
      time: formatLogTime(String(row.created_at ?? "")),
    }));

    return jsonResponse({
      user: buildUserSession(currentUser),
      profile: buildProfilePayload(currentUser),
      wallet: {
        totalEarnings: Math.round(totalCallEarnings),
        todayEarnings: Math.round(todayCallEarnings),
      },
      logs,
    });
  } catch (error) {
    console.error("get-host-dashboard error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in get-host-dashboard." }, 500);
  }
});
