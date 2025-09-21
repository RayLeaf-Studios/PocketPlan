package com.pocket_plan.j7_003.data.sleepreminder

import SleepReminder
import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.DialogPickTimeBinding
import com.pocket_plan.j7_003.databinding.FragmentSleepBinding
import com.pocket_plan.j7_003.databinding.RowSleepBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import org.threeten.bp.DayOfWeek

/**
 * A simple [Fragment] subclass.
 */

class SleepFr : Fragment() {

    private var _fragmentBinding: FragmentSleepBinding? = null
    val fragmentBinding get() = _fragmentBinding!!

    lateinit var myActivity: MainActivity
    lateinit var sleepReminderInstance: SleepReminder

    companion object {
        lateinit var myAdapter: SleepAdapter
    }

    private lateinit var regularCheckBoxList: ArrayList<CheckBox>

    private var customIsInit: Boolean = false
    private var regularIsInit: Boolean = false

    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragmentBinding = FragmentSleepBinding.inflate(inflater, container, false)

        myActivity = activity as MainActivity
        sleepReminderInstance = SleepReminder(myActivity)
        customIsInit = false
        regularIsInit = false

        val myRecycler = fragmentBinding.recyclerViewSleep
        sleepReminderInstance = SleepReminder(myActivity)
        myAdapter = SleepAdapter(myActivity, this)
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        if (sleepReminderInstance.isAnySet()) {
            fragmentBinding.switchEnableReminder.isChecked = true
        }

        if (sleepReminderInstance.daysAreCustom) {
            initializeCustomDaysDisplay()
            fragmentBinding.switchEnableCustomDays.isChecked = true
            updateCustomDisplay()
            fragmentBinding.panelNotCustom.visibility = View.GONE
            fragmentBinding.panelCustom.visibility = View.VISIBLE
        } else {
            initializeRegularDayDisplay()
            updateRegularDisplay()
            fragmentBinding.panelNotCustom.visibility = View.VISIBLE
            fragmentBinding.panelCustom.visibility = View.GONE
            fragmentBinding.switchEnableCustomDays.isChecked = false
            myAdapter.notifyDataSetChanged()
        }

        //switch to enable / disable entire reminder
        fragmentBinding.switchEnableReminder.setOnClickListener {
            if (fragmentBinding.switchEnableReminder.isChecked) {
                sleepReminderInstance.enableAll()
                if (sleepReminderInstance.daysAreCustom) {
                    myAdapter.notifyDataSetChanged()
                } else {
                    updateRegularCheckboxes()
                }
            } else {
                sleepReminderInstance.disableAll()
                if (sleepReminderInstance.daysAreCustom) {
                    myAdapter.notifyDataSetChanged()
                } else {
                    updateRegularCheckboxes()
                }
            }
        }

