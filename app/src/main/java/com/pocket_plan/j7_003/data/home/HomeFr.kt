package com.pocket_plan.j7_003.data.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.notelist.NoteColors
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.todolist.TodoFr
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_home.view.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFr : Fragment() {


    lateinit var myView: View
    private lateinit var timer: CountDownTimer

//    V.2
//    companion object {
//        lateinit var homeTermRecyclerView: RecyclerView
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //V.2
        //CalendarManager.init()

        //initializing layout
        myView = inflater.inflate(R.layout.fragment_home, container, false)

        timer = object : CountDownTimer(Long.MAX_VALUE, 30000) {
            // creates a timer to update the clock
            override fun onTick(millisUntilFinished: Long) {    // called on each tick (~30 sec)
                updateWakeTimePanel()
            }

            override fun onFinish() {   // restarts the timer if fragment isn't closed
                this.start()    // should never happen (due to Long.MAX_VALUE duration)
            }
        }.start()

        //updating ui
        updateWakeTimePanel()
        updateTaskPanel(true)
        updateBirthdayPanel()

        //Onclick listeners for task panel, birthday panel and sleep panel,
        myView.panelTasks.setOnClickListener {
            MainActivity.act.changeToFragment(FT.TASKS)
        }
        myView.panelBirthdays.setOnClickListener { MainActivity.act.changeToFragment(FT.BIRTHDAYS) }
        myView.tvRemainingWakeTime.setOnClickListener { MainActivity.act.changeToFragment(FT.SLEEP) }
        myView.icSleepHome.setOnClickListener { MainActivity.act.changeToFragment(FT.SLEEP) }


        //buttons to create new notes, tasks, terms or items from the home panel
        myView.clAddNote.setOnClickListener {
            NoteEditorFr.noteColor = NoteColors.GREEN
            MainActivity.act.changeToFragment(FT.NOTE_EDITOR)
        }
        myView.clAddTask.setOnClickListener { createTaskFromHome() }
        myView.clAddItem.setOnClickListener {
            MainActivity.tempShoppingFr.openAddItemDialog()
        }

//        V.2
//        myView.btnNewTerm.setOnClickListener {
//            MainActivity.fromHome = true
//            MainActivity.act.changeToCreateTerm()  }

//        recyclerview holding the terms for today
//        homeTermRecyclerView = myView.recycler_view_home
//        val myAdapter = HomeTermAdapterDay()
//        myAdapter.setDate(LocalDate.now())
//        homeTermRecyclerView.adapter = myAdapter
//        homeTermRecyclerView.layoutManager = LinearLayoutManager(MainActivity.act)

        return myView
    }

    /**
     * Called when the fragment is stopped.
     */
    override fun onStop() {
        timer.cancel()
        super.onStop()
    }

    override fun onResume() {
        updateTaskPanel(true)
        super.onResume()
    }

    /**
     * Sets the text of myView.tvTasks to the titles of at most 3 priority 1 tasks
     * @param shake if true, animates a shake animation on myView.ivTaskHome
     */

    private fun updateTaskPanel(shake: Boolean) {
        var p1TaskCounter = 0
        val taskList = TodoFr.todoListInstance

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
        if (displayTaskCount == 0) {
            myView.tvTasks.text = resources.getText(R.string.homeNoTasks)
            myView.tvTasks.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            myView.ivTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorHint
                )
            )
            return
        } else {
            myView.tvTasks.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
            myView.ivTasksHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorGoToSleep
                )
            )
            if(shake){
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake_long)
                myView.ivTasksHome.startAnimation(animationShake)
            }

        }

        //creates text displaying the tasks by concatenating their titles with newlines
        var taskPanelText = "\n"
        for (i in 0 until displayTaskCount) {
            taskPanelText += "•  "+taskList[i].title
            if (i < displayTaskCount) {
                taskPanelText += "\n"
            }
        }

        //displays "+ (additionalTasks) more" if there are more than 3 important tasks
        val additionalTasks = p1TaskCounter - displayTaskCount
        if (additionalTasks != 0) {
            taskPanelText += "+ $additionalTasks more\n"
        }

        //sets the testViews text to taskPanelText
        myView.tvTasks.text = taskPanelText

    }

    private fun updateBirthdayPanel() {
        val birthdaysToday = BirthdayFr.birthdayListInstance.getRelevantCurrentBirthdays()
        val birthdaysToDisplay = minOf(birthdaysToday.size, 3)
        if (birthdaysToDisplay == 0) {
            myView.tvBirthday.text = resources.getText(R.string.homeNoBirthdays)
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
        } else {

            myView.tvBirthday.setTextColor(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
            myView.icBirthdaysHome.setColorFilter(
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorNudelnundGetreideL
                )
            )
        }
        var birthdayText = "\n"
        for (i in 0 until birthdaysToDisplay) {
            birthdayText += "•  " + birthdaysToday[i].name + "\n"
        }
        val excess = birthdaysToday.size - birthdaysToDisplay
        if (excess > 0) {
            birthdayText += "  + $excess\n"
        }
        myView.tvBirthday.text = birthdayText

    }

    private fun updateWakeTimePanel() {

        val (message, status) = SleepFr.sleepReminderInstance.getRemainingWakeDurationString()

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
    private fun createTaskFromHome() {
        //inflate the dialog with custom view
        val myDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_task, null)

        //AlertDialogBuilder
        val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        myBuilder?.setCustomTitle(layoutInflater.inflate(R.layout.title_dialog, null))

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
                val title = myDialogView.etxTitleAddTask.text.toString()
                if (title.isEmpty()) {
                    val animationShake =
                        AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                    myDialogView.etxTitleAddTask.startAnimation(animationShake)
                    return@setOnClickListener
                } else {
                    TodoFr.todoListInstance.addTask(title, index + 1, false)
                    updateTaskPanel(false)
                }
                if (MainActivity.activeFragmentTag == FT.HOME) {
                    Toast.makeText(MainActivity.act, resources.getString(R.string.home_notification_add_task), Toast.LENGTH_SHORT).show()
                }
                myAlertDialog?.dismiss()
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
    }
}


