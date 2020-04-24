package com.example.j7_003.data.database

import com.example.j7_003.data.AppointmentColors
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.example.j7_003.data.Weekdays
import com.example.j7_003.data.database.database_objects.WeekAppointment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class WeekSchedule() {
    companion object {
        private val weekSchedule = HashMap<Weekdays, ArrayList<WeekAppointment>>()
        private const val IDENTIFIER = "WEEK_SCHEDULE"

        fun init() {
            StorageHandler.createJsonFile(
                IDENTIFIER,
                "WeekSchedule.json"
            )

            initMap()
            load()
        }

        fun editAppointmentAtDay(
            title: String,
            note: String,
            weekDay: Weekdays,
            position: Int,
            startHour: Int,
            startMinute: Int,
            duration: Int,
            colors: AppointmentColors
        ) {
            val editableAppointment = weekSchedule[weekDay]?.get(position)

            if (editableAppointment != null) {
            /*    editableAppointment.title = title
                editableAppointment.note = note
                editableAppointment.startHour = startHour
                editableAppointment.startMinute = startMinute
                editableAppointment.duration = duration
                editableAppointment.color = colors
            */}
        }

        fun addAppointmentToDay(
            title: String,
            note: String,
            weekDay: Weekdays,
            startHour: Int,
            startMinute: Int,
            duration: Int,
            colors: AppointmentColors
        ) {
            /*weekSchedule[weekDay]?.add(
                WeekAppointment(
                    title,
                    note,
                    weekDay,
                    startHour,
                    startMinute,
                    duration,
                    colors
                )
            )*/
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[IDENTIFIER],
                weekSchedule
            )
        }

        fun deleteAppointmentAtDay(weekDay: Weekdays, index: Int) {
            weekSchedule[weekDay]?.removeAt(index)
        }

        private fun initMap() {
            for (element in Weekdays.values()) {
                weekSchedule[element] = ArrayList()
            }
        }

        private fun load() {
            val jsonString = StorageHandler.files[IDENTIFIER]?.readText()

            return GsonBuilder().create()
                .fromJson(
                    jsonString,
                    object : TypeToken<HashMap<Weekdays, ArrayList<WeekAppointment>>>() {}.type
                )
        }
    }
}