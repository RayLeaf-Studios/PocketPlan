package com.pocket_plan.j7_003.data.birthdaylist

import android.content.Context
import android.util.Log
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.R
import org.threeten.bp.LocalDate
import kotlin.collections.ArrayList

/**
 * A simple handler to manage the interaction of different objects
 * with a similar structure.
 */
class BirthdayList(val context: Context?): ArrayList<Birthday>() {
    constructor() : this(null)

    init {
        StorageHandler.createJsonFile(StorageId.BIRTHDAYS)
        fetchFromFile()
        sortBirthday()
    }

    /**
     * Adds a birthday to the this and saves the this.
     * @param name The name of the created birthday
     * @param parMonth The month of the birthday
     * @param parDay The day of the birthday
     */
    fun addBirthday(name: String, day: Int, month: Int, year: Int, daysToRemind: Int, expanded: Boolean, notify: Boolean) {
        this.add(Birthday(name, day, month, year, daysToRemind, expanded, notify))
        sortAndSaveBirthdays()
    }

    /**
     * Helper Function to add a full birthday object, used for undoing deletions
      */
    fun addFullBirthday(birthday: Birthday): Int{
        this.add(birthday)
        sortAndSaveBirthdays()
        return this.indexOf(birthday)
    }

    /**
     * Deletes the Birthday at a given index in the this
     * @param index The position of the birthday in the array list
     */
    fun deleteBirthday(index: Int) {
        this.removeAt(index)
        sortAndSaveBirthdays()
    }

    /**
     * Grabs a birthday object and changes its attributes according to the parameters.
     * @param name Name of the Person.
     * @param parDay Day of the birthday.
     * @param parMonth Month of the birthday.
     * @param parReminder Days to be reminded at prior to the birthday.
     * @param parPosition Position of the birthday object int he list.
     */
    fun editBirthday(name: String, parDay: Int, parMonth: Int, parYear: Int, parReminder: Int, parPosition: Int) {
        val editableBirthday: Birthday =
            getBirthday(
                parPosition
            )

        editableBirthday.name = name
        editableBirthday.day = parDay
        editableBirthday.month = parMonth
        editableBirthday.year = parYear
        editableBirthday.daysToRemind = parReminder

        sortAndSaveBirthdays()

    }

    fun sortAndSaveBirthdays(){
        sortBirthday()
        save()
    }

    fun deleteBirthdayObject(birthday: Birthday){
        this.remove(birthday)
        sortBirthday()
        save()
    }

    /**
     * Returns a birthday from arraylist at given index
     * @return Returns requested birthday object
     */
    fun getBirthday(position: Int): Birthday = this[position]

    private fun manageLabels() {
        val months = arrayListOf<Int>()
        var n = 0
        while (n < this.size) {
            if (this[n].daysToRemind < 0) this.remove(this[n])
            else n++
        }

        val today = LocalDate.now()
        var beforeMonth = false
        var afterMonth = false
        this.forEach { m ->
            if(!months.contains(m.month) && m.month != today.monthValue){
                months.add(m.month)
            }

            if (m.month == today.monthValue && m.day < today.dayOfMonth) beforeMonth = true
            else if (m.month == today.monthValue && m.day >= today.dayOfMonth) afterMonth = true
        }

        val monthName = if (context != null) {
            context.resources.getStringArray(R.array.months)[today.monthValue - 1]
        } else today.month.toString().toLowerCase().capitalize()

        if (beforeMonth) {
            this.add(
                Birthday(
                    monthName, 1, today.monthValue, 0, -1 * today.monthValue,
                    expanded = false, notify = false
                )
            )
        }

        if (afterMonth) {
            this.add(
                Birthday(
                    monthName, today.dayOfMonth, today.monthValue, 0, -1 * today.monthValue,
                    expanded = false, notify = false
                )
            )
        }

        months.forEach { m ->
            val month = LocalDate.of(2020, m, 1).month
            val name = if (context != null) {
                context.resources.getStringArray(R.array.months)[month.value - 1]
            } else month.toString().toLowerCase().capitalize()

            Log.e("bt", name)
            this.add(Birthday(name, 0, m,0, -1*m,
                expanded = false, notify = false
            ))
        }
    }

    private fun sortBirthday() {
        manageLabels()
        val localDate = LocalDate.now()
        val day = localDate.dayOfMonth
        val month = localDate.month.value
        val cacheList = ArrayList<Birthday>()
        this.sortWith(compareBy({ it.month }, { it.day }, {it.daysToRemind >= 0}, { it.name }))

        var i = 0
        val spacerBirthday = Birthday("${localDate.year + 1}", 1, 1,0, -200,
            expanded = false,
            notify = false
        )
        cacheList.add(spacerBirthday)
        while(i < this.size) {
            if (getBirthday(i).month < month ||
                (getBirthday(i).month == month && getBirthday(i).day < day)) {
                cacheList.add(getBirthday(i))
                this.remove(getBirthday(i))
            } else {
                i++
            }
        }

        this.sortWith(compareBy(
            { it.month },
            { it.day },
            { it.daysToRemind >= 0},
            { it.name })
        )

        if (cacheList.size == 1) {
            cacheList.remove(spacerBirthday)
        }

        cacheList.forEach { n ->
            this.add(n)
        }
    }

    /**
     * Collects all birthdays that are happening on the current day and returns
     * them as an list.
     * @return List of today's birthdays.
     */
    fun getRelevantCurrentBirthdays(): ArrayList<Birthday> {
        val currentBirthdays = ArrayList<Birthday>()
        val localDate = LocalDate.now()
        this.forEach { n ->
            if (n.month == localDate.monthValue  &&
                n.day == localDate.dayOfMonth &&
                n.daysToRemind >= 0
            ) {
                currentBirthdays.add(n)
            }
        }
        return currentBirthdays
    }

    /**
     * Collects all birthdays which's reminder corresponds to the current day
     * @see getRelevantCurrentBirthdays for current birthdays.
     * @return List of birthdays to be reminded of on the current day.
     */
    fun getRelevantUpcomingBirthdays(): ArrayList<Birthday> {
        val upcomingBirthdays = ArrayList<Birthday>()
        val localDate = LocalDate.now()
        this.forEach { n ->
            if (n.month == localDate.monthValue + 1 && (n.day - n.daysToRemind) ==
                localDate.dayOfMonth && n.daysToRemind != 0)
            {
                upcomingBirthdays.add(n)
            }
        }
        return upcomingBirthdays
    }

    /**
     * Saves all birthdays as collapsed.
     */
    fun collapseAll() {
        this.forEach { e ->
            e.expanded = false
        }
        save()
    }

    /**
     * Returns the x next birthdays from the this as a list. If the requested
     * size is larger than the this size the whole list is returned.
     * @param index Amount of birthdays to return.
     * @return The requested amount of birthdays or the whole this.
     */
    fun getXNextBirthdays(index: Int): ArrayList<Birthday> {
        var min = index

        if (index > this.size) {
            min = this.size
        }

        val xNextBirthdays = ArrayList<Birthday>()

        for (i in 0..min) {
            xNextBirthdays.add(
                getBirthday(
                    i
                )
            )
        }

        return xNextBirthdays
    }

    private fun fetchFromFile() {
        val jsonString = StorageHandler.files[StorageId.BIRTHDAYS]?.readText()

        this.addAll(GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type))
    }

    private fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.BIRTHDAYS], this)
    }
}
