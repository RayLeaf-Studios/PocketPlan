package com.pocket_plan.j7_003.data.calendar.weekschedule
//          <---------- V.2 ---------->

//import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
//import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
//import com.google.gson.GsonBuilder
//import com.google.gson.reflect.TypeToken
//import org.threeten.bp.DayOfWeek
//import org.threeten.bp.Duration
//import org.threeten.bp.LocalTime

class WeekSchedule {
//    companion object {
//        val weekSchedule = HashMap<DayOfWeek, ArrayList<WeekAppointment>>()
//
//        fun init() {
//            StorageHandler.createJsonFile(
//                StorageId.WEEK)
//
//            initMap()
//            load()
//        }
//
//        fun editAppointmentAtDay(
//            title: String,
//            note: String,
//            dayOfWeek: DayOfWeek,
//            position: Int,
//            startTime: LocalTime,
//            duration: Duration
//        ) {
//            val editableAppointment = weekSchedule[dayOfWeek]?.get(position)
//
//            if (editableAppointment != null) {
//                editableAppointment.title = title
//                editableAppointment.addInfo = note
//                editableAppointment.startTime = startTime
//                editableAppointment.duration = duration
//            }
//            save()
//        }
//
//        fun addAppointmentToDay(
//            title: String,
//            note: String,
//            dayOfWeek: DayOfWeek,
//            startTime: LocalTime,
//            duration: Duration
//        ) {
//            weekSchedule[dayOfWeek]?.add(
//                WeekAppointment(
//                    title,
//                    note,
//                    startTime,
//                    dayOfWeek,
//                    duration
//                )
//            )
//            save()
//        }
//
//        fun deleteAppointmentAtDay(dayOfWeek: DayOfWeek, index: Int) {
//            weekSchedule[dayOfWeek]?.removeAt(index)
//            save()
//        }
//
//        private fun initMap() {
//            for (element in DayOfWeek.values()) {
//                weekSchedule[element] = ArrayList()
//            }
//        }
//
//        private fun save() {
//            StorageHandler.saveAsJsonToFile(
//                StorageHandler.files[StorageId.WEEK], weekSchedule)
//        }
//
//        private fun load() {
//            val jsonString = StorageHandler.files[StorageId.WEEK]?.readText()
//
//            return GsonBuilder().create()
//                .fromJson(
//                    jsonString,
//                    object : TypeToken<HashMap<DayOfWeek, ArrayList<WeekAppointment>>>() {}.type
//                )
//        }
//    }
}