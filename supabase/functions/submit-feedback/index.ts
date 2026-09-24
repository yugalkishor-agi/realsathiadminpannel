import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const body = await req.json();
    const text = String(body?.body ?? "").trim().slice(0, 400);
    if (text.length < 12) return jsonResponse({ message: "Feedback must be at least 12 characters." }, 422);
    const db = createAdminClient();
    const { data: user, error: userError } = await db.from("users").select("role").eq("id", userId).single();
    if (userError || !user) return jsonResponse({ message: "Account not found." }, 404);
    const { data, error } = await db.from("feedback_messages").insert({
      user_id: userId,
      role: String(user.role) === "host" ? "host" : "user",
      category: String(body?.category ?? "other").trim().slice(0, 60) || "other",
      body: text,
    }).select("id,status,created_at").single();
    if (error) throw error;
    return jsonResponse({ submitted: true, feedback: data });
  } catch (error) {
    console.error("submit-feedback error", error);
    return requestErrorResponse(error, "Unable to save feedback right now.");
  }
});
