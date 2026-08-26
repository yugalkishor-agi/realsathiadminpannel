package com.incoteam.frndzz.data.model.auth

data class VerifyOtpRequest(
    val phoneNumber: String,
    val phone: String,
    val otpCode: String,
    val otp: String,
    val requestId: String = ""
)
