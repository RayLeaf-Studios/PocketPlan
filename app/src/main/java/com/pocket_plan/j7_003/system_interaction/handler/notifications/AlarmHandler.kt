package com.pocket_plan.j7_003.system_interaction.handler.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import com.pocket_plan.j7_003.system_interaction.receiver.NotificationReceiver
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId

class AlarmHandler {
    companion object {
        fun setBirthdayAlarms(time: String = "12:00", context: Context) {
            try {
                val hour = time.split(":")[0].toInt()
                val minute = time.split(":")[1].toInt()
                val intent = Intent(context, NotificationReceiver::class.java)
                intent.putExtra("Notification", "Birthday")

                val pendingIntent =
                    PendingIntent.getBroadcast(
                        context,
                        100,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                    )

                val alarmManager =
                    context.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

                var notificationTime = LocalDateTime.now()
                    .withHour(hour).withMinute(minute)
                    .withSecond(0).withNano(0)

                //schedule for tomorrow if today's notification time has already passed
                if (!LocalDateTime.now().isBefore(notificationTime)) {
                    notificationTime = notificationTime.plusDays(1)
                }

                val epochTimeToReminder =
                    notificationTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                // Schedule alarm
                setExactOrFallback(alarmManager, epochTimeToReminder, pendingIntent)

            } catch (e: Exception) {/* no-op */}
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
                context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager: AlarmManager =
                context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (isSet) {
                setExactOrFallback(
                    alarmManager,
                    reminderTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    pendingIntent
                )
            } else {
                alarmManager.cancel(pendingIntent)
            }
        }

        /**
         * Exact alarms need a permission from Android 12 on, which can also be revoked by
         * the user. Fall back to an inexact alarm instead of throwing when it is missing.
         */
        private fun setExactOrFallback(
            alarmManager: AlarmManager,
            triggerAtMillis: Long,
            pendingIntent: PendingIntent
        ) {
            val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                    alarmManager.canScheduleExactAlarms()

            if (canScheduleExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
                )
            }
        }
    }
}
