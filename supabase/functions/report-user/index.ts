import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

const reasons = new Set(["harassment", "fake_profile", "inappropriate_content", "other"]);
const contexts = new Set(["call", "chat", "profile", "random_match", "discovery"]);

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const reportedUserId = String(body?.reportedUserId ?? "").trim();
    if (!reportedUserId || reportedUserId === userId) return jsonResponse({ message: "Choose another user to report." }, 422);
    const db = createAdminClient();
    const reason = reasons.has(body?.reason) ? body.reason : "other";
    const context = contexts.has(body?.context) ? body.context : "profile";
    const { data, error } = await db.from("user_reports").insert({
      reporter_id: userId, reported_id: reportedUserId, reason, context,
      note: String(body?.note ?? "").trim().slice(0, 500), status: "pending",
    }).select("id").single();
    if (error) throw error;
    let blocked = false;
    if (body?.block !== false) {
      const blockResult = await db.from("user_blocks").upsert({
        blocker_id: userId, blocked_id: reportedUserId,
      }, { onConflict: "blocker_id,blocked_id" });
      if (blockResult.error) throw blockResult.error;
      blocked = true;
    }
    return jsonResponse({ submitted: true, blocked, reportId: data.id, message: blocked ? "Report submitted. User blocked for future matching." : "Report submitted." });
  } catch (error) {
    console.error("report-user error", error);
    return requestErrorResponse(error, "Unable to submit report right now.");
  }
});
