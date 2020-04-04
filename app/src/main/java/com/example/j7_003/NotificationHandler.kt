package com.example.j7_003

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.example.j7_003.data.Birthday

class NotificationHandler(val context: Context, private val systemNotificationService: Any?) {
    fun createDebugNotification() {
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: Notification.Builder
        val channelId = "com.example.j7_003"
        val description = "debug description"
        val notificationManager: NotificationManager = systemNotificationService as NotificationManager

        val intent = Intent(context, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_DEFAULT)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                .setContentTitle("new debug title")
                .setContentText("new debug text")
                .setSmallIcon(R.drawable.ic_action_todo)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(context)
                .setContentTitle("new debug title")
                .setContentText("new debug text")
                .setSmallIcon(R.drawable.ic_action_todo)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(0, builder.build())
    }

    fun createBirthdayNotification(birthday: Birthday) {
        lateinit var notificationChannel: NotificationChannel
        lateinit var builder: Notification.Builder
        val channelId = "com.example.j7_003"
        val description = "Birthday Notification"
        val notificationManager: NotificationManager = systemNotificationService as NotificationManager

        val intent = Intent(context, LauncherActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                .setContentTitle("Birthday")
                .setContentText("${birthday.name}'s birthday is coming up!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_action_birthday))
                .setContentIntent(pendingIntent)
        } else {
            builder = Notification.Builder(context)
                .setContentTitle("Birthday")
                .setContentText("${birthday.name}'s birthday is coming up!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
        }
        notificationManager.notify(1000, builder.build())
    }
}