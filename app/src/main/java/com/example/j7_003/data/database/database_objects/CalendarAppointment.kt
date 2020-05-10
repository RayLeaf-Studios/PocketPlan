package com.example.j7_003.data.database.database_objects

import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

open class CalendarAppointment(
    open var title: String,
    open var addInfo: String,
    var dateTime: LocalDateTime,
    var eTime: LocalTime
) {
    constructor(title: String, addInfo: String): this(title, addInfo, LocalDateTime.now(), LocalTime.of(0, 0))
}