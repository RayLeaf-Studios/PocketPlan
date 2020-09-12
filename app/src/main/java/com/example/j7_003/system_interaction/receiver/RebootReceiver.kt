package com.example.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.j7_003.data.sleepreminder.SleepReminder
import com.example.j7_003.system_interaction.handler.AlarmHandler
import com.example.j7_003.system_interaction.handler.StorageHandler

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        StorageHandler.createFile("REBOOTRECEIVER", "reboot_receiver.txt")
        if (intent != null) {
            StorageHandler.files["REBOOTRECEIVER"]?.appendText(intent.toString() + "\t\t" + intent.extras.toString() + "\n")
        } else {
            StorageHandler.files["REBOOTRECEIVER"]?.appendText("null\n")
        }

        if (Intent.ACTION_BOOT_COMPLETED == intent!!.action) {
            AlarmHandler.run {
                setBirthdayAlarms(context = context!!)
            }
            SleepReminder.init()
            SleepReminder.updateReminder()
        }
    }
}