package com.example.j7_003.data.database.database_objects

data class CalendarAppointment(
    var date: org.threeten.bp.LocalDate,
    var eTime: org.threeten.bp.LocalTime
) : Appointment()