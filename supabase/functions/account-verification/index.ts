import { authenticateRequest, corsResponse, createAdminClient, jsonResponse } from "../_shared/auth.ts";

const BUCKET = "account-verification-audio";
const MAX_BYTES = 10 * 1024 * 1024;
const PROMPTS = ["क्या हाल है बताएं", "जूस पिला दो मुसम्मी का", "अजी ई गाली दे रहा है", "मुझे घर जाना ह", "जल्दी कर, कल सुबह पनवेल जाना है"];

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const contentType = req.headers.get("content-type") ?? "";
    if (contentType.includes("application/json")) {
      const body = await req.json();
      if (body.action !== "status") return jsonResponse({ message: "Invalid action." }, 422);
      const { data, error } = await db.from("account_voice_verifications")
        .select("status,review_message,metadata").eq("user_id", userId).maybeSingle();
      if (error) throw error;
      return jsonResponse({ status: data?.status ?? "not_submitted", reviewMessage: data?.review_message ?? "", metadata: data?.metadata ?? {} });
    }
    const form = await req.formData();
    const audio = form.get("audio");
    const prompt = String(form.get("prompt") ?? "").trim();
    if (!PROMPTS.includes(prompt)) return jsonResponse({ message: "Voice prompt is invalid." }, 422);
    const metadata = JSON.parse(String(form.get("metadata") ?? "{}")) as Record<string, unknown>;
    if (!(audio instanceof File) || !prompt || audio.size < 1 || audio.size > MAX_BYTES) {
      return jsonResponse({ message: "A valid voice recording under 10 MB is required." }, 422);
    }
    if (!["audio/mp4", "audio/aac", "audio/3gpp", "audio/amr"].includes(audio.type)) {
      return jsonResponse({ message: "Unsupported voice recording format." }, 415);
    }
    const { data: user, error: userError } = await db.from("users").select("username,phone,role")
      .eq("id", userId).single();
    if (userError || !user) return jsonResponse({ message: "Account not found." }, 404);
    if (String(user.role ?? "user") === "host") return jsonResponse({ message: "This account is already verified." }, 409);
    const { data: prior, error: priorError } = await db.from("account_voice_verifications")
      .select("status,audio_path").eq("user_id", userId).maybeSingle();
    if (priorError) throw priorError;
    if (prior?.status === "approved") return jsonResponse({ message: "Verification is already approved." }, 409);
    const path = `${userId}/${Date.now()}.m4a`;
    const { error: uploadError } = await db.storage.from(BUCKET).upload(path, audio, {
      contentType: audio.type, upsert: false,
    });
    if (uploadError) throw uploadError;
    const { error: saveError } = await db.from("account_voice_verifications").upsert({
      user_id: userId, account_name: String(metadata.accountName ?? user.username ?? "").slice(0, 80),
      phone_number: String(user.phone ?? ""), prompt, audio_path: path,
      status: "pending", review_message: "", metadata,
      created_at: new Date().toISOString(), reviewed_at: null, reviewed_by: null,
    });
    if (saveError) {
      await db.storage.from(BUCKET).remove([path]);
      throw saveError;
    }
    if (prior?.audio_path) await db.storage.from(BUCKET).remove([prior.audio_path]);
    return jsonResponse({ status: "pending" });
  } catch (error) {
    console.error("account-verification error", error);
    if (error instanceof Error && error.message.toLowerCase().includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }
    return jsonResponse({ message: error instanceof Error ? error.message : "Verification request failed." }, 500);
  }
});
