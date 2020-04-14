package com.example.j7_003.data

import android.app.*
import android.content.Context
import android.content.Intent
import com.example.j7_003.MainActivity
import com.example.j7_003.notifications.NotificationReceiver
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList
import kotlin.properties.Delegates

class SleepReminder {

    companion object {
        private val myCalendar = Calendar.getInstance()
        private var currentHour by Delegates.notNull<Int>()
        private var currentMinute by Delegates.notNull<Int>()

        private var timings: IntArray = IntArray(4)
        private var isSet: Boolean = false

        var days: BooleanArray = BooleanArray(7)

        private const val fileName: String = "SLEEP_REMINDER"

        fun init() {
            StorageHandler.createJsonFile(fileName, "SReminder.json")
            load()
        }

        fun isRemindTimeReached(): Boolean {
            getClock()

            return compareHours() || compareWithMinutes()
        }

        fun editReminder(newHour: Int, newMinute: Int) {
            timings[0] = newHour
            timings[1] = newMinute
        }

        fun editWakeUp(newHour: Int, newMinute: Int) {
            timings[2] = newHour
            timings[3] = newMinute
        }

        fun disable() {
            isSet = false
        }

        fun enable() {
            isSet = true
        }

        private fun compareHours(): Boolean = currentHour in timings[0]+1 until timings[2]
        private fun compareWithMinutes(): Boolean {
            return when (currentHour) {
                timings[0].toInt() -> currentMinute >= timings[1]
                timings[2].toInt() -> currentMinute < timings[3]
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
                intent.putExtra("Notification", "SReminder")
                intent.putExtra("SReminder", days)

                val pendingIntent = PendingIntent.getBroadcast(
                    MainActivity.myActivity,
                    200,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
                val alarmManager =
                    MainActivity.myActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val notificationTime = Calendar.getInstance()
                notificationTime.set(Calendar.HOUR_OF_DAY, timings[0].toInt())
                notificationTime.set(Calendar.MINUTE, timings[1].toInt()-1)
                notificationTime.set(Calendar.SECOND, 59)

                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }
        }

        fun save() {
            val saveableList = arrayListOf(timings, days)
            StorageHandler.saveAsJsonToFile(StorageHandler.files[fileName], saveableList)
        }

        fun load() {
            val jsonString = StorageHandler.files[fileName]?.readText()

            val loadedData = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Any>>() {}.type) as ArrayList<Any>

            val list1 = loadedData[0] as ArrayList<Int>
            val list2 = loadedData[1] as ArrayList<Boolean>

            timings = list1.toIntArray()
            days = list2.toBooleanArray()
        }
    }
}