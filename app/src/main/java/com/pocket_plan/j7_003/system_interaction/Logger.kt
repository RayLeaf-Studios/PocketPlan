package com.pocket_plan.j7_003.system_interaction

import android.content.Context
import android.os.Build
import android.os.Environment
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.LocalDateTime
import java.io.File

class Logger(context: Context) {
    private val logFile: File

    init {
        AndroidThreeTen.init(context)
        logFile = setStorageLocation(context)
    }

    fun log(location: String, msg: String) {
        logFile.appendText("[${LocalDateTime.now()}], \\$location,\t$msg\n")
    }

    private fun setStorageLocation(context: Context): File {
        return if (Build.VERSION.SDK_INT < 29) {
            File("${Environment.getDataDirectory()}/data/com.example.j7_003", "Log.txt")
        } else {
            File(context.filesDir, "Log.txt")
        }
    }
}