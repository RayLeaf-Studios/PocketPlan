package com.example.j7_003.data.database

import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.system_interaction.handler.AlarmHandler
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.temporal.ChronoUnit
import kotlin.collections.HashMap

/**
 * A simple class to handle different Reminders for a sleep schedule
 */
class SleepReminder {
    companion object {
        var daysAreCustom: Boolean = false
        private const val fileName: String = "SLEEP_REMINDER"

        var reminder = HashMap<DayOfWeek, Reminder>(7)

        /**
         * Initializes the SleepReminder.
         * @see initMap Initializes the Map for usage.
         * @see createFile Makes the save file accessible.
         * @see load Loads data from the save file into class.
         */
        fun init() {
            initMap()
            createFile()
            load()
        }

        /**
         * Marks the as regular and saves the SleepReminders state
         * to the save file.
         * @see setCustom is the according counterpart.
         */
        fun setRegular() {
            daysAreCustom = false
            save()
        }

        /**
         * Marks the SleepReminder as custom and saved the SleepReminders state to the
         * save file.
         * @see setRegular is the according counterpart.
         */
        fun setCustom() {
            daysAreCustom = true
            save()
        }

        /**
         * Edits a specific Reminders WakeUp time and saves to file.
         * @param day A DayOfWeek object to specify the Reminder to edit.
         * @param hour The hour to set the WakeUp time to.
         * @param minute The minute to set the WakeUp time to.
         */
        fun editWakeUpAtDay(day: DayOfWeek, hour: Int, minute: Int) {
            reminder[day]?.editWakeUpTime(hour, minute)
            updateSingleReminder(day)
            save()
        }

        /**
         * Edits the WakeUp time of all Reminders and saves to file.
         * @param hour The hour to set all Reminders to.
         * @param minute The minute to set all Reminders to.
         */
        fun editAllWakeUp(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editWakeUpTime(hour, minute)
            }
            updateReminder()
            save()
        }

        /**
         * Edits a specific Reminders Duration and saves to file.
         * @param day A DayOfWeek object to specify the Reminder to edit.
         * @param hour The hour the Duration will last.
         * @param minute The amount of minutes the Duration will be set to.
         */
        fun editDurationAtDay(day: DayOfWeek, hour: Int, minute: Int) {
            reminder[day]?.editDuration(hour, minute)
            updateSingleReminder(day)
            save()
        }

        /**
         * Edits the Duration of all Reminders and saves to file.
         * @param hour The amount of hours the Duration will be set to.
         * @param minute The amount of minutes the Duration will be set to.
         */
        fun editAllDuration(hour: Int, minute: Int) {
            reminder.forEach { n ->
                n.value.editDuration(hour, minute)
            }
            updateReminder()
            save()
        }

        /**
         * Returns a string containing the RemainingWakeDuration of the current day.
         * @return A string of the remaining time until the Reminders reminderTime.
         */
        fun getRemainingWakeDurationString(): Pair<String, Int> {

            return reminder[LocalDate.now().dayOfWeek]?.getRemainingWakeDuration()!!
        }

        /**
         * Enables all Reminders to notify the user and saves the SleepReminder to the save file.
         * @see disableAll is the counterpart of this function.
         */
        fun enableAll() {
            reminder.forEach { n ->
                n.value.isSet = true
            }
            updateReminder()
            save()
        }


        /**
         * Disables all Reminders from notifying the user and saves the SleepReminder to file.
         * @see enableAll is the counterpart of this function.
         */
        fun disableAll() {
            reminder.forEach { n ->
                 n.value.isSet = false
            }
            updateReminder()
            save()
        }

        private fun initMap() {
            DayOfWeek.values().forEach { n ->
                reminder[n] = Reminder(n)
            }
        }

        private fun createFile() {
            StorageHandler.createJsonFile(
                fileName,
                "SReminder.json",
                text = Gson().toJson(reminder)
            )
            if (SettingsManager.settings["daysAreCustom"] == null) {
                SettingsManager.addSetting("daysAreCustom", daysAreCustom)
            }
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(StorageHandler.files[fileName], reminder)
            SettingsManager.addSetting("daysAreCustom", daysAreCustom)
        }

        private fun load() {
            val jsonString = StorageHandler.files[fileName]?.readText()

            reminder = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<HashMap<DayOfWeek, Reminder>>() {}.type)

            daysAreCustom = SettingsManager.settings["daysAreCustom"] as Boolean

