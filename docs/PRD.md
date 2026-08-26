# Dostt-Style App Product Requirements Document

Base: Frndzz PRD plus Dostt-style discovery/matching, topic-based conversations, and anonymity-first identity. Earning model remains open to all verified hosts and is not gender-restricted.

## 1. Product Overview

This is a mobile-first social calling app where customers can discover hosts or get randomly matched with one, start audio/video conversations around shared topics, chat, manage wallet/coins, and get help through support. Hosts can manage their public profile, live availability, earnings, wallet, and support interactions.

The product has two primary experiences:

- Customer/User side: login, profile setup, discovery through browse and random match, topic-based conversation entry, stories/feed, audio/video calls, chat, wallet, transactions, support, and account settings.
- Host side: host dashboard, profile management, DP upload from gallery, live availability controls, topic/interest tags, earnings, wallet, transactions, KYC, account settings, help/support, and logout.

## 2. Product Goals

- Create a simple OTP-based onboarding flow using mobile number authentication.
- Generate one permanent unique 8-digit numeric App ID on first registration.
- Provide consistent UI/UX across supported Android mobile devices.
- Let customers discover hosts through browse and through "Connect with Random" quick match.
- Let customers start engaging audio/video/chat interactions around topics.
- Let customers and hosts filter/match by topic tags such as Relationships, Career, Travel, Music, Food, and Politics.
- Let customers and hosts filter/match by native language such as Hindi, Telugu, English, and more.
- Let hosts manage profile identity, live status, earnings, wallet, and support flows.
- Keep identity anonymous by default: nickname plus avatar/DP only, with no phone number or personal information exposed.
- Moderate nicknames and prevent mobile numbers or social IDs in display names.
- Maintain verified profiles and content moderation to build trust and reduce fake-profile/fraud risk.
- Prepare the app architecture for production backend, payments, payouts, KYC, and media storage.

## 3. Target Users

### 3.1 Customer/User

Customers are users who want to browse or randomly match with hosts, connect through calls/chat around topics of interest, use coins/wallet, and manage their account.

Primary needs:

- Fast login with mobile number and OTP.
- Clean profile setup with nickname and avatar.
- Easy discovery of available hosts through browsing or instant random match.
- Topic-first conversation starters.
- Simple call/chat entry points with clear on-screen call controls and a live call timer.
- Wallet, transactions, help, and settings.

### 3.2 Host

Hosts are creators/listeners who receive user engagement and earn through the platform. Any verified host is eligible to earn.

Primary needs:

- Host profile setup with gallery DP.
- Permanent App ID shown publicly.
- Topic/interest tags to attract relevant conversations.
- Live audio/video availability controls.
- Earnings and wallet visibility with transparent per-minute rates.
- KYC, transactions, settings, help, and logout.

## 4. Scope

### 4.1 In Scope

- Android app built with Kotlin and Jetpack Compose.
- Node.js Express backend for OTP, profile, matching, host dashboard, host settings, media endpoint, and support chat.
- Supabase-backed user/profile persistence.
- JWT-based access and refresh token sessions.
- OTP login flow with mock and provider-based configuration.
- Profile setup and edit profile flows, including topic/interest tag selection and native language selection.
- 8-digit unique numeric App ID generation.
- Nickname validation and moderation.
- Customer profile card and host profile card.
- Discovery: browse-by-list and "Connect with Random" quick match.
- Topic-tag based filtering/matching.
- Host DP selection from gallery.
- Wallet, transaction, KYC, settings, help/support navigation entries.
- Customer and host side UI consistency requirements.
- Support chat integration architecture.
- Basic report/block affordance on call and chat screens.

### 4.2 Out of Scope for Current Build

- Production-grade real-time audio/video infrastructure.
- Production payment gateway integration.
- Production payout settlement.
- Full KYC vendor integration.
- Production media object storage for host DP/stories.
- Complete admin panel for moderation, payouts, disputes, and host approvals.
- Advanced matchmaking algorithm. Initial version can use simple random eligible-pool selection filtered by topic/language.

