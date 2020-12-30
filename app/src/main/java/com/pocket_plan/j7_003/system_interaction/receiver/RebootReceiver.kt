package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.internal.Storage
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.system_interaction.Logger
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import java.lang.Exception

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidThreeTen.init(context)
        val myLogger = Logger(context)
        myLogger.log("RebootReceiver", "Reboot received")

        try {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                StorageHandler.path = context.filesDir.absolutePath
                AlarmHandler.run {
                    setBirthdayAlarms(context = context)
                }
                SleepReminder(context).updateReminder()
                myLogger.log("RebootReceiver", "Reboot worked!")
            }
        }catch(e: Exception) {
            myLogger.log("RebootReceiver", e.stackTraceToString())
        }

    }
}