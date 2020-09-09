package com.example.j7_003.data.database.database_objects

data class Birthday constructor(var name: String, var day: Int, var month: Int, var year: Int, var daysToRemind: Int) {
    constructor(name: String, day: Int, month: Int, year: Int): this(name, day, month, year, 0)

    fun hasReminder(): Boolean = daysToRemind != 0
}