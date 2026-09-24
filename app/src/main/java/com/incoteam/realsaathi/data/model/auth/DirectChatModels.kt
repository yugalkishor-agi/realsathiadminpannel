package com.incoteam.realsaathi.data.model.auth

import com.google.gson.annotations.SerializedName

data class DirectChatRequest(
    val action: String = "send",
    val conversationId: String,
    val recipientId: String? = null,
    val message: String = ""
)

data class DirectChatRemoteMessage(
    val id: String,
    @SerializedName("sender_id")
    val senderId: String,
    @SerializedName("recipient_id")
    val recipientId: String? = null,
    val body: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("read_at")
    val readAt: String? = null
)

data class DirectChatRemoteThread(
    val conversationId: String,
    val participantId: String,
    val participantName: String,
    val unlockedUntil: String? = null,
    val latestMessage: DirectChatRemoteMessage? = null
)

data class DirectChatResponse(
    val message: DirectChatRemoteMessage? = null,
    val messages: List<DirectChatRemoteMessage> = emptyList(),
    val threads: List<DirectChatRemoteThread> = emptyList(),
    val chargeApplied: Boolean = false,
    val hostRewarded: Boolean = false
)
