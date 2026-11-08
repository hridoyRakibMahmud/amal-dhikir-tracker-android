package com.bindraft.amaldhikirtracker.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.bindraft.amaldhikirtracker.data.local.ReminderSettings
import java.time.LocalDateTime
import java.time.ZoneId

enum class ReminderKind(val requestCode: Int, val extraValue: String) {
    DHIKIR(1001, "dhikir"),
    QURAN(1002, "quran")
}

/**
 * Each reminder has one daily start time; from then on the receiver re-arms itself every hour
 * until that day's quota is met, then arms for tomorrow's start time instead — there's no true
 * periodic alarm that can do this, so each fire schedules the next one.
 *
 * Uses [AlarmManager.setAndAllowWhileIdle] (not the exact-alarm APIs) so no SCHEDULE_EXACT_ALARM
 * permission is needed — a reminder firing a few minutes late under Doze is an acceptable
 * trade-off here.
 */
object ReminderScheduler {
    const val EXTRA_REMINDER_TYPE = "reminder_type"
    private const val HOUR_MILLIS = 60 * 60 * 1000L

    /** (Re)arms for the next tick from now — used when settings change and on boot. */
    fun schedule(context: Context, kind: ReminderKind, settings: ReminderSettings) {
        val alarmManager = alarmManager(context)
        val pendingIntent = pendingIntentFor(context, kind)
        if (!settings.enabled) {
            alarmManager.cancel(pendingIntent)
            return
        }
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextTickMillis(settings.startMinutes), pendingIntent)
    }

    /** Called by the receiver when today's quota is still unmet. */
    fun scheduleNextHourTick(context: Context, kind: ReminderKind) {
        alarmManager(context).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis() + HOUR_MILLIS,
            pendingIntentFor(context, kind)
        )
    }

    /** Called by the receiver once today's quota has been met — goes quiet until tomorrow's start time. */
    fun scheduleNextDayStart(context: Context, kind: ReminderKind, startMinutes: Int) {
        val zone = ZoneId.systemDefault()
        val tomorrowStart = LocalDateTime.now(zone).toLocalDate().plusDays(1).atStartOfDay().plusMinutes(startMinutes.toLong())
        alarmManager(context).setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            tomorrowStart.atZone(zone).toInstant().toEpochMilli(),
            pendingIntentFor(context, kind)
        )
    }

    fun cancel(context: Context, kind: ReminderKind) {
        alarmManager(context).cancel(pendingIntentFor(context, kind))
    }

    private fun alarmManager(context: Context) = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private fun pendingIntentFor(context: Context, kind: ReminderKind): PendingIntent {
        val receiverClass = when (kind) {
            ReminderKind.DHIKIR -> DhikirReminderReceiver::class.java
            ReminderKind.QURAN -> QuranReminderReceiver::class.java
        }
        return PendingIntent.getBroadcast(
            context,
            kind.requestCode,
            Intent(context, receiverClass),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Today's start time if it hasn't passed yet; otherwise almost immediately — catches up when enabled, or rebooted, after the start time has already passed. */
    private fun nextTickMillis(startMinutes: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = LocalDateTime.now(zone)
        val todayStart = now.toLocalDate().atStartOfDay().plusMinutes(startMinutes.toLong())
        val candidate = if (todayStart.isAfter(now)) todayStart else now.plusSeconds(5)
        return candidate.atZone(zone).toInstant().toEpochMilli()
    }
}
