package com.bindraft.amaldhikirtracker.reminder

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bindraft.amaldhikirtracker.AmalApplication
import com.bindraft.amaldhikirtracker.MainActivity
import com.bindraft.amaldhikirtracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Fires hourly from the configured start time until the daily ayah target is reached for every
 * Quran mode that has one. If no mode has a target set, falls back to "any reading logged today".
 */
class QuranReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as AmalApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = app.preferencesManager.getQuranReminderSettings()
                if (!settings.enabled) return@launch

                val modes = app.repository.getAllQuranModes().first()
                val logs = app.repository.getQuranLogsByDate(LocalDate.now()).first()
                val modesWithTarget = modes.filter { it.dailyTarget != null }
                val quotaMet = if (modesWithTarget.isEmpty()) {
                    logs.any { it.ayahsRead > 0 }
                } else {
                    modesWithTarget.all { mode ->
                        val done = logs.filter { it.quranModeId == mode.id }.sumOf { it.ayahsRead }
                        done >= (mode.dailyTarget ?: 0)
                    }
                }

                if (quotaMet) {
                    ReminderScheduler.scheduleNextDayStart(context, ReminderKind.QURAN, settings.startMinutes)
                } else {
                    showNotification(context)
                    ReminderScheduler.scheduleNextHourTick(context, ReminderKind.QURAN)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderScheduler.EXTRA_REMINDER_TYPE, ReminderKind.QURAN.extraValue)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ReminderKind.QURAN.requestCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderNotifications.QURAN_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_quran_notification_title))
            .setContentText(context.getString(R.string.reminder_quran_notification_text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(ReminderKind.QURAN.requestCode, notification) }
    }
}
