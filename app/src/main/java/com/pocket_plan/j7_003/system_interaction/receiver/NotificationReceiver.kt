package com.pocket_plan.j7_003.system_interaction.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.birthdaylist.Birthday
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.notifications.NotificationHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import org.threeten.bp.LocalDate


class NotificationReceiver : BroadcastReceiver() {
    private lateinit var context: Context
    private lateinit var localDate: LocalDate

    override fun onReceive(context: Context, intent: Intent) {
        this.context = context
        AndroidThreeTen.init(this.context)
        StorageHandler.path = context.filesDir.absolutePath
        this.localDate = LocalDate.now()

        when (intent.extras?.get("Notification")) {
            "Birthday" -> birthdayNotifications()
            "SReminder" -> checkSleepNotification(intent)
        }

        SettingsManager.init()
        val time = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
        AlarmHandler.setBirthdayAlarms(time, context = context)
    }

    private fun checkSleepNotification(intent: Intent) {
        SleepReminder(context).reminder[intent.extras?.get("weekday")]?.updateAlarm(
            intent.extras?.getInt("requestCode")!!
        )
        sRNotification()
    }

    private fun sRNotification() {
        NotificationHandler.createNotification(
            "Sleep Reminder", context.resources.getString(R.string.menuTitleSleep), 200,
            context.resources.getString(R.string.sleepNotificationTitle), context.resources.getString(R.string.sleepNotificationText), R.drawable.ic_action_sleepreminder,
            "SReminder", context, 3 * 60 * 60 * 1000
        )
    }

    private fun birthdayNotifications() {
        val birthdayList = BirthdayList(context.resources.getStringArray(R.array.months))

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
            context.resources.getString(R.string.birthdayNotificationSingleUpcText, birthday.name, birthday.daysToRemind, context.resources.getQuantityString(R.plurals.dayIn, birthday.daysToRemind)),
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