package com.incoteam.realsaathi.data.model.auth

data class VerifyOtpRequest(
    val phoneNumber: String,
    val phone: String,
    val otpCode: String,
    val otp: String,
    val requestId: String = "",
    val deviceId: String = "",
    val deviceBrand: String = "",
    val brandName: String = "",
    val countryCode: String = "",
    val appBrand: String = "RealSaathi"
)
