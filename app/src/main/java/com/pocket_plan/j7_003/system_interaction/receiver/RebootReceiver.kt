package com.pocket_plan.j7_003.system_interaction.receiver

import SleepReminder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        AndroidThreeTen.init(context)
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            StorageHandler.path = context.filesDir.absolutePath
            SettingsManager.init()
            AlarmHandler.run {
                val time = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
                setBirthdayAlarms(time, context = context)
            }
            SleepReminder(context).updateReminder()
        }
    }
}