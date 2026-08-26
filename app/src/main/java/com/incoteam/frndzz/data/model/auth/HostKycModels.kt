package com.incoteam.frndzz.data.model.auth

data class HostKycRequest(
    val aadhaarNumber: String,
    val panNumber: String,
    val fullName: String,
    val emailAddress: String,
    val whatsappNumber: String,
    val phoneNumber: String,
    val dateOfBirth: String,
    val accountHolderName: String,
    val bankName: String,
    val bankAccountNumber: String,
    val ifscCode: String,
    val aadhaarImageBase64: String? = null,
    val panImageBase64: String? = null,
    val selfieWithAadhaarImageBase64: String? = null
)

data class HostKycResponse(
    val status: String = "not_submitted",
    val fullName: String? = null,
    val emailAddress: String? = null,
    val whatsappNumber: String? = null,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val aadhaarMasked: String? = null,
    val panMasked: String? = null,
    val bankName: String? = null,
    val bankAccountMasked: String? = null,
    val ifscCode: String? = null,
    val accountHolderName: String? = null,
    val message: String? = null
)
