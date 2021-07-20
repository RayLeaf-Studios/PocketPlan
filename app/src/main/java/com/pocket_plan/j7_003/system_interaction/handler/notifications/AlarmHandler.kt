package com.pocket_plan.j7_003.system_interaction.handler.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.system_interaction.Logger
import com.pocket_plan.j7_003.system_interaction.receiver.NotificationReceiver
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class AlarmHandler {
    companion object {
        fun setBirthdayAlarms(hour: Int = 12, minute: Int = 0, context: Context) {
            val logger = Logger(context)
            try {
                logger.log("AH", "Creating intent")
                val intent = Intent(context, NotificationReceiver::class.java)
                intent.putExtra("Notification", "Birthday")
                logger.log("AH", "Created intent and added key-value")

                logger.log("AH", "Getting Broadcast of created intent")
                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        100,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )
                logger.log("AH", "Got Broadcast")

                logger.log("AH", "Getting AlarmManager instance")
                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager
                logger.log("AH", "Got AlarmManager instance")

                logger.log("AH", "Getting LocalDateTime")
                var notificationTime = LocalDateTime.now()
                logger.log("AH", "Got LocalDateTime")

                logger.log("AH", "Adjusting time for current day")
                if (notificationTime.isAfter(
                        LocalDateTime.now()
                            .withHour(hour).withMinute(minute).withSecond(0)
                    )
                ) {
                    notificationTime = notificationTime.plusDays(1)
                }
                logger.log("AH", "Adjusted time for current day")

                logger.log("AH", "Adjusting time itself")
                notificationTime = notificationTime
                    .withHour(hour).withMinute(minute)
                    .withSecond(0).withNano(0)
                logger.log("AH", "Adjusted time itself")

                // debug
//            val debugTime = LocalDateTime.now().plusSeconds(30)
//            notificationTime = debugTime

                logger.log("AH", "Converting to unix time")
                val epochTimeToReminder =
                    notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                logger.log("AH", "Converted to unix time")

//                alarmManager.setExact(
//                    AlarmManager.RTC,
//                    notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
//                    pendingIntent
//                )

                // Schedule alarm
                logger.log("AH", "Scheduling the alarm")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        epochTimeToReminder,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, epochTimeToReminder, pendingIntent);
                }
                logger.log("AH", "Scheduled the alarm")

            } catch (e: Exception) {
                logger.log("AH", "--------------------------------------------------")
                logger.log("AH", "--------------------------------------------------")
                if (e.message is String)
                    logger.log("AH", e.message!!)
                else
                    logger.log("AH", "No message from the exception")
                logger.log("AH", "")
                logger.log("AH", e.stackTraceToString())
                logger.log("AH", "--------------------------------------------------")
                logger.log("AH", "--------------------------------------------------")
            }
        }

        fun setNewSleepReminderAlarm(
            context: Context, dayOfWeek: DayOfWeek,
            reminderTime: LocalDateTime, requestCode: Int, isSet: Boolean
        ) {
            val intent = Intent(context, NotificationReceiver::class.java)

            intent.putExtra("Notification", "SReminder")
            intent.putExtra("weekday", dayOfWeek.toString())
            intent.putExtra("requestCode", requestCode)

            val pendingIntent: PendingIntent = PendingIntent.getBroadcast(
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (isSet) {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    pendingIntent
                )
            } else {
                alarmManager.cancel(pendingIntent)
            }
        }
    }
}