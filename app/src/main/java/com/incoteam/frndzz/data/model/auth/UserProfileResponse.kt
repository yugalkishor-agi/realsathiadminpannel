package com.incoteam.frndzz.data.model.auth

data class UserProfileResponse(
    val user: UserSession,
    val profile: RemoteUserProfile
)
