package com.incoteam.frndzz.data.model.auth

data class VerifyOtpResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val isNewUser: Boolean = false,
    val user: UserSession,
    val profile: RemoteUserProfile? = null
)
