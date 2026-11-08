package com.bindraft.amaldhikirtracker.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.bindraft.amaldhikirtracker.R

object ReminderNotifications {
    const val DHIKIR_CHANNEL_ID = "dhikir_reminder"
    const val QURAN_CHANNEL_ID = "quran_reminder"

    fun ensureChannels(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                DHIKIR_CHANNEL_ID,
                context.getString(R.string.reminder_dhikir_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
        manager.createNotificationChannel(
            NotificationChannel(
                QURAN_CHANNEL_ID,
                context.getString(R.string.reminder_quran_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )
    }
}
