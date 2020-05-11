package com.example.j7_003.data.database

import com.example.j7_003.system_interaction.handler.StorageHandler
import com.example.j7_003.data.database.database_objects.WeekAppointment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime

class WeekSchedule {
    companion object {
        val weekSchedule = HashMap<DayOfWeek, ArrayList<WeekAppointment>>()
        private const val IDENTIFIER = "WEEK_SCHEDULE"

        fun init() {
            StorageHandler.createJsonFile(
                IDENTIFIER,
                "WeekSchedule.json"
            )

            initMap()
            load()
            addAppointmentToDay("test", "test", DayOfWeek.MONDAY, LocalTime.of(12, 0), Duration.ofHours(4))
        }

        fun editAppointmentAtDay(
            title: String,
            note: String,
            dayOfWeek: DayOfWeek,
            position: Int,
            startTime: LocalTime,
            duration: Duration
        ) {
            val editableAppointment = weekSchedule[dayOfWeek]?.get(position)

            if (editableAppointment != null) {
                editableAppointment.title = title
                editableAppointment.addInfo = note
                editableAppointment.startTime = startTime
                editableAppointment.duration = duration
            }
            save()
        }

        fun addAppointmentToDay(
            title: String,
            note: String,
            dayOfWeek: DayOfWeek,
            startTime: LocalTime,
            duration: Duration
        ) {
            weekSchedule[dayOfWeek]?.add(
                WeekAppointment(
                    title,
                    note,
                    dayOfWeek,
                    startTime,
                    duration
                )
            )
            save()
        }

        fun deleteAppointmentAtDay(dayOfWeek: DayOfWeek, index: Int) {
            weekSchedule[dayOfWeek]?.removeAt(index)
            save()
        }

        private fun initMap() {
            for (element in DayOfWeek.values()) {
                weekSchedule[element] = ArrayList()
            }
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[IDENTIFIER],
                weekSchedule
            )
        }

        private fun load() {
            val jsonString = StorageHandler.files[IDENTIFIER]?.readText()

            return GsonBuilder().create()
                .fromJson(
                    jsonString,
                    object : TypeToken<HashMap<DayOfWeek, HashMap<DayOfWeek, WeekAppointment>>>() {}.type
                )
        }
    }
}