        //switch to enable use of custom days
        fragmentBinding.switchEnableCustomDays.setOnClickListener {
            if (fragmentBinding.switchEnableCustomDays.isChecked) {
                if (!customIsInit) initializeCustomDaysDisplay(); customIsInit = true
                sleepReminderInstance.setCustom()
                updateCustomDisplay()
                animationShowCustom()
            } else {
                if (!regularIsInit) initializeRegularDayDisplay(); regularIsInit = true
                sleepReminderInstance.setRegular()
                updateRegularDisplay()
                animationShowRegular()
            }
        }
        return fragmentBinding.root
    }

    private fun updateRegularDisplay() {
        updateRegularTimes()
        updateRegularCheckboxes()
    }

    private fun updateCustomDisplay() {
        myAdapter.notifyDataSetChanged()
    }

    @SuppressLint("InflateParams")
    private fun initializeCustomDaysDisplay() {
        fragmentBinding.switchEnableCustomDays.isChecked = true
    }

    @SuppressLint("InflateParams")
    private fun initializeRegularDayDisplay() {
        /**
         * initialize lists of regular checkboxes, text view for regular wake time, and text view
         * for regular sleep duration
         */


        regularCheckBoxList = arrayListOf(
            fragmentBinding.cbMonday,
            fragmentBinding.cbTuesday,
            fragmentBinding.cbWednsday,
            fragmentBinding.cbThursday,
            fragmentBinding.cbFriday,
            fragmentBinding.cbSaturday,
            fragmentBinding.cbSunday
        )

        fragmentBinding.panelWakeTime.setOnClickListener {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
                    sleepReminderInstance.editAllWakeUp(
                        h, m
                    )
                    updateRegularDisplay()
                }
            val tpd = when (dark) {
                true -> TimePickerDialog(
                    myActivity,
                    timeSetListener,
                    sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getWakeHour()!!,
                    sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getWakeMinute()!!,
                    true
                )

                else -> TimePickerDialog(
                    myActivity,
                    R.style.DialogTheme,
                    timeSetListener,
                    sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getWakeHour()!!,
                    sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getWakeMinute()!!,
                    true
                )
            }
            tpd.show()
            tpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
            tpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
        }

        fragmentBinding.panelSleepDuration.setOnClickListener {
            /**
             * pick sleep duration for ALL days
             */
            val dialogPickTimeBinding = DialogPickTimeBinding.inflate(layoutInflater)

            dialogPickTimeBinding.npHour.minValue = 0
            dialogPickTimeBinding.npHour.maxValue = 23

            dialogPickTimeBinding.npMinute.minValue = 0
            dialogPickTimeBinding.npMinute.maxValue = 59

            dialogPickTimeBinding.tvHourMinuteDivider.text = "h"
            dialogPickTimeBinding.tvHourMinuteAttachment.text = "m"

            val myBuilder = AlertDialog.Builder(myActivity).setView(dialogPickTimeBinding.root)
            val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)

            titleDialogBinding.tvDialogTitle.text = resources.getText(R.string.sleepDuration)
            myBuilder.setCustomTitle(titleDialogBinding.root)

            val myAlertDialog = myBuilder.create()
            val dialogWindow = myAlertDialog.window
            dialogWindow?.setLayout(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            dialogWindow?.setGravity(Gravity.CENTER)
            myAlertDialog.show()

            dialogPickTimeBinding.npHour.value =
                sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.duration?.toHours()?.toInt()!!
            dialogPickTimeBinding.npMinute.value =
                sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.duration?.toMinutes()
                    ?.toInt()!! % 60

            dialogPickTimeBinding.btnApplyTime.setOnClickListener {
                sleepReminderInstance.editAllDuration(
                    dialogPickTimeBinding.npHour.value,
                    dialogPickTimeBinding.npMinute.value
                )
                myAlertDialog.dismiss()
                updateRegularDisplay()
            }
        }

        regularCheckBoxList.forEachIndexed { i, cb ->
            cb.setOnClickListener {
                val day = DayOfWeek.values()[i]
                if (cb.isChecked) {
                    sleepReminderInstance.reminder[day]?.enable(day)
                } else {
                    sleepReminderInstance.reminder[day]?.disable(day)
                }

                if (!sleepReminderInstance.isAnySet()) {
                    fragmentBinding.switchEnableReminder.isChecked = false
                } else if (!fragmentBinding.switchEnableReminder.isChecked) {
                    fragmentBinding.switchEnableReminder.isChecked = true
                }

            }
        }
    }

    private fun updateRegularTimes() {
        fragmentBinding.tvRegularWakeTime.text =
            sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getWakeUpTimeString()
        fragmentBinding.tvSleepDuration.text =
            sleepReminderInstance.reminder[DayOfWeek.MONDAY]?.getDurationTimeString()
    }

    private fun updateRegularCheckboxes() {
        regularCheckBoxList.forEachIndexed { i, cb ->
            cb.isChecked = sleepReminderInstance.reminder[DayOfWeek.values()[i]]?.isSet!!
        }
    }


    private fun animationShowCustom() {
        fragmentBinding.panelNotCustom.visibility = View.GONE
        val animationHide =
            AnimationUtils.loadAnimation(myActivity, R.anim.scale_down_reverse)
        animationHide.duration = 350
        animationHide.fillAfter = false
        fragmentBinding.panelNotCustom.startAnimation(animationHide)

        fragmentBinding.panelCustom.visibility = View.VISIBLE
        val animationShow =
            AnimationUtils.loadAnimation(myActivity, R.anim.scale_down)
        animationShow.duration = 700
        animationShow.fillAfter = false
        fragmentBinding.panelCustom.startAnimation(animationShow)
    }

    private fun animationShowRegular() {
        fragmentBinding.panelCustom.visibility = View.GONE
        val animationHide =
            AnimationUtils.loadAnimation(myActivity, R.anim.scale_down_reverse)
        animationHide.duration = 700
        animationHide.fillAfter = false
        fragmentBinding.panelCustom.startAnimation(animationHide)

        val params = fragmentBinding.recyclerViewSleep.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = 20000

        fragmentBinding.panelNotCustom.visibility = View.VISIBLE
        val animationShow =
            AnimationUtils.loadAnimation(myActivity, R.anim.scale_down)
        animationShow.duration = 350
        animationShow.fillAfter = false
        animationShow.startOffset = 280
        fragmentBinding.panelNotCustom.startAnimation(animationShow)
    }
}

