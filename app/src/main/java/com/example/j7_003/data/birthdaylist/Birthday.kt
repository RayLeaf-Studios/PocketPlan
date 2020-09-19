package com.example.j7_003.data.birthdaylist

data class Birthday constructor(var name: String, var day: Int, var month: Int, var year: Int, var daysToRemind: Int, var expanded: Boolean, var notify: Boolean) {

    fun hasReminder(): Boolean = daysToRemind != 0
}