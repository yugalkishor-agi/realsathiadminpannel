package com.incoteam.realsaathi.data.model.auth

data class HostDashboardResponse(
    val user: UserSession,
    val profile: RemoteUserProfile,
    val wallet: HostWalletSummary,
    val logs: List<HostDashboardLog> = emptyList()
)

data class HostWalletSummary(
    val totalEarnings: Int = 0,
    val todayEarnings: Int = 0
)

data class HostDashboardLog(
    val name: String,
    val kind: String? = null,
    val isVideo: Boolean,
    val duration: String,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null,
    val callStatus: String? = null,
    val amount: Int,
    val time: String,
    val createdAt: String? = null
)
