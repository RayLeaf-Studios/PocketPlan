package com.example.j7_003

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.Database
import com.example.j7_003.data.database_objects.Birthday
import com.example.j7_003.fragments.*
import com.example.j7_003.notifications.NotificationHandler
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity(){
    private lateinit var homeFragment: HomeFragment
    private lateinit var calenderFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var todoFragment: TodoFragment

    companion object {
        lateinit var myActivity: MainActivity
        lateinit var notificationHandler: NotificationHandler
        lateinit var database: Database
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        database = Database(this)
        notificationHandler = NotificationHandler(this, getSystemService(Context.NOTIFICATION_SERVICE))
        myActivity = this
        supportActionBar?.title = "Home"

        val bottomNavigation : BottomNavigationView = findViewById(R.id.btm_nav)

        var activeFragment = 0

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.home -> {
                    if(activeFragment!=0){
                        supportActionBar?.title = "Home"
                        homeFragment = HomeFragment()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, homeFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                        activeFragment=0
                    }
                }
                R.id.settings -> {
                    if(activeFragment!=1) {
                        supportActionBar?.title = "Settings"
                        settingsFragment = SettingsFragment()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, settingsFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                        activeFragment=1
                    }
                }
                R.id.todolist -> {
                    if(activeFragment!=2) {
                        supportActionBar?.title = "To-Do"
                        todoFragment = TodoFragment()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, todoFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                        activeFragment=2
                    }
                }
                R.id.birthdays -> {
                    if(activeFragment!=3) {
                        supportActionBar?.title = "Birthdays"
                        birthdayFragment = BirthdayFragment()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, birthdayFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                        activeFragment=3
                    }
                }
                R.id.calendar -> {
                    if(activeFragment!=4) {
                        supportActionBar?.title = "Calendar"
                        calenderFragment = CalenderFragment()
                        supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.frame_layout, calenderFragment)
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit()
                        activeFragment=4
                    }
                }
            }
            true
        }

        /**
         * initializes homeFragment as first fragment
         */

        bottomNavigation.selectedItemId = R.id.home

        homeFragment = HomeFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

}


