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

        if(database.birthdayList.size < 1) {
            return
        }

        val notifiableBirthdays: ArrayList<Birthday> = getUpcomingBirthdays()

        notifiableBirthdays.forEach { n ->
            if (n.daysToRemind == 0) {
                notifyBirthdayNow(n)
            } else {
                notifyUpcomingBirthday(n)
            }
        }
    }

    private fun getUpcomingBirthdays(): ArrayList<Birthday> {
        val upcomingBirthdays = ArrayList<Birthday>()
        database.birthdayList.forEach { n ->
            if (n.month == calendar.get(Calendar.MONTH)+1 && (n.day == calendar.get(Calendar.DAY_OF_MONTH) || (n.day - n.daysToRemind) == calendar.get(Calendar.DAY_OF_MONTH))) {
                upcomingBirthdays.add(n)
            }
        }
        return upcomingBirthdays
    }

    private fun notifyBirthdayNow(birthday: Birthday) {
        val notificationManager = myContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(myContext, MainActivity::class.java)

        repeatingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(myContext, 100, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "Birthdays"
        val builder: Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Birthday Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(myContext, channelId)
                .setContentTitle("Birthday")
                .setContentText("It's ${birthday.name}s birthday!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(myContext.resources,
                        R.drawable.ic_action_birthday
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(myContext)
                .setContentTitle("Birthday")
                .setContentText("It's ${birthday.name}s birthday!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(myContext.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        notificationManager.notify(100, builder.build())
    }

    private fun notifyUpcomingBirthday(birthday: Birthday) {
        val notificationManager = myContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(myContext, MainActivity::class.java)

        repeatingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(myContext, 100, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "Birthdays"
        val builder: Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Birthday Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(myContext, channelId)
                .setContentTitle("Birthday")
                .setContentText("${birthday.name}s birthday is coming up in ${birthday.daysToRemind} days!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(myContext.resources,
                        R.drawable.ic_action_birthday
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(myContext)
                .setContentTitle("Birthday")
                .setContentText("${birthday.name}s birthday is coming up in ${birthday.daysToRemind} days!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(myContext.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        notificationManager.notify(100, builder.build())
    }
}