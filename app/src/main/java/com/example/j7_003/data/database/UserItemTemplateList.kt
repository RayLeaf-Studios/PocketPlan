package com.example.j7_003.data.database

import com.example.j7_003.data.database.database_objects.ShoppingItem
import com.example.j7_003.data.database.database_objects.Tag
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.NullPointerException

class UserItemTemplateList: ArrayList<ItemTemplate>() {
    init {
        StorageHandler.createJsonFile(
            "USER_ITEM_TEMPLATES",
            "UserItemTemplates.json"
        )

        fetchList()
    }

    /**
     * Adds a new ItemTemplate made by the user and saves this list to file.
     * @param element The element to added to this list.
     * @return Returns true if the element could be added and saved, false if it didn't.
     */
    override fun add(element: ItemTemplate): Boolean {
        val boolean = super.add(element)
        save()
        return boolean
    }

    /**
     * Edits an element if it could be found by its name. Only the name is required to edit, while
     * tags and units can be edited too, they don't need to.
     * @param name The name of the item.
     * @param tag The tag that can be supplied.
     * @param unit The unit that can be supplied.
     */
    fun edit(name: String, tag: Tag = Tag("", ""), unit: String = "") {
        val index = getIndexByName(name)
        if (index != -1) {
            this[index].n = name

            if (tag.c.isNotEmpty()) {
                this[index].c = tag
            }

            if (unit.isNotEmpty()) {
                this[index].s = unit
            }

            save()
            return
        }

        throw NullPointerException("there is no such item")
    }

    /**
     * Removes an item from this list and returns it to the caller, if there is
     * no item with the supplied name null is returned.
     * @param itemName The name of item.
     * @return The item that was removed, null if there was no item with the given name.
     */
    fun remove(itemName: String): ItemTemplate? {
        for (i in 0 until this.size) {
            if (this[i].n == itemName) {
                save()
                return super.removeAt(i)
            }
        }

        return null
    }

    private fun getIndexByName(name: String): Int {
        this.forEach { e ->
            if (e.n == name) {
                return indexOf(e)
            }
        }

        return -1
    }

    private fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files["USER_ITEM_TEMPLATES"],
            this
        )
    }

    private fun fetchList() {
        val jsonString = StorageHandler.files["USER_ITEM_TEMPLATES"]?.readText()

        this.addAll(
            GsonBuilder().create()
            .fromJson(jsonString,
                object : TypeToken<ArrayList<Pair<Tag, ArrayList<ShoppingItem>>>>() {}.type))
    }
}