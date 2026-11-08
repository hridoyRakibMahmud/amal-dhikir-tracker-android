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

/** Fires hourly from the configured start time until every dhikir with a daily target has reached it. */
class DhikirReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as AmalApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = app.preferencesManager.getDhikirReminderSettings()
                if (!settings.enabled) return@launch

                val dhikirs = app.repository.getAllDhikirs().first()
                val logs = app.repository.getDhikirLogsByDate(LocalDate.now()).first()
                val quotaMet = dhikirs.all { dhikir ->
                    val target = dhikir.dailyTarget ?: return@all true
                    val done = logs.filter { it.dhikirId == dhikir.id }.sumOf { it.count }
                    done >= target
                }

                if (quotaMet) {
                    ReminderScheduler.scheduleNextDayStart(context, ReminderKind.DHIKIR, settings.startMinutes)
                } else {
                    showNotification(context)
                    ReminderScheduler.scheduleNextHourTick(context, ReminderKind.DHIKIR)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun showNotification(context: Context) {
        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(ReminderScheduler.EXTRA_REMINDER_TYPE, ReminderKind.DHIKIR.extraValue)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            ReminderKind.DHIKIR.requestCode,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, ReminderNotifications.DHIKIR_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.reminder_dhikir_notification_title))
            .setContentText(context.getString(R.string.reminder_dhikir_notification_text))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()
        runCatching { NotificationManagerCompat.from(context).notify(ReminderKind.DHIKIR.requestCode, notification) }
    }
}
