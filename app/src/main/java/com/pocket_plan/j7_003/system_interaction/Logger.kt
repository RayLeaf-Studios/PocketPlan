package com.pocket_plan.j7_003.system_interaction

import android.content.Context
import java.io.File
import java.util.*

class Logger(context: Context) {
    private val logFile: File

    init {
        logFile = setStorageLocation(context)
    }

    fun log(location: String, msg: String) {
        logFile.appendText("[${Calendar.getInstance().time}], \\$location,\t$msg\n")
    }

    fun deleteFile() {
        if (logFile.exists()) {
            logFile.delete()
        }
    }

    private fun setStorageLocation(context: Context): File = File(context.filesDir, "Log.txt")
}