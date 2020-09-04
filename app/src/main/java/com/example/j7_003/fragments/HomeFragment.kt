package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.CalendarManager
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.SleepReminder
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.row_term.view.tvTermItemInfo
import kotlinx.android.synthetic.main.row_term.view.tvTermItemTitle
import kotlinx.android.synthetic.main.row_term_day.view.*
import org.threeten.bp.LocalDate


/**
 * A simple [Fragment] subclass.
 */
class HomeFragment : Fragment() {

    lateinit var myView: View

    companion object{
        lateinit var homeTermRecyclerView: RecyclerView
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        SleepReminder.init()
        CalendarManager.init()


        //initializing layout
        myView = inflater.inflate(R.layout.fragment_home, container, false)

        //updating ui
        updateWaketimePanel()
        updateTaskPanel()
        updateBirthdayPanel()

        //Onclick listeners for task panel, birthday panel and sleep panel,
        myView.panelTasks.setOnClickListener { MainActivity.act.changeToToDo() }
        myView.panelBirthdays.setOnClickListener { MainActivity.act.changeToBirthdays() }
        myView.tvRemainingWakeTime.setOnClickListener { MainActivity.act.changeToSleepReminder() }
        myView.icSleepHome.setOnClickListener{MainActivity.act.changeToSleepReminder()}



        //buttons to create new notes, tasks, terms or items from the home panel
        myView.btnNewNote.setOnClickListener{
            MainActivity.fromHome = true
            MainActivity.act.changeToCreateNoteFragment()
        }
        myView.btnNewTask.setOnClickListener{ createTaskFromHome()}
        myView.btnNewTerm.setOnClickListener {
            MainActivity.fromHome = true
            MainActivity.act.changeToCreateTerm()  }
        myView.btnNewItem.setOnClickListener { MainActivity.act.changeToAddItem() }


        //recyclerview holding the terms for today
        homeTermRecyclerView = myView.recycler_view_home
        val myAdapter = HomeTermAdapterDay()
        myAdapter.setDate(LocalDate.now())
        homeTermRecyclerView.adapter = myAdapter
        homeTermRecyclerView.layoutManager = LinearLayoutManager(MainActivity.act)

        return myView
    }

    //Sets the text of tvTasks to the titles of the first 3 important tasks
    @SuppressLint("SetTextI18n")
    private fun updateTaskPanel() {
        var p1TaskCounter = 0
        val taskList = Database.taskList

        //sets p1TaskCounter to amount of Tasks with priority 1
        for (i in 0 until taskList.size) {
            if (taskList[i].priority > 1 || taskList[i].isChecked) {
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
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            myView.icTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            return
        }else{
            myView.tvTasks.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
            myView.icTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
        }

        //creates text displaying the tasks by concatenating their titles with newlines
        var taskPanelText = "\n"
        for (i in 0 until displayTaskCount) {
            taskPanelText += "   "+taskList[i].title.take(28)
            if(taskList[i].title.length>27){
                taskPanelText+="..."
            }
            if (i < displayTaskCount) {
                taskPanelText += "\n"
            }
        }

        //displays "+ (additionalTasks) more" if there are more than 3 important tasks
        val additionalTasks = p1TaskCounter - displayTaskCount
        if (additionalTasks != 0) {
            taskPanelText += "   + $additionalTasks more\n"
        }

        //sets the testViews text to taskPanelText
        myView.tvTasks.text = taskPanelText

    }

    @SuppressLint("SetTextI18n")
    private fun updateBirthdayPanel(){
        val birthdaysToday = Database.getRelevantCurrentBirthdays()
        val birthdaysToDisplay = minOf(birthdaysToday.size, 3)
        if(birthdaysToDisplay == 0){
            myView.tvBirthday.text = "\n   No birthdays today\n"
            myView.tvBirthday.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            myView.icBirthdaysHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            return
        }else{

            myView.tvBirthday.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
            myView.icBirthdaysHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
        }
        var birthdayText = "\n"
        for (i in 0 until birthdaysToDisplay){
            birthdayText += "   "+ birthdaysToday[i].name+"\n"
        }
        val excess = birthdaysToday.size - birthdaysToDisplay
        if(excess > 0){
            birthdayText += "   + $excess more\n"
        }

        myView.tvBirthday.text = birthdayText
    }

    private fun updateWaketimePanel() {

        val (message, status) = SleepReminder.getRemainingWakeDurationString()

        //0 -> positive wake time, 1 -> negative wake time, 2 -> no reminder set
        when (status) {
            0 -> { //show icon, set and show message, text white
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
                myView.icSleepHome.setColorFilter(
                    ContextCompat.getColor(
                        MainActivity.act,
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
                        MainActivity.act,
                        R.color.colorGoToSleep
                    )
                )
                myView.icSleepHome.setColorFilter(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorGoToSleep
                    )
                )
            }
            2 -> {
                //hide icon, hide text
                myView.icSleepHome.visibility = View.GONE
                myView.tvRemainingWakeTime.visibility = View.GONE
            }
        }
    }

    @SuppressLint("InflateParams")
    private fun createTaskFromHome(){
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
                if(title.isEmpty()){
                    Toast.makeText(MainActivity.act, "Can't create an empty task!", Toast.LENGTH_SHORT).show()
                }else {
                    Database.addTask(title, index + 1, false)
                    updateTaskPanel()
                }
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
    }

}


class HomeTermAdapterDay :
    RecyclerView.Adapter<HomeTermAdapterDay.HomeTermViewHolderDay>() {

    private lateinit var daylist: ArrayList<CalendarAppointment>
    fun setDate(date: LocalDate){
        daylist = CalendarManager.getDayView(date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTermViewHolderDay {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_term_day, parent, false)
        return HomeTermViewHolderDay(itemView)
    }

    override fun onBindViewHolder(holder: HomeTermViewHolderDay, position: Int) {

        val currentTerm = daylist[position]

        holder.itemView.setOnClickListener {
            //todo start CreateTermFragment in EDIT mode
//            MainActivity.myActivity.changeToDayView()
        }

        holder.tvTitle.text = currentTerm.title
        holder.tvInfo.text = currentTerm.addInfo

        //hides end time of a term if its identical to start time
        if(currentTerm.startTime == currentTerm.eTime){
            holder.tvStartTime.text = currentTerm.startTime.toString()
            holder.tvEndTime.text = ""
            holder.tvDashUntil.visibility = View.INVISIBLE
        }else{
            holder.tvStartTime.text = currentTerm.startTime.toString()
            holder.tvEndTime.text = currentTerm.eTime.toString()
            holder.tvDashUntil.visibility = View.VISIBLE
        }

    }

    override fun getItemCount() = daylist.size

    class HomeTermViewHolderDay(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_term_day and meta data
         * like position, it also holds references to views inside of the layout
         */
        val tvTitle: TextView = itemView.tvTermItemTitle
        val tvInfo: TextView = itemView.tvTermItemInfo
        val tvStartTime: TextView = itemView.tvTermItemStartTime
        val tvEndTime: TextView = itemView.tvTermItemEndTime
        val tvDashUntil: TextView = itemView.tvDashUntil
    }

}
