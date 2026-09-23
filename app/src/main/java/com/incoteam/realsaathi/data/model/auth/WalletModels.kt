package com.incoteam.realsaathi.data.model.auth

data class WalletSummaryResponse(
    val balanceCoins: Int = 0,
    val transactions: List<RemoteWalletTransaction> = emptyList(),
    val rechargeUpdates: List<RechargeReceipt> = emptyList(),
    val source: String? = null
)

data class RechargeReceipt(
    val id: String,
    val orderId: String,
    val status: String,
    val coins: Int,
    val amountRupees: Int,
    val createdAt: String,
    val orderCreatedAt: String
)

data class RechargeOrderRequest(val action: String, val coins: Int? = null, val orderId: String? = null)

data class RechargeOrderResponse(
    val orderId: String = "",
    val paymentSessionId: String = "",
    val status: String = "pending",
    val coins: Int = 0,
    val amountRupees: Int = 0
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
    val callStatus: String? = null,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null,
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
    val callId: String? = null,
    val callStatus: String? = null,
    val counterpartyId: String? = null,
    val ratePerMinute: Int? = null,
    val durationSeconds: Long? = null,
    val messageCount: Int? = null
)

data class RecordWalletTransactionResponse(
    val recorded: Boolean = false,
    val transaction: RemoteWalletTransaction? = null
)

data class CallEventRequest(
    val callId: String,
    val counterpartyId: String,
    val counterpartyName: String,
    val kind: String,
    val status: String,
    val durationSeconds: Long = 0L,
    val ratePerMinute: Int = 0
)
