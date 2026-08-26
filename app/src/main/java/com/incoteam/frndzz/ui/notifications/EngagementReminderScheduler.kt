package com.incoteam.frndzz.ui.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.incoteam.frndzz.R
import com.incoteam.frndzz.ui.home.HomeActivity

object EngagementReminderScheduler {

    private const val CHANNEL_ID = "frndzzz_engagement_reminders"
    private const val CHANNEL_NAME = "Frndzzz Reminders"
    private const val CHANNEL_DESCRIPTION = "Smart reminder burst when the user comes back online."
    private const val REMINDER_ACTION = "com.incoteam.frndzz.action.SHOW_ENGAGEMENT_REMINDER"
    private const val EXTRA_NOTIFICATION_INDEX = "notification_index"
    private const val REMINDER_REQUEST_CODE = 4021
    private const val REMINDER_NOTIFICATION_ID = 4022
    private const val REMINDER_PREFS = "frndzzz_reminder_prefs"
    private const val KEY_NETWORK_STATE_SYNCED = "network_state_synced"
    private const val KEY_LAST_NETWORK_ONLINE = "last_network_online"
    private const val KEY_LAST_BURST_AT = "last_burst_at"
    private const val NETWORK_BURST_COOLDOWN_MILLIS = AlarmManager.INTERVAL_HOUR * 6
    private val REMINDER_DELAYS_MILLIS = longArrayOf(0L, 28_000L, 90_000L)
    private val REMINDER_COPY = listOf(
        "Online aa gaye? Aaj bas net hi nahi, thoda dil bhi on kar lo. Frndzzz par koi tumhari vibe ka wait kar raha ho sakta hai.",
        "Wi-Fi on ho gaya, ab thodi si romantic feel bhi on kar do. Frndzzz par apni pasandida ladki se baat shuru karo.",
        "Raat, mood aur internet tino ready hain. Frndzzz kholo aur kisi khaas conversation ko start karo."
    )
    private val REMINDER_TITLES = listOf(
        "Tum online aa gaye...",
        "Mood ko bhi online karo",
        "Koi conversation tumhara wait kar rahi hai"
    )

    fun syncCurrentNetworkState(context: Context, isOnline: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_NETWORK_STATE_SYNCED, true)
            .putBoolean(KEY_LAST_NETWORK_ONLINE, isOnline)
            .apply()
    }

    fun onConnectivityChanged(context: Context, isOnline: Boolean) {
        ensureChannel(context)
        val prefs = prefs(context)
        val hasSyncedState = prefs.getBoolean(KEY_NETWORK_STATE_SYNCED, false)
        val wasOnline = prefs.getBoolean(KEY_LAST_NETWORK_ONLINE, isOnline)

        prefs.edit()
            .putBoolean(KEY_NETWORK_STATE_SYNCED, true)
            .putBoolean(KEY_LAST_NETWORK_ONLINE, isOnline)
            .apply()

        if (!hasSyncedState) return
        if (!isOnline || wasOnline == isOnline) return
        if (!canShowNotifications(context)) return

        val now = System.currentTimeMillis()
        val lastBurstAt = prefs.getLong(KEY_LAST_BURST_AT, 0L)
        if (now - lastBurstAt < NETWORK_BURST_COOLDOWN_MILLIS) {
            return
        }

        prefs.edit().putLong(KEY_LAST_BURST_AT, now).apply()
        scheduleOnlineReminderBurst(context)
    }

    fun showReminderNotification(context: Context, index: Int = 0) {
        ensureChannel(context)
        if (!canShowNotifications(context)) return
        val reminderIndex = index.coerceIn(0, REMINDER_COPY.lastIndex)

        val contentIntent = PendingIntent.getActivity(
            context,
            REMINDER_REQUEST_CODE + 50 + reminderIndex,
            Intent(context, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_brand_mark_monochrome)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentTitle(REMINDER_TITLES[reminderIndex])
            .setContentText(REMINDER_COPY[reminderIndex])
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(REMINDER_COPY[reminderIndex])
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            REMINDER_NOTIFICATION_ID + reminderIndex,
            notification
        )
    }

    private fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) != null) return
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
        )
    }

    private fun scheduleOnlineReminderBurst(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java) ?: run {
            REMINDER_DELAYS_MILLIS.indices.forEach { index ->
                showReminderNotification(context, index)
            }
            return
        }

        cancelQueuedReminderBurst(context, alarmManager)
        REMINDER_DELAYS_MILLIS.forEachIndexed { index, delayMillis ->
            if (delayMillis == 0L) {
                showReminderNotification(context, index)
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    System.currentTimeMillis() + delayMillis,
                    reminderPendingIntent(context, index)
                )
            }
        }
    }

    private fun cancelQueuedReminderBurst(context: Context, alarmManager: AlarmManager) {
        REMINDER_DELAYS_MILLIS.indices.forEach { index ->
            alarmManager.cancel(reminderPendingIntent(context, index))
        }
    }

    private fun reminderPendingIntent(context: Context, index: Int): PendingIntent {
        val intent = Intent(context, EngagementReminderReceiver::class.java).apply {
            action = REMINDER_ACTION
            putExtra(EXTRA_NOTIFICATION_INDEX, index)
        }
        return PendingIntent.getBroadcast(
            context,
            REMINDER_REQUEST_CODE + index,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun canShowNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(REMINDER_PREFS, Context.MODE_PRIVATE)

    internal fun reminderAction(): String = REMINDER_ACTION
    internal fun reminderIndexExtra(): String = EXTRA_NOTIFICATION_INDEX
}