## 5. Success Metrics

- OTP login success rate: 95% or higher in healthy backend conditions.
- Profile setup completion rate: 85% or higher after first login.
- App ID duplicate rate: 0.
- Profile save failure rate: below 1% after stable backend connection.
- Random-match success rate: host found within acceptable wait time for 90% or higher during peak hours.
- UI layout consistency: no major layout breakage across common Android screen sizes.
- Host profile DP update success rate: 95% or higher after production storage integration.
- Crash-free sessions: 99% or higher.
- Fake-profile/report rate: tracked from launch and expected to trend downward release over release.

## 6. Core User Flows

### 6.1 Customer Login Flow

1. User enters mobile number.
2. App calls backend OTP send endpoint.
3. User enters 6-digit OTP.
4. App verifies OTP with backend.
5. Backend returns user session, tokens, and profile.
6. If user is new or profile is incomplete, app opens profile setup.
7. If profile is complete, app opens customer home.

Requirements:

- OTP must be 6 digits.
- OTP expiry, retry delay, and max attempts are backend-configurable.
- App must show clear errors for invalid OTP, expired OTP, and backend connection failure.
- On Android device testing, app must not rely on raw `127.0.0.1`; it must use adb reverse, a correct LAN URL, or a deployed backend URL.

### 6.2 First Registration and App ID Flow

1. User verifies OTP for the first time.
2. Backend creates user account.
3. Backend generates an 8-digit numeric App ID.
4. Backend checks the generated ID against existing users.
5. Backend stores ID permanently.
6. App saves ID locally from backend response.

Requirements:

- App ID must be exactly 8 digits.
- App ID must be numeric only.
- App ID must be generated only once during first account creation.
- App ID must never be regenerated during profile save, app restart, dashboard load, or local fallback.
- Database must enforce uniqueness using a unique index/constraint.
- If a generated ID already exists, backend must retry with a new ID.

### 6.3 Profile Setup Flow

1. User lands on setup screen after first login or incomplete profile.
2. User enters nickname.
3. User selects profile attributes: gender, native language, topic tags, and account mode where applicable.
4. User saves profile.
5. App sends profile payload to backend.
6. Backend validates and saves profile.
7. App updates local session/profile state.

Requirements:

- Nickname availability check is not required.
- Any allowed name is fine.
- First letter must become uppercase automatically.
- Original casing and spacing should be preserved otherwise.
- Nickname must reject abusive words, sexual/pornographic words, mobile numbers, and social media IDs/handles.
- Topic tags: user selects from a predefined tag list such as Relationships, Career, Travel, Music, Food, Politics, and Personal Growth.
- Topic tag selection minimum: 1.
- Topic tag selection maximum: 5.
- Native language can be single-select or multi-select from a supported language list.
- Supported languages should be configurable and include Hindi, Telugu, English, and more.

### 6.4 Discovery Flow - Browse

1. Customer opens home/discovery tab.
2. App shows a list/grid of available live hosts.
3. Customer optionally filters hosts by topic tag and language.
4. Customer taps a host card to view profile or start a call/chat directly.

Requirements:

- Only currently live/available hosts are shown by default.
- Optional toggle can show all hosts.
- Filter chips for topic tags and language must be visible at the top of discovery.
- Empty state must appear when no hosts match filters.

### 6.5 Discovery Flow - Connect With Random

1. Customer taps "Connect with Random" after optionally selecting a topic tag or language preference.
2. App requests backend for an eligible, live host matching optional filters.
3. Backend selects a host from the eligible pool.
4. App initiates call/chat session with matched host.
5. If no eligible host is found within a timeout window, app shows a friendly retry/empty state.

Requirements:

- Random match must respect topic/language filters if selected.
- Random match must never reveal customer or host phone numbers.
- Matching request must timeout gracefully, for example after 15-20 seconds.
- User must get a clear retry option when no host is available.
- Backend must exclude hosts who are blocked by or have blocked the requesting customer.

