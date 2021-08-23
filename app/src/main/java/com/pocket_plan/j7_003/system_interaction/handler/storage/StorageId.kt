package com.pocket_plan.j7_003.system_interaction.handler.storage

enum class StorageId(val s: String, val i: Int) {
    SHOPPING("ShoppingList.json", 8),
    NOTES("NoteList.json", 1),
    BIRTHDAYS("BirthdayList.json", 2),
    TASKS("TodoList.json", 3),
    SETTINGS("Settings.json", 4),
    USER_TEMPLATE_LIST("UserShoppingItems.json", 5),
    SLEEP("SleepReminder.json", 6),
    ZIP("backup.zip", 7),
    SHOPPING_LISTS("ShoppingLists.json", 0)
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
