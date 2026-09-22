package com.incoteam.realsaathi.ui.home

import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.incoteam.realsaathi.data.model.auth.RechargeReceipt
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

internal const val RechargeAssistantId = "recharge-assistant"

internal fun rechargeStatusLabel(status: String): String = when (status) {
    "completed" -> "Recharge successful"
    "processing" -> "Recharge processing"
    "failed" -> "Recharge unsuccessful"
    else -> "Recharge pending"
}

internal fun RechargeReceipt.timestampMillis(): Long =
    runCatching { Instant.parse(createdAt).toEpochMilli() }.getOrDefault(0L)

internal fun buildRechargeAssistantThread(now: Long = System.currentTimeMillis()) = ChatThread(
    id = RechargeAssistantId,
    title = "RealSaathi Recharge Assistant",
    subtitle = "Recharge receipts and payment updates",
    isPinned = true,
    lastSeenAtMillis = now,
    messages = listOf(ChatMessage(
        id = "recharge-assistant-welcome",
        text = "Your recharge receipts and payment updates will appear here automatically. This is a notifications-only chat.",
        fromUser = false,
        timestampMillis = now
    ))
)

internal fun mergeRechargeReceipts(
    thread: ChatThread,
    receipts: List<RechargeReceipt>,
    readThroughMillis: Long
): ChatThread {
    val updates = (thread.messages.mapNotNull { it.rechargeReceipt } + receipts)
        .distinctBy { it.id }.sortedWith(compareBy({ it.timestampMillis() }, { it.id })).takeLast(100)
    if (updates.isEmpty()) return thread
    val messages = updates.map { receipt ->
        ChatMessage(
            id = receipt.id,
            text = "${rechargeStatusLabel(receipt.status)} · ₹${receipt.amountRupees}",
            fromUser = false,
            timestampMillis = receipt.timestampMillis(),
            rechargeReceipt = receipt
        )
    }
    return thread.copy(
        messages = messages,
        subtitle = messages.last().text,
        lastSeenAtMillis = messages.last().timestampMillis,
        unreadCount = messages.count { it.timestampMillis > readThroughMillis }
    )
}

internal fun rechargeReadThrough(context: Context, userId: String): Long =
    context.getSharedPreferences("realsaathi_receipts", Context.MODE_PRIVATE).getLong(userId, 0L)

internal fun markRechargeRead(context: Context, userId: String, timestampMillis: Long) {
    context.getSharedPreferences("realsaathi_receipts", Context.MODE_PRIVATE).edit()
        .putLong(userId, maxOf(rechargeReadThrough(context, userId), timestampMillis)).apply()
}

@Composable
internal fun RechargeReceiptCard(receipt: RechargeReceipt) {
    val statusColor = when (receipt.status) {
        "completed" -> Color(0xFFBEF264)
        "failed" -> Color(0xFFFF7AB9)
        else -> Color(0xFFFFCF72)
    }
    val timestamp = receipt.timestampMillis()
    fun date(pattern: String): String = if (timestamp > 0L) {
        SimpleDateFormat(pattern, Locale.ENGLISH).format(Date(timestamp))
    } else "—"

    Surface(
        modifier = Modifier.widthIn(max = 360.dp).fillMaxWidth(),
        color = CardBg,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f))
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("RECHARGE RECEIPT", color = TextSubtle, fontSize = 10.sp, letterSpacing = 1.4.sp)
            Text(rechargeStatusLabel(receipt.status), color = statusColor, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text("₹${receipt.amountRupees}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
            ReceiptDetail("Date", date("dd MMM yyyy"))
            ReceiptDetail("Time", date("hh:mm a z"))
            ReceiptDetail(if (receipt.status == "completed") "Coins added" else "Coins", receipt.coins.toString())
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text("Order ID", color = TextSubtle, fontSize = 12.sp)
                SelectionContainer {
                    Text(receipt.orderId, color = Color.White, fontSize = 12.sp, lineHeight = 18.sp)
                }
            }
            Text(
                when (receipt.status) {
                    "completed" -> "Your coins have been added to your wallet."
                    "failed" -> "Payment was not completed. No coins were added."
                    "processing" -> "Your payment is being confirmed. We'll update this chat."
                    else -> "Waiting for payment confirmation."
                },
                color = TextSubtle, fontSize = 12.sp, lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun ReceiptDetail(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = TextSubtle, fontSize = 12.sp)
        Text(value, color = Color.White, fontSize = 12.sp)
    }
}
