package com.example.j7_003

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.Database
import com.example.j7_003.fragments.*
import com.example.j7_003.notifications.NotificationReceiver
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.*

class MainActivity : AppCompatActivity(){
    private lateinit var homeFragment: HomeFragment
    private lateinit var calenderFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var todoFragment: TodoFragment
    private lateinit var modulesFragment: ModulesFragment
    private lateinit var sleepFragment: SleepFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var writeNoteFragment: WriteNoteFragment

    private lateinit var bottomNavigation: BottomNavigationView

    private var activeFragmentTag = ""

    companion object {
        lateinit var myActivity: MainActivity
//        var editNotePosition: Int = -1
        var holder: NoteAdapter.NoteViewHolder? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)
        myActivity = this
        Database.init()

        //debug kannst gerne mal mit den zeiten rum testen, jetzt sollte es eine notification
        //geben wenn die Reminder time erreicht ist
        /*SleepReminder.editReminder(1, 34)
        SleepReminder.editWakeUp(2, 0)
        SleepReminder.enable()
        SleepReminder.setSleepReminderAlarm()*/

        setBirthdayAlarms()

        bottomNavigation = findViewById(R.id.btm_nav)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.notes -> changeToNotes()
                R.id.todolist -> changeToToDo()
                R.id.home -> changeToHome()
                R.id.calendar -> changeToCalendar()
                R.id.modules -> changeToModules()
            }
            true
        }

        /**
         * Checks intent for passed String-Value, indicating required switching into fragment
         * that isn't the home fragment
         */

        when(intent.getStringExtra("NotificationEntry")){
            "birthdays" -> changeToBirthdays()
            "SReminder" -> TODO("wohin auch immer die sleep reminder notification führen soll")
            else -> changeToHome()
        }

    }

    fun changeToBirthdays(){
        if(activeFragmentTag!="birthdays") {
            birthdayFragment = BirthdayFragment()
            bottomNavigation.selectedItemId = R.id.modules
            supportActionBar?.title = "Birthdays"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, birthdayFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="birthdays"

        }
    }

    private fun changeToToDo(){
        if(activeFragmentTag!="todo") {
            todoFragment = TodoFragment()
            supportActionBar?.title = "To-Do"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, todoFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="todo"
        }
    }

    fun changeToHome(){
        if(activeFragmentTag!="home"){
            homeFragment = HomeFragment()
            supportActionBar?.title = "Home"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, homeFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="home"
            bottomNavigation.selectedItemId=R.id.home
        }
    }

    private fun changeToCalendar(){
        if(activeFragmentTag!="calendar") {
            calenderFragment = CalenderFragment()
            supportActionBar?.title = "Calendar"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, calenderFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="calendar"
            bottomNavigation.selectedItemId=R.id.calendar
        }
    }

    private fun changeToModules(){
        if(activeFragmentTag!="modules") {
            modulesFragment = ModulesFragment()
            supportActionBar?.title = "Modules"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, modulesFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="modules"
            bottomNavigation.selectedItemId=R.id.modules
        }
    }

    fun changeToSettings(){
        if(activeFragmentTag!="settings") {
            settingsFragment = SettingsFragment()
            supportActionBar?.title = "Settings"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, settingsFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="settings"
        }
    }

    fun changeToSleepReminder(){
        if(activeFragmentTag!="sleep") {
            sleepFragment = SleepFragment()
            supportActionBar?.title = "Sleep-Reminder"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, sleepFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="sleep"
        }
    }

     fun changeToNotes(){
        if(activeFragmentTag!="notes") {
            noteFragment = NoteFragment()
            supportActionBar?.title = "Notes"
            supportActionBar?.setDisplayShowCustomEnabled(false)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, noteFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="notes"
            bottomNavigation.selectedItemId=R.id.notes
        }
    }

    fun changeToWriteNoteFragment(){
        if(activeFragmentTag!="writeNote") {
            supportActionBar?.title=""
            writeNoteFragment = WriteNoteFragment()
            supportActionBar?.setDisplayShowCustomEnabled(true)
            supportActionBar?.customView = layoutInflater.inflate(R.layout.appbar_write_note, null)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, writeNoteFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="writeNote"
        }
    }

    fun setBirthdayAlarms(hour: Int = 12, minute: Int = 0) {
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("Notification", "Birthday")

        val pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager

        val notificationTime = Calendar.getInstance()
        notificationTime.set(Calendar.HOUR_OF_DAY, hour)
        notificationTime.set(Calendar.MINUTE, minute)
        notificationTime.set(Calendar.SECOND, 0)

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, notificationTime.timeInMillis, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

    override fun onBackPressed() {

        when {
            activeFragmentTag=="writeNote" -> {
                changeToNotes()
            }
            activeFragmentTag!="home" -> {
                changeToHome()
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

}

