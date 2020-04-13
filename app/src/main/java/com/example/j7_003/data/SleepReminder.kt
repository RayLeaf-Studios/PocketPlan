package com.example.j7_003.data

import android.app.*
import android.content.Context
import android.content.Intent
import com.example.j7_003.MainActivity
import com.example.j7_003.notifications.NotificationReceiver
import java.util.*
import kotlin.properties.Delegates

class SleepReminder {

    companion object {
        private val myCalendar = Calendar.getInstance()
        private var currentHour by Delegates.notNull<Int>()
        private var currentMinute by Delegates.notNull<Int>()

        var reminderHour: Int = 0
        var reminderMinute: Int = 0
        var wakeUpHour: Int = 0
        var wakeUpMinute: Int = 0

        var isSet: Boolean = false

        fun isRemindTimeReached(): Boolean {
            getClock()

            return compareHours() || compareWithMinutes()
            //return (compareHours() == -1) || (compareHours() == 0 && compareMinutes() < 1)
        }

        fun edit(newHour: Int, newMinute: Int) {
            reminderHour = newHour
            reminderMinute = newMinute
        }

        fun disable() {
            isSet = false
        }

        fun enable() {
            isSet = true

        }

        private fun compareHours(): Boolean = currentHour in reminderHour+1 until wakeUpHour
        private fun compareWithMinutes(): Boolean {
            return when (currentHour) {
                reminderHour -> currentMinute >= reminderMinute
                wakeUpHour -> currentMinute < wakeUpMinute
                else -> false
            }
        }

        private fun getClock() {
            currentHour = myCalendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = myCalendar.get(Calendar.MINUTE)
        }

        fun setSleepReminderAlarm() {
            if (isSet) {
                val intent = Intent(MainActivity.myActivity, NotificationReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(
                    MainActivity.myActivity,
                    200,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager =
                    MainActivity.myActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val notificationTime = Calendar.getInstance()
                notificationTime.set(Calendar.HOUR_OF_DAY, reminderHour)
                notificationTime.set(Calendar.MINUTE, reminderHour)
                notificationTime.set(Calendar.SECOND, 0)

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }
    }
}