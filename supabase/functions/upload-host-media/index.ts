import {
  authenticateRequest,
  corsResponse,
  createAdminClient,
  jsonResponse,
} from "../_shared/auth.ts";

const BUCKET_NAME = "host-media";
const MAX_IMAGE_BYTES = 8 * 1024 * 1024;
const MAX_VIDEO_BYTES = 30 * 1024 * 1024;
const ALLOWED_IMAGE_TYPES = [
  "image/jpeg",
  "image/png",
  "image/webp",
  "image/heic",
  "image/heif",
];
const ALLOWED_VIDEO_TYPES = [
  "video/mp4",
  "video/quicktime",
  "video/3gpp",
  "video/webm",
];

function sanitizeFileName(fileName: string) {
  const cleaned = fileName
    .replace(/[^a-zA-Z0-9._-]+/g, "_")
    .replace(/_+/g, "_")
    .replace(/^_+|_+$/g, "");

  return cleaned || `media_${Date.now()}`;
}

function inferExtension(fileName: string, mimeType: string) {
  const match = fileName.match(/\.[a-zA-Z0-9]+$/);
  if (match) return match[0].toLowerCase();

  switch (mimeType) {
    case "image/png":
      return ".png";
    case "image/webp":
      return ".webp";
    case "image/heic":
      return ".heic";
    case "image/heif":
      return ".heif";
    case "video/quicktime":
      return ".mov";
    case "video/webm":
      return ".webm";
    case "video/3gpp":
      return ".3gp";
    case "video/mp4":
      return ".mp4";
    default:
      return ".jpg";
  }
}

async function ensureBucket(supabase: ReturnType<typeof createAdminClient>) {
  const { data: existingBucket, error: getBucketError } = await supabase.storage
    .getBucket(BUCKET_NAME);

  if (!getBucketError && existingBucket) {
    return;
  }

  const { error: createBucketError } = await supabase.storage.createBucket(
    BUCKET_NAME,
    {
      public: true,
      fileSizeLimit: MAX_VIDEO_BYTES,
      allowedMimeTypes: [...ALLOWED_IMAGE_TYPES, ...ALLOWED_VIDEO_TYPES],
    }
  );

  if (createBucketError && !createBucketError.message.toLowerCase().includes("already exists")) {
    throw createBucketError;
  }
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
      .select("id, role")
      .eq("id", userId)
      .single();

    if (currentUserError || !currentUser) {
      return jsonResponse({ message: "Host account not found." }, 404);
    }

    if (String(currentUser.role ?? "").trim().toLowerCase() !== "host") {
      return jsonResponse(
        { message: "Host media upload is only available for host accounts." },
        403
      );
    }

    const formData = await req.formData();
    const purpose = String(formData.get("purpose") ?? "").trim().toLowerCase();
    const file = formData.get("file");

    if (!(file instanceof File)) {
      return jsonResponse({ message: "Media file is required." }, 422);
    }

    if (purpose !== "profile_photo" && purpose !== "story") {
      return jsonResponse({ message: "Upload purpose is invalid." }, 422);
    }

    const mimeType = String(file.type ?? "").trim().toLowerCase();
    const isImage = ALLOWED_IMAGE_TYPES.includes(mimeType);
    const isVideo = ALLOWED_VIDEO_TYPES.includes(mimeType);

    if (purpose === "profile_photo" && !isImage) {
      return jsonResponse(
        { message: "Profile photo should be an image file." },
        422
      );
    }

    if (purpose === "story" && !isImage && !isVideo) {
      return jsonResponse(
        { message: "Story upload supports image or video files only." },
        422
      );
    }

    const fileSizeLimit = isVideo ? MAX_VIDEO_BYTES : MAX_IMAGE_BYTES;
    if (file.size <= 0 || file.size > fileSizeLimit) {
      return jsonResponse(
        {
          message: isVideo
            ? "Video size should stay under 30 MB."
            : "Image size should stay under 8 MB.",
        },
        413
      );
    }

    await ensureBucket(supabase);

    const safeFileName = sanitizeFileName(file.name);
    const fileExtension = inferExtension(safeFileName, mimeType);
    const storagePath =
      `${userId}/${purpose}/${Date.now()}_${crypto.randomUUID()}${fileExtension}`;

    const { error: uploadError } = await supabase.storage
      .from(BUCKET_NAME)
      .upload(storagePath, file, {
        contentType: mimeType,
        cacheControl: "3600",
        upsert: false,
      });

    if (uploadError) {
      return jsonResponse({ message: uploadError.message }, 500);
    }

    const { data: publicUrlData } = supabase.storage
      .from(BUCKET_NAME)
      .getPublicUrl(storagePath);

    return jsonResponse({
      bucket: BUCKET_NAME,
      path: storagePath,
      publicUrl: publicUrlData.publicUrl,
      mediaType: isVideo ? "VIDEO" : "IMAGE",
    });
  } catch (error) {
    console.error("upload-host-media error", error);

    if (error instanceof Error && error.message.includes("token")) {
      return jsonResponse({ message: error.message }, 401);
    }

    return jsonResponse({ message: "Server error in upload-host-media." }, 500);
  }
});
