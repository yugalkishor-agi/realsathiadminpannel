import { createClient } from "https://esm.sh/@supabase/supabase-js@2.45.4";

const encoder = new TextEncoder();

function requiredJwtSecret(name: "JWT_ACCESS_SECRET" | "JWT_REFRESH_SECRET") {
  const secret = Deno.env.get(name);
  if (!secret) throw new Error(`${name} is missing.`);
  return secret;
}

export const DEFAULT_LANGUAGE = "All";
export const DEFAULT_ACCOUNT_MODE = "customer";
export const DEFAULT_HOST_STATUS = "not_applicable";
export const DEFAULT_AUDIO_RATE = 35;
export const DEFAULT_VIDEO_RATE = 65;
export const DEFAULT_AVATAR_ID = 9;
export const HOST_STORY_LIFETIME_MS = 24 * 60 * 60 * 1000;
export const MAX_HOST_STORIES = 12;

export function createAdminClient() {
  const supabaseUrl = Deno.env.get("SUPABASE_URL");
  const serviceRoleKey = Deno.env.get("SUPABASE_SERVICE_ROLE_KEY");

  if (!supabaseUrl || !serviceRoleKey) {
    throw new Error("SUPABASE_URL or SUPABASE_SERVICE_ROLE_KEY is missing.");
  }

  return createClient(supabaseUrl, serviceRoleKey);
}

export function jsonResponse(data: unknown, status = 200) {
  return new Response(JSON.stringify(data), {
    status,
    headers: {
      "Content-Type": "application/json",
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "authorization, x-session-token, x-client-info, apikey, content-type",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    },
  });
}

