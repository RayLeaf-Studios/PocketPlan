package com.example.j7_003.data.settings

import com.example.j7_003.system_interaction.handler.StorageHandler

class SettingsManager {
    val settings = HashMap<String, String>()
    private val fileName = "SETTINGS"

    init {
        createFile()
        saveSettings()
    }

    fun saveSettings() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[fileName], settings)
    }

    private fun createFile() {
        StorageHandler.createJsonFile(fileName, "Settings.json")
    }
}