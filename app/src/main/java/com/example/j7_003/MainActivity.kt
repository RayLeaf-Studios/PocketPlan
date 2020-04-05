package com.example.j7_003

import android.content.Context
import android.os.Bundle
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
    private lateinit var notificationHandler: NotificationHandler
    lateinit var database: Database

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        database = Database(this)

        val bottomNavigation : BottomNavigationView = findViewById(R.id.btm_nav)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.home -> {
                    supportActionBar?.title = "Home"
                    homeFragment = HomeFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, homeFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.settings -> {
                    supportActionBar?.title = "Settings"
                    settingsFragment = SettingsFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, settingsFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.todolist -> {
                    notificationHandler.createDebugNotification()
                    supportActionBar?.title = "ToDo List"
                    todoFragment = TodoFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, todoFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.birthdays -> {
                    //debug >
                    notificationHandler.notifyUpcomingBirthday(
                        Birthday(
                            "Eugen",
                            12,
                            24
                        )
                    )
                    //debug <
                    supportActionBar?.title = "Birthdays"
                    birthdayFragment = BirthdayFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, birthdayFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
                R.id.calendar -> {
                    supportActionBar?.title="Calendar"
                    calenderFragment = CalenderFragment()
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.frame_layout, calenderFragment)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit()
                }
            }
            true
        }

        notificationHandler =
            NotificationHandler(
                this,
                getSystemService(Context.NOTIFICATION_SERVICE)
            )

        /**
         * initializes homeFragment as first fragment
         */

        homeFragment = HomeFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

}


