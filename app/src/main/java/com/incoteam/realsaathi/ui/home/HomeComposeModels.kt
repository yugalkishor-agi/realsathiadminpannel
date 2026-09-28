package com.incoteam.realsaathi.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.incoteam.realsaathi.R
import com.incoteam.realsaathi.data.model.auth.RemoteHostStory
import com.incoteam.realsaathi.data.model.auth.RemoteUserProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

val LocalCoins = compositionLocalOf<MutableState<Int>> {
    error("Coins not provided")
}

val AppBg = Color(0xFF0A0508)
val CardBg = Color(0xFF1A0B14)
val CardBgMuted = Color(0xFF26111D)
val Accent1 = Color(0xFFEE2B9D)
val Accent2 = Color(0xFFA855F7)
val Accent3 = Color(0xFFBEF264)
val TextSubtle = Color(0xFFCDB5C6)
val SuccessGreen = Color(0xFFBEF264)
val WarningAmber = Color(0xFFFFC857)
val DangerRed = Color(0xFFFF5E92)
val OnboardingBackgroundBrush = Brush.verticalGradient(
    listOf(Color(0xFF220340), Color(0xFF3C1272), Color(0xFF4B1887))
)
const val DefaultAvatarId = 9

data class Avatar(
    val id: Int,
    val initials: String,
    val colors: List<Color>,
    @DrawableRes val imageRes: Int
)

data class CallHistory(
    val callId: String,
    val userId: String,
    val name: String,
    val startedAtMillis: Long,
    val durationSeconds: Long,
    val isVideo: Boolean,
    val status: CallHistoryStatus
)

enum class CallHistoryStatus {
    ANSWERED,
    MISSED,
    CANCELLED
}

data class CoinPack(
    val coins: Int,
    val price: Int
)

enum class UserPresence {
    ONLINE,
    BUSY,
    OFFLINE
}

data class User(
    val id: String,
    val name: String,
    val interests: List<String>,
    val language: String,
    val presence: UserPresence,
    val ratePerMinute: Int,
    val hostAudioLive: Boolean = false,
    val hostVideoLive: Boolean = false,
    val busyForMinutes: Int? = null,
    val publicId: String? = null,
    val hostStories: List<RemoteHostStory> = emptyList(),
    val avatarId: Int = DefaultAvatarId,
    val profilePhotoUrl: String? = null
)

data class BlockedProfileUser(
    val id: String,
    val name: String,
    val subtitle: String
)

data class AppLanguage(
    val key: String,
    val label: String
)

enum class HostStoryMediaType {
    IMAGE,
    VIDEO
}

data class UploadedHostStory(
    val id: String,
    val ownerId: String,
    val ownerName: String,
    val title: String,
    val caption: String,
    val mediaUri: String,
    val mediaType: HostStoryMediaType,
    val createdAtMillis: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String,
    val text: String,
    val fromUser: Boolean,
    val timestampMillis: Long,
    val deliveryStatus: ChatDeliveryStatus? = null,
    val seenAtMillis: Long? = null,
    val imageUri: String? = null,
    val voiceNoteDurationSeconds: Int? = null,
    val voiceNoteUri: String? = null,
    val rechargeReceipt: com.incoteam.realsaathi.data.model.auth.RechargeReceipt? = null
)

enum class ChatDeliveryStatus {
    SENT,
    SEEN
}

data class ChatThread(
    val id: String,
    val title: String,
    val subtitle: String,
    val isPinned: Boolean = false,
    val unreadCount: Int = 0,
    val isOnline: Boolean = false,
    val lastSeenAtMillis: Long? = null,
    val unlockedUntilMillis: Long? = null,
    val messages: List<ChatMessage>
)

data class ActiveCallSession(
    val userId: String,
    val name: String,
    val isVideo: Boolean,
    val publicId: String? = null,
    val ratePerMinute: Int = 0,
    val includedSecondsAtStart: Long? = null,
    val startedAtMillis: Long = System.currentTimeMillis(),
    val avatarId: Int = DefaultAvatarId,
    val profilePhotoUrl: String? = null
)

