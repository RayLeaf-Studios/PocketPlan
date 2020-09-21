package com.example.j7_003.data.calendar

import com.example.j7_003.data.calendar.weekschedule.WeekSchedule
import com.example.j7_003.system_interaction.handler.storage.StorageHandler
import com.example.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
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
            StorageHandler.createJsonFile(StorageId.CALENDAR, "Calendar.json")
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
            calendar.add(
                CalendarAppointment(
                title,
                addInfo,
                dateTime.toLocalTime(),
                dateTime.toLocalDate(), eTime)
            )
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
            appointment.startTime = newDateTime.toLocalTime()
            appointment.date = newDateTime.toLocalDate()
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
        fun getAppointment(index: Int): CalendarAppointment = calendar[index]

        fun getDayView(date: LocalDate): ArrayList<CalendarAppointment> {
            val dateList = ArrayList<CalendarAppointment>()
            calendar.forEach { n ->
                if (n.date.isEqual(date)) {
                    dateList.add(n)
                }
            }

            WeekSchedule.init()
            dateList.addAll(WeekSchedule.weekSchedule[date.dayOfWeek] as Collection<CalendarAppointment>)
            dateList.sortBy { it.startTime }

            return dateList
        }

        private fun sort() {
            calendar.sortWith(compareBy({ it.date }, {it.startTime}, { it.title }))
        }

        private fun save() {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[StorageId.CALENDAR], calendar)
        }

        private fun initCalendar() {
            val jsonString = StorageHandler.files[StorageId.CALENDAR]?.readText()

            calendar = GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<CalendarAppointment>>() {}.type)
        }
    }
}