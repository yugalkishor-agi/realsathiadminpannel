import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

async function removeHostMedia(db: ReturnType<typeof createAdminClient>, userId: string) {
  for (const purpose of ["profile_photo", "story"]) {
    const { data, error } = await db.storage.from("host-media")
      .list(`${userId}/${purpose}`, { limit: 100 });
    if (error && !error.message.toLowerCase().includes("not found")) throw error;
    const paths = (data ?? []).filter((item) => item.name && item.id)
      .map((item) => `${userId}/${purpose}/${item.name}`);
    if (paths.length) {
      const result = await db.storage.from("host-media").remove(paths);
      if (result.error) throw result.error;
    }
  }
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const { data: user, error: lookupError } = await db.from("users")
      .select("phone, role").eq("id", userId).single();
    if (lookupError || !user) return jsonResponse({ message: "Account not found." }, 404);
    if (user.role === "host") await removeHostMedia(db, userId);
    const phone = String(user.phone ?? "");
    if (phone) {
      const otpResult = await db.from("otp_codes").delete().eq("phone", phone);
      if (otpResult.error) throw otpResult.error;
    }
    const { error } = await db.from("users").delete().eq("id", userId);
    if (error) throw error;
    return jsonResponse({ deleted: true });
  } catch (error) {
    console.error("delete-account error", error);
    return requestErrorResponse(error, "Unable to delete account right now.");
  }
});
