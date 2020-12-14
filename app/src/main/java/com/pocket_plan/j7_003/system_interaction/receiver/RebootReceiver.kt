package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidThreeTen.init(context)

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            AlarmHandler.run {
                setBirthdayAlarms(context = context)
            }
            SleepReminder(context).updateReminder()
        }
    }
}