package com.pocket_plan.j7_003.data.shoppinglist

import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager

class ShoppingList(private var wrapper: ShoppingListWrapper?) :
    ArrayList<Pair<String, ArrayList<ShoppingItem>>>(), Checkable {
    constructor() : this(null)

    fun setWrapper(newWrapper: ShoppingListWrapper) {
        this.wrapper = newWrapper
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
                e.second[0].checked = true

                if (SettingsManager.getSetting(SettingId.EXPAND_ONE_CATEGORY) as Boolean) {
                    this.forEach {
                        if (it != e)
                            it.second[0].checked = false
                    }
                }

                //Added this to sort after adding element
                sortSublist(e.second)
                sortCategoriesByChecked(e.first)

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

        var numUnchecked = 0
        this.forEach {
            if (!areAllChecked(it.first))
                numUnchecked++
        }

        super.add(
            Pair(
                element.tag,
                arrayListOf(ShoppingItem(element.tag, sublistExpanded, numUnchecked.toString()))
            )
        )

        this.forEach { e ->         // searching the newly added sublist and adding the element
            if (e.first == element.tag) {   // add element to tags sublist and save to file
                e.second.add(element)

                sortSublist(e.second)
                sortCategoriesByChecked(e.first)
            }
        }
    }

    fun expandAllTags() {
        this.forEach { e ->
            e.second[0].checked = true
        }
        save()
    }

    /**
     *
     */
    fun equalizeCheckedStates(tag: String): Boolean {
        val newCheckedState: Boolean = !areAllChecked(tag)
        this[getTagIndex(tag)].second.forEachIndexed { index, shoppingItem ->
            if (index != 0) {
                shoppingItem.checked = newCheckedState
            }
        }

        save()
        return newCheckedState
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
     * @return boolean of new expansion state or null if unsuccessful
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
     * Removes all checked items from shopping list
     */
    fun removeCheckedItems() {
        this.forEach { e ->
            e.second.removeAll { item ->
                item.checked && e.second.indexOf(item) != 0
            }
        }
        this.removeAll { e -> e.second.size == 1 }
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
     * @param removeSublist Defaults to true and dictates whether an empty sublist is deleted.
     * @return  The removed item is returned if the removal succeeded, null otherwise.
     *          also a boolean is returned, stating if the containing sublist was deleted or not.
     */
    fun removeItem(
        tag: String,
        sublistPosition: Int,
        removeSublist: Boolean = true
    ): Pair<ShoppingItem?, Boolean> {
        var removedItem: ShoppingItem? = null
        var sublistGotDeleted = false


        return try {    // trying to remove the item, save the list and return the removed element
            for (i in 0 until this.size) {

                if (this[i].first == tag) {
                    removedItem = this[i].second.removeAt(sublistPosition + 1)
                    if (this[i].second.size == 1 && removeSublist) {   // removing the sublist if it is empty
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

    fun updateOrder() {
        var pos = 0
        this.forEach lit@{
            if (areAllChecked(it.first)) return@lit

            it.second[0].amount = pos.toString()
            pos++
        }
    }

    /**
     * Sorts the given list. Sublists where all items checked are sorted below every not fully
     * checked list is sorted according to its position at time of creation.
     * If sorting the list succeeds a pair is returned containing the prior and new
     * position of the sorted tag.
     * @param tag The tag which should get sorted.
     * @return A pair with the old and new position of the given tag, null if sorting fails.
     */
    fun sortCategoriesByChecked(tag: String): Pair<Int, Int>? {
        val oldPosition = getTagIndex(tag)
//        this.sortWith(compareBy( { areAllChecked(it.first) }, { it.second[0].amount!!.toInt() }))
        this.sortBy { areAllChecked(it.first) }

        var numUnchecked = 0
        this.forEach {
            if (!areAllChecked(it.first))
                numUnchecked++
        }
        this.subList(0, numUnchecked).sortBy { it.second[0].amount!!.toInt() }

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
        wrapper?.save()
    }

    override fun toString(): String {
        var result = "--------------------\n"
        this.forEach { cat ->
            result += cat.first + "(" + cat.second[0].amount + ") " + ": {"
            cat.second.forEach {
                result += it.name + " is " + it.checked.toString() + ", "
            }
            result += "\b\b}\n"
        }
        return result
    }

    override fun check() {
        this.forEach {
            if (it.first == null || it.second == null) {
                throw java.lang.NullPointerException()
            }
        }
    }
}