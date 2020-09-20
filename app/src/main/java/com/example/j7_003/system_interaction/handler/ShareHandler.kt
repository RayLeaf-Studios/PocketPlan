package com.example.j7_003.system_interaction.handler

import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.j7_003.MainActivity

class ShareHandler: FileProvider() {
    fun shareAll() {
        val list = ArrayList<Uri>()
        StorageHandler.files.forEach { (_, file) ->
            list.add(getUriForFile(
                MainActivity.act, "${MainActivity.act.applicationContext.packageName}.provider",
                file))
        }

        val sharingIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        sharingIntent.type = "text/*"

        sharingIntent.putExtra(Intent.EXTRA_STREAM, list)

        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }
}