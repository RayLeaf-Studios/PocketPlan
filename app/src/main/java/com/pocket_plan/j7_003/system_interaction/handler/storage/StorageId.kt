package com.pocket_plan.j7_003.system_interaction.handler.storage

enum class StorageId(val s: String, val i: Int) {
    SETTINGS("Settings.json", 0), BIRTHDAYS("BirthdayList.json", 1), NOTES("NoteList.json", 2),
    SLEEP("SReminder.json", 3), SHOPPING("ShoppingList.json", 4), USER_TEMPLATE_LIST("UserItemTemplates.json", 5),
    TASKS("TaskList.json", 6), ZIP("backup.zip", 7)
    /*, CALENDAR("Calendar.json"), -> V.2, WEEK("WeekList.json"), -> V.2*/
    ;

    companion object {
        fun getByI(i: Int): StorageId? {
            values().forEach { id ->
                if (id.i == i) {
                    return id
                }
            }
            return null
        }
    }
}
