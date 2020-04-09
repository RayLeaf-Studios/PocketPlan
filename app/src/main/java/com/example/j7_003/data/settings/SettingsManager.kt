package com.example.j7_003.data.settings

import android.content.Context
import android.os.Build
import android.os.Environment
import com.google.gson.Gson
import java.io.File

class SettingsManager(context: Context) {
    val settings = HashMap<String, String>()
    private val settingsFile = "settings.txt".setStorageLocation(context)

    init {
        createFile()
        saveSettingsToFile()
    }

    fun saveSettingsToFile() {
        settingsFile.writeText(Gson().toJson(settings))
    }

    private fun createFile() {
        if (!settingsFile.exists()) settingsFile.writeText("{}")
    }

    private fun String.setStorageLocation(context: Context): File {
        return if (Build.VERSION.SDK_INT < 29) {
            File("${Environment.getDataDirectory()}/data/com.example.j7_003",
                this
            )
        } else {
            File(context.filesDir, this)
        }
    }
}