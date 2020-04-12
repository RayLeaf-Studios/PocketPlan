package com.example.j7_003.data.settings

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.j7_003.data.StorageHandler
import com.google.gson.Gson
import java.io.File

class SettingsManager(private val storageHandler: StorageHandler) {
    val settings = HashMap<String, String>()
    private val SETTINGS = "SETTINGS"

    init {
        createFile()
        saveSettings()
    }

    fun saveSettings() {
        storageHandler.saveToFile(StorageHandler.files[SETTINGS], settings)
    }

    private fun createFile() {
        storageHandler.addCollToFiles(SETTINGS, "Settings.json")
    }
}