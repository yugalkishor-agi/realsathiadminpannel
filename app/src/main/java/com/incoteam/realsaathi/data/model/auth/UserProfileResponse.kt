package com.incoteam.realsaathi.data.model.auth

data class UserProfileResponse(
    val user: UserSession,
    val profile: RemoteUserProfile
)
