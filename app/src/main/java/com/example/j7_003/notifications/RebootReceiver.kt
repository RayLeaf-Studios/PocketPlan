package com.example.j7_003.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.j7_003.MainActivity

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                TODO("invoke the alarmmanager setup in the mainactivity")
            }
        }
    }
}