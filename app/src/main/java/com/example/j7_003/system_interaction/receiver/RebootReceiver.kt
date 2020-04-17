package com.example.j7_003.system_interaction.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "i got into the receiver", Toast.LENGTH_LONG).show()
        if (intent != null) {
            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Toast.makeText(context, "i finished the check", Toast.LENGTH_LONG).show()
                setBirthdayAlarms(context)
            }
        }
    }

    private fun setBirthdayAlarms(context: Context?, hour: Int = 12, minute: Int = 0) {
        val intent = Intent(context, NotificationReceiver::class.java)
        intent.putExtra("Notification", "Birthday")

        val pendingIntent = PendingIntent.getBroadcast(context, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context?.getSystemService(AppCompatActivity.ALARM_SERVICE) as AlarmManager

        val notificationTime = Calendar.getInstance()
        notificationTime.set(Calendar.HOUR_OF_DAY, hour)
        notificationTime.set(Calendar.MINUTE, minute)
        notificationTime.set(Calendar.SECOND, 0)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
        Toast.makeText(context, "i finished the alarm", Toast.LENGTH_LONG).show()
    }
}