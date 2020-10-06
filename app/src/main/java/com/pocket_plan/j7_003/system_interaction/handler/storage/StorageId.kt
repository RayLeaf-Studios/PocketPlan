package com.pocket_plan.j7_003.system_interaction.handler.storage

enum class StorageId(val s: String, val i: Int) {
    SETTINGS("Settings.json", 4), BIRTHDAYS("BirthdayList.json", 2), NOTES("NoteList.json", 1),
    SLEEP("SleepReminder.json", 6), SHOPPING("ShoppingList.json", 0), USER_TEMPLATE_LIST("UserShoppingItems.json", 5),
    TASKS("TodoList.json", 3), ZIP("backup.zip", 7)
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
