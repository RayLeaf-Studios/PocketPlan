package com.pocket_plan.j7_003.system_interaction.handler.share

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.system_interaction.handler.notifications.NotificationHandler
import java.lang.StringBuilder

class FileReceiver : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if (intent.action == Intent.ACTION_SEND) {
            if (intent.type == "application/json") {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri).let {
                    val inputStream = contentResolver.openInputStream(it)!!
                    val bufferReader = inputStream.bufferedReader()
                    val builder = StringBuilder()

                    val bytes = ByteArray(8)
                    val l = bufferReader.use {it.readText()}

                    Log.e("here", l.toString())
                    NotificationHandler.createNotification("debug", "debug", 9, "debug", l.toString(), R.drawable.ic_action_settings, "none", this)
                }
            }
        }

        super.onCreate(savedInstanceState)
        return
    }
}
