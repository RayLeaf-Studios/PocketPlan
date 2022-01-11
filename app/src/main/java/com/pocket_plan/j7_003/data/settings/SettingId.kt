package com.pocket_plan.j7_003.data.settings

import java.util.*

enum class SettingId(val default: Any) {
    EXPAND_ONE_CATEGORY(false),
    COLLAPSE_CHECKED_SUBLISTS(true),
    FONT_SIZE("18"),
    NOTE_COLUMNS("2"),
    NOTE_LINES(10.0),
    DAYS_ARE_CUSTOM(false),
    CLOSE_ITEM_DIALOG(false),
    MOVE_CHECKED_DOWN(true),
    THEME_DARK(false),
    SHAPES_ROUND(true),
    SHAKE_TASK_HOME(true),
    NOTES_SWIPE_DELETE(false),
    USE_SYSTEM_THEME(true),
    LANGUAGE (when (Locale.getDefault().displayLanguage) {
       Locale.GERMAN.displayLanguage -> 1.0
       else -> 0.0
    }),
    BIRTHDAY_SHOW_MONTH(true),
    BIRTHDAY_COLORS_SOUTH(false),
    SUGGEST_SIMILAR_ITEMS(true),
    PREVIEW_BIRTHDAY(true),
    BIRTHDAY_NOTIFICATION_TIME("12:00")
}