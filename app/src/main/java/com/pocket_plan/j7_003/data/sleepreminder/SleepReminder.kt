package com.pocket_plan.j7_003.data.sleepreminder

import android.content.Context
import android.util.Log
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.annotation.Factory
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.threeten.bp.*
import org.threeten.bp.DayOfWeek.*
import org.threeten.bp.temporal.ChronoUnit
import org.threeten.bp.temporal.TemporalAdjusters
import java.lang.NullPointerException
import kotlin.math.abs

private const val TAG = "SleepReminder"

/**
 * A simple class to handle different Reminders for a sleep schedule
 */
@Factory
class SleepReminder(
    passedContext: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : Checkable, KoinComponent {

    private val preferencesHandler: PreferencesHandler by inject()

    var myContext = passedContext
    var daysAreCustom: Boolean = preferencesHandler.getDefault(PreferencesHandler.DAYS_ARE_CUSTOM)
    var reminder = HashMap<DayOfWeek, Reminder>(7)

    init {
        runBlocking {
            daysAreCustom = preferencesHandler.read(PreferencesHandler.DAYS_ARE_CUSTOM).first()
        }

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
        reminder.forEach { n -> n.value.editWakeUpTime(hour, minute) }
        updateReminder()
        save()
    }

    fun editDurationAtDay(day: DayOfWeek, hour: Int, minute: Int) {
        reminder[day]?.editDuration(hour, minute)
        updateSingleReminder(day)
        save()
    }

    fun isAnySet(): Boolean {
        reminder.forEach { n ->
            if (n.value.isSet) return true
        }
        return false
    }

    fun editAllDuration(hour: Int, minute: Int) {
        reminder.forEach { n -> n.value.editDuration(hour, minute) }
        updateReminder()
        save()
    }

    fun getRemainingWakeDurationString(): Pair<String, Int> {
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
                if (priorSet) 1 else 2
            )
        } else {
            Pair(
                "${localDateTime.until(nextReminder, ChronoUnit.HOURS)}h " +
                        "${localDateTime.until(nextReminder, ChronoUnit.MINUTES) % 60}m",
                if (nextSet) 0 else 2
            )
        }
    }

    fun enableAll() {
        reminder.forEach { n -> n.value.isSet = true }
        updateReminder()
        save()
    }

    fun disableAll() {
        reminder.forEach { n -> n.value.isSet = false }
        updateReminder()
        save()
    }

    private fun initMap() {
        entries.forEach { n -> reminder[n] = Reminder(n, this) }
    }

    private fun createFile() {
        StorageHandler.createJsonFile(
            StorageId.SLEEP,
            text = Gson().toJson(reminder)
        )
    }

    private fun save() {
        StorageHandler.saveAsJsonToFile(StorageHandler.files[StorageId.SLEEP], reminder)
        runBlocking {
            preferencesHandler.save(PreferencesHandler.DAYS_ARE_CUSTOM, daysAreCustom)
        }
    }

    private fun load() {
        val file = StorageHandler.files[StorageId.SLEEP]
        val raw = file?.takeIf { it.exists() }?.readText()?.trim()
        val gson: Gson = GsonBuilder().create()
        val mapType = object : TypeToken<HashMap<DayOfWeek, Reminder>>() {}.type

        fun freshDefaults(): HashMap<DayOfWeek, Reminder> {
            val fresh = HashMap<DayOfWeek, Reminder>(7)
            entries.forEach { fresh[it] = Reminder(it, this) }
            StorageHandler.saveAsJsonToFile(StorageHandler.files[StorageId.SLEEP], fresh)
            Log.w(TAG, "Loaded defaults for SLEEP due to missing or invalid JSON")
            return fresh
        }

        reminder = try {
            when {
                raw.isNullOrBlank() -> freshDefaults()

                raw.first() == '{' -> {
                    gson.fromJson<HashMap<DayOfWeek, Reminder>>(raw, mapType)
                        ?: freshDefaults()
                }

                raw.first() == '"' -> {
                    // Handle double-encoded JSON (file contains a JSON string that itself is JSON)
                    val inner = runCatching { gson.fromJson(raw, String::class.java) }.getOrNull()
                    if (!inner.isNullOrBlank() && inner.trim().startsWith("{")) {
                        gson.fromJson<HashMap<DayOfWeek, Reminder>>(inner.trim(), mapType)
                            ?: freshDefaults()
                    } else {
                        freshDefaults()
                    }
                }

                else -> freshDefaults()
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to parse SLEEP JSON. Resetting. ${t.message}")
            freshDefaults()
        }

        daysAreCustom = runBlocking(ioDispatcher) {
            preferencesHandler.read(PreferencesHandler.DAYS_ARE_CUSTOM).first()
        }

        reminder.forEach { entry -> entry.value.mySleepReminder = this }

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

        return Pair(closestReminder, priorReminder!!)
    }

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
            if (it.key == null || it.value == null) throw NullPointerException()
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

        fun getWakeHour(): Int = wakeUpTime.hour
        fun getWakeMinute(): Int = wakeUpTime.minute
        fun getDurationHour(): Int = duration.toHours().toInt()
        fun getDurationMinute(): Int = (duration.toMinutes() % 60).toInt()

        fun editWakeUpTime(hour: Int, minute: Int) {
            wakeUpTime = LocalTime.of(hour, minute)
            updateReminderState()
        }

        fun editDuration(hour: Int, minute: Int) {
            duration = Duration.ofHours(hour.toLong()).plusMinutes(minute.toLong())
            updateReminderState()
        }

        fun enable(day: DayOfWeek) {
            isSet = true; mySleepReminder.updateSingleReminder(day)
        }

        fun disable(day: DayOfWeek) {
            isSet = false; mySleepReminder.updateSingleReminder(day)
        }

        fun getWakeUpTimeString(): String = wakeUpTime.toString()

        fun getDurationTimeString(): String =
            "${duration.toHours().toString().padStart(2, '0')}h " +
                    "${(duration.toMinutes() % 60).toString().padStart(2, '0')}m"

        private fun updateReminderState() {
            nextReminder = getNextReminderCustom()
        }

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
