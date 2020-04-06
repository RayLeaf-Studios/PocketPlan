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
import kotlin.collections.ArrayList

class Database(context: Context) : Serializable {

    var birthdayList = ArrayList<Birthday>()
    var taskList = ArrayList<Task>()
    private var taskFile = setStorageLocation("TaskList.txt", context)
    private var birthdayFile = setStorageLocation("BirthdayList.txt", context)
    private val converter = Gson()

    private var debugFile = setStorageLocation("debug.txt", context)


    init {
        createFiles()
        taskList = fetchTaskList()
        //addDebugBirthdays()
        birthdayList = fetchBirthdayList()
        //debug!

        sortBirthday()
//        addDebugBirthdays()
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
        saveTaskList()
    }

    /**
     * Returns a task at a given index in the tasklist.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = taskList[index]

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
     * @param month The month of the birthday
     * @param day The day of the birthday
     * @param note A note belonging to the birthday object
     */
    fun addBirthday(name: String, month: Int, day: Int, note: String) {
        birthdayList.add(
            Birthday(
                name,
                month,
                day,
                note
            )
        )
        saveBirthdayList()
    }

    /**
     * Adds a birthday to the birthdaylist and saves the birthdaylist.
     * @param name The name of the created birthday
     * @param month The month of the birthday
     * @param day The day of the birthday
     */
    fun addBirthday(name: String, month: Int, day: Int) {
        birthdayList.add(
            Birthday(
                name,
                month,
                day
            )
        )
        saveBirthdayList()
    }

    /**
     * Saves the birthday list as json string
     */
    fun saveBirthdayList() {
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
    fun editBirthday(name: String, month: Int, day: Int, index: Int, note: String, dayToRemind: Int) {
        val editableBirthday: Birthday = getBirthday(index)
        editableBirthday.name = name
        editableBirthday.note = note
        editableBirthday.day = day
        editableBirthday.month = month
        editableBirthday.daysToRemind = dayToRemind
        saveBirthdayList()
    }

    fun editBirthday(name: String, month: Int, day: Int, index: Int, note: String) {
        val editableBirthday: Birthday = getBirthday(index)
        editableBirthday.name = name
        editableBirthday.note = note
        editableBirthday.day = day
        editableBirthday.month = month
        saveBirthdayList()
    }

    fun editBirthday(name: String, month: Int, day: Int, index: Int, dayToRemind: Int) {
        val editableBirthday: Birthday = getBirthday(index)
        editableBirthday.name = name
        editableBirthday.day = day
        editableBirthday.month = month
        editableBirthday.daysToRemind = dayToRemind
        saveBirthdayList()
    }

    fun editBirthday(name: String, month: Int, day: Int, index: Int) {
        val editableBirthday: Birthday = getBirthday(index)
        editableBirthday.name = name
        editableBirthday.day = day
        editableBirthday.month = month
        saveBirthdayList()
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

    private fun sortBirthday() {
        birthdayList.sortWith(compareBy({ it.month }, {it.day }, {it.name}))
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
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be database handling

    private fun createFiles() {

        if (!taskFile.exists()) taskFile.writeText("[]")
        if (!birthdayFile.exists()) birthdayFile.writeText("[]")
        if (!debugFile.exists()) debugFile.writeText("[]")


    }

    private fun setStorageLocation(fileName: String, context: Context): File {

        return if (Build.VERSION.SDK_INT < 29) {
            File("${Environment.getDataDirectory()}/data/com.example.j7_003", fileName)
        } else {
            File(context.filesDir, fileName)
        }
    }

    /*private fun debugSave() {
        debugFile.writeText(Klaxon().toJsonString(getNextXBirthdays(3)))
    }*/
}