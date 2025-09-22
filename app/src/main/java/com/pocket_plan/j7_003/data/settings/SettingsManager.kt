package com.pocket_plan.j7_003.data.settings

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId

class SettingsManager {
    companion object : Checkable {
        var settings = HashMap<String, Any>()

        fun init() {
            createFile()
            try {
                load()
            } catch (_: Exception) {
                StorageHandler.files[StorageId.SETTINGS]?.writeText("[]")
            }
        }

        fun getSetting(setting: SettingId): Any? {
            return if (settings.containsKey(setting.name)) {
                settings[setting.name]
            } else setting.default
        }

        fun addSetting(id: SettingId, any: Any) {
            settings[id.name] = any
            save()
        }

        fun restoreDefault() {
            SettingId.entries.forEach { setId -> settings[setId.name] = setId.default }
            save()
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[StorageId.SETTINGS], settings
            )
        }

        private fun load() {
            val jsonString = StorageHandler.files[StorageId.SETTINGS]?.readText()

            val cacheMap: HashMap<String, Any> = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<HashMap<String, Any>>() {}.type)

            cacheMap.forEach { (settingId, value) ->
                try {
                    SettingId.valueOf(settingId)
                    settings[settingId] = value
                } catch (_: Exception) { /* no-op */ }
            }
        }

        private fun createFile() {
            StorageHandler.createJsonFile(StorageId.SETTINGS)
        }

        override fun check() {
            settings.forEach {
                if (it.key == null || it.value == null) {
                    throw NullPointerException()
                }
            }
        }

    }


}