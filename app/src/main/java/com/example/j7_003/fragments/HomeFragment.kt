package com.example.j7_003.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.SleepReminder
import kotlinx.android.synthetic.main.dialog_add_task.view.*
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


        updateWaketimePanel()
        updateTaskPanel()
        updateBirthdayPanel()

        myView.panelTasks.setOnClickListener { MainActivity.myActivity.changeToToDo() }
        myView.panelBirthdays.setOnClickListener { MainActivity.myActivity.changeToBirthdays() }
        myView.tvRemainingWakeTime.setOnClickListener { MainActivity.myActivity.changeToSleepReminder() }
        myView.icSleepHome.setOnClickListener{MainActivity.myActivity.changeToSleepReminder()}


        myView.btnNewNote.setOnClickListener{
            MainActivity.fromHome = true
            MainActivity.myActivity.changeToCreateNoteFragment()
        }
        myView.btnNewTask.setOnClickListener{ createTaskFromHome()}



        return myView
    }

    //Sets the text of tvTasks to the titles of the first 3 important tasks
    fun updateTaskPanel() {
        var p1TaskCounter = 0
        val taskList = Database.taskList

        //sets p1TaskCounter to amount of Tasks with priority 1
        for (i in 0..taskList.size - 1) {
            if (taskList[i].priority > 1) {
                break
            }
            p1TaskCounter++
        }

        //sets displayTaskCount to amount of tasks that will be displayed
        val displayTaskCount = minOf(p1TaskCounter, 3)

        //displays "No important tasks" if there aren't any
        if(displayTaskCount==0){
            myView.tvTasks.text = "\n   No important tasks\n"
            myView.tvTasks.setTextColor(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorHint
                )
            )
            myView.icTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorHint
                )
            )
            return
        }else{
            myView.tvTasks.setTextColor(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorOnBackGround
                )
            )
            myView.icTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorOnBackGround
                )
            )
        }



        //creates text displaying the tasks by concatenating their titles with newlines
        var taskPanelText = "\n"
        for (i in 0 until displayTaskCount) {
            taskPanelText += "   "+taskList[i].title
            if (i < displayTaskCount) {
                taskPanelText += "\n"
            }
        }

        //displays "+ (additionalTasks) more" if there are more than 3 important tasks
        val additionalTasks = p1TaskCounter - displayTaskCount
        if (additionalTasks != 0) {
            taskPanelText += "   + " + additionalTasks + " more\n"
        }

        //sets the testViews text to taskPanelText
        myView.tvTasks.text = taskPanelText

    }

    fun updateBirthdayPanel(){
        val birthdaysToday = Database.getRelevantCurrentBirthdays()
        val birthdaysToDisplay = minOf(birthdaysToday.size, 3)
        if(birthdaysToDisplay == 0){
            myView.tvBirthday.text = "\n   No birthdays today\n"
            myView.tvBirthday.setTextColor(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorHint
                )
            )
            myView.icBirthdaysHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorHint
                )
            )
            return
        }else{

            myView.tvBirthday.setTextColor(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorOnBackGround
                )
            )
            myView.icBirthdaysHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.myActivity,
                    R.color.colorOnBackGround
                )
            )
        }
        var birthdayText = "\n"
        for (i in 0 .. birthdaysToDisplay-1){
            birthdayText += "   "+birthdaysToday.get(i).name+"\n"
        }
        val excess = birthdaysToday.size - birthdaysToDisplay
        if(excess > 0){
            birthdayText += "   + $excess more\n"
        }

        myView.tvBirthday.text = birthdayText
    }

    fun updateWaketimePanel() {

        val (message, status) = SleepReminder.getRemainingWakeDurationString()

        //0 -> positive wake time, 1 -> negative wake time, 2 -> no reminder set
        when (status) {
            0 -> { //show icon, set and show message, text white
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.myActivity,
                        R.color.colorOnBackGround
                    )
                )
                myView.icSleepHome.setColorFilter(
                    ContextCompat.getColor(
                        MainActivity.myActivity,
                        R.color.colorOnBackGround
                    )
                )
            }
            1 -> {
                //show icon, set and show message, text red
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.myActivity,
                        R.color.colorGoToSleep
                    )
                )
                myView.icSleepHome.setColorFilter(
                    ContextCompat.getColor(
                        MainActivity.myActivity,
                        R.color.colorGoToSleep
                    )
                )
            }
            2 -> {
                //hide icon, hide text
                myView.icSleepHome.visibility = View.INVISIBLE
                myView.tvRemainingWakeTime.visibility = View.INVISIBLE
            }
        }
    }

    fun createTaskFromHome(){
        //inflate the dialog with custom view
        val myDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_task, null)

        //AlertDialogBuilder
        val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        myBuilder?.setCustomTitle(layoutInflater.inflate(R.layout.title_dialog_add_task, null))

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //adds listeners to confirmButtons in addTaskDialog
        val taskConfirmButtons = arrayListOf<Button>(
            myDialogView.btnConfirm1,
            myDialogView.btnConfirm2,
            myDialogView.btnConfirm3
        )

        taskConfirmButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                myAlertDialog?.dismiss()
                val title = myDialogView.etxTitleAddTask.text.toString()
                Database.addTask(title, index + 1, false)
                updateTaskPanel()
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
    }

}


