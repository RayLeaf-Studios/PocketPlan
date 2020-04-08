package com.example.j7_003.data

import android.util.Log
import java.util.*
import kotlin.properties.Delegates

class SleepReminder(var reminderHour: Int, var reminderMinute: Int) {
    private val myCalendar = Calendar.getInstance()
    private var currentHour by Delegates.notNull<Int>()
    private var currentMinute by Delegates.notNull<Int>()

    init {
        getClock()
        if(isRemindTimeReached()) {
            Log.e("True", "${reminderHour.compareTo(currentHour)} ${reminderMinute.compareTo(currentMinute)}")
        } else {
            Log.e("False", "${reminderHour.compareTo(currentHour)} ${reminderMinute.compareTo(currentMinute)}")
        }
    }


    private fun isRemindTimeReached(): Boolean = (compareHours() == -1) || (compareHours() == 0 && compareMinutes() < 1)/*|| (compareHours() == 1 && compareMinutes() < 0)*/

    private fun compareHours(): Int = reminderHour.compareTo(currentHour)
    private fun compareMinutes(): Int = reminderMinute.compareTo(currentMinute)

    private fun getClock() {
        currentHour = myCalendar.get(Calendar.HOUR_OF_DAY)
        currentMinute = myCalendar.get(Calendar.MINUTE)
    }
}