### 6.6 Customer Profile Flow

Customer profile screen must show:

- Profile card with nickname and avatar/DP.
- `ID - 12345678` style App ID.
- No mobile number in public profile card.
- Selected topic tags as optional secondary display.
- KYC Verification entry.
- Wallet entry.
- Transactions entry.
- Account Settings entry.
- Help and Support entry.
- Logout action.
- Footer branding line.

Requirements:

- Remove duplicate "Profile" labels.
- Keep navigation bar height and alignment consistent.
- Keep layout visually stable across supported mobile devices.

### 6.7 Host Profile Flow

Host profile screen must show:

- Top navigation/header matching customer-side height and level.
- Profile card with host nickname and avatar/DP.
- `ID - 12345678` style App ID.
- No mobile number in profile card.
- Topic/interest tags.
- KYC Verification entry.
- Wallet entry.
- Transactions entry.
- Account Settings entry.
- Help and Support entry.
- Logout button with centered logout text.
- Footer branding line.

Removed from host profile:

- `Live controls` section. Live controls stay on host dashboard instead.
- Stories section.
- Language/wallet duplicate controls inside edit profile if not part of customer-side profile edit pattern.

Requirements:

- Host profile must follow the same structure and visual language as customer profile.
- Host profile card must not show phone number.
- Host App ID must be numeric and permanent.
- Host logout text must be centered.

### 6.8 Host Edit Profile Flow

1. Host opens edit profile.
2. Host sees a centered circular DP area.
3. Host taps small plus icon.
4. Android gallery/image picker opens.
5. Host selects profile picture.
6. App uploads or stores selected image reference.
7. App updates host profile card with selected DP.
8. Host can edit topic tags and native language from the same screen.

Requirements:

- Host must use gallery-based profile photo.
- Avatar carousel must be removed for host edit profile.
- Customer/user side can continue using avatar selection if required.
- Host DP circle must be centered and visually clean.
- Small plus icon must clearly indicate adding/changing DP.
- If backend media storage returns no URL in current build, app may use local URI as temporary fallback.
- Production release must use durable media storage URL.

### 6.9 Host Dashboard Flow

Host dashboard must support:

- Host profile loading.
- Wallet summary.
- Total earnings and today's earnings.
- Activity logs.
- Audio live status and video live status toggles.
- Host rate settings within admin-configured bounds.
- Topic tag management shortcut.

Requirements:

- Dashboard must load from backend using authenticated token.
- If backend returns empty wallet/logs, UI must show safe empty states.
- Host mode must be persisted after host profile/settings save.

### 6.10 Call Experience Flow

1. Call is initiated from browse, random match, or chat.
2. App shows an active-call screen with clear controls: mute, speaker, camera toggle, and end call.
3. App shows a prominent running call timer throughout the call.
4. On call end, app shows a short summary: duration, coins spent/earned.
5. App returns to the previous screen.

Requirements:

- Call timer must be visible throughout the call.
- Controls must be large enough for reliable touch targets.
- Call screen must not leak phone numbers, contact info, or raw tokens in UI/logs.
- Report/Block option must be accessible from active-call screen.

### 6.11 Chat and Support Flow

Customer/host support flow must support:

- Help and Support entry from profile.
- Support chat payload with message history.
- AI support response when configured.
- Safe fallback error when support AI is unavailable.

Requirements:

- Support AI must answer only app-related questions.
- Support AI must never ask for OTPs, passwords, secret codes, or payment credentials.
- Chat and support screens must handle loading, error, and empty states.

### 6.12 Wallet and Transactions Flow

Wallet and transactions must support:

- Customer wallet/coin view.
- Host earnings view.
- Per-minute audio/video rate applied to call duration.
- Transaction history entry.
- Empty state when no transactions exist.

Requirements:

- Wallet values must never be trusted from client-only state in production.
- Backend must be source of truth for balances, earnings, and transaction logs.
- Production release requires payment and payout reconciliation.

