package com.incoteam.realsaathi.data.model.auth

data class UserSession(
    val id: String,
    val phoneNumber: String,
    val displayName: String? = null,
    val publicId: String? = null,
    val isHost: Boolean = false,
    val deviceId: String? = null,
    val deviceBrand: String? = null,
    val countryCode: String? = null,
    val appBrand: String? = "RealSaathi",
    val sessionVersion: Int = 0
)
