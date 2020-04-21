package com.example.j7_003.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.NewSleepReminder
import com.example.j7_003.data.database.SleepReminder
import kotlinx.android.synthetic.main.dialog_pick_time.view.*
import kotlinx.android.synthetic.main.fragment_sleep.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.DayOfWeek

/**
 * A simple [Fragment] subclass.
 */
class SleepFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        NewSleepReminder.init()
        val myView = inflater.inflate(R.layout.fragment_sleep, container, false)

        val customCheckBoxList = arrayListOf(myView.checkBoxMo, myView.checkBoxTu, myView.checkBoxWe,
            myView.checkBoxTh, myView.checkBoxFr, myView.checkBoxSa, myView.checkBoxSu)

        val customPanelList = arrayListOf(myView.customMondayPanel, myView.customTuesdayPanel,
            myView.customWednesdayPanel, myView.customThursdayPanel, myView.customFridayPanel,
            myView.customSaturdayPanel, myView.customSundayPanel)

        if (NewSleepReminder.daysAreCustom) {
            myView.panelNotCustom.visibility = View.GONE
            myView.panelCustom.visibility = View.VISIBLE
        } else {
            myView.panelNotCustom.visibility = View.VISIBLE
            myView.panelCustom.visibility = View.GONE
        }

        updateFragmentDisplay(myView)
        updateCheckBoxes(customCheckBoxList)
        //initialize checkbox states, read from sleep reminder
        val cbsDayList = arrayListOf(
            myView.cbMonday, myView.cbTuesday, myView.cbWednsday,
            myView.cbThursday, myView.cbFriday, myView.cbSaturday, myView.cbSunday
        )

        cbsDayList.forEachIndexed { i, cb ->
            cb.isChecked = SleepReminder.days[i]
        }
        myView.switchEnableCustomDays.setOnClickListener {
            val speedCustom: Long = 700
            val speedNotCustom: Long = 350
            if (myView.switchEnableCustomDays.isChecked) {
                customPanelList.forEach{p -> p.isClickable = true}
                myView.panelNotCustom.visibility = View.GONE
                val animationHide =
                    AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down_reverse)
                animationHide.duration = speedNotCustom
                animationHide.fillAfter = true
                myView.panelNotCustom.startAnimation(animationHide)

                myView.panelCustom.visibility = View.VISIBLE
                val animationShow =
                    AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down)
                animationShow.duration = speedCustom
                animationShow.fillAfter = true
                animationShow.startOffset = speedNotCustom - 420
                myView.panelCustom.startAnimation(animationShow)
            } else {
                customPanelList.forEach{p -> p.isClickable = false}
                myView.panelCustom.visibility = View.GONE
                val animationHide =
                    AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down_reverse)
                animationHide.duration = speedCustom
                animationHide.fillAfter = true
                myView.panelCustom.startAnimation(animationHide)

                myView.panelNotCustom.visibility = View.VISIBLE
                val animationShow =
                    AnimationUtils.loadAnimation(MainActivity.myActivity, R.anim.scale_down)
                animationShow.duration = speedNotCustom
                animationShow.fillAfter = true
                animationShow.startOffset = speedCustom - 420
                myView.panelNotCustom.startAnimation(animationShow)
            }
        }

        myView.switchEnableReminder.setOnClickListener {
            if (myView.switchEnableReminder.isChecked) {
                NewSleepReminder.enableAll()
            } else {
                NewSleepReminder.disableAll()
            }
        }



        myView.panelWakeTime.setOnClickListener() {
            /**
             * pick wake up time for ALL
             */
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
            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val customTitle =
                LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
            customTitle.tvDialogTitle.text = "Wakeup Time"
            myBuilder.setCustomTitle(customTitle)

            //initialize numberpicker values
            myDialogView.npHour.value = SleepReminder.timings[2]
            myDialogView.npMinute.value = SleepReminder.timings[3]


            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.show()

            //EDIT WAKEUP FOR ALL DAYS HERE
            myDialogView.btnApplyTime.setOnClickListener() {
                NewSleepReminder.editAllWakeUp(
                    myDialogView.npHour.value,
                    myDialogView.npMinute.value
                )
                myAlertDialog.dismiss()
                updateFragmentDisplay(myView)
                updateCheckBoxes(customCheckBoxList)
                //initialize checkbox states, read from sleep reminder

            }


        }

        myView.panelSleepDuration.setOnClickListener() {
            /**
             * pick sleep duration for ALL days
             */
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.dialog_pick_time, null)

            myDialogView.npHour.minValue = 0
            myDialogView.npHour.maxValue = 23

            myDialogView.npMinute.minValue = 0
            myDialogView.npMinute.maxValue = 59

            myDialogView.tvHourMinuteDivider.text = "h"
            myDialogView.tvHourMinuteAttachment.text = "m"

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
            val customTitle =
                LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
            customTitle.tvDialogTitle.text = "Sleep Duration"
            myBuilder.setCustomTitle(customTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.show()

            //initialize numberpicker values
            myDialogView.npHour.value = SleepReminder.sDuration[0]
            myDialogView.npMinute.value = SleepReminder.sDuration[1]

            myDialogView.btnApplyTime.setOnClickListener() {
                NewSleepReminder.editAllDuration(
                    myDialogView.npHour.value,
                    myDialogView.npMinute.value
                )
                myAlertDialog.dismiss()
                updateFragmentDisplay(myView)
                updateCheckBoxes(customCheckBoxList)
                //initialize checkbox states, read from sleep reminder
            }
        }

        //toggle NonCustom Sleepreminder
        cbsDayList.forEachIndexed { i, cb ->
            cb.setOnClickListener {
                if (cb.isChecked) {
                    NewSleepReminder.reminder.get(DayOfWeek.values()[i])?.enable()
                } else {
                    NewSleepReminder.reminder.get(DayOfWeek.values()[i])?.disable()
                }
            }
        }

        //EVERYTHING FOR CUSTOM


        //CHECKBOXES TO ENABLE AND DISABLE CUSTOM DAYS
        customCheckBoxList.forEachIndexed{ i, cb ->
            cb.setOnClickListener{
                if(cb.isChecked){
                    NewSleepReminder.reminder[DayOfWeek.values()[i]]?.enable()
                }else{
                    NewSleepReminder.reminder[DayOfWeek.values()[i]]?.disable()
                }
            }
        }

        customPanelList.forEachIndexed{ i, p ->
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
               //AlertDialogBuilder
               val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
               val customTitle =
                   LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
               customTitle.tvDialogTitle.text = "Wakeup Time - "+DayOfWeek.values()[i].toString()
               myBuilder.setCustomTitle(customTitle)

               //initialize numberpicker values
               myDialogView.npHour.value = SleepReminder.timings[2]
               myDialogView.npMinute.value = SleepReminder.timings[3]

               //show dialog
               val myAlertDialog = myBuilder.create()
               myAlertDialog.show()

               //EDIT WAKEUP FOR ALL DAYS HERE
               myDialogView.btnApplyTime.setOnClickListener() {
                   NewSleepReminder.editWakeUpAtDay(
                       DayOfWeek.values()[i],
                       myDialogView.npHour.value,
                       myDialogView.npMinute.value
                   )
                   myAlertDialog.dismiss()
                   updateFragmentDisplay(myView)
                   updateCheckBoxes(customCheckBoxList)
                   //initialize checkbox states, read from sleep reminder

                   //START SECOND DIALOG
                   val myDialogView =
                       LayoutInflater.from(activity).inflate(R.layout.dialog_pick_time, null)

                   myDialogView.npHour.minValue = 0
                   myDialogView.npHour.maxValue = 23

                   myDialogView.npMinute.minValue = 0
                   myDialogView.npMinute.maxValue = 59

                   myDialogView.tvHourMinuteDivider.text = "h"
                   myDialogView.tvHourMinuteAttachment.text = "m"

                   //AlertDialogBuilder
                   val myBuilder = AlertDialog.Builder(MainActivity.myActivity).setView(myDialogView)
                   val customTitle =
                       LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
                   customTitle.tvDialogTitle.text = "Sleep Duration - "+DayOfWeek.values()[i].toString()
                   myBuilder.setCustomTitle(customTitle)

                   //show dialog
                   val myAlertDialog = myBuilder.create()
                   myAlertDialog.show()

                   //initialize numberpicker values
                   myDialogView.npHour.value = SleepReminder.sDuration[0]
                   myDialogView.npMinute.value = SleepReminder.sDuration[1]

                   myDialogView.btnApplyTime.setOnClickListener() {
                       NewSleepReminder.editDurationAtDay(
                           DayOfWeek.values()[i],
                           myDialogView.npHour.value,
                           myDialogView.npMinute.value
                       )
                       myAlertDialog.dismiss()
                       updateFragmentDisplay(myView)
                       updateCheckBoxes(customCheckBoxList)
                       //initialize checkbox states, read from sleep reminder
                   }


               }
           }
        }
        return myView
    }

    fun updateCheckBoxes(cblist: ArrayList<CheckBox>){
        cblist.forEachIndexed{ i, cb ->
            cb.isChecked = NewSleepReminder.reminder[DayOfWeek.values()[i]]?.isSet!!
        }
    }

    fun updateFragmentDisplay(pv: View) {
        pv.tvWakeTime.text = NewSleepReminder.reminder.get(DayOfWeek.MONDAY)?.getWakeUpTimeString()
        pv.tvSleepDuration.text = NewSleepReminder.reminder.get(DayOfWeek.MONDAY)?.getDurationTimeString()
        val wakeTimePanels = arrayListOf(pv.tvWakeTimeMo, pv.tvWakeTimeTu, pv.tvWakeTimeWe,
            pv.tvWakeTimeThu, pv.tvWakeTimeFr, pv.tvWakeTimeSa, pv.tvWakeTimeSu)

        val durationPanels = arrayListOf(pv.tvSDurMo, pv.tvSDurTu, pv.tvSDurWe,
            pv.tvSDurThu, pv.tvSDurFr, pv.tvSDurSa, pv.tvSDurSu)

        wakeTimePanels.forEachIndexed{ i, tv ->
            tv.setText(NewSleepReminder.reminder[DayOfWeek.values()[i]]?.getWakeUpTimeString())
        }
        durationPanels.forEachIndexed{ i, tv ->
            tv.setText(NewSleepReminder.reminder[DayOfWeek.values()[i]]?.getDurationTimeString())
        }

    }

}


