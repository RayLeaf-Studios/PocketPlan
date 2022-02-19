package com.pocket_plan.j7_003.data.sleepreminder

import android.content.Context
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.data.settings.SettingId
import org.threeten.bp.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters
import java.lang.NullPointerException
import kotlin.math.abs

/**
 * A simple class to handle different Reminders for a sleep schedule
 */
class SleepReminder(passedContext: Context) : Checkable{
    var myContext = passedContext
    var daysAreCustom: Boolean = SettingsManager.getSetting(SettingId.DAYS_ARE_CUSTOM) as Boolean
    var reminder = HashMap<DayOfWeek, Reminder>(7)

    init {
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
     * Checks if any reminder is set.
     * @return Boolean depending on weather at least one reminder is
     * set or not.
     */
    fun isAnySet(): Boolean {
        reminder.forEach { n ->
            if (n.value.isSet) {
                return true
            }
        }
        return false
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
        //ignore this mess, it ain't pretty, but it works as intended
        val nextTwoReminder = getNextTwoReminder()
        val nextReminder = nextTwoReminder.first.nextReminder
        val nextSet = nextTwoReminder.first.isSet
        val priorReminder = nextTwoReminder.second.nextReminder.minusDays(7)
        val priorSet = nextTwoReminder.second.isSet
        val priorReminderDuration = nextTwoReminder.second.duration
        val localDateTime = LocalDateTime.now()

        return if (localDateTime.isBefore(priorReminder.plus(priorReminderDuration))) {
            Pair(
                "${localDateTime.until(priorReminder, ChronoUnit.HOURS)}h " +
                        "${localDateTime.until(priorReminder, ChronoUnit.MINUTES) % 60}m",
                if (priorSet) 1
                else 2
            )
        } else {
            Pair(
                "${localDateTime.until(nextReminder, ChronoUnit.HOURS)}h " +
                        "${localDateTime.until(nextReminder, ChronoUnit.MINUTES) % 60}m",
                if (nextSet) 0
                else 2
            )
        }
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
            reminder[n] = Reminder(n, this)
        }
    }

    private fun createFile() {
        StorageHandler.createJsonFile(
            StorageId.SLEEP, text = Gson().toJson(reminder)
        )
    }

    private fun save() {
        StorageHandler.saveAsJsonToFile(StorageHandler.files[StorageId.SLEEP], reminder)
        SettingsManager.addSetting(SettingId.DAYS_ARE_CUSTOM, daysAreCustom)
    }

    private fun load() {
        val jsonString = StorageHandler.files[StorageId.SLEEP]?.readText()

        reminder = GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<HashMap<DayOfWeek, Reminder>>() {}.type)

        daysAreCustom = SettingsManager.getSetting(SettingId.DAYS_ARE_CUSTOM) as Boolean

        reminder.forEach { entry ->
            entry.value.mySleepReminder = this
        }

        updateReminder()
    }

    private fun getNextTwoReminder(): Pair<Reminder, Reminder> {
        val dateTime = LocalDateTime.now()
        var closestReminder: Reminder = reminder[dateTime.dayOfWeek]!!
        var closestTime = dateTime.until(closestReminder.nextReminder, ChronoUnit.MINUTES)
        reminder.forEach { n ->
            if (abs(dateTime.until(n.value.nextReminder, ChronoUnit.MINUTES)) < closestTime) {
                closestReminder = n.value
                closestTime = dateTime.until(closestReminder.nextReminder, ChronoUnit.MINUTES)
            }
        }

        val priorReminder = if (
            closestReminder.nextReminder.plus(closestReminder.duration).dayOfMonth == closestReminder.nextReminder.dayOfMonth
        ) {
            reminder[closestReminder.nextReminder.minusDays(1).dayOfWeek]
        } else {
            reminder[closestReminder.nextReminder.dayOfWeek]
        }

        return Pair(
            closestReminder,
            priorReminder!!
        )
    }

    /**
     * Calls the updateAlarm function on all Reminders with their according requestCodes.
     * @see Reminder.updateAlarm for alarm updating.
     */
    fun updateReminder() {
        reminder.forEach { n ->
            val i = when (n.key) {
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
        save()
    }

    fun updateSingleReminder(dayOfWeek: DayOfWeek) {
        reminder[dayOfWeek]?.updateAlarm(
            when (dayOfWeek) {
                MONDAY -> 203
                TUESDAY -> 200
                WEDNESDAY -> 201
                THURSDAY -> 202
                FRIDAY -> 205
                SATURDAY -> 204
                SUNDAY -> 206
            }
        )
        save()
    }

    override fun check() {
        reminder.forEach {
            if(it.key == null || it.value == null){
                throw NullPointerException()
            }
        }
    }

    /**
     * A simple local class which instances are used to remind the user
     * of his sleeping habits.
     */
    inner class Reminder(
        @SerializedName(value = "w")
        private val weekday: DayOfWeek,
        @Transient
        var mySleepReminder: SleepReminder
    ) {
        @SerializedName(value = "i")
        var isSet: Boolean = false

        @SerializedName(value = "wT")
        var wakeUpTime: LocalTime = LocalTime.of(9, 0)

        @SerializedName(value = "d")
        var duration: Duration = Duration.ofHours(8).plusMinutes(0)

        @SerializedName(value = "n")
        var nextReminder: LocalDateTime = getNextReminderCustom()

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
            updateReminderState()
        }

        /**
         * Changes the Reminders Duration with the given parameters and
         * then calculates the reminderTime.
         * @param hour The hours the duration will last.
         * @param minute The minutes the duration will last.
         */
        fun editDuration(hour: Int, minute: Int) {
            duration = Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
            updateReminderState()
        }

        /**
         * Marks the Reminder as notifiable.
         * @see disable for the counterpart.
         */
        fun enable(day: DayOfWeek) {
            isSet = true; mySleepReminder.updateSingleReminder(day)
        }

        /**
         * Marks the Reminder as not notifiable.^^
         * @see enable for the counterpart.
         */
        fun disable(day: DayOfWeek) {
            isSet = false; mySleepReminder.updateSingleReminder(day)
        }

        /**
         * @return The WakeUpTime formatted as a string.
         */
        fun getWakeUpTimeString(): String = wakeUpTime.toString()


        /**
         * @return The Duration formatted as "HHh MMm".
         */
        fun getDurationTimeString(): String =
            "${duration.toHours().toString().padStart(2, '0')}h " +
                    "${(duration.toMinutes() % 60).toString().padStart(2, '0')}m"

        private fun updateReminderState() {
            nextReminder = getNextReminderCustom()
        }

        /**
         * Updates the AlarmManager for the calling Reminder.
         * @param requestCode An integer to identify the alarm.
         */
        fun updateAlarm(requestCode: Int) {
            updateReminderState()
            AlarmHandler.setNewSleepReminderAlarm(
                dayOfWeek = weekday,
                requestCode = requestCode,
                reminderTime = nextReminder,
                isSet = isSet,
                context = mySleepReminder.myContext
            )
        }

        private fun getNextReminderCustom(): LocalDateTime {
            val nextReminder = LocalDateTime.now()
                .with(TemporalAdjusters.next(weekday)).with(wakeUpTime)
                .minus(duration)

            return if (nextReminder.toLocalDate() == LocalDate.now()) {
                if (nextReminder.isAfter(LocalDateTime.now())) nextReminder
                else nextReminder.plusDays(7)
            } else if (nextReminder.minusDays(7).isAfter(LocalDateTime.now())) {
                nextReminder.minusDays(7)
            } else nextReminder
        }
    }
}