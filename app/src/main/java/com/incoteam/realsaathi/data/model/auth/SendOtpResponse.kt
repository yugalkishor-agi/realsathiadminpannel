package com.incoteam.realsaathi.data.model.auth

data class SendOtpResponse(
    val requestId: String = "",
    val message: String = "",
    val retryAfterSeconds: Int = 30
)
