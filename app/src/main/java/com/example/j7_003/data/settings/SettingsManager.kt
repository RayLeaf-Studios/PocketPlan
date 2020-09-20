package com.example.j7_003.data.settings

import com.example.j7_003.system_interaction.handler.StorageHandler
import com.example.j7_003.system_interaction.handler.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class SettingsManager {
    companion object {
        var settings = HashMap<String, Any>()

        fun init() {
            createFile()
            load()
        }

        fun getSetting(name: String): Any? {
            return if(settings.containsKey(name)){
                settings[name]
            } else null
        }

        fun addSetting(name: String, any: Any) {
            settings[name] = any
            save()
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[StorageId.SETTINGS], settings
            )
        }

        private fun load() {
            val jsonString = StorageHandler.files[StorageId.SETTINGS]?.readText()

            settings = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<HashMap<String, Any>>() {}.type)
        }

        private fun createFile() {
            StorageHandler.createJsonFile(StorageId.SETTINGS, "Settings.json")
        }
    }
}