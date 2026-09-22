package com.incoteam.realsaathi

import com.incoteam.realsaathi.data.model.auth.RechargeReceipt
import com.incoteam.realsaathi.ui.home.buildRechargeAssistantThread
import com.incoteam.realsaathi.ui.home.displayName
import com.incoteam.realsaathi.ui.home.mergeRechargeReceipts
import com.incoteam.realsaathi.ui.home.timestampMillis
import org.junit.Assert.*
import org.junit.Test

class RechargeReceiptTest {
    private val pending = RechargeReceipt(
        "event-pending", "rs_0123456789abcdef0123456789abcdef", "pending", 320, 100,
        "2026-09-20T09:15:00+00:00", "2026-09-20T09:15:00+00:00"
    )
    private val completed = pending.copy(
        id = "event-completed", status = "completed", createdAt = "2026-09-20T09:16:00Z"
    )

    @Test fun cachedVerificationIdIsNeverShownInTheName() {
        assertEquals("Anaya", displayName("Anaya • ID 10000002"))
        assertEquals("Anaya", displayName(" Anaya "))
        assertEquals("RealSaathi User", displayName(""))
    }

    @Test fun receiptsSurviveRefreshWithoutDuplicateNotifications() {
        val first = mergeRechargeReceipts(buildRechargeAssistantThread(), listOf(completed, pending), 0L)
        val refreshed = mergeRechargeReceipts(first, listOf(completed, pending), 0L)
        assertEquals(2, refreshed.messages.size)
        assertEquals(2, refreshed.unreadCount)
        assertEquals("pending", refreshed.messages.first().rechargeReceipt?.status)
        assertEquals(pending.orderId, refreshed.messages.last().rechargeReceipt?.orderId)
        assertEquals(100, refreshed.messages.last().rechargeReceipt?.amountRupees)
        assertTrue(refreshed.messages.none { it.fromUser })
    }

    @Test fun onlyUpdatesAfterTheLastReadBecomeUnread() {
        val restored = mergeRechargeReceipts(buildRechargeAssistantThread(), listOf(pending, completed), pending.timestampMillis())
        assertEquals(1, restored.unreadCount)
        assertEquals(0, mergeRechargeReceipts(restored, listOf(completed), completed.timestampMillis()).unreadCount)
    }
}
