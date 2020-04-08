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
import android.util.Log
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.Database


class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val repeatingIntent = Intent(context, MainActivity::class.java)
        val database = Database(context)

        repeatingIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        val pendingIntent = PendingIntent.getActivity(context, 100, repeatingIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val channelId = "Birthdays"
        val builder: Notification.Builder

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, "Birthday Notifications", NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(context, channelId)
                //.setContentTitle("Birthday")
                .setContentText("It's ${database.getBirthday(0).name}s birthday!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.resources,
                        R.drawable.ic_action_birthday
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        } else {
            builder = Notification.Builder(context)
                //.setContentTitle("Birthday")
                .setContentText("It's ${database.getBirthday(0).name}s birthday!")
                .setSmallIcon(R.drawable.ic_action_birthday)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.resources,
                        R.mipmap.ic_launcher_round
                    ))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
        }
        notificationManager.notify(100, builder.build())
    }
}