package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.Logger
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidThreeTen.init(context)

        Logger(context).log("RebootReceive", "Device rebooted")

        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            AlarmHandler.run {
                setBirthdayAlarms(context = context)
            }
            Log.e("reboot", "trying")
            SleepReminder().updateReminder()
            Log.e("reboot", "worked")
        }

        Logger(context).log("RebootReceiver", "Set new birthday alarm")
    }
}