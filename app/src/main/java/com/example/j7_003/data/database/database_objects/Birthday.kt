package com.example.j7_003.data.database.database_objects

data class Birthday constructor(var name: String, var day: Int, var month: Int, var year: Int, var daysToRemind: Int, var expanded: Boolean) {
    constructor(name: String, day: Int, month: Int, year: Int, expanded: Boolean): this(name, day, month, year, 0, false)

    fun hasReminder(): Boolean = daysToRemind != 0
}