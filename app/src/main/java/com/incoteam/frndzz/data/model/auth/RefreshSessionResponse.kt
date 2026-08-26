package com.incoteam.frndzz.data.model.auth

data class RefreshSessionResponse(
    val accessToken: String,
    val refreshToken: String? = null,
    val user: UserSession
)
