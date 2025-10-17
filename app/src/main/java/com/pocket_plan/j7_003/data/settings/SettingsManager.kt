package com.pocket_plan.j7_003.data.settings

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SettingsManager {
    companion object : KoinComponent, Checkable {
        private val preferencesHandler: PreferencesHandler by inject()

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

        @Deprecated(
            message = "Will be removed to move persistence to widely supported libraries and apis. Settings should be persist with the new PreferencesHandler.",
            level = DeprecationLevel.ERROR
        )
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

        private fun migrateToPreferences() {
            val migrated = runBlocking(Dispatchers.IO) {
                preferencesHandler
                    .read(PreferencesHandler.SETTINGS_MIGRATION_DONE)
                    .first()
            }

            if (migrated) return

            SettingId.entries.forEach { settingId ->
                val settingValue = getSetting(settingId) ?: return@forEach

                if (settingValue == settingId.default) return@forEach

//                preferencesHandler.save()
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