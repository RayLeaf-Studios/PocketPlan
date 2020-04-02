package com.example.j7_003.logic

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import androidx.annotation.RequiresApi

import com.beust.klaxon.Klaxon
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.time.LocalDate
import java.time.MonthDay

class Database(context: Context) {

    var birthdayList = ArrayList<Birthday>()
    var taskList = ArrayList<Task>()
    /*private var taskFile = File(context.filesDir, "TaskList")
    private var birthdayFile = File(context.filesDir, "BirthdayList")*/
    private var taskFile = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "TaskList.txt") //debug for api level 24
    private var birthdayFile = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "BirthdayList.txt")  // s.o.

    init {
        createFiles()
        taskList = fetchTaskList()
        birthdayList = fetchBirthdayList()
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//

    /**
     * If the file exists, contents are written into it.
     */
    fun saveTaskList() {
        val isNewFileCreated: Boolean = taskFile.exists()
        val jsonString = taskListToJson()

        if(isNewFileCreated) {
            taskFile.writeText(jsonString)
        }
    }

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

    /**
     * Returns a task at a given index in the tasklist.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = taskList[index]


    private fun taskListToJson(): String = Klaxon().toJsonString(taskList)

    private fun fetchTaskList(): ArrayList<Task> {
        val jsonString = taskFile.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be the birthday functionality

    fun addBirthday(name: String, month: Int, day: Int) {
        birthdayList.add(Birthday(name, month, day))
        saveBirthdayList()
    }

    fun saveBirthdayList() {
        val isNewFileCreated: Boolean = birthdayFile.exists()
        val jsonString = birthdayListToJson()

        if(isNewFileCreated) {
            birthdayFile.writeText(jsonString)
        }
    }

    fun deleteBirthday(index: Int) {
        birthdayList.removeAt(index)
        saveBirthdayList()
    }

    private fun fetchBirthdayList(): ArrayList<Birthday> {
        val jsonString = birthdayFile.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
    }

    private fun birthdayListToJson(): String = Klaxon().toJsonString(birthdayList)

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be database handling

    private fun createFiles() {
        if(!taskFile.exists()) taskFile.writeText("[]")
        if(!birthdayFile.exists()) birthdayFile.writeText("[]")
    }
}