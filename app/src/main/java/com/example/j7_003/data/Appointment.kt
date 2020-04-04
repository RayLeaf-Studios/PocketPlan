package com.example.j7_003.data

data class Appointment(var title: String, var note: String, var repetetive: Boolean, var day: Int, var month: Int, var year: Int) {
    constructor(title: String, note: String, day: Int, month: Int, year: Int): this(title, note, false, day, month, year)
}