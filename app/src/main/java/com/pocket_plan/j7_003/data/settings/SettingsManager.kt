package com.pocket_plan.j7_003.data.settings

import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.MainActivity

class SettingsManager {
    companion object {
        var settings = HashMap<SettingId, Any>()

        fun init() {
            createFile()
            load()
        }

        fun getSetting(id: SettingId): Any? {
            return if(settings.containsKey(id)){
                settings[id]
            } else null
        }

        fun addSetting(id: SettingId, any: Any) {
            settings[id] = any
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
                .fromJson(jsonString, object : TypeToken<HashMap<SettingId, Any>>() {}.type)
        }

        private fun createFile() {
            StorageHandler.createJsonFile(StorageId.SETTINGS)
        }
    }
}