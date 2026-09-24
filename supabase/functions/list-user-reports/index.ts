import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const { data: reports, error } = await db.from("user_reports").select("id,reported_id,reason,context,note,status,admin_note,created_at,resolved_at").eq("reporter_id", userId).order("created_at", { ascending: false });
    if (error) throw error;
    const ids = (reports ?? []).map((row) => row.reported_id).filter(Boolean);
    const profiles = ids.length ? await db.from("users").select("id,public_id,username,avatar_id,role").in("id", ids) : { data: [], error: null };
    if (profiles.error) throw profiles.error;
    const byId = new Map((profiles.data ?? []).map((profile) => [profile.id, profile]));
    return jsonResponse({ reports: (reports ?? []).map((row) => ({ ...row, profile: row.reported_id ? byId.get(row.reported_id) ?? null : null })) });
  } catch (error) {
    console.error("list-user-reports error", error);
    return requestErrorResponse(error, "Unable to load reports right now.");
  }
});
