package com.incoteam.realsaathi.data.model.auth

data class RefreshSessionResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: UserSession
)
