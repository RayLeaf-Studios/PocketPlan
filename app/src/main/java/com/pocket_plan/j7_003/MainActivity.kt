package com.pocket_plan.j7_003

import SettingsNavigationFr
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayAdapter
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.notelist.*
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsMainFr
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.settings.sub_categories.SettingsNotesFr
import com.pocket_plan.j7_003.data.settings.sub_categories.SettingsAboutFr
import com.pocket_plan.j7_003.data.settings.sub_categories.SettingsAppearanceFr
import com.pocket_plan.j7_003.data.settings.sub_categories.SettingsBackupFr
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.CustomItemFr
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.SettingsShoppingFr
import com.pocket_plan.j7_003.data.shoppinglist.*
import com.pocket_plan.j7_003.data.sleepreminder.SleepAdapter
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.sleepreminder.SleepReminder
import com.pocket_plan.j7_003.data.todolist.TodoFr
import com.pocket_plan.j7_003.data.todolist.TodoList
import com.pocket_plan.j7_003.data.todolist.TodoTaskAdapter
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import kotlinx.android.synthetic.main.dialog_delete_note.view.*
import kotlinx.android.synthetic.main.header_navigation_drawer.view.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.new_app_bar.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private lateinit var birthdayFr: BirthdayFr

    companion object {
        //contents for shopping list
        lateinit var tagList: TagList
        lateinit var itemTemplateList: ItemTemplateList
        lateinit var userItemTemplateList: UserItemTemplateList
        lateinit var itemNameList: ArrayList<String>

        var addItemDialog: AlertDialog? = null
        var addItemDialogView: View? = null

        lateinit var tempShoppingFr: ShoppingFr
        var justRestarted = false

        lateinit var noteEditorFr: NoteEditorFr
//        var previousFragmentTag: FT = FT.EMPTY
        val previousFragmentStack: Stack<FT> = Stack()
        var activeFragmentTag: FT = FT.EMPTY
        lateinit var act: MainActivity
        lateinit var toolBar: Toolbar
        var editNoteHolder: Note? = null
        var fromHome: Boolean = false
        lateinit var bottomNavigation: BottomNavigationView
//        V.2
//        var editTerm: CalendarAppointment? = null
    }


    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        act = this

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        //TODO REMOVE THIS AND IMPLEMENT PROPERLY LOADING SHOPPING LIST
//        Toast.makeText(act,Locale.getDefault().language, Toast.LENGTH_SHORT).show()

        //Set a reference to this activity so its accessible in the companion object

        //IMPORTANT; ORDER IS CRITICAL
        //Initialize Settings Manager and Time api and AlarmHandler
        SettingsManager.init()
        AndroidThreeTen.init(this)
        AlarmHandler.setBirthdayAlarms(context = this)

        SleepReminder.context = this
        SleepFr.sleepReminderInstance = SleepReminder()

        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        tempShoppingFr = ShoppingFr()

        //initialize toolbar
        setSupportActionBar(myNewToolbar)
        toolBar = myNewToolbar
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu)

        TodoFr.todoListInstance = TodoList()
        TodoFr.myAdapter = TodoTaskAdapter()
        NoteFr.myAdapter = NoteAdapter()
        ShoppingFr.shoppingListAdapter = ShoppingListAdapter()
        SleepFr.myAdapter = SleepAdapter()
        BirthdayFr.myAdapter = BirthdayAdapter()

        val header = nav_drawer.inflateHeaderView(R.layout.header_navigation_drawer)
        val mySpinner = header.ivSpinner
        mySpinner.setOnClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(act, R.anim.icon_easter_egg)
            mySpinner.startAnimation(animationShake)
        }


        //initialize drawer toggle button
        mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        //initialize navigation drawer
        nav_drawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToFragment(FT.SETTINGS)
                R.id.menuSleepReminder -> changeToFragment(FT.SLEEP)
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }

        drawer_layout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                hideKeyboard()
            }

            override fun onDrawerOpened(drawerView: View) {
            }

            override fun onDrawerClosed(drawerView: View) {
            }

            override fun onDrawerStateChanged(newState: Int) {
            }

        })

        //initialize bottomNavigation
        bottomNavigation = findViewById(R.id.btm_nav)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.bottom1 -> changeToFragment(FT.NOTES)
                R.id.bottom2 -> changeToFragment(FT.TASKS)
                R.id.bottom3 -> changeToFragment(FT.HOME)
                R.id.bottom4 -> changeToFragment(FT.SHOPPING)
                R.id.bottom5 -> changeToFragment(FT.BIRTHDAYS)
            }
            true
        }

        //removes longClick tooltips for bottom navigation
        for(i in 0 until bottomNavigation.menu.size()){
            val view = bottomNavigation.findViewById<View>(bottomNavigation.menu.getItem(i).itemId)
            view.setOnLongClickListener {
                true
            }
        }

        //initialize btn to add elements, depending on which fragment is active
        btnAdd.setOnClickListener {
            when (activeFragmentTag) {
                FT.BIRTHDAYS -> {
                    BirthdayFr.editBirthdayHolder = null
                    birthdayFr.openAddBirthdayDialog()
                }

                FT.TASKS -> {
                    TodoFr.myFragment.dialogAddTask()
                }

                FT.NOTES -> {
                    editNoteHolder = null
                    NoteEditorFr.noteColor = NoteColors.GREEN
                    changeToFragment(FT.NOTE_EDITOR)
                }

                FT.SHOPPING -> {
                    tempShoppingFr.openAddItemDialog()
                }

                else -> {/* no-op */
                }
            }
        }
        when (intent.extras?.get("NotificationEntry").toString()) {
            "birthdays" -> changeToFragment(FT.BIRTHDAYS)
            "SReminder" -> changeToFragment(FT.HOME)
            "settings" -> changeToFragment(FT.SETTINGS)
            else -> {
                justRestarted = true
                bottomNavigation.menu.getItem(2).isChecked = true
                changeToFragment(FT.HOME)
            }
        }

        tempShoppingFr.preloadAddItemDialog(layoutInflater)


    }

    /**
     * DEBUG FUNCTIONS
     */

    fun toast(msg: String) {
        Toast.makeText(act, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * UI FUNCTIONS
     */
    fun hideKeyboard() {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = act.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(act)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun setNavBarUnchecked() {
        bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigation.menu.size()) {
            bottomNavigation.menu.getItem(i).isChecked = false
        }
        bottomNavigation.menu.setGroupCheckable(0, true, true)
    }


    //change to fragment of specified tag
    fun changeToFragment(fragmentTag: FT) {

        if (!justRestarted) {
            if (activeFragmentTag == fragmentTag) {
                return
            }
        }
        justRestarted = false
        //Check if the currently requested fragment change comes from note editor, if yes
        //check if there are relevant changes to the note, if yes, open the "Keep changes?"
        //dialog and return
        if (activeFragmentTag == FT.NOTE_EDITOR) {
            if (NoteEditorFr.myFragment.relevantNoteChanges()) {
                NoteEditorFr.myFragment.dialogDiscardNoteChanges(fragmentTag)
                return
            }
        }

        //display add button where it is needed
        btnAdd.visibility = when (fragmentTag) {
            FT.TASKS,
            FT.SHOPPING,
            FT.NOTES,
            FT.BIRTHDAYS -> View.VISIBLE
            else -> View.INVISIBLE
        }

        //Set the correct ActionbarTitle
        myNewToolbar.title = when (fragmentTag) {
            FT.HOME -> resources.getText(R.string.menuTitleHome)
            FT.TASKS -> resources.getText(R.string.menuTitleTasks)
            FT.SETTINGS_ABOUT -> resources.getText(R.string.menuTitleAbout)
            FT.SHOPPING, FT.SETTINGS_SHOPPING -> resources.getText(R.string.menuTitleShopping)
            FT.NOTES, FT.SETTINGS_NOTES -> resources.getText(R.string.menuTitleNotes)
            FT.SETTINGS_NAVIGATION -> resources.getText(R.string.navigationDrawer)
            FT.SETTINGS -> resources.getText(R.string.menuTitleSettings)
            FT.NOTE_EDITOR -> resources.getText(R.string.menuTitleNotesEditor)
            FT.BIRTHDAYS -> resources.getText(R.string.menuTitleBirthdays)
            FT.CUSTOM_ITEMS -> resources.getText(R.string.menuTitleCustomItem)
            FT.SLEEP -> resources.getText(R.string.menuTitleSleep)
            FT.CALENDAR -> resources.getText(R.string.menuTitleCalendar)
            FT.CREATE_TERM -> resources.getText(R.string.menuTitleCreateTerm)
            FT.DAY_VIEW -> resources.getText(R.string.menuTitleDayView)
            FT.SETTINGS_BACKUP -> resources.getText(R.string.backup)
            else -> ""
        }

        //check the correct item in the bottomNavigation
        val checkedBottomNav = when(fragmentTag){
            FT.NOTES -> 0
            FT.TASKS -> 1
            FT.HOME -> 2
            FT.SHOPPING -> 3
            FT.BIRTHDAYS -> 4
            else -> 5
        }

        when(checkedBottomNav){
            5 -> setNavBarUnchecked()
            else -> {
                setNavBarUnchecked()
                bottomNavigation.menu.getItem(checkedBottomNav).isChecked = true
            }
        }

        //save the current activeFragmentTag as previousFragmentTag
//        previousFragmentTag = activeFragmentTag
        previousFragmentStack.push(activeFragmentTag)

        //set the new activeFragment tag
        activeFragmentTag = fragmentTag

        //create fragment object
        val fragment = when (fragmentTag) {
            FT.HOME -> HomeFr()
            FT.TASKS -> TodoFr()
            FT.SHOPPING -> ShoppingFr()
            FT.NOTES -> {
                NoteFr.searching = false
                NoteFr()
            }
            FT.NOTE_EDITOR -> {
                noteEditorFr = NoteEditorFr()
                noteEditorFr
            }
            FT.BIRTHDAYS -> {
                BirthdayFr.searching = false
                birthdayFr = BirthdayFr()
                birthdayFr
            }
            FT.SETTINGS_ABOUT -> SettingsAboutFr()
            FT.SETTINGS_NAVIGATION -> SettingsNavigationFr()
            FT.SETTINGS_NOTES -> SettingsNotesFr()
            FT.SETTINGS_SHOPPING -> SettingsShoppingFr()
            FT.SETTINGS_BACKUP -> SettingsBackupFr()
            FT.SETTINGS_APPEARANCE -> SettingsAppearanceFr()
            FT.SETTINGS -> SettingsMainFr()
            FT.CUSTOM_ITEMS -> CustomItemFr()
            FT.SLEEP -> SleepFr()
//            V.2
//            FT.CALENDAR -> CalenderFr()
//            FT.CREATE_TERM -> CreateTermFr()
//            FT.DAY_VIEW -> DayFr()
            else -> HomeFr()
        }

        //animate fragment change
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()

        when (fragmentTag) {
            FT.BIRTHDAYS -> BirthdayFr.myAdapter.notifyDataSetChanged()
            FT.NOTES -> NoteFr.myAdapter.notifyDataSetChanged()
            FT.SHOPPING -> ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
            FT.TASKS -> TodoFr.myAdapter.notifyDataSetChanged()
            FT.SLEEP -> SleepFr.myAdapter.notifyDataSetChanged()
            else -> { /* no-op */ }
        }
    }

    /**
     * OVERRIDE FUNCTIONS
     */

    override fun onBackPressed() {
        //close drawer when its open
        if (drawer_layout.isDrawerOpen(nav_drawer)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        }

        //When in birthdayFragment and searching, close search and restore fragment to normal mode
        if(activeFragmentTag==FT.BIRTHDAYS && BirthdayFr.searching){
            toolBar.title = getString(R.string.menuTitleBirthdays)
            BirthdayFr.searchView.onActionViewCollapsed()
            BirthdayFr.searching = false
            BirthdayFr.myFragment.updateUndoBirthdayIcon()
            BirthdayFr.myAdapter.notifyDataSetChanged()
            return
        }

        //When in noteFragment and searching, close search and restore fragment to normal mode
        if(activeFragmentTag==FT.NOTES && NoteFr.searching){
            toolBar.title = getString(R.string.menuTitleNotes)
            NoteFr.searchView.onActionViewCollapsed()
            NoteFr.searching = false
            NoteFr.myAdapter.notifyDataSetChanged()
            return
        }

        //handles going back from editor
        if (activeFragmentTag == FT.NOTE_EDITOR) {
            if (NoteEditorFr.myFragment.relevantNoteChanges()) {
                NoteEditorFr.myFragment.dialogDiscardNoteChanges(previousFragmentStack.pop())
                return
            }
        }

        //handles going back from sub settings to settings
        when (activeFragmentTag) {
            FT.SETTINGS_SHOPPING, FT.SETTINGS_BACKUP, FT.SETTINGS_ABOUT,
            FT.SETTINGS_NOTES, FT.SETTINGS_NAVIGATION -> {
                changeToFragment(FT.SETTINGS)
                return
            }
            FT.CUSTOM_ITEMS -> {
                changeToFragment(FT.SETTINGS_SHOPPING)
                return
            }
            FT.SETTINGS -> {
                changeToFragment(FT.HOME)
                return
            }
            FT.HOME -> {
                super.onBackPressed()
                return
            }
            else -> {
                /* no-op */
            }
        }


//        when (previousFragmentTag) {
//            FT.HOME -> changeToFragment(FT.HOME)
//            FT.NOTES -> changeToFragment(FT.NOTES)
//            FT.SHOPPING -> changeToFragment(FT.SHOPPING)
//            FT.SETTINGS -> changeToFragment(FT.SETTINGS)
//            FT.TASKS -> changeToFragment(FT.TASKS)
//            FT.SLEEP -> changeToFragment(FT.SLEEP)
//            else -> super.onBackPressed()
//        }

        changeToFragment(previousFragmentStack.pop())
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun refreshData(){
        NoteFr.noteListInstance = NoteList()
        BirthdayFr.birthdayListInstance = BirthdayList(act)
        ShoppingFr.shoppingListInstance = ShoppingList()
        SettingsManager.init()
        SleepFr.sleepReminderInstance = SleepReminder()
        TodoFr.todoListInstance = TodoList()
    }


    @SuppressLint("InflateParams")

    private fun loadDefaultSettings() {
        setDefault(SettingId.NOTE_COLUMNS, "2")
        setDefault(SettingId.NOTE_LINES, 10.0)
        setDefault(SettingId.FONT_SIZE, "18")
        setDefault(SettingId.CLOSE_ITEM_DIALOG, false)
        setDefault(SettingId.EXPAND_ONE_CATEGORY, false)
        setDefault(SettingId.COLLAPSE_CHECKED_SUBLISTS, false)
        setDefault(SettingId.MOVE_CHECKED_DOWN, true)
    }

    private fun setDefault(setting: SettingId, value: Any) {
        if (SettingsManager.getSetting(setting) == null) {
            SettingsManager.addSetting(setting, value)
        }
    }

    /**
     * Opens a dialog, asking the user to confirm a deletion by swiping a seekBar and then
     * pressing a button. The action to be executed when the button is pressed can be passed as a lambda.
     * @param titleId Resource id pointing to the String that will be displayed as dialog title
     * @param action Lambda that will be executed when btnDelete is pressed
     */

    @SuppressLint("InflateParams")
    fun dialogConfirmDelete(titleId: Int, action: () -> Unit) {
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete_note, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(act).setView(myDialogView)
        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = getString(titleId)
        myBuilder.setCustomTitle(customTitle)
        val myAlertDialog = myBuilder.create()

        val btnCancelDelete = myDialogView.btnCancelDelete
        val btnDelete = myDialogView.btnDelete
        val sbDelete = myDialogView.sbDelete

        var allowDelete = false

        //allow deletion and set color to delete button if seekBar is at 100%, remove color and
        //disallow deletion otherwise
        sbDelete.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                /* no-op */
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                /* no-op */
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    allowDelete = true
                    btnDelete.setBackgroundResource(R.drawable.round_corner_red)
                    btnDelete.setTextColor(
                        ContextCompat.getColor(
                            act,
                            R.color.colorOnBackGround
                        )
                    )
                } else {
                    if (allowDelete) {
                        allowDelete = false
                        btnDelete.setBackgroundResource(R.drawable.round_corner_gray)
                        btnDelete.setTextColor(
                            ContextCompat.getColor(
                                act,
                                R.color.colorHint
                            )
                        )
                    }

                }

            }
        })

        //Shake animate seekBar if its not at 100%, execute delete action and dismiss dialog otherwise
        btnDelete.setOnClickListener {
            if (!allowDelete) {
                val animationShake =
                    AnimationUtils.loadAnimation(act, R.anim.shake)
                sbDelete.startAnimation(animationShake)
                return@setOnClickListener
            }
            action()
            myAlertDialog.dismiss()
        }

        //hide dialog when "Cancel" is pressed
        btnCancelDelete.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }


}