            updateReminder()
        }

        /**
         * Calls the updateAlarm function on all Reminders with their according requestCodes.
         * @see Reminder.updateAlarm for alarm updating.
         */
         fun updateReminder() {
            reminder.forEach { n ->
                val i = when(n.key) {
                    MONDAY -> 203
                    TUESDAY -> 200
                    WEDNESDAY -> 201
                    THURSDAY -> 202
                    FRIDAY -> 205
                    SATURDAY -> 204
                    SUNDAY -> 206
                }
                n.value.updateAlarm(i)
            }
        }

        private fun updateSingleReminder(dayOfWeek: DayOfWeek) {
           reminder[dayOfWeek]?.updateAlarm(
               when(dayOfWeek) {
                   MONDAY -> 203
                   TUESDAY -> 200
                   WEDNESDAY -> 201
                   THURSDAY -> 202
                   FRIDAY -> 205
                   SATURDAY -> 204
                   SUNDAY -> 206
               }
           )
        }

        /**
         * A simple local class which instances are used to remind the user
         * of his sleeping habits.
         */
        class Reminder(private val weekday: DayOfWeek) {
            var isSet: Boolean = false
            private lateinit var reminderTime: LocalTime
            private var wakeUpTime: LocalTime = LocalTime.of(12, 0)
            private var duration: Duration = Duration.ofHours(8).plusMinutes(0)

            init {
                calcReminderTime()
            }

            /**
             * @return The hour of the Reminders wakeUpTime as int.
             */
            fun getWakeHour(): Int = wakeUpTime.hour

            /**
             * @return The minute of the Reminders wakeUpTime as an int.
             */
            fun getWakeMinute(): Int = wakeUpTime.minute

            /**
             * @return The hour of the Reminders duration as an int.
             */
            fun getDurationHour(): Int = duration.toHours().toInt()

            /**
             * @return The minute of the Reminders duration as an int.
             */
            fun getDurationMinute(): Int = (duration.toMinutes() % 60).toInt()

            /**
             * Changes the Reminders wakeUpTime with the given parameters and
             * then calculates the reminderTime.
             * @param hour The hour to set the wakeUpTime to.
             * @param minute The minute to set the wakeUpTime to.
             */
            fun editWakeUpTime(hour: Int, minute: Int) {
                wakeUpTime = LocalTime.of(hour, minute)
                calcReminderTime()
            }

            /**
             * Changes the Reminders Duration with the given parameters and
             * then calculates the reminderTime.
             * @param hour The hours the duration will last.
             * @param minute The minutes the duration will last.
             */
            fun editDuration(hour: Int, minute: Int) {
                duration = Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
                calcReminderTime()
            }

            /**
             * Marks the Reminder as notifiable.
             * @see disable for the counterpart.
             */
            fun enable(day: DayOfWeek) { isSet = true; updateSingleReminder(day); save() }

            /**
             * Marks the Reminder as not notifiable.
             * @see enable for the counterpart.
             */
            fun disable(day: DayOfWeek) { isSet = false; updateSingleReminder(day); save() }

            /**
             * @return The reminderTime formatted as a string.
             */
            fun getRemindTimeString(): String = reminderTime.toString()

            /**
             * @return The WakeUpTime formatted as a string.
             */
            fun getWakeUpTimeString(): String = wakeUpTime.toString()

            /**
             * @return The Duration formatted as "HHh MMm".
             */
            fun getDurationTimeString(): String =
                "${duration.toHours().toString().padStart(2, '0')}h " +
                    "${(duration.toMinutes()%60).toString().padStart(2, '0')}m"

            /**
             * @return The remainingWakeDuration formatted as "HHh MMm".
             */
            fun getRemainingWakeDuration(): Pair<String, Int> {
                return Pair(
                    "${(LocalTime.now().until(reminderTime, ChronoUnit.HOURS))}h " +
                        "${(LocalTime.now().until(reminderTime, ChronoUnit.MINUTES) % 60)}m",
                    if (!isSet) 2
                    else if (LocalTime.now().until(reminderTime, ChronoUnit.MINUTES) % 60 < 0) 1
                    else 0
                )
            }

            private fun calcReminderTime() {
               reminderTime = wakeUpTime.minus(duration)
            }

            /**
             * Updates the AlarmManager for the calling Reminder.
             * @param requestCode An integer to identify the alarm.
             */
            fun updateAlarm(requestCode: Int) {
                AlarmHandler.setNewSleepReminderAlarm(
                    dayOfWeek = weekday,
                    requestCode = requestCode,
                    wakeUpTime = wakeUpTime,
                    duration = duration,
                    isSet = isSet
                )
            }
        }
    }
}