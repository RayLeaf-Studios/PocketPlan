package com.example.j7_003.data.database

import com.example.j7_003.system_interaction.handler.StorageHandler
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

        var timings: IntArray = IntArray(4)
        var sDuration: IntArray = IntArray(2)
        var isSet: Boolean = false

        var days: BooleanArray = BooleanArray(7)

        private const val fileName: String = "SLEEP_REMINDER"

        fun init() {
            StorageHandler.createJsonFile(
                fileName,
                "SReminder.json"
            )
            load()
        }

        fun isRemindTimeReached(): Boolean {
            getClock()

            return compareHours() || compareWithMinutes()
        }

        fun editDuration(newHour: Int, newMinute: Int) {
            sDuration[0] = newHour
            sDuration[1] = newMinute
            save()
        }

        fun editWakeUp(newHour: Int, newMinute: Int) {
            timings[2] = newHour
            timings[3] = newMinute
            save()
        }

        fun disable() {
            isSet = false
            save()
        }

        fun enable() {
            isSet = true
            save()
        }

        fun setDay(index: Int, bool: Boolean) {
            days[index] = bool
            save()
        }

        fun getRemindTimeString(): String = "${timings[0].toString().padStart(2, '0')}:${timings[1].toString().padStart(2, '0')}"

        private fun compareHours(): Boolean = currentHour in timings[0]+1 until timings[2]
        private fun compareWithMinutes(): Boolean {
            return when (currentHour) {
                timings[0] -> currentMinute >= timings[1]
                timings[2] -> currentMinute < timings[3]
                else -> false
            }
        }

        private fun getClock() {
            currentHour = myCalendar.get(Calendar.HOUR_OF_DAY)
            currentMinute = myCalendar.get(Calendar.MINUTE)
        }

        private fun save() {
            val saveableList = arrayListOf(
                timings,
                days,
                isSet,
                sDuration
            )
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[fileName],
                saveableList
            )
        }

        private fun calcReminder(){
            if (timings[2] - sDuration[0] < 1) {
                timings[0] = 24 + (timings[2] - sDuration[0])
            } else {
                timings[0] = timings[2] - sDuration[0]
            }

            if (timings[3] - sDuration[1] < 1) {
                timings[0] -= 1
                timings[1] = 60 + (timings[3] - sDuration[1])
            } else {
                timings[1] = timings[3] - sDuration[1]
            }
        }

        private fun load() {
            val jsonString = StorageHandler.files[fileName]?.readText()

            val loadedData = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Any>>() {}.type) as ArrayList<Any>

            val list1 = loadedData[0] as ArrayList<Int>
            val list2 = loadedData[1] as ArrayList<Boolean>
            val lIsSet = loadedData[2] as Boolean
            val lDuration = loadedData[3] as ArrayList<Int>

            timings = list1.toIntArray()
            days = list2.toBooleanArray()
            isSet = lIsSet
            sDuration = lDuration.toIntArray()
        }
    }
}