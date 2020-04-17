package com.example.j7_003.data.handler

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import com.example.j7_003.MainActivity
import com.example.j7_003.R

class NotificationHandler {
    companion object {
        @Suppress("DEPRECATION")
        fun createNotification(
            channelId: String,
            name: String,
            requestCode: Int,
            contentTitle: String,
            contentText: String,
            icon: Int,
            intentValue: String,
            myContext: Context
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
}