package com.example.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.j7_003.data.sleepreminder.SleepReminder
import com.example.j7_003.system_interaction.handler.AlarmHandler

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            AlarmHandler.run {
                setBirthdayAlarms(context = context!!)
            }
            SleepReminder.init()
            SleepReminder.updateReminder()
        }
    }
}