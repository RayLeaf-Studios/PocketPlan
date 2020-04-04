package com.example.j7_003.data.database_objects

data class Birthday constructor(var name: String, var month: Int, var day: Int, var note: String, var daysToRemind: Int?) {
    constructor(name: String, month: Int, day: Int, note: String) : this(name, month, day, note, null)
    constructor(name: String, month: Int, day: Int): this(name, month, day, "", null)
}