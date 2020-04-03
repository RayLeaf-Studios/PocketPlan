package com.example.j7_003.logic

data class Birthday constructor(var name: String, var month: Int, var day: Int) {
    lateinit var note: String
    constructor(name: String, month: Int, day: Int, note: String) : this(name, month, day) {
        this.note = note
    }
}