export function corsResponse() {
  return new Response("ok", {
    headers: {
      "Access-Control-Allow-Origin": "*",
      "Access-Control-Allow-Headers": "authorization, x-session-token, x-client-info, apikey, content-type",
      "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    },
  });
}

export function requestErrorResponse(error: unknown, fallback: string) {
  const message = error instanceof Error ? error.message : "";
  if (message.toLowerCase().includes("token") || message.includes("Missing session")) {
    return jsonResponse({ message }, 401);
  }
  return jsonResponse({ message: fallback }, 500);
}

export function normalizeIndianPhone(rawValue: string) {
  const input = String(rawValue ?? "").trim();
  const digits = input.replace(/\D/g, "");

  if (input.startsWith("+") && /^\+[1-9]\d{9,14}$/.test(input)) {
    return input;
  }

  if (digits.length === 10) return `+91${digits}`;
  if (digits.length === 11 && digits.startsWith("0")) return `+91${digits.slice(1)}`;
  if (digits.length === 12 && digits.startsWith("91")) return `+${digits}`;

  return "";
}

export function digitsOnly(value: string) {
  return String(value ?? "").replace(/\D/g, "");
}

export function formatPhoneForSession(phoneNumber: string) {
  const digits = digitsOnly(phoneNumber);
  return digits.startsWith("91") ? `+${digits}` : `+91${digits}`;
}

export function generateOtpCode(length = 6) {
  let otpCode = "";
  const values = new Uint32Array(length);
  crypto.getRandomValues(values);

  for (const value of values) {
    otpCode += (value % 10).toString();
  }

  return otpCode;
}

export async function sha256Hex(value: string) {
  const digest = await crypto.subtle.digest("SHA-256", encoder.encode(value));
  return Array.from(new Uint8Array(digest))
    .map((byte) => byte.toString(16).padStart(2, "0"))
    .join("");
}

export function sanitizeUsername(input: string) {
  const cleaned = String(input ?? "")
    .toLowerCase()
    .replace(/\s+/g, "_")
    .replace(/[^a-z0-9_.]/g, "")
    .replace(/^[_\.]+|[_\.]+$/g, "")
    .slice(0, 15);

  return cleaned;
}

function randomAlphaChunk(length = 6) {
  const alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
  const values = new Uint32Array(length);
  crypto.getRandomValues(values);

  return Array.from(values, (value) => alphabet[value % alphabet.length]).join("");
}

export function buildDisplayName(seed = "") {
  const normalizedSeed = String(seed ?? "")
    .toLowerCase()
    .replace(/[^a-z0-9]/g, "");

  const suffix = normalizedSeed.length >= 6 && /[a-z]/.test(normalizedSeed)
    ? normalizedSeed.slice(0, 6)
    : randomAlphaChunk(6);

  return `realsaathi_${suffix}`;
}

export function normalizeInterests(rawValue: unknown) {
  if (!Array.isArray(rawValue)) return [];

  return rawValue
    .map((item) => String(item ?? "").trim())
    .filter((item) => item.length > 0)
    .slice(0, 8);
}

function normalizeHostStoryMediaType(rawValue: unknown) {
  return String(rawValue ?? "").trim().toUpperCase() === "VIDEO"
    ? "VIDEO"
    : "IMAGE";
}

function sanitizeStoryText(rawValue: unknown, fallbackValue: string, maxLength: number) {
  const cleaned = String(rawValue ?? "").trim().slice(0, maxLength);
  return cleaned || fallbackValue;
}

export function normalizeHostStories(
  rawValue: unknown,
  ownerId = "",
  ownerName = ""
) {
  if (!Array.isArray(rawValue)) return [];

  const now = Date.now();

  return rawValue
    .map((item) => {
      const story = item && typeof item === "object"
        ? (item as Record<string, unknown>)
        : {};
      const createdAtMillis = Number(story.createdAtMillis ?? Date.now());
      const normalizedCreatedAtMillis =
        Number.isFinite(createdAtMillis) && createdAtMillis > 0
          ? Math.round(createdAtMillis)
          : Date.now();
      const mediaUrl = String(story.mediaUrl ?? story.mediaUri ?? "").trim();

      if (!mediaUrl) {
        return null;
      }

      return {
        id: String(story.id ?? "").trim() || crypto.randomUUID(),
        ownerId: String(story.ownerId ?? "").trim() || ownerId,
        ownerName: String(story.ownerName ?? "").trim() || ownerName,
        title: sanitizeStoryText(
          story.title,
          "Fresh story",
          60
        ),
        caption: sanitizeStoryText(
          story.caption,
          "",
          180
        ),
        mediaUrl,
        mediaType: normalizeHostStoryMediaType(story.mediaType),
        createdAtMillis: normalizedCreatedAtMillis,
      };
    })
    .filter((story): story is NonNullable<typeof story> => {
      return (
        !!story &&
        story.createdAtMillis + HOST_STORY_LIFETIME_MS > now
      );
    })
    .sort((left, right) => right.createdAtMillis - left.createdAtMillis)
    .slice(0, MAX_HOST_STORIES);
}

export function buildUserSession(userRow: Record<string, unknown>) {
  const phone = digitsOnly(String(userRow.phone ?? ""));
  const userId = String(userRow.id ?? "");
  const nickname =
    String(userRow.nickname ?? "").trim() ||
    String(userRow.username ?? "").trim() ||
    buildDisplayName(userId);
  return {
    id: userId,
    phoneNumber: formatPhoneForSession(phone),
    displayName: nickname,
    isHost: String(userRow.role ?? "").trim().toLowerCase() === "host",
    deviceId: String(userRow.device_id ?? "").trim() || null,
    deviceBrand: String(userRow.device_brand ?? "").trim() || null,
    countryCode: String(userRow.signup_country ?? "").trim() || null,
    appBrand: String(userRow.app_brand ?? "RealSaathi").trim() || "RealSaathi",
    sessionVersion: Number(userRow.session_version ?? 0) || 0,
  };
}

export function buildProfilePayload(userRow: Record<string, unknown>) {
  const nickname =
    String(userRow.nickname ?? "").trim() ||
    String(userRow.username ?? "").trim() ||
    buildDisplayName(String(userRow.id ?? ""));
  const userId = String(userRow.id ?? "").trim();
  const hostStories = normalizeHostStories(
    userRow.host_story_items,
    userId,
    nickname
  );

  return {
    nickname,
    username: nickname,
    deviceId: String(userRow.device_id ?? "").trim() || null,
    deviceBrand: String(userRow.device_brand ?? "").trim() || null,
    countryCode: String(userRow.signup_country ?? "").trim() || null,
    appBrand: String(userRow.app_brand ?? "RealSaathi").trim() || "RealSaathi",
    gender: String(userRow.gender ?? "").trim(),
    preferredLanguage:
      String(userRow.language ?? "").trim() || DEFAULT_LANGUAGE,
    avatarId: Number(userRow.avatar_id ?? DEFAULT_AVATAR_ID) || DEFAULT_AVATAR_ID,
    interests: normalizeInterests(userRow.interests),
    accountMode:
      String(userRow.account_mode ?? "").trim() || DEFAULT_ACCOUNT_MODE,
    hostStatus:
      String(userRow.host_status ?? "").trim() || DEFAULT_HOST_STATUS,
    communityName: String(userRow.community_name ?? "").trim(),
    communityCity: String(userRow.community_city ?? "").trim(),
    communityAbout: String(userRow.community_about ?? "").trim(),
    communityExperience: String(userRow.community_experience ?? "").trim(),
    hostAudioLive: Boolean(userRow.host_audio_live),
    hostVideoLive: Boolean(userRow.host_video_live),
    hostAudioRate:
      Number(userRow.host_audio_rate ?? DEFAULT_AUDIO_RATE) || DEFAULT_AUDIO_RATE,
    hostVideoRate:
      Number(userRow.host_video_rate ?? DEFAULT_VIDEO_RATE) || DEFAULT_VIDEO_RATE,
    hostProfilePhotoUrl: String(userRow.host_profile_photo_url ?? "").trim(),
    hostStories,
  };
}

function base64UrlEncodeString(value: string) {
  const bytes = encoder.encode(value);
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function base64UrlEncodeBytes(bytes: Uint8Array) {
  let binary = "";
  for (const byte of bytes) {
    binary += String.fromCharCode(byte);
  }
  return btoa(binary).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
}

function base64UrlDecodeString(value: string) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized + "=".repeat((4 - (normalized.length % 4 || 4)) % 4);
  return atob(padded);
}

function durationToSeconds(rawValue: string, fallbackSeconds: number) {
  const value = String(rawValue ?? "").trim();
  const match = value.match(/^(\d+)([smhd])?$/i);
  if (!match) return fallbackSeconds;

  const amount = Number(match[1]);
  const unit = (match[2] || "s").toLowerCase();

  switch (unit) {
    case "m":
      return amount * 60;
    case "h":
      return amount * 60 * 60;
    case "d":
      return amount * 60 * 60 * 24;
    default:
      return amount;
  }
}

async function signJwt(payload: Record<string, unknown>, secret: string) {
  const header = {
    alg: "HS256",
    typ: "JWT",
  };

  const encodedHeader = base64UrlEncodeString(JSON.stringify(header));
  const encodedPayload = base64UrlEncodeString(JSON.stringify(payload));
  const signingInput = `${encodedHeader}.${encodedPayload}`;

  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    encoder.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const signature = new Uint8Array(
    await crypto.subtle.sign("HMAC", cryptoKey, encoder.encode(signingInput))
  );

  return `${signingInput}.${base64UrlEncodeBytes(signature)}`;
}

async function verifyJwtSignature(signingInput: string, signature: string, secret: string) {
  const cryptoKey = await crypto.subtle.importKey(
    "raw",
    encoder.encode(secret),
    { name: "HMAC", hash: "SHA-256" },
    false,
    ["sign"]
  );

  const expectedSignature = new Uint8Array(
    await crypto.subtle.sign("HMAC", cryptoKey, encoder.encode(signingInput))
  );

  return base64UrlEncodeBytes(expectedSignature) === signature;
}

async function decodeVerifiedJwt(
  token: string,
  secret: string,
  tokenLabel: string
) {
  const [encodedHeader, encodedPayload, signature] = token.split(".");

  if (!encodedHeader || !encodedPayload || !signature) {
    throw new Error(`Malformed ${tokenLabel}.`);
  }

  const signingInput = `${encodedHeader}.${encodedPayload}`;
  const isValidSignature = await verifyJwtSignature(
    signingInput,
    signature,
    secret
  );

  if (!isValidSignature) {
    throw new Error(`Invalid ${tokenLabel} signature.`);
  }

  const payloadText = base64UrlDecodeString(encodedPayload);
  return JSON.parse(payloadText) as Record<string, unknown>;
}

export async function authenticateRequest(req: Request) {
  const sessionHeader =
    req.headers.get("X-Session-Token") ||
    req.headers.get("x-session-token") ||
    req.headers.get("Authorization") ||
    req.headers.get("authorization");

  const token = String(sessionHeader ?? "").trim().replace(/^Bearer\s+/i, "");

  if (!token) {
    throw new Error("Missing session token.");
  }

  const accessSecret = requiredJwtSecret("JWT_ACCESS_SECRET");
  const payload = await decodeVerifiedJwt(token, accessSecret, "access token");
  const expiresAt = Number(payload?.exp ?? 0);
  const userId = String(payload?.sub ?? "").trim();

  if (!userId) {
    throw new Error("Access token subject is missing.");
  }

  if (expiresAt > 0 && expiresAt <= Math.floor(Date.now() / 1000)) {
    throw new Error("Access token expired.");
  }

  const supabase = createAdminClient();
  const { data: currentUser, error } = await supabase
    .from("users")
    .select("session_version, account_status")
    .eq("id", userId)
    .maybeSingle();
  if (error || !currentUser) throw new Error("Unable to validate session.");
  const accountStatus = String(currentUser.account_status ?? "active").trim().toLowerCase();
  if (["blocked", "banned", "suspended"].includes(accountStatus)) {
    throw new Error("This account is not allowed to access the app.");
  }
  if (Number(payload?.sessionVersion ?? 0) !== Number(currentUser.session_version ?? 0)) {
    throw new Error("Access token session replaced by a login on another device.");
  }

  return {
    userId,
    phoneNumber: String(payload?.phoneNumber ?? "").trim(),
    isHost: Boolean(payload?.isHost),
    raw: payload,
  };
}

export async function authenticateRefreshToken(refreshToken: string) {
  const token = String(refreshToken ?? "").trim().replace(/^Bearer\s+/i, "");
  if (!token) {
    throw new Error("Missing refresh token.");
  }

  const refreshSecret = requiredJwtSecret("JWT_REFRESH_SECRET");
  const payload = await decodeVerifiedJwt(token, refreshSecret, "refresh token");
  const expiresAt = Number(payload?.exp ?? 0);
  const userId = String(payload?.sub ?? "").trim();
  const tokenType = String(payload?.type ?? "").trim().toLowerCase();

  if (!userId) {
    throw new Error("Refresh token subject is missing.");
  }

  if (tokenType != "refresh") {
    throw new Error("Refresh token type is invalid.");
  }

  if (expiresAt > 0 && expiresAt <= Math.floor(Date.now() / 1000)) {
    throw new Error("Refresh token expired.");
  }

  return {
    userId,
    raw: payload,
  };
}

export async function issueAuthTokens(userSession: {
  id: string;
  phoneNumber: string;
  isHost: boolean;
  sessionVersion?: number;
}) {
  const accessSecret = requiredJwtSecret("JWT_ACCESS_SECRET");
  const refreshSecret = requiredJwtSecret("JWT_REFRESH_SECRET");
  const accessExpiresIn = durationToSeconds(
    Deno.env.get("JWT_ACCESS_EXPIRES_IN") ?? "15m",
    900
  );
  const refreshExpiresIn = durationToSeconds(
    Deno.env.get("JWT_REFRESH_EXPIRES_IN") ?? "30d",
    2_592_000
  );
  const now = Math.floor(Date.now() / 1000);

  const accessToken = await signJwt(
    {
      phoneNumber: userSession.phoneNumber,
      isHost: userSession.isHost,
      iss: "realsaathi-edge-function",
      sub: userSession.id,
      sessionVersion: Number(userSession.sessionVersion ?? 0),
      iat: now,
      exp: now + accessExpiresIn,
    },
    accessSecret
  );

  const refreshToken = await signJwt(
    {
      type: "refresh",
      iss: "realsaathi-edge-function",
      sub: userSession.id,
      sessionVersion: Number(userSession.sessionVersion ?? 0),
      iat: now,
      exp: now + refreshExpiresIn,
    },
    refreshSecret
  );

  return {
    accessToken,
    refreshToken,
  };
}
