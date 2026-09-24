import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const blockedUserId = String(body?.blockedUserId ?? "").trim();
    if (!blockedUserId || blockedUserId === userId) {
      return jsonResponse({ message: "Choose another user to block." }, 422);
    }
    const { error } = await createAdminClient().from("user_blocks").upsert(
      { blocker_id: userId, blocked_id: blockedUserId },
      { onConflict: "blocker_id,blocked_id" }
    );
    if (error) throw error;
    return jsonResponse({ blocked: true, message: "User blocked." });
  } catch (error) {
    console.error("block-user error", error);
    return requestErrorResponse(error, "Unable to block user right now.");
  }
});
