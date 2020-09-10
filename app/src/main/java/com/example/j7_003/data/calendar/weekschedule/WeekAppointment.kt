package com.example.j7_003.data.calendar.weekschedule

import com.example.j7_003.data.calendar.CalendarAppointment
import com.google.gson.annotations.SerializedName
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalTime

data class WeekAppointment(
    @SerializedName(value = "wTitle")
    override var title: String,

    @SerializedName(value = "wAddInfo")
    override var addInfo: String,

    @SerializedName(value = "wStartTime")
    override var startTime: LocalTime,

    var dayOfWeek: DayOfWeek,
    var duration: Duration
) : CalendarAppointment(title, addInfo, startTime)