package com.example.j7_003.logic

import com.beust.klaxon.Json

data class Task(var title: String, var priority: Int) {

    fun changePriority(priority: Int) {
        this.priority = priority
    }
}