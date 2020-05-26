package com.example.j7_003.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.SleepReminder
import kotlinx.android.synthetic.main.dialog_pick_time.view.*
import kotlinx.android.synthetic.main.fragment_sleep.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalTime

/**
 * A simple [Fragment] subclass.
 */

class SleepFragment : Fragment() {
    private lateinit var regularCheckBoxList: ArrayList<CheckBox>
    private lateinit var regularWakeTimeText: TextView
    private lateinit var regularDurationTimeText: TextView

    private lateinit var customCheckBoxList: ArrayList<CheckBox>
    private lateinit var customPanelList: ArrayList<ConstraintLayout>
    private lateinit var customWakeTimeTexts: ArrayList<TextView>
    private lateinit var customDurationTexts: ArrayList<TextView>

    private var customIsInit: Boolean = false
    private var regularIsInit: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        customIsInit = false
        regularIsInit = false

        val myView = MainActivity.sleepView

        SleepReminder.init()

        if(SleepReminder.isAnySet()){

        }

        if(SleepReminder.daysAreCustom){
            initializeCustomDaysDisplay(myView)
            myView.switchEnableCustomDays.isChecked = true
            updateCustomDisplay()
            myView.panelNotCustom.visibility = View.GONE
            myView.panelCustom.visibility = View.VISIBLE
        }else{
            initializeRegularDayDisplay(myView)
            updateRegularDisplay()
            myView.panelNotCustom.visibility = View.VISIBLE
            myView.panelCustom.visibility = View.GONE
            myView.switchEnableCustomDays.isChecked = false
        }

        //switch to enable / disable entire reminder
        myView.switchEnableReminder.setOnClickListener {
            if (myView.switchEnableReminder.isChecked) {
                SleepReminder.enableAll()
                if(SleepReminder.daysAreCustom){
                    updateCustomCheckBoxes()
                }else{
                    updateRegularCheckboxes()
                }
            } else {
                SleepReminder.disableAll()
                if(SleepReminder.daysAreCustom){
                    updateCustomCheckBoxes()
                }else{
                    updateRegularCheckboxes()
                }
            }
        }

