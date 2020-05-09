package com.example.j7_003.system_interaction.handler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.MainActivity
import com.example.j7_003.system_interaction.receiver.NotificationReceiver
import org.threeten.bp.*
import org.threeten.bp.temporal.ChronoUnit
import java.util.*

class AlarmHandler {
    companion object {
        fun setBirthdayAlarms(hour: Int = 12, minute: Int = 0, context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "Birthday")

            val pendingIntent =
                PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager =
                context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

            val notificationTime = Calendar.getInstance()

            if (notificationTime.get(Calendar.HOUR_OF_DAY) >= hour) {
                if (notificationTime.get(Calendar.MINUTE) >= minute) {
                    notificationTime.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            notificationTime.set(Calendar.HOUR_OF_DAY, hour)
            notificationTime.set(Calendar.MINUTE, minute)
            notificationTime.set(Calendar.SECOND, 0)

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                notificationTime.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }

        fun setNewSleepReminderAlarm(
            context: Context = MainActivity.myActivity,
            dayOfWeek: DayOfWeek,
            reminderTime: LocalDateTime,
            requestCode: Int,
            isSet: Boolean
        ) {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "SReminder")
            intent.putExtra("weekday", dayOfWeek.toString())
            intent.putExtra("requestCode", requestCode)

            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

//            if (isSet) {
//                alarmManager.setRepeating(
//                    AlarmManager.RTC_WAKEUP,
//                    //LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
//                    //reminderTime.atZone(ZoneId.systemDefault()).plusHours(2).toInstant().toEpochMilli(),
//                    //reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
//                    LocalDateTime.now().until(reminderTime, ChronoUnit.MILLIS),
//                    86400000,
//                    pendingIntent
//                )
//            } else {
//                alarmManager.cancel(pendingIntent)
//            }
//            Log.e("debug", "${ZoneId.systemDefault()}\n${reminderTime.dayOfWeek}:\t$reminderTime" +
//                    "\n${reminderTime.dayOfWeek}:\t${reminderTime.atZone(ZoneId.systemDefault()).plusHours(2).toInstant()}" +
//                    "\n${reminderTime.atZone(ZoneId.systemDefault()).plusHours(2).toInstant().toEpochMilli()}" +
//                    "\n${LocalDateTime.now().until(reminderTime, ChronoUnit.MILLIS)}" +
//                    "\n$pendingIntent" +
//                    "\n$requestCode" +
//                    "\n$isSet"
//            )
        }
    }
}