package com.example.j7_003.logic

import android.content.Context

import com.beust.klaxon.Klaxon
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File


class Database(context: Context) {


    var taskList = ArrayList<Task>()
    var file = File(context.filesDir, "TaskList")

    init {
        createFile()
        taskList = TaskList()
    }



    /**
     * To be implemented...
     * Will update storage files from remote host and will be called on init.
     */
    private fun update() {

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

    fun TaskList(): ArrayList<Task> {
        val jsonString = file.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
    }

    fun addTask(title: String, priority: Int) {
        taskList.add(Task(title, priority))
        saveTaskList()
        //todo save with adding task
    }

    private fun taskListToJson(): String {
        return Klaxon().toJsonString(taskList)
    }

    private fun createFile() {
        if(!file.exists()) file.writeText("")
    }
}