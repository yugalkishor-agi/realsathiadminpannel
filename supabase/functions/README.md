# Supabase Edge Functions

This folder contains the Fast2SMS WhatsApp OTP Edge Functions used by the Android app in `supabase_edge` auth mode.

## Functions

- `send-otp`
- `verify-otp`
- `refresh-session`
- `save-profile`
- `support-chat`
- `get-host-dashboard`
- `save-host-settings`
- `upload-host-media`

## Required Secrets

Set these in Supabase Edge Function secrets before deploying:

- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`
- `FAST2SMS_AUTH_KEY`
- `FAST2SMS_WHATSAPP_PHONE_NUMBER_ID`
- `FAST2SMS_WHATSAPP_MESSAGE_ID`
- `JWT_ACCESS_SECRET`
- `JWT_REFRESH_SECRET`
- `JWT_ACCESS_EXPIRES_IN`
- `JWT_REFRESH_EXPIRES_IN`
- `GROQ_API_KEY`
- `GROQ_MODEL` (optional, defaults to `llama-3.3-70b-versatile`)
- `OTP_EXPIRY_SECONDS`
- `OTP_RETRY_AFTER_SECONDS`
- `OTP_MAX_ATTEMPTS`

## Deploy

```bash
supabase functions deploy send-otp --no-verify-jwt
supabase functions deploy verify-otp --no-verify-jwt
supabase functions deploy refresh-session --no-verify-jwt
supabase functions deploy save-profile --no-verify-jwt
supabase functions deploy support-chat --no-verify-jwt
supabase functions deploy get-host-dashboard --no-verify-jwt
supabase functions deploy save-host-settings --no-verify-jwt
supabase functions deploy upload-host-media --no-verify-jwt
```

These functions use the app's custom `X-Session-Token` flow for protected requests.
Keep gateway JWT verification disabled on deploy, otherwise Supabase will reject requests
before the function code runs with `UNAUTHORIZED_NO_AUTH_HEADER` / `Missing authorization header`.

## Schema

Apply the host/profile upgrade SQL before using the host dashboard flow:

- `supabase/migrations/20260418_host_profile_upgrade.sql`
- `supabase/migrations/20260420_host_story_sync.sql`
