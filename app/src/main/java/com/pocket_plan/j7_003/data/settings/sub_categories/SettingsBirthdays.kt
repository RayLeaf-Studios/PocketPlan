package com.pocket_plan.j7_003.data.settings.sub_categories

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.FragmentSettingsBirthdaysBinding
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler

class SettingsBirthdays : Fragment() {
    private var _fragmentSettingsBirthdaysBinding: FragmentSettingsBirthdaysBinding? = null
    private val fragmentSettingsBirthdaysBinding get() = _fragmentSettingsBirthdaysBinding!!

    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsBirthdaysBinding = FragmentSettingsBirthdaysBinding.inflate(inflater, container, false)

        initializeDisplayValues()
        initializeListeners()

        return fragmentSettingsBirthdaysBinding.root
    }

    private fun initializeDisplayValues() {

        fragmentSettingsBirthdaysBinding.swShowMonth.isChecked =
            SettingsManager.getSetting(SettingId.BIRTHDAY_SHOW_MONTH) as Boolean

        fragmentSettingsBirthdaysBinding.swSouthColors.isChecked =
            SettingsManager.getSetting(SettingId.BIRTHDAY_COLORS_SOUTH) as Boolean

        fragmentSettingsBirthdaysBinding.swPreview.isChecked =
            SettingsManager.getSetting(SettingId.PREVIEW_BIRTHDAY) as Boolean

        fragmentSettingsBirthdaysBinding.tvBirthdayNotifTime.text = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
    }

    private fun initializeListeners() {
        //Switch for only showing one category as expanded
        fragmentSettingsBirthdaysBinding.swShowMonth.setOnClickListener {
            SettingsManager.addSetting(SettingId.BIRTHDAY_SHOW_MONTH, fragmentSettingsBirthdaysBinding.swShowMonth.isChecked)
        }

        fragmentSettingsBirthdaysBinding.swSouthColors.setOnClickListener {
            SettingsManager.addSetting(SettingId.BIRTHDAY_COLORS_SOUTH, fragmentSettingsBirthdaysBinding.swSouthColors.isChecked)
        }

        fragmentSettingsBirthdaysBinding.swPreview.setOnClickListener {
            SettingsManager.addSetting(SettingId.PREVIEW_BIRTHDAY, fragmentSettingsBirthdaysBinding.swPreview.isChecked)
        }

        fragmentSettingsBirthdaysBinding.clBirthdayTime.setOnClickListener {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
                    //react to new time with h / m here
                    val newTime = h.toString().padStart(2, '0') + ":" + m.toString().padStart(2, '0')
                    SettingsManager.addSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME, newTime)
                    AlarmHandler.setBirthdayAlarms(newTime, activity as MainActivity)
                    fragmentSettingsBirthdaysBinding.tvBirthdayNotifTime.text = newTime
                }
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