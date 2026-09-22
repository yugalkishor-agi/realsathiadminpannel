package com.incoteam.realsaathi.data.model.auth

data class SaveHostSettingsRequest(
    val hostAudioLive: Boolean,
    val hostVideoLive: Boolean,
    val hostAudioRate: Int,
    val hostVideoRate: Int,
    val hostProfilePhotoUrl: String? = null,
    val hostStories: List<RemoteHostStory>? = null,
    val topicTags: List<String>? = null,
    val nativeLanguages: List<String>? = null
)
