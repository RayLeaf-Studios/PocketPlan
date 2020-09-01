package com.example.j7_003.data.database

import com.example.j7_003.data.database.database_objects.ShoppingItem
import com.example.j7_003.data.database.database_objects.Tag
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.NullPointerException
import kotlin.collections.ArrayList

class ShoppingList : ArrayList<Pair<Tag, ArrayList<ShoppingItem>>>() {
    init {
        StorageHandler.createJsonFile(
            "SHOPPING_LIST",
            "ShoppingList.json"
        )

        fetchList()
    }

    /**
     * Adds a given ShoppingElement to this list, according to its given tag.
     * If no element of the given tag existed before, the list generate a new sublist,
     * beginning with a marker element, to mark the expansion state of the given sublist.
     * After that the given element is added to the newly created sublist and the list is sorted.
     * @param element The element to be added to the list.
     */
    fun add(element: ShoppingItem) {
        this.forEach { e ->         // searching for preexistence of the elements tag
            if (e.first == element.tag) {   // add element to tags sublist and save to file
                e.second.add(element)
                save()
            }
        }

        // TODO erstellen eigener kategorie oder in sonstige packen?
        // aktuell wird eigene kategorie erstellt

        // creating the new sublist for the given tag, beginning with an marker
        super.add(Pair(element.tag, arrayListOf(ShoppingItem(element.tag, false))))

        this.forEach { e ->         // searching the newly added sublist and adding the element
            if (e.first == element.tag) {   // add element to tags sublist and save to file
                e.second.add(element)
                save()
            }
        }

        // TODO sort the list as needed
    }

    /**
     * This method checks if the given sublist should expanded, if the given sublist doesn't
     * exist an exception is thrown.
     * @param tag The tag to be checked.
     * @throws NullPointerException If there is no such tag inside the list.
     */
    fun isTagExpanded(position: Int): Boolean {      // TODO durch tag oder position suchen?

       return try{
           this[position].second[0].checked
       }catch(e: NullPointerException){
           false
       }

    }

    fun flipExpansionState(position: Int): Boolean {
        return try{
            this[position].second[0].checked = !this[position].second[0].checked
            true
        }catch(e: NullPointerException){
            false
        }
    }


    //TODO optimize folling two functions
    fun getSublistLength(tag: Tag): Int{
        this.forEach {
            if(it.first == tag){
                return it.second.size - 1
            }
        }
        return 0
    }
    fun getItem(tag: Tag, subPosition: Int): ShoppingItem? {
        this.forEach{
            if(it.first == tag){
                return it.second[subPosition+1]
            }

        }
        return null
    }

    /**
     * Tries to remove an item from the list, if there are no items left in the given category,
     * the whole sublist is removed. Depending on the outcome of the removal either the removed
     * item is returned or null.
     * @param tagPosition The position of the sublist.
     * @param sublistPosition The position of the item inside the sublist.
     * @return The removed item is returned if the removal succeeded, null otherwise.
     */
    fun removeItem(tagPosition: Int, sublistPosition: Int): ShoppingItem? {
        return try {    // trying to remove the item, save the list and return the removed element
            val removedItem = this[tagPosition].second.removeAt(sublistPosition + 1)
            if (this[tagPosition].second.isEmpty()) {   // removing the sublist if it is empty
                super.removeAt(tagPosition)
            }
            save()
            removedItem
        } catch (e: NullPointerException) {
            null
        }
    }

    private fun sort() {
        TODO()
    }

    private fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files["SHOPPING_LIST"],
            this
        )
    }

    private fun fetchList() {
        val jsonString = StorageHandler.files["SHOPPING_LIST"]?.readText()

        this.addAll(GsonBuilder().create()
            .fromJson(jsonString,
                object : TypeToken<ArrayList<Pair<Tag, ArrayList<ShoppingItem>>>>() {}.type))
    }
}