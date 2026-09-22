import {
  authenticateRequest,
  corsResponse,
  jsonResponse,
  requestErrorResponse,
} from "../_shared/auth.ts";

const encoder = new TextEncoder();
const TOKEN_LIFETIME_SECONDS = 24 * 60 * 60;

function writeUint16(target: Uint8Array, offset: number, value: number) {
  new DataView(target.buffer, target.byteOffset, target.byteLength)
    .setUint16(offset, value, false);
}

function writeUint64(target: Uint8Array, offset: number, value: number) {
  new DataView(target.buffer, target.byteOffset, target.byteLength)
    .setBigUint64(offset, BigInt(value), false);
}

function toBase64(bytes: Uint8Array) {
  let binary = "";
  for (let index = 0; index < bytes.length; index += 1) {
    binary += String.fromCharCode(bytes[index]);
  }
  return btoa(binary);
}

function zegoUserId(accountId: string) {
  return accountId
    .trim()
    .replace(/[^A-Za-z0-9_]/g, "")
    .slice(0, 32);
}

async function generateToken04(
  appId: number,
  userId: string,
  serverSecret: string,
  lifetimeSeconds: number,
) {
  if (!Number.isInteger(appId) || appId <= 0) {
    throw new Error("ZEGO_APP_ID is invalid.");
  }
  if (!userId) {
    throw new Error("ZEGO user ID is invalid.");
  }
  if (encoder.encode(serverSecret).length !== 32) {
    throw new Error("ZEGO_SERVER_SECRET must contain exactly 32 bytes.");
  }

  const createdAt = Math.floor(Date.now() / 1000);
  const expiresAt = createdAt + lifetimeSeconds;
  const nonceBytes = new Uint32Array(1);
  crypto.getRandomValues(nonceBytes);
  const plaintext = encoder.encode(JSON.stringify({
    app_id: appId,
    user_id: userId,
    ctime: createdAt,
    expire: expiresAt,
    nonce: nonceBytes[0] & 0x7fffffff,
    payload: "",
  }));

  const iv = new Uint8Array(16);
  crypto.getRandomValues(iv);
  const key = await crypto.subtle.importKey(
    "raw",
    encoder.encode(serverSecret),
    { name: "AES-CBC" },
    false,
    ["encrypt"],
  );
  const encrypted = new Uint8Array(await crypto.subtle.encrypt(
    { name: "AES-CBC", iv },
    key,
    plaintext,
  ));

  const packed = new Uint8Array(8 + 2 + iv.length + 2 + encrypted.length);
  let offset = 0;
  writeUint64(packed, offset, expiresAt);
  offset += 8;
  writeUint16(packed, offset, iv.length);
  offset += 2;
  packed.set(iv, offset);
  offset += iv.length;
  writeUint16(packed, offset, encrypted.length);
  offset += 2;
  packed.set(encrypted, offset);

  return {
    token: `04${toBase64(packed)}`,
    expiresAt,
  };
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") {
    return corsResponse();
  }
  if (req.method !== "POST") {
    return jsonResponse({ message: "Method not allowed." }, 405);
  }

  try {
    const session = await authenticateRequest(req);
    const appId = Number(Deno.env.get("ZEGO_APP_ID"));
    const serverSecret = Deno.env.get("ZEGO_SERVER_SECRET") ?? "";
    const userId = zegoUserId(session.userId);
    const generated = await generateToken04(
      appId,
      userId,
      serverSecret,
      TOKEN_LIFETIME_SECONDS,
    );

    return jsonResponse({
      appId,
      token: generated.token,
      userId,
      expiresAt: generated.expiresAt,
    });
  } catch (error) {
    console.error("zego-token error", error);
    return requestErrorResponse(error, "Unable to initialize calling right now.");
  }
});
