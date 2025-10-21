package com.pocket_plan.j7_003.data.settings.sub_categories

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.databinding.FragmentSettingsBirthdaysBinding
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.PreferencesHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class SettingsBirthdays(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) :
    Fragment() {

    private val preferencesHandler: PreferencesHandler by inject()

    private var _fragmentSettingsBirthdaysBinding: FragmentSettingsBirthdaysBinding? = null
    private val fragmentSettingsBirthdaysBinding get() = _fragmentSettingsBirthdaysBinding!!

    private var dark = preferencesHandler.getDefault(PreferencesHandler.THEME_DARK)
    private var oldTime = preferencesHandler.getDefault(PreferencesHandler.BIRTHDAY_NOTIFICATION_TIME)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentSettingsBirthdaysBinding =
            FragmentSettingsBirthdaysBinding.inflate(inflater, container, false)

        lifecycleScope.launch(ioDispatcher) {
            dark = preferencesHandler.read(PreferencesHandler.THEME_DARK).first()
            oldTime = preferencesHandler.read(PreferencesHandler.BIRTHDAY_NOTIFICATION_TIME).first()

            initializeDisplayValues()
            initializeListeners()
        }

        return fragmentSettingsBirthdaysBinding.root
    }

    private suspend fun initializeDisplayValues() {

        fragmentSettingsBirthdaysBinding.swShowMonth.isChecked =
            preferencesHandler.read(PreferencesHandler.BIRTHDAY_SHOW_MONTH).first()

        fragmentSettingsBirthdaysBinding.swSouthColors.isChecked =
            preferencesHandler.read(PreferencesHandler.BIRTHDAY_COLORS_SOUTH).first()

        fragmentSettingsBirthdaysBinding.swPreview.isChecked =
            preferencesHandler.read(PreferencesHandler.PREVIEW_BIRTHDAY).first()

        fragmentSettingsBirthdaysBinding.tvBirthdayNotifTime.text =
            preferencesHandler.read(PreferencesHandler.BIRTHDAY_NOTIFICATION_TIME).first()
    }

    private fun initializeListeners() {
        //Switch for only showing one category as expanded
        fragmentSettingsBirthdaysBinding.swShowMonth.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val showMonth = fragmentSettingsBirthdaysBinding.swShowMonth.isChecked
                preferencesHandler.save(PreferencesHandler.BIRTHDAY_SHOW_MONTH, showMonth)
            }
        }

        fragmentSettingsBirthdaysBinding.swSouthColors.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val showShoutColors = fragmentSettingsBirthdaysBinding.swSouthColors.isChecked
                preferencesHandler.save(PreferencesHandler.BIRTHDAY_COLORS_SOUTH, showShoutColors)
            }
        }

        fragmentSettingsBirthdaysBinding.swPreview.setOnClickListener {
            lifecycleScope.launch(ioDispatcher) {
                val showPreview = fragmentSettingsBirthdaysBinding.swPreview.isChecked
                preferencesHandler.save(PreferencesHandler.PREVIEW_BIRTHDAY, showPreview)
            }
        }

        fragmentSettingsBirthdaysBinding.clBirthdayTime.setOnClickListener {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
                    //react to new time with h / m here
                    val newTime = h.toString().padStart(2, '0') +
                            ":" + m.toString().padStart(2, '0')

                    lifecycleScope.launch(ioDispatcher) {
                        preferencesHandler.save(
                            PreferencesHandler.BIRTHDAY_NOTIFICATION_TIME,
                            newTime
                        )
                    }
                    AlarmHandler.setBirthdayAlarms(newTime, activity as MainActivity)
                    fragmentSettingsBirthdaysBinding.tvBirthdayNotifTime.text = newTime
                }

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