package com.example.j7_003.logic

import android.content.Context
import java.io.File

class Database(context: Context) {

    var taskList = ArrayList<Task>()
    private var file = File(context.filesDir, "TaskList")

    init {
        file.delete()
        file = File(context.filesDir, "TaskList")   //for debugging, will be changed after implementation of update function


        update()
        writeIntoFile("new Task", 3)
        debugReadFile()
    }

    /**
     * To be implemented...
     * Will load in the task list from local files.
     */
    private fun loadTaskList() {
        //replace this by reading from file
        //loadDebugList()

    }

    /**
     * Task list used to debug some task list functionality.
     */
    fun loadDebugList(){
        //loads in list of default values, testing empty strings, long strings, duplicate elements
        taskList = arrayListOf(
            Task(file.readText(), 3),
            Task("KotlinKotlinKotlinKotlin", 2),
            Task("Python", 1),
            Task("C++", 2),
            Task("", 2),
            Task("Python", 1),
            Task("HTML", 2),
            Task("Javascript", 2),
            Task("CSS", 2)
        )
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
    private fun writeIntoFile(name: String, priority: Int) {
        val isNewFileCreated: Boolean = file.exists()

        if(!isNewFileCreated) {
            file.writeText("$name, $priority\n")
        }
    }

    /**
     * To be implemented...
     * Check for empty file will throw an exception or something else for error handling
     *
     * Reads each line in file and split each line into taskName and taskPriority
     * and create with the a task with the given parameters.
     *
     * file content example:
     * 1. new Task, 3
     * 2. new new Task, 1
     * ...
     *
     * task name: "new Task", priority: 3
     */
    private fun debugReadFile() {

        file.forEachLine { n ->
                val taskProperties = n.split(", ")
                taskList.add(Task(taskProperties[0], taskProperties[1].toInt()))
        }
    }
}