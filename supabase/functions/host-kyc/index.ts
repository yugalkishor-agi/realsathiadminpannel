import { authenticateRequest, corsResponse, createAdminClient, jsonResponse, requestErrorResponse } from "../_shared/auth.ts";

function digits(value: unknown) { return String(value ?? "").replace(/\D/g, ""); }
function text(value: unknown) { return String(value ?? "").trim(); }
function mask(value: string, visible = 4) { return `${"X".repeat(Math.max(0, value.length - visible))}${value.slice(-visible)}`; }
function mapRow(row: Record<string, unknown> | null) {
  if (!row) return { status: "not_submitted", message: "Complete KYC to enable withdrawals. Review is completed within 48 hours after submission." };
  return {
    status: text(row.status), fullName: text(row.full_name), emailAddress: text(row.email_address),
    whatsappNumber: text(row.whatsapp_number), phoneNumber: text(row.phone_number),
    dateOfBirth: text(row.date_of_birth), aadhaarMasked: text(row.aadhaar_masked),
    panMasked: text(row.pan_masked), bankName: text(row.bank_name),
    bankAccountMasked: text(row.bank_account_masked), ifscCode: text(row.ifsc_code),
    accountHolderName: text(row.account_holder_name), message: text(row.message),
  };
}

function validateImage(value: unknown): string | null {
  const image = text(value);
  return /^data:image\/(?:jpeg|jpg|png);base64,[A-Za-z0-9+/=]+$/.test(image) && image.length <= 2_500_000
    ? image : null;
}

function validAadhaar(value: string): boolean {
  if (!/^[2-9]\d{11}$/.test(value)) return false;
  const d = [
    [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], [1, 2, 3, 4, 0, 6, 7, 8, 9, 5],
    [2, 3, 4, 0, 1, 7, 8, 9, 5, 6], [3, 4, 0, 1, 2, 8, 9, 5, 6, 7],
    [4, 0, 1, 2, 3, 9, 5, 6, 7, 8], [5, 9, 8, 7, 6, 0, 4, 3, 2, 1],
    [6, 5, 9, 8, 7, 1, 0, 4, 3, 2], [7, 6, 5, 9, 8, 2, 1, 0, 4, 3],
    [8, 7, 6, 5, 9, 3, 2, 1, 0, 4], [9, 8, 7, 6, 5, 4, 3, 2, 1, 0],
  ];
  const p = [
    [0, 1, 2, 3, 4, 5, 6, 7, 8, 9], [1, 5, 7, 6, 2, 8, 3, 0, 9, 4],
    [5, 8, 0, 3, 7, 9, 6, 1, 4, 2], [8, 9, 1, 6, 0, 4, 3, 5, 2, 7],
    [9, 4, 5, 3, 1, 2, 6, 8, 7, 0], [4, 2, 8, 6, 9, 0, 3, 7, 1, 5],
    [2, 7, 9, 3, 8, 0, 6, 4, 1, 5], [7, 0, 4, 6, 9, 1, 3, 2, 5, 8],
  ];
  let checksum = 0;
  [...value].reverse().forEach((digit, index) => {
    checksum = d[checksum][p[index % 8][Number(digit)]];
  });
  return checksum === 0;
}

Deno.serve(async (req) => {
  if (req.method === "OPTIONS") return corsResponse();
  if (req.method !== "GET" && req.method !== "POST") return jsonResponse({ message: "Method not allowed." }, 405);
  try {
    const { userId } = await authenticateRequest(req);
    const db = createAdminClient();
    const { data: user, error: userError } = await db.from("users").select("role").eq("id", userId).single();
    if (userError) throw userError;
    if (user?.role !== "host") return jsonResponse({ message: "Host account required." }, 403);
    const { data: existing, error: lookupError } = await db.from("host_kyc_details")
      .select("*").eq("user_id", userId).maybeSingle();
    if (lookupError) throw lookupError;
    if (req.method === "GET") return jsonResponse(mapRow(existing));
    if (existing && ["pending", "submitted", "under_review", "approved"].includes(text(existing.status).toLowerCase())) {
      return jsonResponse(mapRow(existing));
    }

    const body = await req.json();
    const aadhaar = digits(body?.aadhaarNumber);
    const pan = text(body?.panNumber).toUpperCase();
    const fullName = text(body?.fullName).replace(/\s+/g, " ");
    const bankName = text(body?.bankName);
    const bankAccount = digits(body?.bankAccountNumber);
    const ifsc = text(body?.ifscCode).toUpperCase();
    const accountHolder = text(body?.accountHolderName).replace(/\s+/g, " ");
    const email = text(body?.emailAddress);
    const whatsapp = digits(body?.whatsappNumber);
    const phone = digits(body?.phoneNumber);
    const dob = text(body?.dateOfBirth);
    const aadhaarImage = validateImage(body?.aadhaarImageBase64);
    const panImage = validateImage(body?.panImageBase64);
    const selfieImage = validateImage(body?.selfieWithAadhaarImageBase64);
    const dobMatch = /^(\d{2})\/(\d{2})\/(\d{4})$/.exec(dob);
    const dobDate = dobMatch ? new Date(Date.UTC(Number(dobMatch[3]), Number(dobMatch[2]) - 1, Number(dobMatch[1]))) : null;
    const dobValid = !!dobMatch && !!dobDate && dobDate.getUTCDate() === Number(dobMatch[1]) &&
      dobDate.getUTCMonth() === Number(dobMatch[2]) - 1 && dobDate.getTime() <= Date.now();
    if (!validAadhaar(aadhaar) || !/^[A-Z]{5}\d{4}[A-Z]$/.test(pan) ||
      fullName.length < 2 || !/^[a-zA-Z\s.]+$/.test(fullName) ||
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email) ||
      !/^\d{10}$/.test(whatsapp) || !/^\d{10}$/.test(phone) || !dobValid ||
      bankName.length < 2 || !/^\d{6,18}$/.test(bankAccount) ||
      !/^[A-Z]{4}0[A-Z0-9]{6}$/.test(ifsc) ||
      accountHolder.toUpperCase() !== fullName.toUpperCase() ||
      !aadhaarImage || !panImage || !selfieImage) {
      return jsonResponse({ message: "Please enter valid KYC details and photos." }, 422);
    }
    const { data, error } = await db.from("host_kyc_details").upsert({
      user_id: userId, full_name: fullName, email_address: email,
      whatsapp_number: whatsapp, phone_number: phone, date_of_birth: dob,
      aadhaar_masked: mask(aadhaar), pan_masked: mask(pan, 2), bank_name: bankName,
      bank_account_masked: mask(bankAccount), ifsc_code: ifsc,
      account_holder_name: accountHolder, aadhaar_image_data: aadhaarImage,
      pan_image_data: panImage, selfie_aadhaar_image_data: selfieImage,
      status: "pending", message: "Verification submitted. Review will be completed within 48 hours.",
      updated_at: new Date().toISOString(),
    }, { onConflict: "user_id" }).select("*").single();
    if (error) throw error;
    return jsonResponse(mapRow(data));
  } catch (error) {
    console.error("host-kyc error", error);
    return requestErrorResponse(error, "Unable to process KYC right now.");
  }
});
