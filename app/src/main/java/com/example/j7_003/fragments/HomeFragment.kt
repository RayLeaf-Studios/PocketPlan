package com.example.j7_003.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.SleepReminder
import kotlinx.android.synthetic.main.fragment_home.view.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    lateinit var myView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        SleepReminder.init()

        myView = inflater.inflate(R.layout.fragment_home, container, false)
        updateRemainingWakeimeDisplay()
//        displayTasks(myView)
        return myView
    }

    fun updateRemainingWakeimeDisplay(){

        val (message, status) = SleepReminder.getRemainingWakeDurationString()

        //0 -> positive wake time, 1 -> negative wake time, 2 -> no reminder set
        when(status){
            0 -> { //show icon, set and show message, text white
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(ContextCompat.getColor(MainActivity.myActivity, R.color.colorOnBackGround))
            }
            1 ->{
                //show icon, set and show message, text red
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(ContextCompat.getColor(MainActivity.myActivity, R.color.colorNoteRed))
            }
            2 ->{
                //hide icon, hide text
                myView.icSleepHome.visibility = View.INVISIBLE
                myView.tvRemainingWakeTime.visibility = View.INVISIBLE
            }
        }
    }

//    fun displayTasks(myview: View){
//        var p1TaskCounter = 0
//        val taskList = Database.taskList
//        for(i in 0..taskList.size-1){
//            if(taskList[i].priority>1){
//                break
//            }
//            p1TaskCounter++
//        }
//        val displayTaskCount = minOf(p1TaskCounter, 3)
//        var taskPanelText = ""
//        for(i in 0 until displayTaskCount){
//            taskPanelText+="• "+taskList[i].title
////          taskPanelText+=(i+1).toString()+". "+taskList[i].title
//            if(i<displayTaskCount-1){
//                taskPanelText+="\n"
//            }
//        }
//        val additionalTasks = p1TaskCounter-displayTaskCount
//        if(additionalTasks!=0){
//            var addedLetter = "s"
//            if(additionalTasks==1){
//                addedLetter = ""
//            }
//            taskPanelText+="\n& "+additionalTasks+" more task"+addedLetter
//        }
//        if(taskPanelText.length==0){
//            myview.task_panel.visibility = View.GONE
//        }else {
//            myView.task_panel.text = taskPanelText
//
//        }
    }


