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
import android.os.Message
import android.util.Log
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database
import com.example.j7_003.data.database_objects.Birthday
import java.util.*
import kotlin.collections.ArrayList


class NotificationReceiver : BroadcastReceiver() {
    lateinit var myContext: Context
    lateinit var database: Database
    private val calendar = Calendar.getInstance()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            myContext = context
        } else {
            return
        }

        database = Database(myContext)

        if (database.birthdayList.size < 1) {
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
        database.birthdayList.forEach { n ->
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
        database.birthdayList.forEach { n ->
            if (n.month == calendar.get(Calendar.MONTH) + 1 && n.day == calendar.get(Calendar.DAY_OF_MONTH) && n.daysToRemind == 0)
                currentBirthdays.add(n)
        }
        return currentBirthdays
    }

    private fun notifyBirthdayNow(birthday: Birthday) {
        createNotification(
            "Birthdays",
            "Birthday Notification",
            100,
            "Birthday",
            "It's ${birthday.name}s birthday!"
        )
    }

    private fun notifyCurrentBirthdays(currentBirthdays: Int) {
        createNotification(
            "Birthdays",
            "Birthday Notification",
            102,
            "Birthdays",
            "There are $currentBirthdays birthdays today!"
        )
    }

    private fun notifyUpcomingBirthday(birthday: Birthday) {
        createNotification(
            "Upcoming Birthdays",
            "Birthday Notification",
            101,
            "Upcoming Birthday",
            "${birthday.name}s birthday is coming up in ${birthday.daysToRemind} days!"
        )
    }

    private fun notifyUpcomingBirthdays(upcomingBirthdays: Int) {
        createNotification(
            "Upcoming Birthdays",
            "Birthday Notification",
            103,
            "Upcoming Birthdays",
            "$upcomingBirthdays birthdays are coming up!"
        )
    }

    private fun createNotification(
        channelId: String,
        name: String,
        requestCode: Int,
        contentTitle: String,
        contentText: String
    ) {
        val notificationManager =
            myContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(myContext, MainActivity::class.java)

        repeatingIntent.putExtra("NotificationEntry", "true")


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
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        myContext.resources,
                        R.drawable.ic_action_birthday
                    )
                )
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(myContext)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_action_birthday)
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