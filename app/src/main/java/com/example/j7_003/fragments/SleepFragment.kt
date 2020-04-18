package com.example.j7_003.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.j7_003.MainActivity

import com.example.j7_003.R
import com.example.j7_003.data.database.SleepReminder
import kotlinx.android.synthetic.main.dialog_pick_time.view.*
import kotlinx.android.synthetic.main.fragment_sleep.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

/**
 * A simple [Fragment] subclass.
 */
class SleepFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        SleepReminder.init()
        val myView = inflater.inflate(R.layout.fragment_sleep, container, false)

        updateFragmentDisplay(myView)
        val cbsDayList = arrayListOf(myView.cbMonday, myView.cbTuesday, myView.cbWednsday,
            myView.cbThursday, myView.cbFriday, myView.cbSaturday, myView.cbSunday)

        //initialize checkbox states, read from sleep reminder
        cbsDayList.forEachIndexed{i, cb ->
            cb.isChecked = SleepReminder.days[i]
        }
        myView.switchEnableReminder.setOnClickListener {
            if(myView.switchEnableReminder.isChecked){
                SleepReminder.enable()
            }else{
                SleepReminder.disable()
            }
        }


        myView.constraintLayout.setOnClickListener() {
            /**
             * pick wake up time
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


            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.show()

            myDialogView.btnApplyTime.setOnClickListener(){
                SleepReminder.editWakeUp(myDialogView.npHour.value, myDialogView.npMinute.value)
                myAlertDialog.dismiss()
                updateFragmentDisplay(myView)

            }


        }

        myView.constraintLayout2.setOnClickListener() {
            /**
             * pick sleep duration
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

            myDialogView.btnApplyTime.setOnClickListener(){
                SleepReminder.editDuration(myDialogView.npHour.value, myDialogView.npMinute.value)
                myAlertDialog.dismiss()
                updateFragmentDisplay(myView)
            }
        }

        cbsDayList.forEachIndexed{i, cb ->
            cb.setOnClickListener{
                SleepReminder.setDay(i, cb.isChecked)
                }
        }
        return myView
    }

    fun updateFragmentDisplay(passedView: View){
        passedView.tvWakeTime.text = SleepReminder.getWakeUpTimeString()
        passedView.tvSleepDuration.text = SleepReminder.getDurationTimeString()
    }

}


