package com.pocket_plan.j7_003.system_interaction.handler.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.pocket_plan.j7_003.data.settings.Languages
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import java.util.Locale

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private fun getDefaultLanguageIndex(): Double {
    when {
        Locale.getDefault().language.startsWith(Languages.ROMANIAN.code) -> Languages.ROMANIAN.index
        Locale.getDefault().language.startsWith(Languages.ITALIAN.code) -> Languages.ITALIAN.index
        Locale.getDefault().language.startsWith(Languages.RUSSIAN.code) -> Languages.RUSSIAN.index
        Locale.getDefault().language.startsWith(Languages.SPANISH.code) -> Languages.SPANISH.index
        Locale.getDefault().language.startsWith(Languages.FRENCH.code) -> Languages.FRENCH.index
        Locale.getDefault().language.startsWith(Languages.GERMAN.code) -> Languages.GERMAN.index
        else -> Languages.ENGLISH.index
    }

    return 0.0
}

private infix fun <T> Preferences.Key<T>.withValue(value: T): Pair<Preferences.Key<*>, *> {
    return Pair(this, value!!)
}


@Single
class PreferencesHandler(private val context: Context) {

    suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.dataStore.edit { preferences ->
            preferences[key] = value
        }
    }

    fun <T> read(key: Preferences.Key<T>): Flow<T> {
        return context.dataStore.data.map { preferences ->
            @Suppress("UNCHECKED_CAST") // we don't need to check the cast because the map values ensure matching types
            preferences[key] ?: defaults[key] as T
        }
    }

    companion object {
        val SHOPPING_MIGRATION_DONE = booleanPreferencesKey("shopping_migration_done")
        val SETTINGS_MIGRATION_DONE = booleanPreferencesKey("settings_migration_done")
        val EXPAND_ONE_CATEGORY = booleanPreferencesKey("expand_one_category")
        val COLLAPSE_CHECKED_SUBLISTS = booleanPreferencesKey("collapse_checked_sublists")
        val FONT_SIZE = intPreferencesKey("font_size")
        val NOTE_COLUMNS = intPreferencesKey("note_columns")
        val NOTE_LINES = doublePreferencesKey("note_lines")
        val DAYS_ARE_CUSTOM = booleanPreferencesKey("days_are_custom")
        val CLOSE_ITEM_DIALOG = booleanPreferencesKey("close_item_dialog")
        val MOVE_CHECKED_DOWN = booleanPreferencesKey("move_checked_down")
        val THEME_DARK = booleanPreferencesKey("theme_dark")
        val DARK_BORDER_STYLE = doublePreferencesKey("dark_border_style")
        val SHAPES_ROUND = booleanPreferencesKey("shapes_round")
        val SHAKE_TASK_HOME = booleanPreferencesKey("shake_task_home")
        val NOTES_SWIPE_DELETE = booleanPreferencesKey("notes_swipe_delete")
        val USE_SYSTEM_THEME = booleanPreferencesKey("use_system_theme")
        val LANGUAGE = doublePreferencesKey("language")
        val BIRTHDAY_SHOW_MONTH = booleanPreferencesKey("birthday_show_month")
        val BIRTHDAY_COLORS_SOUTH = booleanPreferencesKey("birthday_colors_south")
        val SUGGEST_SIMILAR_ITEMS = booleanPreferencesKey("suggest_similar_items")
        val PREVIEW_BIRTHDAY = booleanPreferencesKey("preview_birthday")
        val BIRTHDAY_NOTIFICATION_TIME = stringPreferencesKey("birthday_notification_time")
        val RANDOMIZE_NOTE_COLORS = booleanPreferencesKey("randomize_note_colors")
        val LAST_USED_NOTE_COLOR = doublePreferencesKey("last_used_note_color")
        val NOTES_SHOW_CONTAINED = booleanPreferencesKey("notes_show_contained")
        val NOTES_MOVE_UP_CURRENT = booleanPreferencesKey("notes_move_up_current")
        val NOTES_ARCHIVE = booleanPreferencesKey("notes_archive")
        val NOTES_FIXED_SIZE = booleanPreferencesKey("notes_fixed_size")
        val NOTES_DIRS_TO_TOP = booleanPreferencesKey("notes_dirs_to_top")
        val SYNC_SERVER_URL = stringPreferencesKey("sync_server_url")

        private val defaults: Map<Preferences.Key<*>, *> = mapOf(
            SHOPPING_MIGRATION_DONE withValue false,
            SETTINGS_MIGRATION_DONE withValue false,

            EXPAND_ONE_CATEGORY withValue false,
            COLLAPSE_CHECKED_SUBLISTS withValue true,
            FONT_SIZE withValue 18,
            NOTE_COLUMNS withValue 2,
            NOTE_LINES withValue 10.0,
            DAYS_ARE_CUSTOM withValue false,
            CLOSE_ITEM_DIALOG withValue false,
            MOVE_CHECKED_DOWN withValue true,
            THEME_DARK withValue false,
            DARK_BORDER_STYLE withValue 2.0,
            SHAPES_ROUND withValue true,
            SHAKE_TASK_HOME withValue true,
            NOTES_SWIPE_DELETE withValue false,
            USE_SYSTEM_THEME withValue true,
            LANGUAGE withValue getDefaultLanguageIndex(),
            BIRTHDAY_SHOW_MONTH withValue true,
            BIRTHDAY_COLORS_SOUTH withValue false,
            SUGGEST_SIMILAR_ITEMS withValue true,
            PREVIEW_BIRTHDAY withValue true,
            BIRTHDAY_NOTIFICATION_TIME withValue "12:00",
            RANDOMIZE_NOTE_COLORS withValue true,
            LAST_USED_NOTE_COLOR withValue 0.0,
            NOTES_SHOW_CONTAINED withValue true,
            NOTES_MOVE_UP_CURRENT withValue false,
            NOTES_ARCHIVE withValue true,
            NOTES_FIXED_SIZE withValue true,
            NOTES_DIRS_TO_TOP withValue true,
            SYNC_SERVER_URL withValue "http://"
        )
    }
}