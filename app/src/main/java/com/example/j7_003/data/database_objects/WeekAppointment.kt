package com.example.j7_003.data.database_objects

import com.example.j7_003.data.AppointmentColors
import com.example.j7_003.data.Weekdays

data class WeekAppointment(
    var title: String,
    var note: String,
    var weekday: Weekdays,
    var startHour: Int,
    var startMinute: Int,
    var duration: Int,
    var color: AppointmentColors
)