package com.example.j7_003.data.database.database_objects

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalTime

data class CalendarAppointment(
    @SerializedName(value = "cATitle")
    override var title: String,

    @SerializedName(value = "cAAddInfo")
    override var addInfo: String,

    @SerializedName(value = "dTime")
    override var dateTime: org.threeten.bp.LocalDateTime,

    var eTime: LocalTime
) : Appointment(title, addInfo, dateTime)