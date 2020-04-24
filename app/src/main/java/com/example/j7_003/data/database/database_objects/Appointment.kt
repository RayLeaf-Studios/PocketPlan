package com.example.j7_003.data.database.database_objects

import org.threeten.bp.LocalTime

open class Appointment(
    var title: String = "",
    var addInfo: String = "",
    var sTime: LocalTime = LocalTime.MIDNIGHT
)