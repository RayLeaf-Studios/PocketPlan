package com.example.j7_003.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database
import com.example.j7_003.data.database_objects.Birthday
import java.util.*
import kotlin.collections.ArrayList


class NotificationReceiver : BroadcastReceiver() {
    private lateinit var myContext: Context
    private val calendar = Calendar.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            myContext = context
        } else {
            return
        }

        if (intent != null) {
            when (intent.extras?.get("Notification")) {
                "Birthday" -> birthdayNotifications()
                "SReminder" -> sRNotification()
            }
        }
    }

    private fun sRNotification() {
        createNotification(
            "Sleep Reminder",
            "Sleep Reminder Notification",
            200,
            "Sleep Time",
            "It's time to go to bed, have a good nights sleep!",
            R.drawable.ic_action_sleepreminder,
            "SReminder"
        )
    }

    private fun birthdayNotifications() {
        if (Database.birthdayList.size < 1) {
            return
        }

        val notifiableUpcomingBirthdays = getUpcomingBirthdays()
        val notifiableCurrentBirthdays = getCurrentBirthdays()

        if (notifiableCurrentBirthdays.size > 1) {
            notifyCurrentBirthdays(notifiableCurrentBirthdays.size)
        } else if (notifiableCurrentBirthdays.size == 1) {
            notifyBirthdayNow(notifiableCurrentBirthdays[0])
        }

        if (notifiableUpcomingBirthdays.size > 1) {
            notifyUpcomingBirthdays(notifiableUpcomingBirthdays.size)
        } else if (notifiableUpcomingBirthdays.size == 1) {
            notifyUpcomingBirthday(notifiableUpcomingBirthdays[0])
        }
    }

    private fun getUpcomingBirthdays(): ArrayList<Birthday> {
        val upcomingBirthdays = ArrayList<Birthday>()
        Database.birthdayList.forEach { n ->
            if (n.month == calendar.get(Calendar.MONTH) + 1 && (n.day - n.daysToRemind) == calendar.get(
                    Calendar.DAY_OF_MONTH
                ) && n.daysToRemind != 0
            ) {
                upcomingBirthdays.add(n)
            }
        }
        return upcomingBirthdays
    }

    private fun getCurrentBirthdays(): ArrayList<Birthday> {
        val currentBirthdays = ArrayList<Birthday>()
        Database.birthdayList.forEach { n ->
            if (n.month == calendar.get(Calendar.MONTH) + 1 && n.day == calendar.get(Calendar.DAY_OF_MONTH) && n.daysToRemind == 0)
                currentBirthdays.add(n)
        }
        return currentBirthdays
    }

    private fun notifyBirthdayNow(birthday: Birthday) {
        createNotification(
            "Birthday Notification",
            "Birthdays",
            100,
            "Birthday",
            "It's ${birthday.name}s birthday!",
            R.drawable.ic_action_birthday,
            "birthdays"
        )
    }

    private fun notifyCurrentBirthdays(currentBirthdays: Int) {
        createNotification(
            "Birthday Notification",
            "Birthdays",
            102,
            "Birthdays",
            "There are $currentBirthdays birthdays today!",
            R.drawable.ic_action_birthday,
            "birthdays"
        )
    }

    private fun notifyUpcomingBirthday(birthday: Birthday) {
        createNotification(
            "Birthday Notification",
            "Upcoming Birthdays",
            101,
            "Upcoming Birthday",
            "${birthday.name}s birthday is coming up in ${birthday.daysToRemind} ${if(birthday.daysToRemind ==1 ) {"day"} else {"days"}}!",
            R.drawable.ic_action_birthday,
            "birthdays"
        )
    }

    private fun notifyUpcomingBirthdays(upcomingBirthdays: Int) {
        createNotification(
            "Birthday Notification",
            "Upcoming Birthdays",
            103,
            "Upcoming Birthdays",
            "$upcomingBirthdays birthdays are coming up!",
            R.drawable.ic_action_birthday,
            "birthdays"
        )
    }

    @Suppress("DEPRECATION")
    private fun createNotification(
        channelId: String,
        name: String,
        requestCode: Int,
        contentTitle: String,
        contentText: String,
        icon: Int,
        intentValue: String
    ) {
        val notificationManager =
            myContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(myContext, MainActivity::class.java)

        repeatingIntent.putExtra("NotificationEntry", intentValue)


        repeatingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(
            myContext,
            requestCode,
            repeatingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder: Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(myContext, channelId)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(icon)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        myContext.resources,
                        icon
                    )
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(myContext)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(icon)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        myContext.resources,
                        R.mipmap.ic_launcher_round
                    )
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        notificationManager.notify(requestCode, builder.build())
    }
}