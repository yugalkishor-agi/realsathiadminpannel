package com.incoteam.realsaathi.core.session

import android.content.Context
import com.incoteam.realsaathi.data.model.auth.RefreshSessionResponse
import com.incoteam.realsaathi.data.model.auth.RemoteUserProfile
import com.incoteam.realsaathi.data.model.auth.UserSession
import com.incoteam.realsaathi.data.model.auth.VerifyOtpResponse

class SessionManager(context: Context) {

    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveAuthSession(response: VerifyOtpResponse) {
        val needsProfileSetup = response.isNewUser || profileNeedsSetup(response.profile)
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, response.accessToken)
            .putString(KEY_REFRESH_TOKEN, response.refreshToken)
            .putString(KEY_USER_ID, response.user.id)
            .putString(KEY_PHONE_NUMBER, response.user.phoneNumber)
            .putString(KEY_DISPLAY_NAME, response.user.displayName)
            .putString(KEY_PUBLIC_ID, resolvePublicId(response.user))
            .putBoolean(KEY_IS_HOST, response.user.isHost)
            .putBoolean(KEY_NEEDS_PROFILE_SETUP, needsProfileSetup)
            .apply()
    }

    fun updateUserSession(user: UserSession) {
        preferences.edit()
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_PHONE_NUMBER, user.phoneNumber)
            .putString(KEY_DISPLAY_NAME, user.displayName)
            .putString(KEY_PUBLIC_ID, resolvePublicId(user))
            .putBoolean(KEY_IS_HOST, user.isHost)
            .apply()
    }

    fun saveRefreshedSession(response: RefreshSessionResponse) {
        val editor = preferences.edit()
            .putString(KEY_ACCESS_TOKEN, response.accessToken)
            .putString(KEY_USER_ID, response.user.id)
            .putString(KEY_PHONE_NUMBER, response.user.phoneNumber)
            .putString(KEY_DISPLAY_NAME, response.user.displayName)
            .putString(KEY_PUBLIC_ID, resolvePublicId(response.user))
            .putBoolean(KEY_IS_HOST, response.user.isHost)
        if (!response.refreshToken.isNullOrBlank()) {
            editor.putString(KEY_REFRESH_TOKEN, response.refreshToken)
        }
        editor.apply()
    }

    fun getPhoneNumber(): String = preferences.getString(KEY_PHONE_NUMBER, "").orEmpty()

    fun getDisplayName(): String = preferences.getString(KEY_DISPLAY_NAME, "").orEmpty()

    fun getPublicId(): String {
        val publicId = preferences.getString(KEY_PUBLIC_ID, "").orEmpty()
        return publicId.takeIf { isValidPublicId(it) }.orEmpty()
    }

    fun getAccessToken(): String = preferences.getString(KEY_ACCESS_TOKEN, "").orEmpty()

    fun getRefreshToken(): String = preferences.getString(KEY_REFRESH_TOKEN, "").orEmpty()

    fun getUserId(): String = preferences.getString(KEY_USER_ID, "").orEmpty()

    fun isHost(): Boolean = preferences.getBoolean(KEY_IS_HOST, false)

    fun hasActiveSession(): Boolean {
        return getAccessToken().isNotBlank() &&
            getUserId().isNotBlank() &&
            getPhoneNumber().isNotBlank()
    }

    fun needsProfileSetup(): Boolean {
        return preferences.getBoolean(KEY_NEEDS_PROFILE_SETUP, false)
    }

    fun markProfileSetupCompleted() {
        preferences.edit()
            .putBoolean(KEY_NEEDS_PROFILE_SETUP, false)
            .apply()
    }

    fun logout() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "realsaathi_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_PUBLIC_ID = "public_id"
        private const val KEY_IS_HOST = "is_host"
        private const val KEY_NEEDS_PROFILE_SETUP = "needs_profile_setup"
    }

    private fun profileNeedsSetup(profile: RemoteUserProfile?): Boolean {
        if (profile == null) return true

        val nickname = (profile.nickname ?: profile.username).trim()
        val gender = profile.gender.orEmpty().trim()
        val interests = profile.interests.orEmpty().filter { it.isNotBlank() }

        return nickname.isBlank() ||
            nickname.startsWith("realsaathi_", ignoreCase = true) ||
            gender.isBlank() ||
            interests.isEmpty()
    }

    private fun resolvePublicId(user: UserSession): String {
        return user.publicId?.trim()
            ?.takeIf { isValidPublicId(it) }
            ?: getPublicId().takeIf { isValidPublicId(it) }
            ?: ""
    }

    private fun isValidPublicId(publicId: String): Boolean {
        return publicId.matches(Regex("^\\d{8}$"))
    }
}
