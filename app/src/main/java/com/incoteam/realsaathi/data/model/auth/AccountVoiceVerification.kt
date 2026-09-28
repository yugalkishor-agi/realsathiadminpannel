package com.incoteam.realsaathi.data.model.auth

data class AccountVoiceVerification(
    val status: String = "not_submitted",
    val reviewMessage: String = "",
    val metadata: Map<String, Any> = emptyMap()
)
