package com.incoteam.frndzz.data.model.auth

data class SaveProfileRequest(
    val nickname: String,
    val gender: String,
    val preferredLanguage: String,
    val avatarId: Int,
    val interests: List<String>,
    val accountMode: String,
    val topicTags: List<String> = interests,
    val nativeLanguages: List<String> = listOf(preferredLanguage).filter { it.isNotBlank() },
    val communityName: String? = null,
    val communityCity: String? = null,
    val communityAbout: String? = null,
    val communityExperience: String? = null
)
