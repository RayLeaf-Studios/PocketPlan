package com.example.j7_003.data.database.database_objects

import com.google.gson.annotations.SerializedName
import org.threeten.bp.DayOfWeek
import org.threeten.bp.Duration
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

data class WeekAppointment(
    @SerializedName(value = "wTitle")
    override var title: String,

    @SerializedName(value = "wAddInfo")
    override var addInfo: String,

    var dayOfWeek: DayOfWeek,
    var startTime: LocalTime,
    var duration: Duration
) : CalendarAppointment(title, addInfo)