### 6.13 Report and Block Flow

1. Customer or host taps Report/Block from call, chat, or profile screen.
2. User selects a reason: harassment, fake profile, inappropriate content, or other.
3. App submits report to backend.
4. App immediately blocks future matching/contact with that user.

Requirements:

- Blocking must prevent blocked-pair combinations from random match and discovery.
- Reports must be queued for review.
- Current build can log reports to backend/db for manual review.
- Moderation panel is future scope but should be prioritized early.

## 7. Functional Requirements

### 7.1 Authentication

- User can request OTP by mobile number.
- User can verify OTP.
- Backend creates account if phone number is new.
- Backend returns access token and refresh token.
- App stores tokens securely enough for current Android build.
- App refreshes or clears session when token is invalid.

### 7.2 User Identity

- Every user must have one permanent user ID.
- Every user must have one permanent public App ID.
- Public App ID format: 8 numeric digits.
- Public App ID display format: `ID - 12345678`.
- Public profile must not expose mobile number.
- Identity is anonymous by default: nickname plus avatar/DP is the public identity.

### 7.3 Nickname Rules

Allowed:

- Letters.
- Numbers.
- Spaces.
- Underscore.
- Dot.
- User-chosen casing, including all uppercase.

Blocked:

- Abusive or vulgar words.
- Sexual/pornographic words.
- Mobile numbers.
- Social media handles or IDs.
- Excessive invalid symbols.

Normalization:

- Collapse repeated spaces.
- Trim leading spaces.
- Limit length to 24 characters.
- Uppercase first letter found in the nickname.

### 7.4 Topic Tags and Language

- Topic tags must come from a predefined backend-maintained list.
- Supported languages must come from a predefined backend-maintained list.
- Lists should be extensible without an app update, for example through config endpoints.
- Topic tags and language are used for discovery filters and random-match eligibility.

### 7.5 Matching Engine - Basic

- Maintain a pool of currently live/eligible hosts.
- Filter pool by requested topic tags and/or language.
- Exclude blocked-pair combinations.
- Select one host through random selection for MVP.
- Return no-match response if pool is empty after wait window.
- Future matching can use weighting by rating, availability, response rate, language fit, and earnings fairness.

### 7.6 Customer UI

- Customer home must show discovery/calling-oriented experience.
- Browse and random-match entry points must both be available.
- Customer profile must match agreed structure.
- Customer profile card must show nickname and App ID only.
- Customer navigation bar must be visually stable and correctly sized.

### 7.7 Host UI

- Host side must visually align with customer side.
- Host profile must use same profile/settings structure as customer profile.
- Host edit profile must allow gallery DP.
- Host edit profile must allow topic-tag and language editing.
- Host profile must remove stories/live controls from profile screen.
- Host dashboard can keep operational live controls.

### 7.8 Device UI Consistency

- App must render with a consistent visual scale across Android devices.
- Navigation bar height must remain consistent between customer and host.
- Profile screens must not shift drastically between devices.
- Font scale and screen width differences must be normalized where product requires same design.
- UI must be checked on small, standard, and large mobile screens.

### 7.9 Backend APIs

Required endpoints:

- `GET /health`
- `POST /v1/auth/otp/send`
- `POST /v1/auth/otp/verify`
- `POST /v1/auth/session/refresh`
- `POST /v1/profile/save`
- `GET /v1/discovery/hosts`
- `POST /v1/discovery/random-match`
- `GET /v1/host/dashboard`
- `POST /v1/host/dashboard`
- `POST /v1/host/settings/save`
- `POST /v1/host/media/upload`
- `POST /v1/support/chat`
- `POST /v1/report`
- `GET /v1/config/tags`
- `GET /v1/config/languages`

### 7.10 Media

- Host can select profile photo from gallery.
- App should upload host photo through backend media endpoint.
- Backend should return a stable public URL.
- Current placeholder media endpoint may return empty URL.
- Production must integrate durable storage.

