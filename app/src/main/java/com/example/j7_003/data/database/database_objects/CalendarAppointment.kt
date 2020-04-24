package com.example.j7_003.data.database.database_objects

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalTime

data class CalendarAppointment(
    @SerializedName(value = "cATitle")
    override var title: String,

    @SerializedName(value = "cAAddInfo")
    override var addInfo: String,

    var date: org.threeten.bp.LocalDate,

    @SerializedName(value = "cATime")
    override var sTime: LocalTime,

    var eTime: LocalTime = sTime
) : Appointment(title, addInfo, sTime)