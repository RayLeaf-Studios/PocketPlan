package com.example.j7_003.logic

data class Task(val title: String, var priority: Int) {

    fun changePriority(priority: Int) {
        this.priority = priority
    }

    override fun toString() = "$title, $priority"
}