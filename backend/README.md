# RealSaathi Backend

Backend auth module for the RealSaathi calling app.

## Tech

- Node.js
- Express
- Supabase PostgreSQL
- JWT access/refresh tokens
- OTP provider abstraction with `mock` mode enabled by default and Fast2SMS WhatsApp support
- Groq-powered support chat for RealSaathi app help

## Folder Structure

```text
backend/
  src/
    config/
    controllers/
    middlewares/
    models/
    repositories/
    routes/
    services/
    utils/
    validators/
    app.js
    server.js
  supabase/
    migrations/
      001_auth_schema.sql
  .env.example
  package.json
```

## Setup

1. Copy `.env.example` to `.env`
2. Add your Supabase project URL and service role key
3. If you want live WhatsApp OTP via Fast2SMS, set:
   - `OTP_PROVIDER=fast2sms_whatsapp`
   - `FAST2SMS_API_KEY`
   - `FAST2SMS_WHATSAPP_PHONE_NUMBER_ID`
   - `FAST2SMS_WHATSAPP_MESSAGE_ID`
4. For support chat, set:
   - `GROQ_API_KEY`
   - `GROQ_MODEL` (optional, defaults to `llama-3.3-70b-versatile`)
5. Create or use a Fast2SMS WhatsApp template that accepts one variable for the OTP code
6. Run the SQL migration in `supabase/migrations/001_auth_schema.sql`
7. Start the server:

```bash
npm install
npm run dev
```

## Endpoints

### `POST /v1/auth/otp/send`

Request:

```json
{
  "phoneNumber": "+919876543210"
}
```

Response:

```json
{
  "requestId": "0d5d25e2-353d-486d-a1ef-6b9f9f4a7f28",
  "message": "OTP sent successfully.",
  "retryAfterSeconds": 30
}
```

In development with `OTP_PROVIDER=mock`, the response also includes `debugOtp`.

With `OTP_PROVIDER=fast2sms_whatsapp`, the backend sends the OTP through Fast2SMS WhatsApp using the configured template and stores the provider reference in `otp_requests`.

### `POST /v1/support/chat`

Request:

```json
{
  "message": "How do I change my nickname?",
  "history": [
    { "role": "user", "content": "Hi" },
    { "role": "assistant", "content": "Hello, how can I help?" }
  ]
}
```

Headers:

```http
X-Session-Token: <access-token>
```

Response:

```json
{
  "answer": "Go to Profile > Edit Profile, then update your nickname and save it.",
  "model": "llama-3.3-70b-versatile",
  "escalated": false
}
```

This endpoint uses Groq chat completions and the app's current user context to answer only RealSaathi support questions.

### `POST /v1/auth/otp/verify`

Request:

```json
{
  "phoneNumber": "+919876543210",
  "otpCode": "123456",
  "requestId": "0d5d25e2-353d-486d-a1ef-6b9f9f4a7f28"
}
```

Response:

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "isNewUser": true,
  "user": {
    "id": "4ce8d4a8-b5b6-41b2-b1a4-4ab9af9769c0",
    "phoneNumber": "+919876543210",
    "displayName": "realsaathi_3210",
    "isHost": false
  }
}
```
