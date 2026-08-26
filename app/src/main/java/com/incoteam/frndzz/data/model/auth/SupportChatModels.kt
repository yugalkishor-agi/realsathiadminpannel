package com.incoteam.frndzz.data.model.auth

data class SupportChatMessage(
    val role: String,
    val content: String
)

data class SupportChatRequest(
    val message: String,
    val history: List<SupportChatMessage> = emptyList()
)

data class SupportChatResponse(
    val answer: String,
    val model: String? = null,
    val escalated: Boolean = false
)
