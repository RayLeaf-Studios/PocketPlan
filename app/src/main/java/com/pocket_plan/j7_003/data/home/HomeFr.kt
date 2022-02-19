package com.pocket_plan.j7_003.data.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.notelist.NoteColors
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.data.todolist.TodoFr
import kotlinx.android.synthetic.main.fragment_home.view.*


/**
 * A simple [Fragment] subclass.
 */
class HomeFr : Fragment() {
    private lateinit var myActivity: MainActivity

    private var cr = 0f
    private lateinit var myBirthdayFr: BirthdayFr
    private lateinit var mySleepFr: SleepFr

    lateinit var myView: View
    private lateinit var timer: CountDownTimer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myActivity = (activity as MainActivity)

        cr = myActivity.resources.getDimension(R.dimen.cornerRadius)
        myBirthdayFr = myActivity.getFragment(FT.BIRTHDAYS) as BirthdayFr
        mySleepFr = myActivity.getFragment(FT.SLEEP) as SleepFr
        mySleepFr.sleepReminderInstance = SleepReminder(myActivity)

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
            myActivity.changeToFragment(FT.TASKS)
        }
        myView.panelBirthdays.setOnClickListener { myActivity.changeToFragment(FT.BIRTHDAYS) }
        myView.tvRemainingWakeTime.setOnClickListener { myActivity.changeToFragment(FT.SLEEP) }
        myView.icSleepHome.setOnClickListener { myActivity.changeToFragment(FT.SLEEP) }


        //buttons to create new notes, tasks, terms or items from the home panel
        myView.clAddNote.setOnClickListener {
            PreferenceManager.getDefaultSharedPreferences(myActivity).edit()
                .putBoolean("editingNote", false).apply()
            NoteEditorFr.noteColor = NoteColors.GREEN
            myActivity.changeToFragment(FT.NOTE_EDITOR)
        }
        myView.clAddTask.setOnClickListener { myActivity.todoFr!!.dialogAddTask() }
        myView.clAddItem.setOnClickListener {
            myActivity.multiShoppingFr!!.editing = false
            myActivity.multiShoppingFr!!.openAddItemDialog()
        }

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
        updateWakeTimePanel()
        updateTaskPanel(true)
        super.onResume()
    }

    /**
     * Sets the text of myView.tvTasks to the titles of at most 3 priority 1 tasks
     * @param shake if true, animates a shake animation on myView.ivTaskHome
     */

    @SuppressLint("ResourceType")
    fun updateTaskPanel(shake: Boolean) {
        val density = myActivity.resources.displayMetrics.density
        val (_, status) = mySleepFr.sleepReminderInstance.getRemainingWakeDurationString()
        val params = myView.panelTasks.layoutParams as ViewGroup.MarginLayoutParams
        val sideMargin = (density * 3).toInt()
        val bottomMargin = (density * 10).toInt()

        if (status == 2) {
            //no sleep, bigger distance
            params.setMargins(sideMargin, (density * 15).toInt(), sideMargin, bottomMargin)
        } else {
            //sleep present, smaller distance
            params.setMargins(sideMargin, bottomMargin, sideMargin, bottomMargin)
        }


        if (SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean) {
            myView.panelTasks.radius = cr
        }

        var myShake = shake
        if (!(SettingsManager.getSetting(SettingId.SHAKE_TASK_HOME) as Boolean)) {
            myShake = false
        }
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
                myActivity.colorForAttr(R.attr.colorHint)
            )
            myView.ivTasksHome.setColorFilter(
                myActivity.colorForAttr(R.attr.colorHint)
            )
            return
        } else {
            myView.tvTasks.setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )

            myView.ivTasksHome.setColorFilter(
                myActivity.colorForAttr(R.attr.colorGoToSleep)
            )

            if (myShake) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake_long)
                myView.ivTasksHome.startAnimation(animationShake)
            }

        }

        //creates text displaying the tasks by concatenating their titles with newlines
        var taskPanelText = "\n"
        for (i in 0 until displayTaskCount) {
            taskPanelText += taskList[i].title
            if (i < displayTaskCount) {
                taskPanelText += "\n"
            }
        }

        //displays "+ (additionalTasks) more" if there are more than 3 important tasks
        val additionalTasks = p1TaskCounter - displayTaskCount
        if (additionalTasks != 0) {
            taskPanelText += "+ $additionalTasks\n"
        }

        //sets the testViews text to taskPanelText
        myView.tvTasks.text = taskPanelText

    }

    private fun updateBirthdayPanel() {

        //round corners of birthday panel if settings say so
        if (SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean) {
            myView.panelBirthdays.radius = cr
        }

        //get list of birthdays today
        val birthdaysToday = myBirthdayFr.birthdayListInstance.getRelevantCurrentBirthdays()

        //get amount of birthdays to display (max = 3)
        val birthdaysToDisplay = minOf(birthdaysToday.size, 3)

        if (birthdaysToDisplay != 0) {
            //if there are any birthdays today set color to be black / white and set text to these birthdays
            myView.tvBirthday.setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
            myView.icBirthdaysHome.setColorFilter(
                myActivity.colorForAttr(R.attr.colorBirthdayNotify)
            )
            var birthdayText = "\n"
            for (i in 0 until birthdaysToDisplay) {
                birthdayText += birthdaysToday[i].name + "\n"
            }
            val excess = birthdaysToday.size - birthdaysToDisplay
            if (excess > 0) {
                birthdayText += "+ $excess\n"
            }
            myView.tvBirthday.text = birthdayText
            return
        }
        //no birthday today, set colors to gray
        myView.tvBirthday.setTextColor(
            myActivity.colorForAttr(R.attr.colorHint)
        )
        myView.icBirthdaysHome.setColorFilter(
            myActivity.colorForAttr(R.attr.colorHint)
        )

        //check for ANY birthday in the next 30 days
        val nextBirthday = myBirthdayFr.birthdayListInstance.getNextRelevantBirthday()
        if (nextBirthday != null && SettingsManager.getSetting(SettingId.PREVIEW_BIRTHDAY) as Boolean) {
            //if any birthday was found, display it
            val daysUntilString = when (val daysUntil = nextBirthday!!.daysUntil()) {
                //"tomorrow"
                1 -> myActivity.resources.getString(R.string.birthdayTomorrow)
                //"in x days"
                else ->
                    myActivity.resources.getString(R.string.birthdayIn) + " " + daysUntil
                        .toString() + " " + myActivity.resources.getQuantityString(
                        R.plurals.dayIn,
                        daysUntil
                    )
            }
            val birthdayText = nextBirthday.name + " " + daysUntilString
            myView.tvBirthday.text = birthdayText
            return
        }

        //no birthday today nor any birthday in the next 30 days => display "No birthdays"
        myView.tvBirthday.text = resources.getText(R.string.homeNoBirthdays)

    }

    /**
     * Checks if SleepReminder is active and shows time / icon in correct color if that's the case.
     */
    private fun updateWakeTimePanel() {

        val (message, status) = mySleepFr.sleepReminderInstance.getRemainingWakeDurationString()

        //0 -> positive wake time, 1 -> negative wake time, 2 -> no reminder set
        when (status) {
            0 -> { //show icon, set and show message, text white
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
                myView.icSleepHome.setColorFilter(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
            }
            1 -> {
                //show icon, set and show message, text red
                myView.icSleepHome.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.text = message
                myView.tvRemainingWakeTime.visibility = View.VISIBLE
                myView.tvRemainingWakeTime.setTextColor(
                    myActivity.colorForAttr(R.attr.colorGoToSleep)
                )
                myView.icSleepHome.setColorFilter(
                    myActivity.colorForAttr(R.attr.colorGoToSleep)
                )
            }
            2 -> {
                //hide icon, hide text
                myView.icSleepHome.visibility = View.GONE
                myView.tvRemainingWakeTime.visibility = View.GONE
            }
        }
    }
}


