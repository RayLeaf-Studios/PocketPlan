package com.example.j7_003.data.database

import com.example.j7_003.data.database.database_objects.Appointment
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class Calendar {
    companion object {
        var calendar = ArrayList<CalendarAppointment>()

        fun init() {
            StorageHandler.createJsonFile("CALENDAR", "Calendar.json")
            initCalendar()
        }

        fun addAppointment(title : String, addInfo: String, date: LocalDate, time: LocalTime) {
            calendar.add(CalendarAppointment(title, addInfo, date, time))
            save()
        }

        fun editAppointment(
            index: Int,
            title: String,
            addInfo: String,
            date: LocalDate,
            sTime: LocalTime,
            eTime: LocalTime = sTime
        ) {
            val appointment = getAppointment(index)
            appointment.title = title
            appointment.addInfo = addInfo
            appointment.date = date
            appointment.sTime = sTime
            appointment.eTime = eTime

            save()
        }

        fun deleteAppointment(index: Int) {
            calendar.removeAt(index)
            save()
        }

        fun getAppointment(index: Int): CalendarAppointment =
            calendar[index]

        private fun save() {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files["CALENDAR"],
                calendar
            )
        }

        private fun initCalendar() {
            val jsonString = StorageHandler.files["CALENDAR"]?.readText()

            calendar = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<CalendarAppointment>>() {}.type)
        }
    }
}