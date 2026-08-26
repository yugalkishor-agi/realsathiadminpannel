package com.incoteam.frndzz.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EngagementReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == EngagementReminderScheduler.reminderAction()) {
            val index = intent.getIntExtra(EngagementReminderScheduler.reminderIndexExtra(), 0)
            EngagementReminderScheduler.showReminderNotification(context, index)
        }
    }
}
