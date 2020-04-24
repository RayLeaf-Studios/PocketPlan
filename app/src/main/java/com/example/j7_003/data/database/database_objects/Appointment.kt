package com.example.j7_003.data.database.database_objects

import org.threeten.bp.LocalTime

open class Appointment(
    open var title: String,
    open var addInfo: String,
    open var sTime: LocalTime
)