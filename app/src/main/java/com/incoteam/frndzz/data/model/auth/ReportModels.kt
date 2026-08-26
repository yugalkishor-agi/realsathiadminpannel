package com.incoteam.frndzz.data.model.auth

data class ReportUserRequest(
    val reportedUserId: String,
    val reason: String,
    val context: String,
    val note: String = "",
    val block: Boolean = true
)

data class ReportUserResponse(
    val submitted: Boolean = false,
    val blocked: Boolean = false,
    val reportId: String? = null,
    val message: String = ""
)

data class UnblockUserRequest(
    val blockedUserId: String
)

data class UnblockUserResponse(
    val unblocked: Boolean = false,
    val message: String = ""
)
