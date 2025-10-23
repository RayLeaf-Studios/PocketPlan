package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class RebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED != intent.action) return

        AndroidThreeTen.init(context)
        StorageHandler.path = context.filesDir.absolutePath
        SettingsManager.init()

        val sleepReminder = SleepReminder(context, Dispatchers.IO)
        val preferencesHandler = PreferencesHandler(context)

        AlarmHandler.run {
            val time = runBlocking {
                preferencesHandler.read(PreferencesHandler.BIRTHDAY_NOTIFICATION_TIME).first()
            }
            setBirthdayAlarms(time, context = context)
        }
        sleepReminder.updateReminder()
    }
}