## 8. Data Requirements

### 8.1 User

Fields:

- `id`
- `phone_number`
- `public_id`
- `nickname`
- `username`
- `avatar_url`
- `account_mode`
- `role`
- `gender`
- `native_languages`
- `topic_tags`
- `host_status`
- `host_audio_live`
- `host_video_live`
- `host_audio_rate`
- `host_video_rate`
- `host_profile_photo_url`
- `host_story_items`
- `created_at`
- `updated_at`

Constraints:

- `public_id` must be unique when it matches `^[0-9]{8}$`.
- `phone_number` must map to one active user account.

### 8.2 User Profile

Fields:

- `user_id`
- `display_name`
- `nickname`
- `username`
- `gender`
- `language`
- `interests`
- `topic_tags`
- `account_mode`
- `avatar`
- `created_at`
- `updated_at`

### 8.3 OTP Request

Fields:

- `phone_number`
- `otp_code`
- `request_id`
- `expires_at`
- `attempts`
- `status`
- `created_at`

### 8.4 Wallet and Transactions

Future production fields:

- `wallet_id`
- `user_id`
- `balance`
- `currency`
- `transaction_id`
- `transaction_type`
- `amount`
- `status`
- `reference_id`
- `created_at`

### 8.5 Reports and Blocks

Report fields:

- `report_id`
- `reporter_id`
- `reported_id`
- `reason`
- `context`
- `status`
- `created_at`

Block fields:

- `block_id`
- `blocker_id`
- `blocked_id`
- `created_at`

## 9. UX Requirements

### 9.1 Visual Direction

- Dark theme with brand-defined accent colors.
- Rounded cards and clear touch targets.
- Mobile-first layout.
- Same design hierarchy on host and customer profile screens.
- Profile sections must be easy to scan.

### 9.2 Navigation Bar

- Host and customer navigation bars must share the same height and vertical alignment.
- Icons and labels must not appear too high or too short.
- Active state must be clear.
- Bar must respect safe areas and gesture navigation.

### 9.3 Discovery Screen

- Topic tag chips and language filter must be clearly visible at the top.
- "Connect with Random" must be a prominent primary action.
- Random match entry should be visually separate from browse list.
- Browse list must show host cards with DP, nickname, tags, and live indicator.

### 9.4 Profile Card

- Only nickname and App ID are required in profile card.
- Topic tags can appear as optional secondary display.
- No mobile number.
- ID label must be `ID - 12345678`.
- Missing ID must show a safe loading/empty state, not alphabetic fallback.

### 9.5 Call Screen

- Timer must be prominent and always visible during active call.
- Controls must be large and unambiguous.
- Core controls: mute, speaker, camera, end call.
- Report/Block must be accessible without cluttering primary controls.

### 9.6 Logout and Footer

- Logout button text must be centered.
- Footer branding line must appear below logout.
- Footer behavior must match customer-side behavior.

## 10. Security and Safety

- Never ask users for OTP outside OTP verification flow.
- Never expose access or refresh tokens in UI/logs.
- Do not show phone number in public profile cards or during matched calls/chats.
- Nicknames must block abusive, sexual, pornographic, mobile number, and social handle content.
- Support AI must avoid collecting secrets or payment credentials.
- Reports and blocking are core requirements because random matching with strangers is a core flow.
- Verified-profile badge and moderation queue should be prioritized early because fake-profile risk is known in this product category.
- Sensitive chat/call screens should prevent accidental exposure where supported.

## 11. Technical Architecture

### 11.1 Android

- Kotlin.
- Jetpack Compose.
- Material 3.
- Retrofit/OkHttp for backend APIs.
- SharedPreferences/session manager for local session state.
- Android image picker for host DP selection.
- Compose navigation/state-based screen switching.

### 11.2 Backend