        //switch to enable use of custom days
        myView.switchEnableCustomDays.setOnClickListener {
            if (myView.switchEnableCustomDays.isChecked) {
                if(!customIsInit) initializeCustomDaysDisplay(myView); customIsInit = true
                SleepReminder.setCustom()
                updateCustomDisplay()
                animationShowCustom(myView)
            } else {
                if(!regularIsInit) initializeRegularDayDisplay(myView); regularIsInit = true
                SleepReminder.setRegular()
                updateRegularDisplay()
                animationShowRegular(myView)
            }
        }
        return myView
    }

    private fun updateRegularDisplay(){
        updateRegularTimes()
        updateRegularCheckboxes()
    }

    private fun updateCustomDisplay(){
        updateCustomTimes()
        updateCustomCheckBoxes()
    }

    private fun initializeCustomDaysDisplay(v: View){
        /**
         * initialize lists of  custom panels, custom checkboxes, custom wake time text views and
         * custom duration text views
         */
        customCheckBoxList = arrayListOf(v.checkBoxMo, v.checkBoxTu, v.checkBoxWe,
            v.checkBoxTh, v.checkBoxFr, v.checkBoxSa, v.checkBoxSu)

        customPanelList = arrayListOf(v.customMondayPanel, v.customTuesdayPanel,
            v.customWednesdayPanel, v.customThursdayPanel, v.customFridayPanel,
            v.customSaturdayPanel, v.customSundayPanel)

        customWakeTimeTexts = arrayListOf(v.tvWakeTimeMo, v.tvWakeTimeTu, v.tvWakeTimeWe,
            v.tvWakeTimeThu, v.tvWakeTimeFr, v.tvWakeTimeSa, v.tvWakeTimeSu)

        customDurationTexts = arrayListOf(v.tvSDurMo, v.tvSDurTu, v.tvSDurWe,
            v.tvSDurThu, v.tvSDurFr, v.tvSDurSa, v.tvSDurSu)
        /**
         * make custom panel visible, notCustom panel invisible, initialize custom switch as on
         */
        v.switchEnableCustomDays.isChecked = true
        v.panelCustom.visibility = View.VISIBLE
        v.panelNotCustom.visibility = View.GONE
        /**
         * onclick listeners for custom checkboxes
         */
        customCheckBoxList.forEachIndexed{ i, cb ->
            cb.setOnClickListener{
                val day = DayOfWeek.values()[i]
                if(cb.isChecked){
                    SleepReminder.reminder[day]?.enable(day)
                }else{
                    SleepReminder.reminder[day]?.disable(day)
                }
            }
        }

        customDurationTexts.forEachIndexed{i, p ->
            p.setOnClickListener{
                val myDialogView = LayoutInflater.from(activity)
                    .inflate(R.layout.dialog_pick_time, null)

                myDialogView.npHour.minValue = 0
                myDialogView.npHour.maxValue = 23

                myDialogView.npMinute.minValue = 0
                myDialogView.npMinute.maxValue = 59

                myDialogView.tvHourMinuteDivider.text = "h"
                myDialogView.tvHourMinuteAttachment.text = "m"

                val myBuilder2 = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
                val customTitle2 =
                    LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
                customTitle2.tvDialogTitle.text = "Sleep Duration - "+DayOfWeek.values()[i].toString()
                myBuilder2.setCustomTitle(customTitle2)

                val myAlertDialog2 = myBuilder2.create()
                myAlertDialog2.show()

                myDialogView.npHour.value = SleepReminder.reminder[DayOfWeek.values()[i]]?.getDurationHour()!!
                myDialogView.npMinute.value = SleepReminder.reminder[DayOfWeek.values()[i]]?.getDurationMinute()!!

                myDialogView.btnApplyTime.setOnClickListener() {
                    SleepReminder.editDurationAtDay(
                        DayOfWeek.values()[i],
                        myDialogView.npHour.value,
                        myDialogView.npMinute.value
                    )
                    myAlertDialog2.dismiss()
                    updateCustomDisplay()

                }
            }

        }

        customWakeTimeTexts.forEachIndexed{ i, p ->
            p.setOnClickListener{
                val myDialogView =
                    LayoutInflater.from(activity).inflate(R.layout.dialog_pick_time, null)

                myDialogView.npHour.minValue = 0
                myDialogView.npHour.maxValue = 23

                myDialogView.npMinute.minValue = 0
                myDialogView.npMinute.maxValue = 59

                myDialogView.tvHourMinuteDivider.text = ":"
                myDialogView.tvHourMinuteAttachment.text = ""

                myDialogView.npHour.setFormatter { i ->
                    if (i < 10) "0$i" else "$i"
                }

                myDialogView.npMinute.setFormatter { i ->
                    if (i < 10) "0$i" else "$i"
                }

                val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
                val customTitle =
                    LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
                customTitle.tvDialogTitle.text = "Wakeup Time - "+DayOfWeek.values()[i].toString()
                myBuilder.setCustomTitle(customTitle)

                myDialogView.npHour.value = SleepReminder.reminder[DayOfWeek.values()[i]]?.getWakeHour()!!
                myDialogView.npMinute.value = SleepReminder.reminder[DayOfWeek.values()[i]]?.getWakeMinute()!!

                val myAlertDialog = myBuilder.create()
                myAlertDialog.show()

                myDialogView.btnApplyTime.setOnClickListener() {
                    SleepReminder.editWakeUpAtDay(
                        DayOfWeek.values()[i],
                        myDialogView.npHour.value,
                        myDialogView.npMinute.value
                    )
                    myAlertDialog.dismiss()
                    updateCustomDisplay()

                }
            }
        }
    }

    private fun initializeRegularDayDisplay(v: View){
        /**
         * initialize lists of regular checkboxes, text view for regular wake time, and text view
         * for regular sleep duration
         */

        regularWakeTimeText = v.tvWakeTime
        regularDurationTimeText = v.tvSleepDuration

        regularCheckBoxList = arrayListOf(
            v.cbMonday, v.cbTuesday, v.cbWednsday,
            v.cbThursday, v.cbFriday, v.cbSaturday, v.cbSunday
        )

        v.panelWakeTime.setOnClickListener{
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.dialog_pick_time, null)

            myDialogView.npHour.minValue = 0
            myDialogView.npHour.maxValue = 23

            myDialogView.npMinute.minValue = 0
            myDialogView.npMinute.maxValue = 59

            myDialogView.tvHourMinuteDivider.text = ":"
            myDialogView.tvHourMinuteAttachment.text = ""

            myDialogView.npHour.setFormatter   { i -> if (i < 10) "0$i" else "$i"}
            myDialogView.npMinute.setFormatter { i -> if (i < 10) "0$i" else "$i"}

            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val customTitle = LayoutInflater.from(activity)
                .inflate(R.layout.title_dialog_add_task, null)

            customTitle.tvDialogTitle.text = "Wakeup Time"
            myBuilder.setCustomTitle(customTitle)


            //todo get regular wakehour here
            myDialogView.npHour.value = SleepReminder.reminder[DayOfWeek.values()[0]]?.getWakeHour()!!
            myDialogView.npMinute.value = SleepReminder.reminder[DayOfWeek.values()[0]]?.getWakeMinute()!!

            val myAlertDialog = myBuilder.create()
            myAlertDialog.show()

            /**
             * edit wakeup for all here
             */

            myDialogView.btnApplyTime.setOnClickListener {
                SleepReminder.editAllWakeUp(
                    myDialogView.npHour.value,
                    myDialogView.npMinute.value
                )
                myAlertDialog.dismiss()
               updateRegularDisplay()
            }
        }

        v.panelSleepDuration.setOnClickListener {
            /**
             * pick sleep duration for ALL days
             */
            val myDialogView = LayoutInflater.from(activity)
                .inflate(R.layout.dialog_pick_time, null)

            myDialogView.npHour.minValue = 0
            myDialogView.npHour.maxValue = 23

            myDialogView.npMinute.minValue = 0
            myDialogView.npMinute.maxValue = 59

            myDialogView.tvHourMinuteDivider.text = "h"
            myDialogView.tvHourMinuteAttachment.text = "m"

            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val customTitle = LayoutInflater.from(activity)
                .inflate(R.layout.title_dialog_add_task, null)

            customTitle.tvDialogTitle.text = "Sleep Duration"
            myBuilder.setCustomTitle(customTitle)

            val myAlertDialog = myBuilder.create()
            myAlertDialog.show()

            //TODO GET DURATION FOR ALL HERE
            myDialogView.npHour.value = 0
            myDialogView.npMinute.value = 0

            myDialogView.btnApplyTime.setOnClickListener() {
                SleepReminder.editAllDuration(
                    myDialogView.npHour.value,
                    myDialogView.npMinute.value
                )
                myAlertDialog.dismiss()
               updateRegularDisplay()
            }
        }

        regularCheckBoxList.forEachIndexed { i, cb ->
            cb.setOnClickListener {
                val day = DayOfWeek.values()[i]
                if (cb.isChecked) {
                    SleepReminder.reminder[day]?.enable(day)
                } else {
                    SleepReminder.reminder[day]?.disable(day)
                }
            }
        }
    }

    private fun updateCustomTimes(){
        customWakeTimeTexts.forEachIndexed{ i, tv ->
            tv.text = SleepReminder.reminder[DayOfWeek.values()[i]]?.getWakeUpTimeString()
        }
        customDurationTexts.forEachIndexed{ i, tv ->
            tv.text = SleepReminder.reminder[DayOfWeek.values()[i]]?.getDurationTimeString()
        }
    }

    private fun updateCustomCheckBoxes(){
        customCheckBoxList.forEachIndexed{ i, cb ->
            cb.isChecked = SleepReminder.reminder[DayOfWeek.values()[i]]?.isSet!!
        }
    }

    private fun updateRegularTimes(){
        regularWakeTimeText.text = SleepReminder.reminder[DayOfWeek.MONDAY]?.getWakeUpTimeString()
        regularDurationTimeText.text = SleepReminder.reminder[DayOfWeek.MONDAY]?.getDurationTimeString()
    }

    private fun updateRegularCheckboxes(){
        regularCheckBoxList.forEachIndexed { i, cb ->
            cb.isChecked = SleepReminder.reminder[DayOfWeek.values()[i]]?.isSet!!
        }
    }


    private fun animationShowCustom(v: View){
        customPanelList.forEach{p -> p.isClickable = true}
        v.panelNotCustom.visibility = View.GONE
        val animationHide =
            AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down_reverse)
        animationHide.duration = 350
        animationHide.fillAfter = true
        v.panelNotCustom.startAnimation(animationHide)

        v.panelCustom.visibility = View.VISIBLE
        val animationShow =
            AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down)
        animationShow.duration = 700
        animationShow.fillAfter = true
        v.panelCustom.startAnimation(animationShow)
    }

    private fun animationShowRegular(v: View){
        customPanelList.forEach{p -> p.isClickable = false}
        v.panelCustom.visibility = View.GONE
        val animationHide =
            AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down_reverse)
        animationHide.duration = 700
        animationHide.fillAfter = true
        v.panelCustom.startAnimation(animationHide)

        v.panelNotCustom.visibility = View.VISIBLE
        val animationShow =
            AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down)
        animationShow.duration = 350
        animationShow.fillAfter = true
        animationShow.startOffset = 280
        v.panelNotCustom.startAnimation(animationShow)
    }

}