// Strip the old generated suffix from cached call/chat names as well.
fun displayName(nickname: String): String = nickname
    .replace(Regex("\\s+• ID \\d{8}$"), "")
    .trim().ifBlank { "RealSaathi User" }

fun User.displayLabel(): String = displayName(name)

fun ActiveCallSession.displayLabel(): String = displayName(name)

fun isValidPublicIdValue(publicId: String): Boolean = publicId.matches(Regex("^\\d{8}$"))

sealed class ProfileRoute {
    data object Profile : ProfileRoute()
    data object Wallet : ProfileRoute()
    data object Transactions : ProfileRoute()
    data object Settings : ProfileRoute()
    data object Help : ProfileRoute()
    data object About : ProfileRoute()
    data object Blocked : ProfileRoute()
    data object EditProfile : ProfileRoute()
    data object Faqs : ProfileRoute()
    data object Feedback : ProfileRoute()
    data object Reports : ProfileRoute()
    data object DeleteAccount : ProfileRoute()
    data object Language : ProfileRoute()
}

val avatarList = listOf(
    Avatar(DefaultAvatarId, "NX", listOf(Color(0xFF1EA5FF), Color(0xFFFF4FD8)), R.drawable.avatar_ai_9),
    Avatar(10, "RV", listOf(Color(0xFF24B7FF), Color(0xFFFF55D9)), R.drawable.avatar_ai_10),

    Avatar(11, "AR", listOf(Color(0xFF1D8DFF), Color(0xFFFF4FC8)), R.drawable.avatar_ai_11),
    Avatar(12, "SK", listOf(Color(0xFF29C2FF), Color(0xFFFF68E0)), R.drawable.avatar_ai_12),
    Avatar(20, "F1", listOf(Accent2, Accent1), R.drawable.onboarding_female_avatar1),
    Avatar(21, "F2", listOf(Accent2, Accent1), R.drawable.onboarding_female_avatar2),
    Avatar(22, "F3", listOf(Accent2, Accent1), R.drawable.onboarding_female_avatar3),
    Avatar(30, "M1", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar1),
    Avatar(31, "M2", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar2),
    Avatar(32, "M3", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar3),
    Avatar(33, "M4", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar4),
    Avatar(34, "M5", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar5),
    Avatar(35, "M6", listOf(Accent2, Accent1), R.drawable.onboarding_male_avatar6)
)

val walletCoinPacks = listOf(
    CoinPack(99, 31),
    CoinPack(249, 75),
    CoinPack(320, 100),
    CoinPack(799, 250),
    CoinPack(1499, 455),
    CoinPack(2999, 899)
)

val supportedLanguages = listOf(
    AppLanguage("All", "All"),
    AppLanguage("Hindi", "हिन्दी"),
    AppLanguage("Marathi", "मराठी"),
    AppLanguage("Gujarati", "ગુજરાતી"),
    AppLanguage("Marwadi", "मारवाड़ी"),
    AppLanguage("Urdu", "اردو"),
    AppLanguage("Assamese", "অসমীয়া"),
    AppLanguage("Nepali", "नेपाली"),
    AppLanguage("Konkani", "कोंकणी"),
    AppLanguage("Tamil", "தமிழ்"),
    AppLanguage("Telugu", "తెలుగు"),
    AppLanguage("Kannada", "ಕನ್ನಡ"),
    AppLanguage("Malayalam", "മലയാളം"),
    AppLanguage("Odia", "ଓଡ଼ିଆ"),
    AppLanguage("Bengali", "বাংলা"),
    AppLanguage("Punjabi", "ਪੰਜਾਬੀ"),
    AppLanguage("English", "English")
)

val profileInterestOptions = listOf(
    "Relationships",
    "Career",
    "Travel",
    "Music",
    "Food",
    "Politics",
    "Personal Growth",
    "Movies",
    "Fitness",
    "Study",
    "Friendship",
    "Life Advice"
)

private const val HostStoryLifetimeMillis = 24L * 60L * 60L * 1000L
private const val MaxStoredHostStories = 12

