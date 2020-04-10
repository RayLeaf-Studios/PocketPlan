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

class MainActivity : AppCompatActivity(){
    private lateinit var homeFragment: HomeFragment
    private lateinit var calenderFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var todoFragment: TodoFragment
    private lateinit var modulesFragment: ModulesFragment
    private lateinit var sleepFragment: SleepFragment
    private lateinit var noteFragment: NoteFragment

    private lateinit var bottomNavigation: BottomNavigationView

    private var activeFragmentTag = ""

    companion object {
        lateinit var myActivity: MainActivity
        lateinit var database: Database
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        database = Database(this)
        myActivity = this

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
            else -> changeToHome()
        }

    }

    fun changeToBirthdays(){
        if(activeFragmentTag!="birthdays") {
            bottomNavigation.selectedItemId = R.id.modules
            supportActionBar?.title = "Birthdays"
            birthdayFragment = BirthdayFragment()
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
            supportActionBar?.title = "To-Do"
            todoFragment = TodoFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, todoFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="todo"
        }
    }

    private fun changeToHome(){
        if(activeFragmentTag!="home"){
            supportActionBar?.title = "Home"
            homeFragment = HomeFragment()
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
            supportActionBar?.title = "Calendar"
            calenderFragment = CalenderFragment()
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
            supportActionBar?.title = "Modules"
            modulesFragment = ModulesFragment()
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
            supportActionBar?.title = "Settings"
            settingsFragment = SettingsFragment()
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
            supportActionBar?.title = "Sleep-Reminder"
            sleepFragment = SleepFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, sleepFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="sleep"
        }
    }

    private fun changeToNotes(){
        if(activeFragmentTag!="notes") {
            supportActionBar?.title = "Notes"
            noteFragment= NoteFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, noteFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="notes"
            bottomNavigation.selectedItemId=R.id.notes
        }
    }

    private fun setBirthdayAlarms() {
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, AlarmManager.INTERVAL_DAY, AlarmManager.INTERVAL_DAY, pendingIntent)
    }

}

