package com.example.j7_003.data.database

import com.example.j7_003.data.Weekdays
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import kotlin.collections.HashMap

class NewSleepReminder {
    companion object {
        var daysAreCustom: Boolean = false
        private const val fileName: String = "SLEEP_REMINDER_DEBUG"

        var reminder = HashMap<Weekdays, Reminder>(8)

        fun init() {
            initMap()
            createFile()
            load()
        }

        fun editWakeUpAtDay(day: Weekdays, hour: Int, minute: Int) {
            reminder[day]?.editWakeUpTime(hour, minute)
            save()
        }

        fun editAllWakeUp(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editWakeUpTime(hour, minute)
            }
            save()
        }

        fun editDurationAtDay(day: Weekdays, hour: Int, minute: Int) {
            reminder[day]?.editDuration(hour, minute)
            save()
        }

        fun editAllDuration(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editDuration(hour, minute)
            }
            save()
        }

        fun enableAll() {
            reminder.forEach { n ->
                if (n.key != Weekdays.NULL) n.value.isSet = true
            }
        }

        fun disableAll() {
            reminder.forEach { n ->
                if (n.key != Weekdays.NULL) n.value.isSet = false
            }
        }

        private fun initMap() {
            Weekdays.values().forEach { n ->
                reminder[n] = Reminder()
            }
        }

        private fun createFile() {
            StorageHandler.createJsonFile(
                fileName,
                "SReminder_Debug.json",
                text = Gson().toJson(reminder)
            )
        }

        private fun save() {
            reminder[Weekdays.NULL] = Reminder()
            reminder[Weekdays.NULL]?.isSet = daysAreCustom
            StorageHandler.saveAsJsonToFile(StorageHandler.files[fileName], reminder)
        }

        private fun load() {
            val jsonString = StorageHandler.files[fileName]?.readText()

            reminder = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<HashMap<Weekdays, Reminder>>() {}.type)

            daysAreCustom = reminder[Weekdays.NULL]?.isSet!!
        }

        class Reminder {
            var isSet: Boolean = false
            private lateinit var reminderTime: LocalTime
            private var wakeUpTime: LocalTime = LocalTime.of(0, 0)
            private var duration: Duration = Duration.ofHours(0).plusMinutes(0)

            fun editWakeUpTime(hour: Int, minute: Int) {
                wakeUpTime = LocalTime.of(hour, minute)
                calcReminderTime()
            }

            fun editDuration(hour: Int, minute: Int) {
                duration = Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
                calcReminderTime()
            }

            fun enable() { isSet = true; save() }

            fun disable() { isSet = false; save() }

            fun getRemindTimeString(): String = reminderTime.toString()

            fun getWakeUpTimeString(): String = wakeUpTime.toString()

            fun getDurationTimeString(): String = "${duration.toHours()}h ${duration.toMinutes()%60}m"

            fun getRemainingWakeDuration(): String {
                return "${(LocalTime.now().until(reminderTime, ChronoUnit.HOURS))}h " +
                        "${(LocalTime.now().until(reminderTime, ChronoUnit.MINUTES)%60)}m"
            }

            private fun calcReminderTime() {
               reminderTime = wakeUpTime.minus(duration)
            }
        }
    }
}