class SleepAdapter(mainActivity: MainActivity, sleepFr: SleepFr) :
    RecyclerView.Adapter<SleepAdapter.SleepViewHolder>() {
    private val myFragment = sleepFr
    private val myActivity = mainActivity
    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)


    private var dayStrings = arrayOf(
        myActivity.resources.getString(R.string.sleepMon),
        myActivity.resources.getString(R.string.sleepTue),
        myActivity.resources.getString(R.string.sleepWed),
        myActivity.resources.getString(R.string.sleepThu),
        myActivity.resources.getString(R.string.sleepFri),
        myActivity.resources.getString(R.string.sleepSat),
        myActivity.resources.getString(R.string.sleepSun)
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepViewHolder {
        val rowSleepBinding =
            RowSleepBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SleepViewHolder(rowSleepBinding)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: SleepViewHolder, position: Int) {
        val day = DayOfWeek.values()[position]
        holder.day = day

        if (round) {
            holder.binding.crvSleep.radius = cr
        }

        //initialize the day string
        holder.binding.tvDayString.text = dayStrings[position]

        //initialize the checked State of the reminder checkBox
        holder.binding.cbRemindMe.isChecked =
            myFragment.sleepReminderInstance.reminder[day]?.isSet!!

        //initialize wake up time string
        holder.binding.tvWakeTimeRow.text =
            myFragment.sleepReminderInstance.reminder[day]?.getWakeUpTimeString()

        //initialize duration string
        holder.binding.tvDurationRow.text =
            myFragment.sleepReminderInstance.reminder[day]?.getDurationTimeString()


        //listener for checkbox enabling reminder
        holder.binding.cbRemindMe.setOnClickListener {
            if (holder.binding.cbRemindMe.isChecked) {
                myFragment.sleepReminderInstance.reminder[day]?.enable(day)
            } else {
                myFragment.sleepReminderInstance.reminder[day]?.disable(day)
            }

            if (!myFragment.sleepReminderInstance.isAnySet()) {
                myFragment.fragmentBinding.switchEnableReminder.isChecked = false
            } else if (!myFragment.fragmentBinding.switchEnableReminder.isChecked) {
                myFragment.fragmentBinding.switchEnableReminder.isChecked = true
            }
        }

        holder.binding.clTapFieldDuration.setOnClickListener {
            val dialogPickTimeBinding =
                DialogPickTimeBinding.inflate(LayoutInflater.from(myActivity))

            dialogPickTimeBinding.npHour.minValue = 0
            dialogPickTimeBinding.npHour.maxValue = 23

            dialogPickTimeBinding.npMinute.minValue = 0
            dialogPickTimeBinding.npMinute.maxValue = 59

            dialogPickTimeBinding.tvHourMinuteDivider.text = "h"
            dialogPickTimeBinding.tvHourMinuteAttachment.text = "m"

            val myBuilder2 = AlertDialog.Builder(myActivity).setView(dialogPickTimeBinding.root)
            val titleDialogBinding = TitleDialogBinding.inflate(LayoutInflater.from(myActivity))
            titleDialogBinding.tvDialogTitle.text = myActivity.getString(
                R.string.sleepDurationDay
            )
            myBuilder2.setCustomTitle(titleDialogBinding.root)

            val myAlertDialog2 = myBuilder2.create()
            myAlertDialog2.show()

            dialogPickTimeBinding.npHour.value =
                myFragment.sleepReminderInstance.reminder[day]?.getDurationHour()!!
            dialogPickTimeBinding.npMinute.value =
                myFragment.sleepReminderInstance.reminder[day]?.getDurationMinute()!!

            dialogPickTimeBinding.btnApplyTime.setOnClickListener {
                myFragment.sleepReminderInstance.editDurationAtDay(
                    day,
                    dialogPickTimeBinding.npHour.value,
                    dialogPickTimeBinding.npMinute.value
                )
                myAlertDialog2.dismiss()
                SleepFr.myAdapter.notifyItemChanged(position)

            }

        }
        holder.binding.clTapFieldWakeUp.setOnClickListener {
            val timeSetListener =
                TimePickerDialog.OnTimeSetListener { _: TimePicker?, h: Int, m: Int ->
                    myFragment.sleepReminderInstance.editWakeUpAtDay(day, h, m)
                    SleepFr.myAdapter.notifyItemChanged(position)
                }
            val tpd = when (dark) {
                true ->
                    TimePickerDialog(
                        myActivity,
                        timeSetListener,
                        myFragment.sleepReminderInstance.reminder[day]?.getWakeHour()!!,
                        myFragment.sleepReminderInstance.reminder[day]?.getWakeMinute()!!,
                        true
                    )

                else ->
                    TimePickerDialog(
                        myActivity,
                        R.style.DialogTheme,
                        timeSetListener,
                        myFragment.sleepReminderInstance.reminder[day]?.getWakeHour()!!,
                        myFragment.sleepReminderInstance.reminder[day]?.getWakeMinute()!!,
                        true
                    )
            }
            tpd.show()
            tpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
            tpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
        }
    }

    override fun getItemCount(): Int = 7

    class SleepViewHolder(rowSleepBinding: RowSleepBinding) :
        RecyclerView.ViewHolder(rowSleepBinding.root) {
        lateinit var day: DayOfWeek
        var binding = rowSleepBinding
    }

}
