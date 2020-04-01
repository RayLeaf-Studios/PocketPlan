package com.example.j7_003.logic

class Database() {

    var taskList = ArrayList<Task>()

    init {
        update()
        loadTaskList()
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
        taskList = arrayListOf<Task>(
            Task("Java", 3),
            Task("KotlinKotlinKotlinKotlin", 2),
            Task("Python", 1),
            Task("C++", 2),
            Task("", 2),
            Task("Python", 1),
            Task("HTML", 3),
            Task("Javascript", 2),
            Task("CSS", 3)
        )
    }

    /**
     * To be implemented...
     * Will update storage files from remote host and will be called on init.
     */
    private fun update() {

    }
}