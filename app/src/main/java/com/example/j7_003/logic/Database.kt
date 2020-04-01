package com.example.j7_003.logic

class Database() {

    var taskList = ArrayList<Task>()

    init {
        loadTaskList()
    }

    fun loadTaskList() {
        //replace this by reading from file
        //loadDebugList()

    }

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
}