object UserPrefs {
    private const val PREF = "compose_user_profile"
    private const val KEY_NICKNAME = "nickname"
    private const val KEY_USERNAME = "username"
    private const val KEY_PUBLIC_ID = "public_id"
    private const val KEY_USERNAME_LAST_CHANGED = "username_last_changed"
    private const val KEY_RESERVED_USERNAMES = "reserved_usernames"
    private const val KEY_GENDER = "gender"
    private const val KEY_GENDER_LOCKED = "gender_locked"
    private const val KEY_AVATAR = "avatar_id"
    private const val KEY_INTERESTS = "selected_interests"
    private const val KEY_LANGUAGE = "preferred_language"
    private const val KEY_COINS = "wallet_coins"
    private const val KEY_PROFILE_CUSTOMIZED = "profile_customized"
    private const val KEY_ACCOUNT_MODE = "account_mode"
    private const val KEY_HOST_AUDIO_LIVE = "host_audio_live"
    private const val KEY_HOST_VIDEO_LIVE = "host_video_live"
    private const val KEY_HOST_AUDIO_RATE = "host_audio_rate"
    private const val KEY_HOST_VIDEO_RATE = "host_video_rate"
    private const val KEY_HOST_DISPLAY_NAME = "host_display_name"
    private const val KEY_HOST_PROFILE_PHOTO_URI = "host_profile_photo_uri"
    private const val KEY_PROFILE_PHOTO_URL = "profile_photo_url"
    private const val KEY_HOST_UPLOADED_STORIES = "host_uploaded_stories"
    private const val KEY_COMMUNITY_NAME = "community_name"
    private const val KEY_COMMUNITY_CITY = "community_city"
    private const val KEY_COMMUNITY_ABOUT = "community_about"
    private const val KEY_COMMUNITY_EXPERIENCE = "community_experience"
    private const val KEY_CONFIG_TOPIC_OPTIONS = "config_topic_options"
    private const val KEY_CONFIG_LANGUAGE_OPTIONS = "config_language_options"
    private const val KEY_BOUND_USER_ID = "bound_user_id"

    fun prepareForAccount(ctx: Context, userId: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val previousUserId = prefs.getString(KEY_BOUND_USER_ID, "").orEmpty()
        if (previousUserId.isNotBlank() && previousUserId != userId) clearAccountData(ctx)
        prefs.edit().putString(KEY_BOUND_USER_ID, userId).apply()
    }

