package com.example.j7_003

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.database_objects.Birthday
import com.example.j7_003.fragments.*
import com.example.j7_003.notifications.NotificationHandler
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_panel.*

@SuppressLint("Registered")
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var homeFragment: HomeFragment
    lateinit var calenderFragment: CalenderFragment
    lateinit var birthdayFragment: BirthdayFragment
    lateinit var settingsFragment: SettingsFragment
    lateinit var todoFragment: TodoFragment
    lateinit var notificationHandler: NotificationHandler

    @SuppressLint("InflateParams", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        setSupportActionBar(toolBar)
        val actionBar = supportActionBar
        actionBar?.title = "Home"

        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(this, drawerLayout, toolBar, (R.string.open), (R.string.close)){}

        notificationHandler =
            NotificationHandler(
                this,
                getSystemService(Context.NOTIFICATION_SERVICE)
            )

        drawerToggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        homeFragment = HomeFragment()
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, homeFragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when(menuItem.itemId){
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
                settingsFragment =
                    SettingsFragment()
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
                notificationHandler.notifyUpcomingBirthday(
                    Birthday(
                        "Eugen",
                        12,
                        24
                    )
                )
                supportActionBar?.title = "Birthdays"
                Toast.makeText(this, "i got called", Toast.LENGTH_LONG)
                birthdayFragment =
                    BirthdayFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, birthdayFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
            R.id.calendar -> {
                supportActionBar?.title = "Calendar"
                calenderFragment =
                    CalenderFragment()
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.frame_layout, calenderFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else {
            super.onBackPressed()
        }
    }
}