//V2
//class HomeTermAdapterDay :
//    RecyclerView.Adapter<HomeTermAdapterDay.HomeTermViewHolderDay>() {
//
//    private lateinit var dayList: ArrayList<CalendarAppointment>
//    fun setDate(date: LocalDate) {
//        dayList = CalendarManager.getDayView(date)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeTermViewHolderDay {
//        val itemView = LayoutInflater.from(parent.context)
//            .inflate(R.layout.row_term_day, parent, false)
//        return HomeTermViewHolderDay(itemView)
//    }
//
//    override fun onBindViewHolder(holder: HomeTermViewHolderDay, position: Int) {
//
//        val currentTerm = dayList[position]
//
//        holder.itemView.setOnClickListener {
//            //start CreateTermFragment in EDIT mode
////            MainActivity.myActivity.changeToDayView()
//        }
//
//        holder.tvTitle.text = currentTerm.title
//        holder.tvInfo.text = currentTerm.addInfo
//
//        //hides end time of a term if its identical to start time
//        if (currentTerm.startTime == currentTerm.eTime) {
//            holder.tvStartTime.text = currentTerm.startTime.toString()
//            holder.tvEndTime.text = ""
//            holder.tvDashUntil.visibility = View.INVISIBLE
//        } else {
//            holder.tvStartTime.text = currentTerm.startTime.toString()
//            holder.tvEndTime.text = currentTerm.eTime.toString()
//            holder.tvDashUntil.visibility = View.VISIBLE
//        }
//    }
//
//    override fun getItemCount() = dayList.size
//
//    class HomeTermViewHolderDay(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        /**
//         * One instance of this class will contain one "instance" of row_term_day and meta data
//         * like position, it also holds references to views inside of the layout
//         */
//        val tvTitle: TextView = itemView.tvTermItemTitle
//        val tvInfo: TextView = itemView.tvTermItemInfo
//        val tvStartTime: TextView = itemView.tvTermItemStartTime
//        val tvEndTime: TextView = itemView.tvTermItemEndTime
//        val tvDashUntil: TextView = itemView.tvDashUntil
//    }
//}
