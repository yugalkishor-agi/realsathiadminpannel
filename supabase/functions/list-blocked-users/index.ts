import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const { data: blocks, error } = await db.from("user_blocks").select("id,blocked_id,created_at").eq("blocker_id", userId).order("created_at", { ascending: false });
    if (error) throw error;
    const ids = (blocks ?? []).map((row) => row.blocked_id).filter(Boolean);
    const profiles = ids.length ? await db.from("users").select("id,public_id,username,avatar_id,role").in("id", ids) : { data: [], error: null };
    if (profiles.error) throw profiles.error;
    const byId = new Map((profiles.data ?? []).map((profile) => [profile.id, profile]));
    return jsonResponse({ blockedUsers: (blocks ?? []).map((row) => ({ ...row, profile: byId.get(row.blocked_id) ?? null })) });
  } catch (error) {
    console.error("list-blocked-users error", error);
    return requestErrorResponse(error, "Unable to load blocked users right now.");
  }
});
