package com.bindraft.amaldhikirtracker.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bindraft.amaldhikirtracker.AmalApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** AlarmManager alarms don't survive a reboot — reschedule any enabled reminders. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val app = context.applicationContext as AmalApplication
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ReminderScheduler.schedule(context, ReminderKind.DHIKIR, app.preferencesManager.getDhikirReminderSettings())
                ReminderScheduler.schedule(context, ReminderKind.QURAN, app.preferencesManager.getQuranReminderSettings())
            } finally {
                pendingResult.finish()
            }
        }
    }
}
