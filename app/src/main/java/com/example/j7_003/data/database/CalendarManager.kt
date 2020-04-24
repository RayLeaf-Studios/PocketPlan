package com.example.j7_003.data.database

import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

/**
 * A class to handle Appointments according the gregorian calendar.
 */
class CalendarManager {
    companion object {
        var calendar = ArrayList<CalendarAppointment>()

        /**
         * Initializes the calendar, the file is registered, the data loaded from a file
         * and sorts the list.
         */
        fun init() {
            StorageHandler.createJsonFile("CALENDAR", "Calendar.json")
            initCalendar()
            sort()
        }

        /**
         * Adds a CalendarAppointment, sorts the list and saves the list to file.
         * @param title Title of the CalendarAppointment.
         * @param addInfo Additional information of the Appointment.
         * @param dateTime Time parameters of the Appointment, formatted as LocalDateTime.
         * @param eTime Time the Appointment ends, formatted as LocalTime.
         */
        fun addAppointment(
            title : String,
            addInfo: String,
            dateTime: LocalDateTime,
            eTime: LocalTime
        ) {
            calendar.add(CalendarAppointment(title, addInfo, dateTime, eTime))
            sort()
            save()
        }

        /**
         * Edits the requested Appointment with the given parameters, sorts the list
         * and saves it to file.
         * @param index Position of the requested Appointment.
         * @param title Title of the CalendarAppointment.
         * @param addInfo Additional information of the Appointment.
         * @param newDateTime Time parameters of the Appointment, formatted as LocalDateTime.
         * @param eTime Time the Appointment ends, formatted as LocalTime.
         */
        fun editAppointment(
            index: Int,
            title: String,
            addInfo: String,
            newDateTime: LocalDateTime,
            eTime: LocalTime
        ) {
            val appointment = getAppointment(index)
            appointment.title = title
            appointment.addInfo = addInfo
            appointment.dateTime = newDateTime
            appointment.eTime = eTime

            sort()
            save()
        }

        /**
         * Deletes the requested Appointment, sorts the list and saves it to file.
         * @param index Position of the requested Appointment.
         */
        fun deleteAppointment(index: Int) {
            calendar.removeAt(index)
            sort()
            save()
        }

        /**
         * Fetches an Appointment from a given position in the list.
         * @param index Position of the requested Appointment.
         * @return The Appointment from the list at a given position.
         */
        fun getAppointment(index: Int): CalendarAppointment =
            calendar[index]

        private fun sort() {
            calendar.sortWith(compareBy({ it.dateTime }, { it.title }))
        }

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