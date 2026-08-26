package com.incoteam.frndzz.data.model.auth

data class UserSession(
    val id: String,
    val phoneNumber: String,
    val displayName: String? = null,
    val publicId: String? = null,
    val isHost: Boolean = false
)
