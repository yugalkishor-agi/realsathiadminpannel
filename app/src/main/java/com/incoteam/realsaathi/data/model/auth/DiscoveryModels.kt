package com.incoteam.realsaathi.data.model.auth

data class DiscoveryHost(
    val id: String,
    val nickname: String,
    val username: String? = null,
    val publicId: String? = null,
    val avatarUrl: String? = null,
    val hostProfilePhotoUrl: String? = null,
    val topicTags: List<String> = emptyList(),
    val nativeLanguages: List<String> = emptyList(),
    val preferredLanguage: String = "All",
    val hostAudioLive: Boolean = false,
    val hostVideoLive: Boolean = false,
    val hostAudioRate: Int = 35,
    val hostVideoRate: Int = 65,
    val hostStatus: String = "not_applicable",
    val hostStories: List<RemoteHostStory> = emptyList()
)

data class DiscoveryFilters(
    val topicTags: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val liveOnly: Boolean = true
)

data class DiscoveryHostsResponse(
    val hosts: List<DiscoveryHost> = emptyList(),
    val filters: DiscoveryFilters = DiscoveryFilters()
)

data class RandomMatchRequest(
    val topicTags: List<String> = emptyList(),
    val languages: List<String> = emptyList()
)

data class RandomMatchResponse(
    val matched: Boolean = false,
    val host: DiscoveryHost? = null,
    val message: String = ""
)
