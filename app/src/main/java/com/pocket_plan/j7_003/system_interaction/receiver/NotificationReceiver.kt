package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.system_interaction.handler.notifications.NotificationHandler
import com.pocket_plan.j7_003.data.birthdaylist.Birthday
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.system_interaction.Logger
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import org.threeten.bp.LocalDate
import kotlin.collections.ArrayList


class NotificationReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    private lateinit var localDate: LocalDate

    override fun onReceive(context: Context, intent: Intent) {
        val logger = Logger(context)
        try {
            logger.log("NR", "Initializing NR")
            this.context = context
            AndroidThreeTen.init(this.context)
            this.localDate = LocalDate.now()
            logger.log("NR", "Initialized NR")

            logger.log("NR", "Handling notifications")
            when (intent.extras?.get("Notification")) {
                "Birthday" -> birthdayNotifications()
                "SReminder" -> checkSleepNotification(intent)
            }
            logger.log("NR", "Handled notifications")

            logger.log("NR", "Resetting alarm")
            AlarmHandler.setBirthdayAlarms(context = context)
            logger.log("NR", "Reset alarm")
        } catch (e: Exception) {
            logger.log("NR", "--------------------------------------------------")
            logger.log("NR", "--------------------------------------------------")
            if (e.message is String)
                logger.log("NR", e.message!!)
            else
                logger.log("NR", "No message from the exception")
            logger.log("NR", "")
            logger.log("NR", e.stackTraceToString())
            logger.log("NR", "--------------------------------------------------")
            logger.log("NR", "--------------------------------------------------")
        }
    }

    private fun checkSleepNotification(intent: Intent) {
        SleepReminder(context).reminder[intent.extras?.get("weekday")]?.updateAlarm(
            intent.extras?.getInt("requestCode")!!
        )
        sRNotification()
    }

    private fun sRNotification() {
        NotificationHandler.createNotification(
            "Sleep Reminder", context.resources.getString(R.string.menuTitleSleep),
            200, context.resources.getString(R.string.sleepNotificationTitle),
            context.resources.getString(R.string.sleepNotificationText),
            R.drawable.ic_action_sleepreminder, "SReminder", context
        )
    }

    private fun birthdayNotifications() {
        val birthdayList = BirthdayList()

        if (birthdayList.isEmpty()) {
            return
        }

        val notifiableUpcomingBirthdays = getUpcomingBirthdays(birthdayList)
        val notifiableCurrentBirthdays = getCurrentBirthdays(birthdayList)

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

    private fun getUpcomingBirthdays(birthdayList: BirthdayList): ArrayList<Birthday> {
        val upcomingBirthdays = ArrayList<Birthday>()
        birthdayList.forEach { n ->
            val calculatedDate = LocalDate.now().plusDays(n.daysToRemind.toLong())
            if (n.notify && calculatedDate.monthValue == n.month &&
                calculatedDate.dayOfMonth == n.day && n.daysToRemind > 0) {
                upcomingBirthdays.add(n)
            }
        }
        return upcomingBirthdays
    }

    private fun getCurrentBirthdays(birthdayList: BirthdayList): ArrayList<Birthday> {
        val currentBirthdays = ArrayList<Birthday>()
        birthdayList.forEach { n ->
            if (n.notify && n.month == localDate.monthValue &&
                n.day == localDate.dayOfMonth) {
                currentBirthdays.add(n)
            }
        }
        return currentBirthdays
    }

    private fun notifyBirthdayNow(birthday: Birthday) {
        NotificationHandler.createNotification(
            "Birthday Notification", context.resources.getString(R.string.menuTitleBirthdays),
            100, context.resources.getString(R.string.birthdayNotificationTitle),
            context.resources.getString(R.string.birthdayNotificationSingleText, birthday.name),
            R.drawable.ic_action_birthday, "birthdays", context
        )
    }

    private fun notifyCurrentBirthdays(currentBirthdays: Int) {
        NotificationHandler.createNotification(
            "Birthday Notification", context.resources.getString(R.string.menuTitleBirthdays),
            102, context.resources.getString(R.string.birthdayNotificationTitle),
            context.resources.getString(R.string.birthdayNotificationMultText, currentBirthdays),
            R.drawable.ic_action_birthday, "birthdays", context
        )
    }

    private fun notifyUpcomingBirthday(birthday: Birthday) {
        NotificationHandler.createNotification(
            "Birthday Notification", context.resources.getString(R.string.birthdayNotificationTitleUpc),
            101, context.resources.getString(R.string.birthdayNotificationTitleUpc),
            context.resources.getString(R.string.birthdayNotificationSingleUpcText, birthday.name, birthday.daysToRemind),
            R.drawable.ic_action_birthday, "birthdays", context
        )
    }

    private fun notifyUpcomingBirthdays(upcomingBirthdays: Int) {
        NotificationHandler.createNotification(
            "Birthday Notification", context.resources.getString(R.string.birthdayNotificationTitleUpc),
            103, context.resources.getString(R.string.birthdayNotificationTitleUpc),
            context.resources.getString(R.string.birthdayNotificationMultUpcText, upcomingBirthdays),
            R.drawable.ic_action_birthday, "birthdays", context
        )
    }
}