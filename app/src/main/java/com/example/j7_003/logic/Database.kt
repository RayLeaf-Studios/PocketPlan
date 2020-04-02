package com.example.j7_003.logic

import android.content.Context
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS

import com.beust.klaxon.Klaxon
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class Database(context: Context) {

    var taskList = ArrayList<Task>()
    /*var file = File(context.filesDir, "TaskList")*/
    var file = File(Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "TaskList.txt") //debug for api level 24

    init {
        createFile()
        taskList = fetchTaskList()
    }

    /**
     * If the file exists, contents are written into it.
     *
     * Still needs exception handling
     */
    fun saveTaskList() {
        val isNewFileCreated: Boolean = file.exists()
        val jsonString = taskListToJson()

        if(isNewFileCreated) {
            file.writeText(jsonString)
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
    }

    /**
     * Returns a task at a given index in the tasklist.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = taskList[index]


    private fun taskListToJson(): String {
        return Klaxon().toJsonString(taskList)
    }

    private fun createFile() {
        if(!file.exists()) file.writeText("[]")
    }

    private fun fetchTaskList(): ArrayList<Task> {
        val jsonString = file.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
    }
}