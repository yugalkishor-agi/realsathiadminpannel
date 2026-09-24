package com.incoteam.realsaathi.data.model.auth

import com.google.gson.annotations.SerializedName

data class ReportUserRequest(
    val reportedUserId: String = "",
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

data class BlockedProfile(
    val id: String = "",
    @SerializedName("public_id")
    val publicId: String? = null,
    val username: String? = null,
    val avatarId: Int? = null,
    val role: String? = null
)

data class BlockedUserEntry(
    val id: String = "",
    @SerializedName("blocked_id")
    val blockedId: String = "",
    @SerializedName("created_at")
    val createdAt: String = "",
    val profile: BlockedProfile? = null
)

data class BlockedUsersResponse(val blockedUsers: List<BlockedUserEntry> = emptyList())

data class UserReportEntry(
    val id: String = "",
    @SerializedName("reported_id")
    val reportedId: String? = null,
    val reason: String = "other",
    val context: String = "profile",
    val note: String? = null,
    val status: String = "pending",
    @SerializedName("admin_note")
    val adminNote: String? = null,
    @SerializedName("created_at")
    val createdAt: String = "",
    @SerializedName("resolved_at")
    val resolvedAt: String? = null,
    val profile: BlockedProfile? = null
)

data class UserReportsResponse(val reports: List<UserReportEntry> = emptyList())
