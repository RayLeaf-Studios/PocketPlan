package com.example.j7_003.data

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.j7_003.data.database_objects.*
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Database(context: Context) : Serializable {

    var birthdayList = ArrayList<Birthday>()
    var taskList = ArrayList<Task>()
    private var taskFile = setStorageLocation("TaskList.txt", context)
    private var birthdayFile = setStorageLocation("BirthdayList.txt", context)
    private val converter = Gson()
    private val calendar = Calendar.getInstance()

    //private val reminder = SleepReminder().checkClock()

    init {
        createFiles()
        taskList = fetchTaskList()
        birthdayList = fetchBirthdayList()
        sortTasks()
        sortBirthday()
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//


    /**
     * Adds a task to the tasklist and saves the tasklist.
     * @param title The title of the created task
     * @param priority The priority the task will be set to
     */
    fun addTask(title: String, priority: Int) {
        taskList.add(
            Task(
                title,
                priority
            )
        )
        sortTasks()
        saveTaskList()
    }

    /**
     * Deletes a task at a given index.
     * @param index The index of the list, which will be removed.
     */
    fun deleteTask(index: Int) {
        taskList.removeAt(index)
        saveTaskList()
    }

    fun editTask(position: Int, index: Int, title: String) {
        val editableTask: Task = getTask(position)
        editableTask.title = title
        editableTask.priority = index + 1
        sortTasks()
        saveTaskList()
    }

    /**
     * Returns a task at a given index in the tasklist.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = taskList[index]

    fun sortTasks() {
        taskList.sortWith(compareBy({it.priority}, {it.title}))
    }

    private fun fetchTaskList(): ArrayList<Task> {

        val jsonString = taskFile.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
    }

    private fun saveTaskList() {
        taskFile.writeText(converter.toJson(taskList))
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be the birthday functionality
    /**
     * Adds a birthday to the birthdaylist and saves the birthdaylist.
     * @param name The name of the created birthday
     * @param parMonth The month of the birthday
     * @param parDay The day of the birthday
     */
    fun addBirthday(name: String, parDay: Int, parMonth: Int, parReminder: Int): Boolean {
        if(parMonth in 1..12 && parDay in 1..GregorianCalendar(
                calendar.get(Calendar.YEAR),
                parMonth -1,
                Calendar.DAY_OF_MONTH).getActualMaximum(Calendar.DAY_OF_MONTH)) {
            birthdayList.add(
                Birthday(name, parMonth, parDay, parReminder)
            )

            saveBirthdayList()
            sortBirthday()
            return true
            }
        return false
    }

    private fun saveBirthdayList() {
        birthdayFile.writeText(converter.toJson(birthdayList))
    }

    /**
     * Deletes the Birthday at a given index in the birthdaylist
     * @param index The position of the birthday in the array list
     */
    fun deleteBirthday(index: Int) {
        birthdayList.removeAt(index)
        saveBirthdayList()
    }

    /**
     * To be implemented...
     * Will edit a given birthday object
     */

    fun editBirthday(name: String, parDay: Int, parMonth: Int, parReminder: Int, parPosition: Int): Boolean {
        if(parMonth in 1..12 && parDay in 1..GregorianCalendar(
                calendar.get(Calendar.YEAR),
                parMonth -1,
                Calendar.DAY_OF_MONTH).getActualMaximum(Calendar.DAY_OF_MONTH)
            ) {
            val editableBirthday: Birthday = getBirthday(parPosition)

            editableBirthday.name = name
            editableBirthday.day = parDay
            editableBirthday.month = parMonth
            editableBirthday.daysToRemind = parReminder

            sortBirthday()
            saveBirthdayList()
            return true
            }

        return false
    }

    /**
     * Returns a birthday from arraylist at given index
     * @return Returns requested birthday object
     */
    fun getBirthday(position: Int): Birthday = birthdayList[position]

    private fun fetchBirthdayList(): ArrayList<Birthday> {
        val jsonString = birthdayFile.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
    }

    fun sortBirthday() {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val cacheList = ArrayList<Birthday>()
        var i = 0

        birthdayList.sortWith(compareBy({ it.month }, { it.day }, { it.name }))

        while(i < birthdayList.size) {
            if (getBirthday(i).month < month || (getBirthday(i).month == month && getBirthday(i).day < day)) {
                cacheList.add(getBirthday(i))
                birthdayList.remove(getBirthday(i))
            } else {
                i++
            }
        }

        birthdayList.sortWith(compareBy({ it.month < month }, { it.month }, { it.day < day }, { it.day }, { it.name }))

        cacheList.forEach { n ->
            birthdayList.add(n)
        }
    }

    private fun addDebugBirthdays() {
        birthdayList = arrayListOf(
            Birthday("Nasus", 1, 23),
            Birthday("Veigar", 12, 24),
            Birthday("Sion", 5, 1),
            Birthday("Ezreal", 3, 5),
            Birthday("Leona", 4, 7),
            Birthday("Jarvan IV", 1, 9),
            Birthday("Sejuani", 6, 12),
            Birthday("Max Mustermann", 1, 2),
            Birthday("Darius", 1, 2),
            Birthday("Xerath", 12, 12),
            Birthday("Svobby", 5, 28),
            Birthday("Angela Merkel", 1, 25),
            Birthday("Niemand", 17, 2),
            Birthday("Test", 3, 2)
        )

        fun getXNextBirthdays(index: Int): ArrayList<Birthday> {
            var min = index
            if (index > birthdayList.size) {
                min = birthdayList.size
            }

            val xNextBirthdays = ArrayList<Birthday>()

            for (i in 0..min) {
                xNextBirthdays.add(birthdayList[i])
            }

            return xNextBirthdays
        }
    }
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be database handling

    private fun createFiles() {

        if (!taskFile.exists()) taskFile.writeText("[]")
        if (!birthdayFile.exists()) birthdayFile.writeText("[]")


    }

    private fun setStorageLocation(fileName: String, context: Context): File {

        return if (Build.VERSION.SDK_INT < 29) {
            File("${Environment.getDataDirectory()}/data/com.example.j7_003", fileName)
        } else {
            File(context.filesDir, fileName)
        }
    }
}