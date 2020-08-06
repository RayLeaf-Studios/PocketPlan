package com.example.j7_003.data.database.database_objects

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

open class CalendarAppointment(
    open var title: String,
    open var addInfo: String,
    open var startTime: LocalTime,
    var date: LocalDate,
    var eTime: LocalTime
) {
    constructor(
        title: String,
        addInfo: String,
        startTime: LocalTime
    ): this(title, addInfo, startTime, LocalDate.now(), LocalTime.of(0, 0))
}