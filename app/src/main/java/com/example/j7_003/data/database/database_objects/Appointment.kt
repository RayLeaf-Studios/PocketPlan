package com.example.j7_003.data.database.database_objects

data class Appointment(var title: String, var note: String, var repetitive: Boolean, var day: Int, var month: Int, var year: Int) {



    constructor(
        title: String,
        note: String,
        day: Int,
        month: Int,
        year: Int): this(title, note, false, day, month, year)

    constructor(title: String, note: String, repetitive: Boolean): this(title, note, repetitive, 0, 0, 0)
}