package com.pocket_plan.j7_003.data.settings

import androidx.datastore.preferences.core.Preferences
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

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

        @Deprecated(
            message = "Will be removed to move persistence to widely supported libraries and apis. Settings should be persist with the new PreferencesHandler.",
            level = DeprecationLevel.ERROR
        )
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
                } catch (_: Exception) { /* no-op */
                }
            }
        }

        fun migrateToPreferences() {

            val migrated = runBlocking {
                preferencesHandler
                    .read(PreferencesHandler.SETTINGS_MIGRATION_DONE)
                    .first()
            }

            if (migrated) return

            SettingId.entries.forEach { settingId ->
                val settingValue = settings[settingId.name] ?: return@forEach

                // no need to migrate default values
                if (settingValue == settingId.default) return@forEach

                // special case to migrate note columns from string to int
                if (settingId == SettingId.NOTE_COLUMNS || settingId == SettingId.FONT_SIZE) {
                    runBlocking {
                        when (settingId) {
                            SettingId.FONT_SIZE -> preferencesHandler.save(
                                PreferencesHandler.FONT_SIZE,
                                (settingValue as String).toInt()
                            )

                            SettingId.NOTE_COLUMNS -> preferencesHandler.save(
                                PreferencesHandler.NOTE_COLUMNS,
                                (settingValue as String).toInt()
                            )

                            else -> Unit
                        }
                    }

                    return@forEach
                }

                runBlocking {
                    when (settingId.default) {
                        is Boolean -> {
                            val key =
                                getPropertyByName<Preferences.Key<Boolean>>(settingId.name)
                            preferencesHandler.save(key, settingValue as Boolean)
                        }

                        is String -> {
                            val key = getPropertyByName<Preferences.Key<String>>(settingId.name)
                            preferencesHandler.save(key, settingValue as String)
                        }

                        is Int -> {
                            val key = getPropertyByName<Preferences.Key<Int>>(settingId.name)
                            preferencesHandler.save(key, settingValue as Int)
                        }

                        is Double -> {
                            val key = getPropertyByName<Preferences.Key<Double>>(settingId.name)
                            preferencesHandler.save(key, settingValue as Double)
                        }
                    }
                }

                runBlocking {
                    preferencesHandler.save(PreferencesHandler.SETTINGS_MIGRATION_DONE, true)
                }
            }
        }

        inline fun <reified T> getPropertyByName(propertyName: String): T {
            val prop =
                PreferencesHandler.Companion::class.memberProperties.find { it.name == propertyName }
                    ?: throw IllegalArgumentException()

            // Check if property is public
            if (prop.visibility != kotlin.reflect.KVisibility.PUBLIC) {
                throw IllegalArgumentException()
            }

            prop.isAccessible = true
            val value = prop.get(PreferencesHandler.Companion)

            // Check if the value is of the expected type
            return if (value is T) {
                value
            } else {
                throw IllegalArgumentException()
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