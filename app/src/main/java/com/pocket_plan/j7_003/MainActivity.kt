package com.pocket_plan.j7_003
import android.annotation.SuppressLint
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
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.header_navigation_drawer.view.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.toolbar.*
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
        val previousFragmentStack: Stack<FT> = Stack()
        lateinit var act: MainActivity
        lateinit var toolBar: Toolbar
        var editNoteHolder: Note? = null
        lateinit var bottomNavigation: BottomNavigationView
        //V.2
        //var editTerm: CalendarAppointment? = null
    }


    override fun onRestart() {
        if (previousFragmentStack.isEmpty() || previousFragmentStack.peek() == FT.EMPTY) {
            previousFragmentStack.clear()
            previousFragmentStack.push(FT.EMPTY)
        }
        super.onRestart()
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        //Set a reference to this activity in the companion object
        act = this

        if (previousFragmentStack.isEmpty()) {
            previousFragmentStack.push(FT.EMPTY)
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        //IMPORTANT; ORDER IS CRITICAL
        //Initialize Settings Manager and Time api and AlarmHandler
        SettingsManager.init()
        AndroidThreeTen.init(this)
        AlarmHandler.setBirthdayAlarms(context = this)

        //Initialize Sleepreminder
        SleepReminder.context = this
        SleepFr.sleepReminderInstance = SleepReminder()

        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        //Initialize temporary ShoppingFr Instance
        tempShoppingFr = ShoppingFr()

        //initialize toolbar
        setSupportActionBar(myNewToolbar)
        toolBar = myNewToolbar
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu)

        //Initialize adapters and necessary list instances
        TodoFr.todoListInstance = TodoList()
        TodoFr.myAdapter = TodoTaskAdapter()
        NoteFr.myAdapter = NoteAdapter()
        ShoppingFr.myAdapter = ShoppingListAdapter()
        SleepFr.myAdapter = SleepAdapter()
        BirthdayFr.myAdapter = BirthdayAdapter()

        //Initialize header and icon in side drawer
        val header = nav_drawer.inflateHeaderView(R.layout.header_navigation_drawer)
        val mySpinner = header.ivSpinner
        mySpinner.setOnClickListener {
            val animationSpin =
                AnimationUtils.loadAnimation(act, R.anim.icon_easter_egg)
            mySpinner.startAnimation(animationSpin)
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
        val navList = arrayListOf(FT.NOTES, FT.TASKS, FT.HOME, FT.SHOPPING, FT.BIRTHDAYS)
        bottomNavigation = findViewById(R.id.btm_nav)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            if (!navList.contains(previousFragmentStack.peek()) || bottomNavigation.selectedItemId != item.itemId) {
                when (item.itemId) {
                    R.id.bottom1 -> changeToFragment(FT.NOTES)
                    R.id.bottom2 -> changeToFragment(FT.TASKS)
                    R.id.bottom3 -> changeToFragment(FT.HOME)
                    R.id.bottom4 -> changeToFragment(FT.SHOPPING)
                    R.id.bottom5 -> changeToFragment(FT.BIRTHDAYS)
                }
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
            when (previousFragmentStack.peek()) {
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

        //When activity is entered via special intent, change to respective fragment
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

        //preload add item dialog to reduce loading time
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
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
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


    /**
     * Changes to the fragment specified by
     * @param fragmentTag
     * initializes fragment instances, sets correct actionbarTitle, selects correct
     * item in bottom navigation
     */

    fun changeToFragment(fragmentTag: FT) {
        //Check if the currently requested fragment change comes from note editor, if yes
        //check if there are relevant changes to the note, if yes, open the "Keep changes?"
        //dialog and return
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            if (NoteEditorFr.myFragment.relevantNoteChanges()) {
                NoteEditorFr.myFragment.dialogDiscardNoteChanges()
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
            FT.SETTINGS -> resources.getText(R.string.menuTitleSettings)
            FT.NOTE_EDITOR -> resources.getText(R.string.menuTitleNotesEditor)
            FT.BIRTHDAYS -> resources.getText(R.string.menuTitleBirthdays)
            FT.CUSTOM_ITEMS -> resources.getText(R.string.menuTitleCustomItem)
            FT.SLEEP -> resources.getText(R.string.menuTitleSleep)
            FT.CALENDAR -> resources.getText(R.string.menuTitleCalendar)
            FT.CREATE_TERM -> resources.getText(R.string.menuTitleCreateTerm)
            FT.DAY_VIEW -> resources.getText(R.string.menuTitleDayView)
            FT.SETTINGS_BACKUP -> resources.getText(R.string.backup)
            FT.SETTINGS_APPEARANCE -> resources.getText(R.string.settings_title_appearance)
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

        if (previousFragmentStack.peek() != fragmentTag) {
            previousFragmentStack.push(fragmentTag)
        }

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
            FT.SETTINGS_NOTES -> SettingsNotesFr()
            FT.SETTINGS_SHOPPING -> SettingsShoppingFr()
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

        //refresh recycler adapters
        when (fragmentTag) {
            FT.BIRTHDAYS -> BirthdayFr.myAdapter.notifyDataSetChanged()
            FT.NOTES -> NoteFr.myAdapter.notifyDataSetChanged()
            FT.SHOPPING -> ShoppingFr.myAdapter.notifyDataSetChanged()
            FT.SLEEP -> SleepFr.myAdapter.notifyDataSetChanged()
            FT.TASKS -> TodoFr.myAdapter.notifyDataSetChanged()
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
        if (previousFragmentStack.peek() == FT.BIRTHDAYS && BirthdayFr.searching) {
            toolBar.title = getString(R.string.menuTitleBirthdays)
            BirthdayFr.searchView.onActionViewCollapsed()
            BirthdayFr.searching = false
            BirthdayFr.myFragment.updateUndoBirthdayIcon()
            BirthdayFr.myAdapter.notifyDataSetChanged()
            return
        }

        //When in noteFragment and searching, close search and restore fragment to normal mode
        if (previousFragmentStack.peek() == FT.NOTES && NoteFr.searching) {
            toolBar.title = getString(R.string.menuTitleNotes)
            NoteFr.searchView.onActionViewCollapsed()
            NoteFr.searching = false
            NoteFr.myAdapter.notifyDataSetChanged()
            return
        }

        //handles going back from editor
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            if (NoteEditorFr.myFragment.relevantNoteChanges()) {
                NoteEditorFr.myFragment.dialogDiscardNoteChanges()
                return
            }
        }

        previousFragmentStack.pop()
        if (previousFragmentStack.peek() != FT.EMPTY) {
            changeToFragment(previousFragmentStack.peek())
        } else super.onBackPressed()
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

    fun resetSettings(){
        val action: () -> Unit = {
            SettingsManager.settings.clear()
            loadDefaultSettings()
            SettingsAppearanceFr.myFr.initializeDisplayValues()
        }
        dialogConfirmDelete(R.string.titleRestoreSettings, action)
    }

    private fun loadDefaultSettings() {
        setDefault(SettingId.NOTE_COLUMNS, "2")
        setDefault(SettingId.NOTE_LINES, 10.0)
        setDefault(SettingId.FONT_SIZE, "18")
        setDefault(SettingId.CLOSE_ITEM_DIALOG, false)
        setDefault(SettingId.EXPAND_ONE_CATEGORY, false)
        setDefault(SettingId.COLLAPSE_CHECKED_SUBLISTS, false)
        setDefault(SettingId.MOVE_CHECKED_DOWN, true)
        setDefault(SettingId.SHAPES_ROUND, false)
        setDefault(SettingId.SAFETY_SLIDER_DIALOG, true)
        setDefault(SettingId.SHAKE_TASK_HOME, true)
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
        val safetySlider = SettingsManager.getSetting(SettingId.SAFETY_SLIDER_DIALOG) as Boolean

        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(act).setView(myDialogView)
        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = getString(titleId)
        myBuilder.setCustomTitle(customTitle)
        val myAlertDialog = myBuilder.create()

        val btnCancelDelete = myDialogView.btnCancelDelete
        val btnDelete = myDialogView.btnDelete
        val sbDelete = myDialogView.sbDelete

        var allowDelete: Boolean
        if(safetySlider){
            allowDelete = false
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

            myDialogView.sbDelete.visibility = View.VISIBLE
            myDialogView.tvSwipeToDelete.visibility = View.VISIBLE
        }
        else{
            allowDelete = true
            myDialogView.sbDelete.visibility = View.GONE
            myDialogView.tvSwipeToDelete.visibility = View.GONE
            btnDelete.setBackgroundResource(R.drawable.round_corner_red)
            btnDelete.setTextColor(
                ContextCompat.getColor(
                    act,
                    R.color.colorOnBackGround
                )
            )
        }

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

