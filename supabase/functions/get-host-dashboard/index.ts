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

    const { data: callRows, error: callRowsError } = await supabase
      .from("call_logs")
      .select("caller_id, call_type, duration_seconds, cost, created_at, call_status")
      .eq("receiver_id", userId)
      .order("created_at", { ascending: false })
      .limit(20);

    if (callRowsError) {
      return jsonResponse({ message: callRowsError.message }, 500);
    }

    const { data: allCallAmounts, error: allCallAmountsError } = await supabase
      .from("call_logs")
      .select("cost, created_at, call_status")
      .eq("receiver_id", userId);

    if (allCallAmountsError) {
      return jsonResponse({ message: allCallAmountsError.message }, 500);
    }

    const { data: giftRows, error: giftRowsError } = await supabase
      .from("gifts")
      .select("amount, created_at")
      .eq("receiver_id", userId);

    if (giftRowsError) {
      return jsonResponse({ message: giftRowsError.message }, 500);
    }

    const callerIds = Array.from(
      new Set(
        (callRows ?? [])
          .map((row) => String(row.caller_id ?? "").trim())
          .filter((value) => value.length > 0)
      )
    );

    let callerMap = new Map<string, string>();
    if (callerIds.length > 0) {
      const { data: callerRows, error: callerRowsError } = await supabase
        .from("users")
        .select("id, username, phone")
        .in("id", callerIds);

      if (callerRowsError) {
        return jsonResponse({ message: callerRowsError.message }, 500);
      }

      callerMap = new Map(
        (callerRows ?? []).map((row) => [
          String(row.id ?? ""),
          String(row.username ?? "").trim() ||
            buildDisplayName(String(row.id ?? "")),
        ])
      );
    }

    const indiaDayStart = startOfIndiaDay();
    const totalCallEarnings = (allCallAmounts ?? []).reduce((total, row) => {
      const status = String(row.call_status ?? "").trim().toLowerCase();
      if (status && status !== "completed") return total;
      return total + asNumber(row.cost);
    }, 0);
    const todayCallEarnings = (allCallAmounts ?? []).reduce((total, row) => {
      const status = String(row.call_status ?? "").trim().toLowerCase();
      if (status && status !== "completed") return total;
      return new Date(String(row.created_at ?? "")).getTime() >=
        indiaDayStart.getTime()
        ? total + asNumber(row.cost)
        : total;
    }, 0);
    const totalGiftEarnings = (giftRows ?? []).reduce(
      (total, row) => total + asNumber(row.amount),
      0
    );
    const todayGiftEarnings = (giftRows ?? []).reduce((total, row) => {
      return new Date(String(row.created_at ?? "")).getTime() >=
        indiaDayStart.getTime()
        ? total + asNumber(row.amount)
        : total;
    }, 0);

    const logs = (callRows ?? []).map((row) => ({
      name:
        callerMap.get(String(row.caller_id ?? "")) ??
        buildDisplayName(String(row.caller_id ?? "")),
      isVideo:
        String(row.call_type ?? "").trim().toLowerCase() === "video",
      duration: formatDuration(Number(row.duration_seconds ?? 0)),
      amount: Math.round(asNumber(row.cost)),
      time: formatLogTime(String(row.created_at ?? "")),
    }));

    return jsonResponse({
      user: buildUserSession(currentUser),
      profile: buildProfilePayload(currentUser),
      wallet: {
        totalEarnings: Math.round(totalCallEarnings + totalGiftEarnings),
        todayEarnings: Math.round(todayCallEarnings + todayGiftEarnings),
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
