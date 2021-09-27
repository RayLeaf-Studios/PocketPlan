package com.pocket_plan.j7_003.data.settings.sub_categories

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import kotlinx.android.synthetic.main.fragment_settings_birthdays.view.*
import org.threeten.bp.DayOfWeek

class SettingsBirthdays : Fragment() {
    private lateinit var swShowMonth: SwitchCompat
    private lateinit var swSouth: SwitchCompat
    private lateinit var swPreview: SwitchCompat
    private lateinit var clBirthdayTime: ConstraintLayout
    private lateinit var tvBirthdayNotif: TextView

    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_settings_birthdays, container, false)

        initializeComponents(myView)
        initializeAdapters()
        initializeDisplayValues()
        initializeListeners()

        return myView
    }

    private fun initializeComponents(myView: View) {

        //initialize references to view
        swShowMonth = myView.swShowMonth
        swSouth = myView.swSouthColors
        swPreview = myView.swPreview
        clBirthdayTime = myView.cvBirthdayTime
        tvBirthdayNotif = myView.tvBirthdayNotifTime
    }

    private fun initializeAdapters(){}

    private fun initializeDisplayValues() {

        swShowMonth.isChecked =
            SettingsManager.getSetting(SettingId.BIRTHDAY_SHOW_MONTH) as Boolean

        swSouth.isChecked =
            SettingsManager.getSetting(SettingId.BIRTHDAY_COLORS_SOUTH) as Boolean

        swPreview.isChecked =
            SettingsManager.getSetting(SettingId.PREVIEW_BIRTHDAY) as Boolean

        tvBirthdayNotif.text = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
    }

    private fun initializeListeners() {
        //Switch for only showing one category as expanded
        swShowMonth.setOnClickListener {
            SettingsManager.addSetting(SettingId.BIRTHDAY_SHOW_MONTH, swShowMonth.isChecked)
        }

        swSouth.setOnClickListener {
            SettingsManager.addSetting(SettingId.BIRTHDAY_COLORS_SOUTH, swSouth.isChecked)
        }

        swPreview.setOnClickListener {
            SettingsManager.addSetting(SettingId.PREVIEW_BIRTHDAY, swPreview.isChecked)
        }

        clBirthdayTime.setOnClickListener {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
                    //react to new time with h / m here
                    val newTime = h.toString().padStart(2, '0') + ":" + m.toString().padStart(2, '0')
                    SettingsManager.addSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME, newTime)
                    AlarmHandler.setBirthdayAlarms(newTime, activity as MainActivity)
                    tvBirthdayNotif.text = newTime
                }
            //TODO GET OLD SETTING
            val oldTime = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String

            val oldHour = oldTime.split(":")[0].toInt()
            val oldMin = oldTime.split(":")[1].toInt()

            val tpd = when (dark) {
                true -> TimePickerDialog(
                    activity,
                    timeSetListener,
                    oldHour,
                    oldMin,
                    true
                )
                else -> TimePickerDialog(
                    activity,
                    R.style.DialogTheme,
                    timeSetListener,
                    oldHour,
                    oldMin,
                    true
                )
            }
            tpd.show()
            tpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    (activity as MainActivity).colorForAttr(R.attr.colorOnBackGround)
                )
            tpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    (activity as MainActivity).colorForAttr(R.attr.colorOnBackGround)
                )
        }
    }
}