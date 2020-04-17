package com.example.j7_003.system_interaction.handler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.data.database.SleepReminder
import com.example.j7_003.system_interaction.receiver.NotificationReceiver
import java.util.*

class AlarmHandler {
    companion object {
        fun setBirthdayAlarms(hour: Int = 12, minute: Int = 0, context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "Birthday")

            val pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

            val notificationTime = Calendar.getInstance()

            if (notificationTime.get(Calendar.HOUR_OF_DAY) > hour) {
                if (notificationTime.get(Calendar.MINUTE) > minute) {
                    notificationTime.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            notificationTime.set(Calendar.HOUR_OF_DAY, hour)
            notificationTime.set(Calendar.MINUTE, minute)
            notificationTime.set(Calendar.SECOND, 0)

            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        }

        fun setSleepReminderAlarm(context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "SReminder")
            intent.putExtra(
                "SReminder",
                SleepReminder.days
            )

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                200,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val notificationTime = Calendar.getInstance()

            if (notificationTime.get(Calendar.HOUR_OF_DAY) > SleepReminder.timings[0]) {
                if (notificationTime.get(Calendar.MINUTE) > SleepReminder.timings[1]) {
                    notificationTime.add(Calendar.DAY_OF_MONTH, 1)
                }
            }

            notificationTime.set(Calendar.HOUR_OF_DAY, SleepReminder.timings[0])
            notificationTime.set(Calendar.MINUTE, SleepReminder.timings[1])
            notificationTime.set(Calendar.SECOND, 0)

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                notificationTime.timeInMillis,
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )
        }
    }
}