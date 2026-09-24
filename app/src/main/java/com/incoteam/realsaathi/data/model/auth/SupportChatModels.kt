package com.incoteam.realsaathi.data.model.auth

data class SupportChatMessage(
    val role: String,
    val content: String
)

data class SupportChatRequest(
    val message: String = "",
    val history: List<SupportChatMessage> = emptyList(),
    val action: String = "send"
)

data class SupportChatRemoteMessage(
    val id: String,
    val senderType: String,
    val body: String,
    val createdAt: String
)

data class SupportChatResponse(
    val answer: String = "",
    val model: String? = null,
    val escalated: Boolean = false,
    val threadId: String? = null,
    val status: String? = null,
    val messages: List<SupportChatRemoteMessage> = emptyList()
)
