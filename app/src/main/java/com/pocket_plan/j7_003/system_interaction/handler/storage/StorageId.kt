package com.pocket_plan.j7_003.system_interaction.handler.storage

enum class StorageId(val s: String, val i: Int) {
    SHOPPING("ShoppingList.json", 8),
    NOTES("NoteList.json", 1),
    BIRTHDAYS("BirthdayList.json", 2),
    TASKS("TodoList.json", 3),
    SETTINGS("Settings.json", 4),
    USER_TEMPLATE_LIST("UserShoppingItems.json", 5),
    SLEEP("SleepReminder.json", 6),
    // code 7 is reserved for backups and should not be used
    SHOPPING_LISTS("ShoppingLists.json", 0),
    SHOPPING_SYNC("ShoppingListSync.json", 9)
    ;

    companion object {
        fun getByI(i: Int): StorageId? {
            entries.forEach { id ->
                if (id.i == i) {
                    return id
                }
            }
            return null
        }
    }
}
