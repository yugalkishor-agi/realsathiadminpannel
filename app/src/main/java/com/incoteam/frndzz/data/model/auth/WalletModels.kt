package com.incoteam.frndzz.data.model.auth

data class WalletSummaryResponse(
    val balanceCoins: Int = 0,
    val transactions: List<RemoteWalletTransaction> = emptyList(),
    val source: String? = null
)

data class RemoteWalletTransaction(
    val id: String? = null,
    val kind: String = "adjustment",
    val title: String = "Wallet update",
    val detail: String = "",
    val amountText: String = "0",
    val coinsDelta: Int = 0,
    val rupeesDelta: Int = 0,
    val rechargeAmountRupees: Int = 0,
    val status: String = "completed",
    val counterpartyName: String? = null,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null,
    val giftName: String? = null,
    val giftCount: Int = 0,
    val giftContextLabel: String? = null,
    val createdAt: String? = null
)

data class RecordWalletTransactionRequest(
    val kind: String,
    val title: String,
    val detail: String,
    val amountText: String,
    val coinsDelta: Int,
    val rupeesDelta: Int = 0,
    val rechargeAmountRupees: Int = 0,
    val status: String = "completed",
    val counterpartyName: String? = null,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null,
    val giftName: String? = null,
    val giftCount: Int = 0,
    val giftContextLabel: String? = null
)

data class RecordWalletTransactionResponse(
    val recorded: Boolean = false,
    val transaction: RemoteWalletTransaction? = null
)
