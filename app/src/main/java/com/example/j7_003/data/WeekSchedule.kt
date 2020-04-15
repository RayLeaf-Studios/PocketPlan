package com.example.j7_003.data

import com.example.j7_003.data.database_objects.WeekAppointment
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class WeekSchedule() {
    val weekSchedule = HashMap<Weekdays , ArrayList<WeekAppointment>>()
    private val IDENTIFIER = "WEEK_SCHEDULE"

    init {
        StorageHandler.createJsonFile(IDENTIFIER, "WeekSchedule.json")
        initMap()
        load()
    }

    fun addAppointmentToDay(title: String, note: String, weekDay: Weekdays, startHour: Int, startMinute: Int, duration: Int) {
        weekSchedule[weekDay]?.add(WeekAppointment(title, note, weekDay, startHour, startMinute, duration, AppointmentColors.RED))
        StorageHandler.saveAsJsonToFile(StorageHandler.files[IDENTIFIER], weekSchedule)
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