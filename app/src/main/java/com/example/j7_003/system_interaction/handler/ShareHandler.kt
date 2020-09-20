package com.example.j7_003.system_interaction.handler

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.example.j7_003.MainActivity
import java.io.File

class ShareHandler: FileProvider() {
    fun shareAll() {
        val file: File = StorageHandler.files["SETTINGS"]!!
        val sharingIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        sharingIntent.type = "text/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM, getUriForFile(MainActivity.act,
            "${MainActivity.act.applicationContext.packageName}.provider", file))
        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        MainActivity.act.startActivity(Intent.createChooser(sharingIntent, "Share via"))
    }
}