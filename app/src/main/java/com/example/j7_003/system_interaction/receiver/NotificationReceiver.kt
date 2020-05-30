package com.example.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.SleepReminder
import com.example.j7_003.system_interaction.handler.NotificationHandler
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.example.j7_003.data.database.database_objects.Birthday
import org.threeten.bp.LocalDate
import kotlin.collections.ArrayList


class NotificationReceiver : BroadcastReceiver() {
    private lateinit var myContext: Context
    private val localDate = LocalDate.now()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null) {
            myContext = context
        } else {
            return
        }

        if (intent != null) {
            when (intent.extras?.get("Notification")) {
                "Birthday" -> birthdayNotifications()
                "SReminder" -> checkSleepNotification(intent)
            }
        }
    }

    private fun checkSleepNotification(intent: Intent) {
        SleepReminder.init()
        SleepReminder.reminder[intent.extras?.get("weekday")]?.updateAlarm(
            intent.extras?.getInt("requestCode")!!
        )
        sRNotification()
    }

    private fun sRNotification() {
        NotificationHandler.createNotification(
            "Sleep Reminder",
            "Sleep Reminder Notification",
            200,
            "Sleep Time",
            "It's time to go to bed, have a good nights sleep!",
            R.drawable.ic_action_sleepreminder,
            "SReminder",
            myContext
        )
    }

    private fun birthdayNotifications() {
        StorageHandler.createJsonFile("BIRTHDAYLIST", "BirthdayList.json", myContext)
        Database.birthdayList = Database.fetchBList()
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
            if (n.month == localDate.monthValue && (n.day - n.daysToRemind) ==
                localDate.dayOfMonth && n.daysToRemind > 0)
            {
                upcomingBirthdays.add(n)
            }
        }
        return upcomingBirthdays
    }

    private fun getCurrentBirthdays(): ArrayList<Birthday> {
        val currentBirthdays = ArrayList<Birthday>()
        Database.birthdayList.forEach { n ->
            if (n.month == localDate.monthValue &&
                n.day == localDate.dayOfMonth &&
                n.daysToRemind == 0
            ) {
                currentBirthdays.add(n)
            }
        }
        return currentBirthdays
    }

    private fun notifyBirthdayNow(birthday: Birthday) {
        NotificationHandler.createNotification(
            "Birthday Notification",
            "Birthdays",
            100,
            "Birthday",
            "It's ${birthday.name}s birthday!",
            R.drawable.ic_action_birthday,
            "birthdays",
            myContext
        )
    }

    private fun notifyCurrentBirthdays(currentBirthdays: Int) {
        NotificationHandler.createNotification(
            "Birthday Notification",
            "Birthdays",
            102,
            "Birthdays",
            "There are $currentBirthdays birthdays today!",
            R.drawable.ic_action_birthday,
            "birthdays",
            myContext
        )
    }

    private fun notifyUpcomingBirthday(birthday: Birthday) {
        NotificationHandler.createNotification(
            "Birthday Notification",
            "Upcoming Birthdays",
            101,
            "Upcoming Birthday",
            "${birthday.name}s birthday is coming up in ${birthday.daysToRemind} ${if(birthday.daysToRemind ==1 ) {"day"} else {"days"}}!",
            R.drawable.ic_action_birthday,
            "birthdays",
            myContext
        )
    }

    private fun notifyUpcomingBirthdays(upcomingBirthdays: Int) {
        NotificationHandler.createNotification(
            "Birthday Notification",
            "Upcoming Birthdays",
            103,
            "Upcoming Birthdays",
            "$upcomingBirthdays birthdays are coming up!",
            R.drawable.ic_action_birthday,
            "birthdays",
            myContext
        )
    }


}