    fun clearAccountData(ctx: Context) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .remove(KEY_NICKNAME).remove(KEY_USERNAME).remove(KEY_PUBLIC_ID)
            .remove(KEY_USERNAME_LAST_CHANGED).remove(KEY_RESERVED_USERNAMES)
            .remove(KEY_GENDER).remove(KEY_GENDER_LOCKED).remove(KEY_AVATAR)
            .remove(KEY_INTERESTS).remove(KEY_LANGUAGE).remove(KEY_COINS)
            .remove(KEY_PROFILE_CUSTOMIZED).remove(KEY_ACCOUNT_MODE)
            .remove(KEY_HOST_AUDIO_LIVE).remove(KEY_HOST_VIDEO_LIVE)
            .remove(KEY_HOST_AUDIO_RATE).remove(KEY_HOST_VIDEO_RATE)
            .remove(KEY_HOST_DISPLAY_NAME).remove(KEY_HOST_PROFILE_PHOTO_URI)
            .remove(KEY_PROFILE_PHOTO_URL).remove(KEY_HOST_UPLOADED_STORIES)
            .remove(KEY_COMMUNITY_NAME).remove(KEY_COMMUNITY_CITY)
            .remove(KEY_COMMUNITY_ABOUT).remove(KEY_COMMUNITY_EXPERIENCE)
            .remove(KEY_ONBOARDING_AGE)
            .remove(KEY_BOUND_USER_ID).apply()
    }

    fun saveNickname(ctx: Context, name: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val normalizedName = sanitizeStoredUsername(name)
        val currentName = prefs.getString(KEY_USERNAME, "").orEmpty()
        val editor = prefs.edit()
            .putString(KEY_NICKNAME, normalizedName)
            .putString(KEY_USERNAME, normalizedName)
        if (normalizedName != currentName) {
            editor
                .putLong(KEY_USERNAME_LAST_CHANGED, System.currentTimeMillis())
                .putBoolean(KEY_PROFILE_CUSTOMIZED, true)
        }
        editor.apply()
    }

    fun saveUsername(ctx: Context, name: String) = saveNickname(ctx, name)

    fun savePublicId(ctx: Context, publicId: String) {
        val normalizedId = publicId.trim()
        if (!isValidPublicId(normalizedId)) return
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PUBLIC_ID, normalizedId)
            .apply()
    }

    fun getNickname(ctx: Context): String {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NICKNAME, prefs.getString(KEY_USERNAME, ""))
            .orEmpty()
    }

    fun getUsername(ctx: Context): String {
        return getNickname(ctx)
    }

    fun getPublicId(ctx: Context): String {
        val publicId = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_PUBLIC_ID, "")
            .orEmpty()
        return publicId.takeIf { isValidPublicId(it) }.orEmpty()
    }

    fun resolvePublicId(ctx: Context, primaryPublicId: String?): String {
        val primary = primaryPublicId.orEmpty().trim()
        return when {
            isValidPublicId(primary) -> primary
            else -> getPublicId(ctx)
        }
    }

    fun getUsernameLastChanged(ctx: Context): Long {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_USERNAME_LAST_CHANGED, 0L)
    }

    fun ensureStoredUsernameReserved(ctx: Context) {
        // Nicknames are now freely editable, so this is kept as a no-op for compatibility.
    }

    fun isUsernameAvailable(ctx: Context, name: String, currentUsername: String = getUsername(ctx)): Boolean {
        return sanitizeStoredUsername(name).isNotBlank()
    }

    fun saveGender(ctx: Context, gender: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_GENDER, gender)
            .putBoolean(KEY_GENDER_LOCKED, true)
            .putBoolean(KEY_PROFILE_CUSTOMIZED, true)
            .apply()
    }

    private const val KEY_ONBOARDING_AGE = "onboarding_age"

    fun saveOnboardingAge(ctx: Context, age: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            .putString(KEY_ONBOARDING_AGE, age).apply()
    }

    fun getOnboardingAge(ctx: Context): String =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ONBOARDING_AGE, "").orEmpty()

    fun seedNickname(ctx: Context, name: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_USERNAME, "").isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_NICKNAME, sanitizeStoredUsername(name))
                .putString(KEY_USERNAME, sanitizeStoredUsername(name))
                .putLong(KEY_USERNAME_LAST_CHANGED, 0L)
                .apply()
        }
    }

    fun seedUsername(ctx: Context, name: String) = seedNickname(ctx, name)

    fun seedGender(ctx: Context, gender: String) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (prefs.getString(KEY_GENDER, "").isNullOrBlank()) {
            prefs.edit()
                .putString(KEY_GENDER, gender)
                .putBoolean(KEY_GENDER_LOCKED, false)
                .apply()
        }
    }

    fun unlockSeededProfile(ctx: Context) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_PROFILE_CUSTOMIZED, false)) {
            prefs.edit()
                .putLong(KEY_USERNAME_LAST_CHANGED, 0L)
                .putBoolean(KEY_GENDER_LOCKED, false)
                .apply()
        }
    }

    fun getGender(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_GENDER, "")
            .orEmpty()
    }

    fun isGenderLocked(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_GENDER_LOCKED, false)
    }

    fun saveAccountMode(ctx: Context, accountMode: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ACCOUNT_MODE, accountMode)
            .apply()
    }

    fun getAccountMode(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_ACCOUNT_MODE, "")
            .orEmpty()
    }

    fun saveCommunityDetails(
        ctx: Context,
        name: String,
        city: String,
        about: String,
        experience: String
    ) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_COMMUNITY_NAME, name.trim())
            .putString(KEY_COMMUNITY_CITY, city.trim())
            .putString(KEY_COMMUNITY_ABOUT, about.trim())
            .putString(KEY_COMMUNITY_EXPERIENCE, experience.trim())
            .apply()
    }

    fun getCommunityName(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_COMMUNITY_NAME, "")
            .orEmpty()
    }

    fun getCommunityCity(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_COMMUNITY_CITY, "")
            .orEmpty()
    }

    fun getCommunityAbout(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_COMMUNITY_ABOUT, "")
            .orEmpty()
    }

    fun getCommunityExperience(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_COMMUNITY_EXPERIENCE, "")
            .orEmpty()
    }

    fun saveAvatar(ctx: Context, id: Int) {
        val resolvedAvatarId = avatarList.firstOrNull { it.id == id }?.id ?: DefaultAvatarId
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_AVATAR, resolvedAvatarId)
            .apply()
    }

    fun getAvatar(ctx: Context): Int {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_AVATAR, DefaultAvatarId)
    }

    fun saveInterests(ctx: Context, interests: List<String>) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_INTERESTS, interests.toSet())
            .putBoolean(KEY_PROFILE_CUSTOMIZED, true)
            .apply()
    }

    fun getInterests(ctx: Context): List<String> {
        val saved = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getStringSet(KEY_INTERESTS, emptySet())
            .orEmpty()
        return getTopicOptions(ctx).filter { it in saved }
    }

    fun saveTopicOptions(ctx: Context, tags: List<String>) {
        val normalizedTags = normalizeOptionList(tags, profileInterestOptions)
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONFIG_TOPIC_OPTIONS, JSONArray(normalizedTags).toString())
            .apply()
    }

    fun getTopicOptions(ctx: Context): List<String> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_CONFIG_TOPIC_OPTIONS, "")
            .orEmpty()
        return parseStringArray(raw).ifEmpty { profileInterestOptions }
    }

    fun saveLanguageOptions(ctx: Context, languages: List<String>) {
        val normalizedLanguages = normalizeOptionList(
            values = languages.filterNot { it.equals("All", ignoreCase = true) },
            fallback = supportedLanguages.filterNot { it.key == "All" }.map { it.key }
        )
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CONFIG_LANGUAGE_OPTIONS, JSONArray(normalizedLanguages).toString())
            .apply()
    }

    fun getConfiguredLanguages(ctx: Context, includeAll: Boolean = true): List<AppLanguage> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_CONFIG_LANGUAGE_OPTIONS, "")
            .orEmpty()
        val defaultLanguages = supportedLanguages.filterNot { it.key == "All" }.map { it.key }
        val configuredKeys = parseStringArray(raw).ifEmpty { defaultLanguages }
        val languages = configuredKeys.map { key ->
            supportedLanguages.firstOrNull { it.key == key } ?: AppLanguage(key, key)
        }
        return if (includeAll) {
            listOf(AppLanguage("All", "All")) + languages
        } else {
            languages
        }
    }

    fun saveLanguage(ctx: Context, language: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language)
            .putBoolean(KEY_PROFILE_CUSTOMIZED, true)
            .apply()
    }

    fun getLanguage(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, "All")
            .orEmpty()
    }

    fun saveCoins(ctx: Context, coins: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_COINS, coins)
            .apply()
    }

    fun getCoins(ctx: Context): Int {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_COINS, 120)
    }

    fun saveHostAudioLiveEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HOST_AUDIO_LIVE, enabled)
            .apply()
    }

    fun getHostAudioLiveEnabled(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_HOST_AUDIO_LIVE, true)
    }

    fun saveHostVideoLiveEnabled(ctx: Context, enabled: Boolean) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HOST_VIDEO_LIVE, enabled)
            .apply()
    }

    fun getHostVideoLiveEnabled(ctx: Context): Boolean {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_HOST_VIDEO_LIVE, true)
    }

    fun saveHostAudioRate(ctx: Context, rupeesPerMinute: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOST_AUDIO_RATE, rupeesPerMinute)
            .apply()
    }

    fun getHostAudioRate(ctx: Context): Int {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_HOST_AUDIO_RATE, 20)
    }

    fun saveHostVideoRate(ctx: Context, rupeesPerMinute: Int) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putInt(KEY_HOST_VIDEO_RATE, rupeesPerMinute)
            .apply()
    }

    fun getHostVideoRate(ctx: Context): Int {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getInt(KEY_HOST_VIDEO_RATE, 60)
    }

    fun saveHostDisplayName(ctx: Context, displayName: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOST_DISPLAY_NAME, displayName)
            .apply()
    }

    fun getHostDisplayName(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_HOST_DISPLAY_NAME, "Aisha")
            .orEmpty()
    }

    fun saveHostProfilePhotoUri(ctx: Context, photoUri: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOST_PROFILE_PHOTO_URI, photoUri)
            .apply()
    }

    fun getHostProfilePhotoUri(ctx: Context): String {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_HOST_PROFILE_PHOTO_URI, "")
            .orEmpty()
    }

    fun saveProfilePhotoUrl(ctx: Context, url: String) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString(KEY_PROFILE_PHOTO_URL, url).apply()
    }

    fun getProfilePhotoUrl(ctx: Context): String =
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(KEY_PROFILE_PHOTO_URL, "").orEmpty()

    fun saveHostUploadedStories(
        ctx: Context,
        stories: List<UploadedHostStory>,
        nowMillis: Long = System.currentTimeMillis()
    ) {
        val activeStories = sanitizeHostStories(
            ctx = ctx,
            stories = stories,
            nowMillis = nowMillis,
            persistSanitized = false
        )
        persistHostUploadedStories(ctx, activeStories)
    }

    fun getHostUploadedStories(
        ctx: Context,
        nowMillis: Long = System.currentTimeMillis()
    ): List<UploadedHostStory> {
        val raw = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_HOST_UPLOADED_STORIES, "[]")
            .orEmpty()
        val parsedStories = runCatching {
            val json = JSONArray(raw)
            buildList {
                repeat(json.length()) { index ->
                    val item = json.optJSONObject(index) ?: return@repeat
                    val mediaType = runCatching {
                        HostStoryMediaType.valueOf(item.optString("mediaType", HostStoryMediaType.IMAGE.name))
                    }.getOrDefault(HostStoryMediaType.IMAGE)
                    add(
                        UploadedHostStory(
                            id = item.optString("id", "story_$index"),
                            ownerId = item.optString("ownerId", "host-preview-story"),
                            ownerName = item.optString("ownerName", "Aisha"),
                            title = item.optString("title", "Fresh story"),
                            caption = item.optString("caption", ""),
                            mediaUri = item.optString("mediaUri", ""),
                            mediaType = mediaType,
                            createdAtMillis = item.optLong("createdAtMillis", System.currentTimeMillis())
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
        return sanitizeHostStories(
            ctx = ctx,
            stories = parsedStories,
            nowMillis = nowMillis,
            persistSanitized = true
        )
    }

    private fun persistHostUploadedStories(ctx: Context, stories: List<UploadedHostStory>) {
        val payload = JSONArray().apply {
            stories
                .sortedByDescending { it.createdAtMillis }
                .take(MaxStoredHostStories)
                .forEach { story ->
                    put(
                        JSONObject()
                            .put("id", story.id)
                            .put("ownerId", story.ownerId)
                            .put("ownerName", story.ownerName)
                            .put("title", story.title)
                            .put("caption", story.caption)
                            .put("mediaUri", story.mediaUri)
                            .put("mediaType", story.mediaType.name)
                            .put("createdAtMillis", story.createdAtMillis)
                    )
                }
        }
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HOST_UPLOADED_STORIES, payload.toString())
            .apply()
    }

    private fun sanitizeHostStories(
        ctx: Context,
        stories: List<UploadedHostStory>,
        nowMillis: Long,
        persistSanitized: Boolean
    ): List<UploadedHostStory> {
        val orderedStories = stories
            .filter { it.mediaUri.isNotBlank() }
            .sortedByDescending { it.createdAtMillis }
        val activeStories = orderedStories
            .filter { isHostStoryActive(it.createdAtMillis, nowMillis) }
            .take(MaxStoredHostStories)
        val activeIds = activeStories.map { it.id }.toSet()
        val activeMediaUris = activeStories.map { it.mediaUri }.toSet()
        orderedStories
            .filter { it.id !in activeIds && it.mediaUri !in activeMediaUris }
            .forEach { deleteManagedHostStoryMedia(ctx, it.mediaUri) }
        if (persistSanitized) {
            persistHostUploadedStories(ctx, activeStories)
        }
        return activeStories
    }

    fun syncFromRemoteProfile(ctx: Context, profile: RemoteUserProfile) {
        val prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        val normalizedNickname = sanitizeStoredUsername(
            profile.nickname?.takeIf { it.isNotBlank() } ?: profile.username
        )
        val remotePublicId = profile.publicId?.trim().orEmpty()
        val storedPublicId = prefs.getString(KEY_PUBLIC_ID, "").orEmpty()
        val normalizedPublicId = when {
            isValidPublicId(remotePublicId) -> remotePublicId
            isValidPublicId(storedPublicId) -> storedPublicId
            else -> ""
        }

        val normalizedAvatarId =
            avatarList.firstOrNull { it.id == profile.avatarId }?.id ?: DefaultAvatarId
        val normalizedTopicTags = profile.topicTags.orEmpty().ifEmpty { profile.interests.orEmpty() }
            .filter { it in getTopicOptions(ctx) }
        val normalizedLanguage = profile.nativeLanguages.orEmpty().firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: profile.preferredLanguage

        prefs.edit()
            .putString(KEY_NICKNAME, normalizedNickname)
            .putString(KEY_USERNAME, normalizedNickname)
            .putString(KEY_PUBLIC_ID, normalizedPublicId)
            .putString(KEY_GENDER, profile.gender.orEmpty())
            .putInt(KEY_AVATAR, normalizedAvatarId)
            .putStringSet(KEY_INTERESTS, normalizedTopicTags.toSet())
            .putString(KEY_LANGUAGE, normalizedLanguage)
            .putString(KEY_ACCOUNT_MODE, profile.accountMode)
            .putString(KEY_COMMUNITY_NAME, profile.communityName.orEmpty())
            .putString(KEY_COMMUNITY_CITY, profile.communityCity.orEmpty())
            .putString(KEY_COMMUNITY_ABOUT, profile.communityAbout.orEmpty())
            .putString(KEY_COMMUNITY_EXPERIENCE, profile.communityExperience.orEmpty())
            .putBoolean(KEY_PROFILE_CUSTOMIZED, true)
            .putBoolean(KEY_HOST_AUDIO_LIVE, profile.hostAudioLive)
            .putBoolean(KEY_HOST_VIDEO_LIVE, profile.hostVideoLive)
            .putInt(KEY_HOST_AUDIO_RATE, profile.hostAudioRate)
            .putInt(KEY_HOST_VIDEO_RATE, profile.hostVideoRate)
            .putString(KEY_HOST_DISPLAY_NAME, normalizedNickname.ifBlank { profile.username })
            .putString(KEY_HOST_PROFILE_PHOTO_URI, profile.hostProfilePhotoUrl.orEmpty())
            .putString(KEY_PROFILE_PHOTO_URL, profile.profilePhotoUrl.orEmpty())
            .apply()

        saveHostUploadedStories(
            ctx,
            profile.hostStories.orEmpty().map { it.toUploadedHostStory() }
        )
    }

    fun generateRandomNickname(ctx: Context): String {
        return createAvailableNickname(ctx, "realsaathi_${randomLetterChunk(4)}")
    }

    fun generateRandomUsername(ctx: Context): String = generateRandomNickname(ctx)

    private fun createAvailableNickname(ctx: Context, preferredName: String): String {
        val base = sanitizeStoredUsername(preferredName)
            .ifBlank { "realsaathi_${randomLetterChunk(4)}" }
            .take(24)
        return base
    }

    private fun sanitizeStoredUsername(input: String): String {
        val cleaned = input
            .replace("\\s+".toRegex(), " ")
            .filter { it.isLetterOrDigit() || it == '_' || it == '.' || it.isWhitespace() }
            .trim(' ', '.', '_')
            .take(24)

        val firstLetterIndex = cleaned.indexOfFirst { it.isLetter() }
        if (firstLetterIndex < 0) {
            return cleaned
        }

        return cleaned.replaceRange(
            firstLetterIndex,
            firstLetterIndex + 1,
            cleaned[firstLetterIndex].uppercase()
        )
    }

    private fun usernameKey(input: String): String = sanitizeStoredUsername(input)

    private fun isValidPublicId(publicId: String): Boolean {
        return publicId.matches(Regex("^\\d{8}$"))
    }

    private fun parseStringArray(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return runCatching {
            val json = JSONArray(raw)
            buildList {
                repeat(json.length()) { index ->
                    val value = json.optString(index).trim()
                    if (value.isNotBlank() && value !in this) {
                        add(value)
                    }
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun normalizeOptionList(values: List<String>, fallback: List<String>): List<String> {
        val normalized = values
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
            .take(40)
        return normalized.ifEmpty { fallback }
    }

    private fun randomLetterChunk(length: Int): String {
        val alphabet = ('a'..'z').toList()
        return buildString {
            repeat(length) {
                append(alphabet.random())
            }
        }
    }
}

private fun isHostStoryActive(createdAtMillis: Long, nowMillis: Long): Boolean {
    return createdAtMillis > 0L && createdAtMillis + HostStoryLifetimeMillis > nowMillis
}

fun UploadedHostStory.toRemoteHostStory(): RemoteHostStory {
    return RemoteHostStory(
        id = id,
        ownerId = ownerId,
        ownerName = ownerName,
        title = title,
        caption = caption,
        mediaUrl = mediaUri,
        mediaType = mediaType.name,
        createdAtMillis = createdAtMillis
    )
}

fun RemoteHostStory.toUploadedHostStory(): UploadedHostStory {
    val safeCreatedAt = createdAtMillis.takeIf { it > 0L } ?: System.currentTimeMillis()
    val normalizedMediaType = if (mediaType.orEmpty().equals("VIDEO", ignoreCase = true)) {
        HostStoryMediaType.VIDEO
    } else {
        HostStoryMediaType.IMAGE
    }
    return UploadedHostStory(
        id = id.orEmpty().ifBlank { "host_story_$safeCreatedAt" },
        ownerId = ownerId.orEmpty().ifBlank { "host-preview-story" },
        ownerName = ownerName.orEmpty().ifBlank { "Host" },
        title = title.orEmpty().ifBlank { "Fresh story" },
        caption = caption.orEmpty(),
        mediaUri = mediaUrl.orEmpty(),
        mediaType = normalizedMediaType,
        createdAtMillis = safeCreatedAt
    )
}

private fun deleteManagedHostStoryMedia(ctx: Context, mediaUri: String) {
    if (mediaUri.isBlank()) return
    val uri = runCatching { Uri.parse(mediaUri) }.getOrNull() ?: return
    if (uri.scheme != "file") return
    val filePath = uri.path.orEmpty()
    if (filePath.isBlank()) return
    runCatching {
        val storyDirectory = File(ctx.filesDir, "host_story_uploads").canonicalFile
        val mediaFile = File(filePath).canonicalFile
        val isInsideStoryDirectory = mediaFile.path == storyDirectory.path ||
            mediaFile.path.startsWith(storyDirectory.path + File.separator)
        if (isInsideStoryDirectory && mediaFile.exists()) {
            mediaFile.delete()
        }
    }
}

fun getLanguageLabel(key: String): String {
    return supportedLanguages.firstOrNull { it.key == key }?.label ?: "All"
}

fun toast(context: Context, msg: String) {
    android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
}

fun safeOpenUri(
    context: Context,
    uri: Uri,
    errorMessage: String
) {
    val intent = Intent(Intent.ACTION_VIEW, uri)
    safeStartActivity(context, intent, errorMessage)
}

fun safeStartActivity(
    context: Context,
    intent: Intent,
    errorMessage: String
) {
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        toast(context, errorMessage)
    }
}
