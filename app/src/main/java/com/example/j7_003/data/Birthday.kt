package com.example.j7_003.data

data class Birthday constructor(var name: String, var month: Int, var day: Int, var note: String) {
    constructor(name: String, month: Int, day: Int) : this(name, month, day, "")
}