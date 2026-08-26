package com.incoteam.frndzz.data.model.auth

data class RemoteUserProfile(
    val nickname: String? = null,
    val username: String,
    val publicId: String? = null,
    val gender: String? = null,
    val preferredLanguage: String = "All",
    val nativeLanguages: List<String> = emptyList(),
    val avatarId: Int = 1,
    val interests: List<String> = emptyList(),
    val topicTags: List<String> = emptyList(),
    val accountMode: String = "customer",
    val hostStatus: String = "not_applicable",
    val communityName: String? = null,
    val communityCity: String? = null,
    val communityAbout: String? = null,
    val communityExperience: String? = null,
    val hostAudioLive: Boolean = false,
    val hostVideoLive: Boolean = false,
    val hostAudioRate: Int = 35,
    val hostVideoRate: Int = 65,
    val hostProfilePhotoUrl: String? = null,
    val hostStories: List<RemoteHostStory> = emptyList()
)
