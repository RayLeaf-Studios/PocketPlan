package com.pocket_plan.j7_003.data.shoppinglist

import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import java.lang.NullPointerException
import kotlin.collections.ArrayList

class UserItemTemplateList: ArrayList<ItemTemplate>(), Checkable {
    init {
        StorageHandler.createJsonFile(StorageId.USER_TEMPLATE_LIST)
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
     * Removes an item from this list and returns it to the caller, if there is
     * no item with the supplied name null is returned.
     * @param itemName The name of item.
     * @return The item that was removed, null if there was no item with the given name.
     */
    fun removeItem(itemName: String): ItemTemplate? {
        for (i in 0 until this.size) {
            if (this[i].n == itemName) {
                val item = super.removeAt(i)
                save()
                return item
            }
        }
        return null
    }

    fun getTemplateByName(name: String): ItemTemplate? {
        this.forEach { e ->
            if (e.n.equals(name, ignoreCase = true)) {
                return e
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

    fun save() {
        val list = ArrayList<TMPTemplate>()
        this.forEach { e ->
            list.add(TMPTemplate(e.n, e.c, e.s))
        }

        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.USER_TEMPLATE_LIST], list)
    }

    private fun fetchList() {
        val list = ArrayList<TMPTemplate>()
        val jsonString = StorageHandler.files[StorageId.USER_TEMPLATE_LIST]?.readText()

        list.addAll(
            GsonBuilder().create()
            .fromJson(jsonString,
                object : TypeToken<ArrayList<TMPTemplate>>() {}.type))

        list.forEach { e ->
            this.add(ItemTemplate(e.n, e.c, e.s))
        }
    }

    private class TMPTemplate(val n: String, val c: String, val s: String)

    override fun check() {
        this.forEach {
            if(it == null){
                throw NullPointerException()
            }
        }
    }

}