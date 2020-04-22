package com.example.j7_003.data.database

import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.system_interaction.handler.AlarmHandler
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.DayOfWeek
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.temporal.ChronoUnit
import kotlin.collections.HashMap

class NewSleepReminder {
    companion object {
        var daysAreCustom: Boolean = false
        private const val fileName: String = "SLEEP_REMINDER_DEBUG"

        var reminder = HashMap<DayOfWeek, Reminder>(7)

        fun init() {
            initMap()
            createFile()
            load()
        }

        fun setRegular() {
            daysAreCustom = false
            save()
        }

        fun setCustom() {
            daysAreCustom = true
            save()
        }

        fun editWakeUpAtDay(day: DayOfWeek, hour: Int, minute: Int) {
            reminder[day]?.editWakeUpTime(hour, minute)
            updateSingleReminder(day)
            save()
        }

        fun editAllWakeUp(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editWakeUpTime(hour, minute)
            }
            save()
        }

        fun editDurationAtDay(day: DayOfWeek, hour: Int, minute: Int) {
            reminder[day]?.editDuration(hour, minute)
            updateSingleReminder(day)
            save()
        }

        fun editAllDuration(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editDuration(hour, minute)
            }
            save()
        }

        fun getRemainingWakeDurationString(): String {
            return reminder[LocalDate.now().dayOfWeek]?.getRemainingWakeDuration()!!
        }

        fun enableAll() {
            reminder.forEach { n ->
                n.value.isSet = true
            }
            save()
        }

        fun disableAll() {
            reminder.forEach { n ->
                 n.value.isSet = false
            }
            save()
        }

        private fun initMap() {
            values().forEach { n ->
                reminder[n] = Reminder()
            }
        }

        private fun createFile() {
            StorageHandler.createJsonFile(
                fileName,
                "SReminder_Debug.json",
                text = Gson().toJson(reminder)
            )
            if (SettingsManager.settings["daysAreCustom"] == null) {
                SettingsManager.addSetting("daysAreCustom", daysAreCustom)
            }
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(StorageHandler.files[fileName], reminder)
            SettingsManager.addSetting("daysAreCustom", daysAreCustom)
            updateReminder()
        }

        private fun load() {
            val jsonString = StorageHandler.files[fileName]?.readText()

            reminder = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<HashMap<DayOfWeek, Reminder>>() {}.type)

            daysAreCustom = SettingsManager.settings["daysAreCustom"] as Boolean

            updateReminder()
        }

        private fun updateReminder() {
            var i = 200
            reminder.forEach { n ->
                n.value.updateAlarm(n.key, i)
                i++
            }
        }

        private fun updateSingleReminder(dayOfWeek: DayOfWeek) {
           reminder[dayOfWeek]?.updateAlarm(
               dayOfWeek,
               when(dayOfWeek) {
                   SATURDAY -> 200
                   TUESDAY -> 201
                   SUNDAY -> 202
                   THURSDAY -> 203
                   FRIDAY -> 204
                   WEDNESDAY -> 205
                   MONDAY -> 206
               }
           )
        }

        class Reminder {
            var isSet: Boolean = false
            private var reminderTime = LocalTime.of(23, 59)
            private var wakeUpTime: LocalTime = LocalTime.of(12, 0)
            private var duration: Duration = Duration.ofHours(8).plusMinutes(0)

            fun getWakeHour(): Int = wakeUpTime.hour
            fun getWakeMinute(): Int = wakeUpTime.minute

            fun getDurationHour(): Int = duration.toHours().toInt()
            fun getDurationMinute(): Int = (duration.toMinutes() % 60).toInt()

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

            fun getDurationTimeString(): String =
                "${duration.toHours().toString().padStart(2, ' ')}h " +
                    "${(duration.toMinutes()%60).toString().padStart(2, ' ')}m"

            fun getRemainingWakeDuration(): String {
                return "${(LocalTime.now().until(reminderTime, ChronoUnit.HOURS))}h " +
                        "${(LocalTime.now().until(reminderTime, ChronoUnit.MINUTES)%60)}m"
            }

            private fun calcReminderTime() {
               reminderTime = wakeUpTime.minus(duration)
            }

            fun updateAlarm(weekdays: DayOfWeek, requestCode: Int) {
                AlarmHandler.setNewSleepReminderAlarm(
                    dayOfWeek = weekdays,
                    requestCode = requestCode,
                    reminderTime = reminderTime
                )
            }
        }
    }
}