- Node.js.
- Express.
- Supabase PostgreSQL.
- JWT access/refresh token service.
- OTP provider abstraction.
- Fast2SMS/WhatsApp OTP support.
- Groq-powered support chat when configured.
- Simple in-memory or DB-backed eligible-host pool for random matching in MVP.
- Matching service can later evolve to a queue/matching service.

### 11.3 Local Development

- Backend runs on port `4000`.
- Android emulator can use `10.0.2.2:4000` only when explicitly configured for emulator testing.
- Physical Android device requires adb reverse, LAN-accessible backend URL, or deployed backend URL.
- Health check endpoint must be used before testing OTP.

## 12. Release Criteria

### 12.1 MVP Release

MVP can release when:

- OTP login works on real device.
- New user account creation works.
- 8-digit numeric App ID is generated once and remains permanent.
- Duplicate App IDs are prevented by backend and database.
- Profile save works for customer and host, including topic tags and language.
- Browse discovery works end-to-end.
- Random match works end-to-end.
- Host DP selection works visually.
- Customer and host profile screens match required structure.
- Call screen shows working timer and controls.
- Report/Block flow works end-to-end, even with manual-review backend.
- Navigation bar is consistent across tested devices.
- Wallet/transactions/KYC/help entries are present with safe placeholder screens if full modules are pending.
- Backend health check is stable.
- App does not crash on poor network or backend error.

### 12.2 Production Release

Production release additionally requires:

- Real OTP provider fully configured.
- Production Supabase migrations applied.
- Durable media storage for host DP.
- Payment gateway and wallet ledger.
- Host payout flow.
- KYC provider integration.
- Admin moderation panel for report review, host approval, profile verification, disputes.
- Improved matching algorithm beyond simple random selection.
- Analytics and crash reporting.
- Privacy policy and terms.
- Play Store compliance review.

## 13. Known Gaps and Risks

- Media upload endpoint currently may behave as placeholder and return empty public URL.
- KYC is currently a profile entry/placeholder unless vendor integration is added.
- Wallet and transaction values need server-side ledger before production money movement.
- Real-time call infrastructure must be finalized before launch-scale calling.
- Random matching with strangers increases exposure to fake profiles, fraud, and harassment risk.
- Verification and report/block must not be deprioritized post-MVP.
- UI normalization can make screens look more similar across devices, but Android OS font/display settings may still create edge cases unless explicitly handled.
- Supabase unique public ID migration must be applied to the live database, not only kept in repo.
- Nickname moderation should be expanded with server-maintained blocked word lists.
- Simple random-pool matching may perform poorly at low host liquidity/off-peak hours.
- Low-liquidity periods may need a "notify me when available" fallback.

## 14. Open Questions

- Should App ID be generated from a random secure source only, or include internal sequencing for support lookup?
- Should hosts require KYC before going live?
- Should host DP require moderation approval before becoming public?
- Should customer and host wallets be separate ledger types?
- Which payment gateway and payout provider will be used?
- What is the exact coin-to-INR conversion and host earning rate model?
- What call provider will be used for production audio/video?
- Should random-match preference be mandatory or optional before matching?
- What is the acceptable wait-time threshold before showing "no host available" in random match?
- Should topic tags be host-only, customer-only, or both-sided?

## 15. Priority Roadmap

### P0

- Stabilize OTP login on physical devices.
- Apply unique 8-digit public ID migration.
- Fix profile save reliability.
- Finalize customer and host profile UI structure.
- Make host DP gallery flow stable.
- Build browse discovery and basic random match.
- Build report/block flow, even if manual-review backend.
- Verify UI consistency across target devices.

### P1

- Add durable media storage.
- Add wallet ledger APIs.
- Add transaction history APIs.
- Add host KYC flow.
- Add topic tag/language config-driven management.
- Add production OTP provider.

### P2

- Add admin panel for moderation, host approval, and disputes.
- Add call provider integration for production scale.
- Add advanced/weighted matching algorithm.
- Add advanced moderation and verified-profile badges.
- Add analytics dashboards.
- Add payout automation.

