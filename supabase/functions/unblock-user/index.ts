import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const blockedUserId = String(body?.blockedUserId ?? "").trim();
    if (!blockedUserId) return jsonResponse({ message: "Blocked user is required." }, 422);
    const { error } = await createAdminClient().from("user_blocks").delete()
      .eq("blocker_id", userId).eq("blocked_id", blockedUserId);
    if (error) throw error;
    return jsonResponse({ unblocked: true, message: "User unblocked." });
  } catch (error) {
    console.error("unblock-user error", error);
    return requestErrorResponse(error, "Unable to unblock user right now.");
  }
});
