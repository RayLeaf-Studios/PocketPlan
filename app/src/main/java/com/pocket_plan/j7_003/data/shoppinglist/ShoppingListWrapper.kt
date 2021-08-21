package com.pocket_plan.j7_003.data.shoppinglist

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId

/**
 * A simple wrapper for shopping lists to easily manage multiple instances of them.
 */
class ShoppingListWrapper(defaultListName: String = ""): ArrayList<Pair<String, ShoppingList>>() {
    init {
        StorageHandler.createJsonFile(StorageId.SHOPPING_LISTS)
        fetchList()
        if (this.size == 0)
            this.add(defaultListName, ShoppingList(this))
    }

    /**
     * Returns the shopping list with the given name.
     * @param name The name of the searched list
     * @return Returns the requested shopping list
     */
    fun getListByName(name: String): ShoppingList? {
        this.forEach {
            if (it.first == name)
                return it.second
        }
        return null
    }

    /**
     * Adds a new shopping list with the given name.
     * @param name The name the new will be identified with
     * @return A boolean depending on the success of the addition of the new list
     */
    fun add(name: String): Boolean = this.add(name, ShoppingList(this))

    /**
     * Adds a new pair of given name and shopping list to the wrapper.
     * @param name The name the shopping list will be identified with
     * @param list The new list added to the wrapper
     * @return A boolean depending on the success of the addition of the new list
     */
    fun add(name: String, list: ShoppingList): Boolean {
        if (contains(name)) {
            return false
        }

        val success = this.add(Pair(name, list))
        save()
        return success
    }

    /**
     * Removes the identifying pair described by the given name from the wrapper.
     * @param name The string the list is identified with
     * @return A boolean depending on the success of the removal
     */
    fun remove(name: String): Boolean {
        if (this.size == 1)
            return false

        var toDelete: Pair<String, ShoppingList>? = null
        this.forEach {
            if (it.first == name)
                toDelete = it
        }

        val success = this.remove(toDelete)
        save()

        return success
    }

    fun rename(oldName: String, newName: String): Boolean {
        var index = -1
        var list = ShoppingList()

        this.forEach {
            if (it.first == oldName) {
                index = indexOf(it)
                list = it.second
                return@forEach
            }
        }
        if (index == -1)
            return false

        this[index] = this[index].copy(newName, list)
        save()
        return true
    }

    /**
     * Checks if the wrapper contains a list with the given name.
     * @param name The name the searched list is supposed to be identified with
     * @return True if a list with the given name is found, false otherwise
     */
    fun contains(name: String): Boolean {
        this.forEach {
            if (it.first == name)
                return true
        }
        return false
    }

    private fun fetchList() {
        val jsonString = StorageHandler.files[StorageId.SHOPPING_LISTS]?.readText()
        val list: ArrayList<Pair<String, ShoppingList>> = GsonBuilder().create().fromJson(
                jsonString,
                object : TypeToken<ArrayList<Pair<String, ShoppingList>>>() {}.type
            )
        list.forEach{
            it.second.setWrapper(this)
        }
        this.addAll(list)
    }

    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.SHOPPING_LISTS],
            this
        )
    }
}