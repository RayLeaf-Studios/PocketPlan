package com.example.j7_003.data

import android.content.Context
import android.os.Build
import android.os.Environment
import com.example.j7_003.data.database_objects.Appointment
import com.google.gson.Gson
import java.io.File

class WeekScheduleHandler(context: Context) {
    val weekSchedule = HashMap<String , ArrayList<Appointment>>(7)
    val weekScheduleFile = setStorageLocation("WeekSchedule.txt", context)
    private val days = arrayOf(MON, TUE, WED, THU, FRI, SAT, SUN)

    companion object {
        val MON = "Monday"
        val TUE = "Tuesday"
        val WED = "Wednesday"
        val THU = "Thursday"
        val FRI = "Friday"
        val SAT = "Saturday"
        val SUN = "Sunday"
    }

    init {
        createFile()
        initMap()
        addAppointmentToDay(FRI, "test", "test", false)
        saveWeekSchedule()
    }

    fun addAppointmentToDay(day: String, title: String, note: String, repetitive: Boolean) {
        weekSchedule[day]?.add(Appointment(title, note, repetitive))
        saveWeekSchedule()
    }

    private fun initMap() {
        for (element in days) {
            weekSchedule[element] = ArrayList<Appointment>()
        }
    }

    private fun saveWeekSchedule() {
        weekScheduleFile.writeText(Gson().toJson(weekSchedule))
    }

    private fun createFile() {
        if(!weekScheduleFile.exists()) weekScheduleFile.writeText("{\"Monday\":[],\"Thursday\":[],\"Friday\":[],\"Sunday\":[],\"Wednesday\":[],\"Tuesday\":[],\"Saturday\":[],\"Sunday\":[]}")
    }

    private fun setStorageLocation(fileName: String, context: Context): File {

        return if (Build.VERSION.SDK_INT < 29) {
            File("${Environment.getDataDirectory()}/data/com.example.j7_003", fileName)
        } else {
            File(context.filesDir, fileName)
        }
    }
}