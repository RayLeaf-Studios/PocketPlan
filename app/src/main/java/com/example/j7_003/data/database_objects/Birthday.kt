package com.example.j7_003.data.database_objects

data class Birthday constructor(var name: String, var month: Int, var day: Int, var daysToRemind: Int?) {
    constructor(name: String, month: Int, day: Int): this(name, month, day, null)

    fun hasReminder(): Boolean = daysToRemind != 0
}