package com.example.j7_003.logic

import android.content.Context
import android.os.Build
import android.os.Environment
import com.beust.klaxon.Klaxon
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import kotlin.collections.ArrayList

class Database(context: Context) {

    var birthdayList = ArrayList<Birthday>()
    var taskList = ArrayList<Task>()
    private var taskFile = setStorageLocation("TaskList.txt", context)
    private var birthdayFile = setStorageLocation("BirthdayList.txt", context)

    private var debugFile = setStorageLocation("debug.txt", context)


    init {
        createFiles()
        taskList = fetchTaskList()
        birthdayList = fetchBirthdayList()

        sortBirthday()
        saveBirthdayList()
        debugSave()
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
        taskList.add(Task(title, priority))
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
        getTask(position).title = title
        getTask(position).priority = index + 1
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

        val isNewFileCreated: Boolean = taskFile.exists()
        val jsonString = Klaxon().toJsonString(taskList)

        if (isNewFileCreated) {
            taskFile.writeText(jsonString)
        }

    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be the birthday functionality

    fun addBirthday(name: String, month: Int, day: Int, note: String) {
        birthdayList.add(Birthday(name, month, day, note))
        saveBirthdayList()
    }

    fun addBirthday(name: String, month: Int, day: Int) {
        birthdayList.add(Birthday(name, month, day))
        saveBirthdayList()
    }

    fun saveBirthdayList() {
        val isNewFileCreated: Boolean = birthdayFile.exists()
        val jsonString = Klaxon().toJsonString(birthdayList)

        if (isNewFileCreated) {
            birthdayFile.writeText(jsonString)
        }
    }

    fun deleteBirthday(index: Int) {
        birthdayList.removeAt(index)
        saveBirthdayList()
    }

    fun editBirthday() {
        //todo
    }

    fun getBirthday(position: Int): Birthday = birthdayList[position]

    private fun fetchBirthdayList(): ArrayList<Birthday> {
        val jsonString = birthdayFile.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
    }

    private fun sortBirthday() {
        birthdayList.sortWith(compareBy({ it.month }, {it.day }, {it.name}))
    }

    fun getNextXBirthdays(count: Int): ArrayList<String>{
                //debug gibt die nächsten count birthdays ab (inklusive) heute als arraylist zurück

        var dateNow: LocalDate
        var daysLeft: Long
        var cacheLeft: Long = 365
        val closestBirthdays = ArrayList<String>()

        birthdayList.forEach { n ->

            /*daysLeft = ChronoUnit.DAYS.between(otherDay, dateNow)

            if(daysLeft < cacheLeft) {
                cacheLeft = daysLeft
                if((n != birthdayList.last() && n.hashCode() != birthdayList[index + 1].hashCode()) || (n.hashCode() != birthdayList[index + 1].hashCode())) {
                    closestBirthdays.add(n)
                }
            } else {
                return@forEachIndexed
            }*/
        }

        return closestBirthdays
    }

    private fun addDebugBirthdays() {
        addBirthday("eugen", 12, 24)
        addBirthday("micha", 9, 15)
        addBirthday("nico", 3, 24)
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

    private fun debugSave() {
        debugFile.writeText(Klaxon().toJsonString(getNextXBirthdays(3)))
    }
}