package com.pocket_plan.j7_003.system_interaction.handler.storage

enum class StorageId(val s: String) {
    SETTINGS("Settings.json"), BIRTHDAYS("BirthdayList.json"), NOTES("NoteList.json"),
    SLEEP("SReminder.json"), SHOPPING("ShoppingList.json"), CALENDAR("Calendar.json"),
    WEEK("WeekList.json"), USER_TEMPLATE_LIST("UserItemTemplates.json"),
    TASKS("TaskList.json"), ZIP("backup.zip")
}