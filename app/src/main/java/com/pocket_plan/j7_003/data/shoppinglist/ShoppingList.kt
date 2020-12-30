package com.pocket_plan.j7_003.data.shoppinglist

import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import java.lang.Exception
import kotlin.collections.ArrayList

class ShoppingList : ArrayList<Pair<String, ArrayList<ShoppingItem>>>() {
    init {
        StorageHandler.createJsonFile(StorageId.SHOPPING)
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

                //Added this to sort after adding element
                sortSublist(e.second)
                sortCategoriesByChecked(e.first)

                save()
                return
            }
        }

        /**
         * creating the new sublist for the given tag, beginning with an marker
         *
         * decide if the new category should start as collapsed or expanded, depending on
         * the setting COLLAPSE_CHECKED_SUBLISTS
         */

        var sublistExpanded = true

        if (SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean && somethingIsExpanded()) {
            sublistExpanded = false
        }

        super.add(Pair(element.tag, arrayListOf(ShoppingItem(element.tag, sublistExpanded))))

        this.forEach { e ->         // searching the newly added sublist and adding the element
            if (e.first == element.tag) {   // add element to tags sublist and save to file
                e.second.add(element)

                sortSublist(e.second)
                sortCategoriesByChecked(e.first)

                save()
            }
        }
    }

    fun expandAllTags() {
        this.forEach { e ->
            e.second[0].checked = true
        }
        save()
    }

    fun collapseAllTags() {
        this.forEach { e ->
            e.second[0].checked = false
        }
        save()
    }

    fun somethingIsExpanded(): Boolean {
        this.forEach { e ->
            if (e.second[0].checked) {
                return true
            }
        }
        return false
    }

    fun somethingsCollapsed(): Boolean {
        this.forEach { e ->
            if (!e.second[0].checked) {
                return true
            }
        }
        return false
    }

    /**
     * Checks whether all items of the list are unchecked, unrelated to their tag.
     * @return  `true` when no items are checked, `false` otherwise
     */
    fun somethingIsChecked(): Boolean {
        this.forEach { e ->     // go through this list
            e.second.forEach { a ->     // go through the sublist
                if (e.second.indexOf(a) != 0) {     // checks if the current item is a marker
                    if (a.checked) {            // check if the current item is checked
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * This method checks if the given sublist should be expanded.
     * @param tag The tag of the requested sublist.
     */
    fun isTagExpanded(tag: String): Boolean {
        return try {
            this[getTagIndex(tag)].second[0].checked
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Flips the current expansion state markers checked boolean.
     * @param tag The tag of the sublist to flip.
     * @return True if the expansion state markers checked boolean is flipped, false otherwise.
     */
    fun flipExpansionState(tag: String): Boolean? {
        return try {
            val newState = !this[getTagIndex(tag)].second[0].checked
            this[getTagIndex(tag)].second[0].checked = newState
            save()
            newState
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Flips the checked boolean of the requested item.
     * @param tag The tag of the sublist.
     * @param sublistPosition The position of the requested item.
     * @return The new index of the requested item, -1 if the flipping process fails
     */
    fun flipItemCheckedState(tag: String, sublistPosition: Int): Int {
        return try {
            val itemCache: ShoppingItem = this[getTagIndex(tag)].second[sublistPosition + 1]
            itemCache.checked = !itemCache.checked
            sortSublist(this[getTagIndex(tag)].second)
            save()
            this[getTagIndex(tag)].second.indexOf(itemCache) - 1
        } catch (e: Exception) {
            -1
        }
    }

    /**
     * Unchecks all items currently present in the given sublists.
     */
    fun uncheckAll() {
        this.forEach { e ->
            e.second.forEach { s ->
                if (e.second.indexOf(s) != 0) {
                    s.checked = false
                }
            }
        }
        save()
    }

    /**
     * Tries to fetch the length of the sublist with given tag if the sublist exists.
     * @param tag The tag the sublist is supposed to have.
     * @return Either the size of the list or zero if it doesn't exist.
     */
    fun getSublistLength(tag: String): Int {
        this.forEach {
            if (it.first == tag) {
                return it.second.size - 1
            }
        }
        return 0
    }

    /**
     * Retrieves the amount of unchecked items of a given sublist.
     * @param tag The tag of the sublist which will be searched.
     * @return The amount of unchecked items in the given tags sublist.
     */
    fun getUncheckedSize(tag: String): Int {
        var counter = 0
        for (i in 1 until this[getTagIndex(tag)].second.size) {
            if (!this[getTagIndex(tag)].second[i].checked) {
                counter++
            }
        }
        return counter
    }

    /**
     * Retrieves an item from a given tags sublist by position without deleting it.
     * @param tag The tag of the needed sublist.
     * @param subPosition The position of the requested item inside the sublist.
     * @return If the item could be found it is returned, null otherwise.
     */
    fun getItem(tag: String, subPosition: Int): ShoppingItem? {
        this.forEach {
            if (it.first == tag) {
                return it.second[subPosition + 1]
            }

        }
        return null
    }

    /**
     * Returns whether all items are checked or not, in the given tags list.
     * @param tag   The tag to search.
     * @return      `true` if all items are checked in the sublist, `false` otherwise.
     */
    fun areAllChecked(tag: String): Boolean {
        return try {
            getUncheckedSize(tag) == 0
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Returns the index of a given tag inside this list.
     * @param tag   The tag, of the returned index.
     * @return      Returns the index of the given tag.
     */
    fun getTagIndex(tag: String): Int {
        for (i in 0 until this.size) {
            if (this[i].first == tag) {
                return i
            }
        }

        return -1
    }

    /**
     * Tries to remove an item from the list, if there are no items left in the given category,
     * the whole sublist is removed. Depending on the outcome of the removal either the removed
     * item is returned or null.
     * @param tag The tag of the sublist.
     * @param sublistPosition The position of the item inside the sublist.
     * @return  The removed item is returned if the removal succeeded, null otherwise.
     *          also a boolean is returned, stating if the containing sublist was deleted or not.
     */
    fun removeItem(tag: String, sublistPosition: Int): Pair<ShoppingItem?, Boolean> {
        var removedItem: ShoppingItem? = null
        var sublistGotDeleted = false


        return try {    // trying to remove the item, save the list and return the removed element
            for (i in 0 until this.size) {

                if (this[i].first == tag) {
                    removedItem = this[i].second.removeAt(sublistPosition + 1)
                    if (this[i].second.size == 1) {   // removing the sublist if it is empty
                        super.remove(this[i])
                        sublistGotDeleted = true
                    }

                    break
                }
            }

            save()
            Pair(removedItem, sublistGotDeleted)
        } catch (e: NullPointerException) {
            Pair(null, sublistGotDeleted)
        }
    }

    /**
     * Sorts the given list. Sublists where all items checked are sorted below.
     * If sorting the list succeeds a pair is returned containing the prior and new
     * position of the sorted tag.
     * @param tag The tag which should get sorted.
     * @return A pair with the old and new position of the given tag, null if sorting fails.
     */
    fun sortCategoriesByChecked(tag: String): Pair<Int, Int>? {
        val oldPosition = getTagIndex(tag)
        this.sortBy { areAllChecked(it.first) }
        save()
        val returnPair = Pair(oldPosition, getTagIndex(tag))

        return if (returnPair.first == returnPair.second) {
            null
        } else {
            returnPair
        }
    }

    private fun sortSublist(list: ArrayList<ShoppingItem>) {
        val markerList: ArrayList<ShoppingItem> = arrayListOf(list[0])
        list.remove(markerList[0])
        list.sortWith(compareBy({ it.checked }, { it.name }))
        markerList.addAll(list)
        list.clear()
        list.addAll(markerList)
    }

    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.SHOPPING], this
        )
    }

    private fun fetchList() {
        val jsonString = StorageHandler.files[StorageId.SHOPPING]?.readText()

        this.addAll(
            GsonBuilder().create()
                .fromJson(
                    jsonString,
                    object : TypeToken<ArrayList<Pair<String, ArrayList<ShoppingItem>>>>() {}.type
                )
        )
    }
}