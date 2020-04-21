package com.example.j7_003.system_interaction.handler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.MainActivity
import com.example.j7_003.data.Weekdays
import com.example.j7_003.data.database.SleepReminder
import com.example.j7_003.system_interaction.receiver.NotificationReceiver
import org.threeten.bp.*
import org.threeten.bp.temporal.TemporalAdjuster
import org.threeten.bp.temporal.TemporalAdjusters
import java.util.*

class AlarmHandler {
    companion object {
        fun setBirthdayAlarms(hour: Int = 12, minute: Int = 0, context: Context) {
            val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "Birthday")

            val pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarmManager = context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

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
            requestCode: Int,
            reminderTime: LocalTime
        ) {
            val intent: Intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "SReminder")
            intent.putExtra("ReminderDay", "$dayOfWeek")

            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val notificationTime: LocalDateTime
            val now: LocalDate = LocalDate.now()

            fun nowOrNext(): Int {
                val day: DayOfWeek = now.dayOfWeek


                return if (dayOfWeek == day) {
                    if (LocalTime.now().isAfter(reminderTime)) {
                        now.with(TemporalAdjusters.next(day)).dayOfMonth
                    } else {
                        now.dayOfMonth
                    }
                } else {
                    now.with(TemporalAdjusters.next(dayOfWeek)).dayOfMonth
                }
                /*return if (reminderTime.isBefore(LocalTime.now())) {
                    LocalDate.now().plusDays(7).dayOfMonth
                } else LocalDate.now().dayOfMonth*/
            }

            notificationTime = LocalDateTime.of(
                now.year,
                now.month,
                nowOrNext(),
                reminderTime.hour,
                reminderTime.minute
            )

            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
            )

            Log.e("debug", notificationTime.toString())
            Log.e("debug", pendingIntent.toString())
            Log.e("debug", dayOfWeek.toString())
        }

        fun setSleepReminderAlarm(context: Context) {
           val intent = Intent(context, NotificationReceiver::class.java)
            intent.putExtra("Notification", "SReminder")

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

            SleepReminder.init()
            if (SleepReminder.isSet) {
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    notificationTime.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )
            }else{
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}