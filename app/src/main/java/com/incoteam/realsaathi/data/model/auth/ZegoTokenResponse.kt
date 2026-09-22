package com.incoteam.realsaathi.data.model.auth

data class ZegoTokenResponse(
    val appId: Long = 0L,
    val token: String = "",
    val userId: String = "",
    val expiresAt: Long = 0L
)
