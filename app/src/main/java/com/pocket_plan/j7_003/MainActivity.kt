package com.pocket_plan.j7_003

import android.annotation.SuppressLint
import android.app.Activity
import android.content.res.Configuration
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.content.res.Resources
import android.os.Bundle
import android.util.TypedValue
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.notelist.*
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsMainFr
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.settings.sub_categories.*
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.CustomItemFr
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.SettingsShoppingFr
import com.pocket_plan.j7_003.data.shoppinglist.*
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.todolist.TodoFr
import com.pocket_plan.j7_003.data.todolist.TodoList
import com.pocket_plan.j7_003.data.todolist.TodoTaskAdapter
import com.pocket_plan.j7_003.system_interaction.Logger
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import kotlinx.android.synthetic.main.dialog_delete.view.*
import kotlinx.android.synthetic.main.header_navigation_drawer.view.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private var birthdayFr: BirthdayFr? = null
    private var homeFr: HomeFr? = null
    lateinit var multiShoppingFr: MultiShoppingFr
    private var noteEditorFr: NoteEditorFr? = null
    private var noteFr: NoteFr? = null
    private var sleepFr: SleepFr? = null

    var shoppingTitle: View? = null
    lateinit var toolBar: Toolbar

    lateinit var itemTemplateList: ItemTemplateList
    lateinit var userItemTemplateList: UserItemTemplateList

    lateinit var myBtnAdd: FloatingActionButton

    companion object {
        //contents for shopping list
        lateinit var itemNameList: ArrayList<String>

        val previousFragmentStack: Stack<FT> = Stack()
        lateinit var bottomNavigation: BottomNavigationView
    }

    private fun setLocale(activity: Activity, languageCode: String?) {
        val locale = Locale(languageCode!!)
        Locale.setDefault(locale)
        val resources: Resources = activity.resources
        val config: Configuration = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }

    override fun onRestart() {
        if (previousFragmentStack.isEmpty() || previousFragmentStack.peek() == FT.EMPTY) {
            previousFragmentStack.clear()
            previousFragmentStack.push(FT.EMPTY)
        }
        super.onRestart()
    }


    fun getFragment(tag: FT): Fragment? = when (tag) {
        FT.BIRTHDAYS -> birthdayFr as Fragment
        FT.SLEEP -> sleepFr as Fragment
        FT.SHOPPING -> multiShoppingFr
        FT.NOTES -> noteFr as Fragment
        FT.NOTE_EDITOR -> noteEditorFr as Fragment
        else -> null
    }

    fun colorForAttr(
        attrColor: Int,
        typedValue: TypedValue = TypedValue(),
        resolveRefs: Boolean = true
    ): Int {
        theme.resolveAttribute(attrColor, typedValue, resolveRefs)
        return typedValue.data
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {

        previousFragmentStack.clear()
        previousFragmentStack.push(FT.EMPTY)

        StorageHandler.path = this.filesDir.absolutePath

        SettingsManager.init()

        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        //set correct language depending on setting
        val languageCode = when (SettingsManager.getSetting(SettingId.LANGUAGE)) {
            0.0 -> "en"
            //1.0 = de
            else -> "de"
        }
        setLocale(this, languageCode)

        //check if settings say to use system theme, if yes, set theme setting to system theme
        if (SettingsManager.getSetting(SettingId.USE_SYSTEM_THEME) as Boolean) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == UI_MODE_NIGHT_YES) {
                true -> SettingsManager.addSetting(SettingId.THEME_DARK, true)
                else -> SettingsManager.addSetting(SettingId.THEME_DARK, false)
            }
        }

        //set correct theme depending on setting
        val themeToSet = when (SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean) {
            true -> R.style.AppThemeDark
            else -> R.style.AppThemeLight
        }
        setTheme(themeToSet)

        //create main_panel
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        //IMPORTANT: ORDER IS CRITICAL HERE
        //Initialize Time api and AlarmHandler
        AndroidThreeTen.init(this)
        val time = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
        AlarmHandler.setBirthdayAlarms(time, context = this)

        //Initialize toolbar
        setSupportActionBar(myNewToolbar)
        toolBar = myNewToolbar
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu)

        //Initialize adapters and necessary list instances
        TodoFr.todoListInstance = TodoList()
        TodoFr.myAdapter = TodoTaskAdapter(this)



        //Initialize fragment classes necessary for home
        sleepFr = SleepFr()
        birthdayFr = BirthdayFr()
        homeFr = HomeFr()


        //Initialize header and icon in side drawer
        val header = nav_drawer.inflateHeaderView(R.layout.header_navigation_drawer)

        // deletion of log file
        Logger(this).deleteFile()

        //display current versionName
        val versionString = "v " + packageManager.getPackageInfo(packageName, 0).versionName
        header.tvVersion.text = versionString

        //spinning app Icon
        val myLogo = header.ivLogo
        var allowSpin = true
        myLogo.setOnClickListener {
            if (!allowSpin) {
                return@setOnClickListener
            }
            allowSpin = false
            val animationSpin =
                AnimationUtils.loadAnimation(this, R.anim.icon_easter_egg)

            animationSpin.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation?) {
                    /* no-op */
                }

                override fun onAnimationEnd(animation: Animation?) {
                    allowSpin = true
                }

                override fun onAnimationRepeat(animation: Animation?) {
                    /* no-op */
                }

            })
            myLogo.startAnimation(animationSpin)
        }

        //initialize drawer toggle button
        mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        //initialize bottom navigation
        bottomNavigation = findViewById(R.id.btm_nav)

        //preload add item dialog to reduce loading time
        multiShoppingFr = MultiShoppingFr()
        multiShoppingFr.shoppingListWrapper = ShoppingListWrapper(getString(R.string.menuTitleShopping))

        //When activity is entered via special intent, change to respective fragment
        when (intent.extras?.get("NotificationEntry").toString()) {
            "birthdays" -> changeToFragment(FT.BIRTHDAYS)
            "SReminder" -> changeToFragment(FT.HOME)
            "settings" -> changeToFragment(FT.SETTINGS)
            "appearance" -> changeToFragment(FT.SETTINGS_APPEARANCE)
            else -> {
                if (previousFragmentStack.peek() == FT.EMPTY) {
                    changeToFragment(FT.HOME)
                } else {
                    changeToFragment(previousFragmentStack.pop())
                }
            }
        }

        multiShoppingFr.preloadAddItemDialog(this, layoutInflater)

        //Initialize remaining fragments
        noteFr = NoteFr()
        NoteFr.myAdapter = NoteAdapter(this, noteFr!!)

        //initialize navigation drawer listener
        nav_drawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToFragment(FT.SETTINGS)
                R.id.menuSleepReminder -> changeToFragment(FT.SLEEP)
                R.id.menuHelp -> changeToFragment(FT.SETTINGS_HOWTO)
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

        this.myBtnAdd = btnAdd

        //initialize btn to add elements, depending on which fragment is active
        btnAdd.setOnClickListener {
            when (previousFragmentStack.peek()) {
                FT.BIRTHDAYS -> {
                    BirthdayFr.editBirthdayHolder = null
                    birthdayFr!!.openAddBirthdayDialog()
                }

                FT.TASKS -> {
                    TodoFr.myFragment.dialogAddTask()
                }

                FT.NOTES -> {
                    PreferenceManager.getDefaultSharedPreferences(this).edit()
                        .putBoolean("editingNote", false).apply()
                    NoteEditorFr.noteColor = NoteColors.GREEN
                    changeToFragment(FT.NOTE_EDITOR)
                }

                FT.SHOPPING -> {
                    multiShoppingFr.editing = false
                    multiShoppingFr.openAddItemDialog()
                }

                else -> {/* no-op */
                }
            }
        }

        //removes longClick tooltips for bottom navigation
        for (i in 0 until bottomNavigation.menu.size()) {
            val view = bottomNavigation.findViewById<View>(bottomNavigation.menu.getItem(i).itemId)
            view.setOnLongClickListener {
                true
            }
        }
    }

    /**
     * DEBUG FUNCTIONS
     */

    fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    /**
     * UI FUNCTIONS
     */

    fun hideKeyboard() {
        val imm = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view: View? = this.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
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

    fun changeToFragment(fragmentTag: FT): Fragment? {
        //Check if the currently requested fragment change comes from note editor, if yes
        //check if there are relevant changes to the note, if yes, open the "Keep changes?"
        //dialog and return
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            noteEditorFr =
                supportFragmentManager.findFragmentByTag(FT.NOTE_EDITOR.name) as NoteEditorFr
            if (noteEditorFr!!.relevantNoteChanges()) {
                noteEditorFr!!.dialogDiscardNoteChanges(fragmentTag)
                return null
            } else {
                previousFragmentStack.pop()
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

        //Set correct soft input mode
        this.window.setSoftInputMode(
            when (fragmentTag) {
                FT.NOTE_EDITOR -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                else -> WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
            }
        )

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
            FT.SETTINGS_BACKUP -> resources.getText(R.string.backup)
            FT.SETTINGS_APPEARANCE -> resources.getText(R.string.settings_title_appearance)
            FT.SETTINGS_HOWTO -> resources.getText(R.string.settingsHelp)
            FT.SETTINGS_BIRTHDAYS -> resources.getText(R.string.menuTitleBirthdays)
            else -> ""
        }

        //check the correct item in the bottomNavigation
        val checkedBottomNav = when (fragmentTag) {
            FT.NOTES -> 0
            FT.TASKS -> 1
            FT.HOME -> 2
            FT.SHOPPING -> 3
            FT.BIRTHDAYS -> 4
            else -> 5
        }

        when (checkedBottomNav) {
            5 -> setNavBarUnchecked()
            else -> {
                setNavBarUnchecked()
                bottomNavigation.menu.getItem(checkedBottomNav).isChecked = true
            }
        }

        if (previousFragmentStack.peek() != fragmentTag) {
            previousFragmentStack.push(fragmentTag)
        }

        bottomNavigation.visibility = when (fragmentTag == FT.NOTE_EDITOR) {
            true -> View.GONE
            else -> View.VISIBLE
        }

        //create fragment object
        val fragment = when (fragmentTag) {
            FT.HOME -> homeFr
            FT.TASKS -> TodoFr()
            FT.SHOPPING -> {
                multiShoppingFr
            }
            FT.NOTES -> {
                NoteFr.searching = false
                noteFr
            }
            FT.NOTE_EDITOR -> {
                noteEditorFr = NoteEditorFr()
                noteEditorFr
            }
            FT.BIRTHDAYS -> {
                birthdayFr!!.searching = false
                birthdayFr
            }
            FT.SETTINGS_ABOUT -> SettingsAboutFr()
            FT.SETTINGS_NOTES -> SettingsNotesFr()
            FT.SETTINGS_SHOPPING -> SettingsShoppingFr()
            FT.SETTINGS_APPEARANCE -> SettingsAppearanceFr()
            FT.SETTINGS -> SettingsMainFr()
            FT.CUSTOM_ITEMS -> CustomItemFr()
            FT.SLEEP -> sleepFr
            FT.SETTINGS_HOWTO -> SettingsHowTo()
            FT.SETTINGS_BIRTHDAYS -> SettingsBirthdays()
            else -> homeFr
        }

        //animate fragment change
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, fragment, fragmentTag.name)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
        }
        return fragment
    }

    /**
     * OVERRIDE FUNCTIONS
     */
    override fun onDestroy() {
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            val noteEditorFr = getFragment(FT.NOTE_EDITOR) as NoteEditorFr
            val noteContent = noteEditorFr.getNoteContent()
            val noteTitle = noteEditorFr.getNoteTitle()

            if(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("editingNote", false)){
                //Save new note, if a note was edited, app was closed, and neither confirm nor delete was chosen
                    //User now has 2 notes, one unedited, and one including his last edit
                val oldNoteContent = PreferenceManager.getDefaultSharedPreferences(this).getString("editNoteContent", "")
                val oldNoteTitle = PreferenceManager.getDefaultSharedPreferences(this).getString("editNoteTitle", "")
                if(oldNoteContent != noteContent || oldNoteTitle != noteTitle){
                    noteFr?.noteListInstance?.addNote(noteTitle, noteContent, NoteColors.RED)
                }
            }else{
                noteFr?.noteListInstance?.addNote(noteTitle, noteContent, NoteColors.RED)
            }
        }
        super.onDestroy()
        this.finish()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        //close drawer when its open
        if (drawer_layout.isDrawerOpen(nav_drawer)) {
            drawer_layout.closeDrawer(GravityCompat.START)
            return
        }

        //When in birthdayFragment and searching, close search and restore fragment to normal mode
        if (previousFragmentStack.peek() == FT.BIRTHDAYS && birthdayFr!!.searching) {
            myBtnAdd.visibility = View.VISIBLE
            toolBar.title = getString(R.string.menuTitleBirthdays)
            birthdayFr!!.searchView.onActionViewCollapsed()
            birthdayFr!!.searching = false
            birthdayFr!!.updateUndoBirthdayIcon()
            birthdayFr!!.myAdapter.notifyDataSetChanged()
            return
        }

        //When in noteFragment and searching, close search and restore fragment to normal mode
        if (previousFragmentStack.peek() == FT.NOTES && NoteFr.searching) {
            myBtnAdd.visibility = View.VISIBLE
            toolBar.title = getString(R.string.menuTitleNotes)
            noteFr!!.searchView.onActionViewCollapsed()
            NoteFr.searching = false
            NoteFr.myAdapter.notifyDataSetChanged()
            return
        }

        //handles going back from editor
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            noteEditorFr =
                supportFragmentManager.findFragmentByTag(FT.NOTE_EDITOR.name) as NoteEditorFr
            if (noteEditorFr!!.relevantNoteChanges()) {
                noteEditorFr!!.dialogDiscardNoteChanges()
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

    @SuppressLint("InflateParams")

    fun loadDefaultSettings() {
        setDefault(SettingId.NOTE_COLUMNS, "2")
        setDefault(SettingId.NOTE_LINES, 10.0)
        setDefault(SettingId.FONT_SIZE, "18")
        setDefault(SettingId.CLOSE_ITEM_DIALOG, false)
        setDefault(SettingId.EXPAND_ONE_CATEGORY, false)
        setDefault(SettingId.COLLAPSE_CHECKED_SUBLISTS, true)
        setDefault(SettingId.MOVE_CHECKED_DOWN, true)
        setDefault(SettingId.SHAPES_ROUND, true)
        setDefault(SettingId.SHAKE_TASK_HOME, true)
        setDefault(SettingId.THEME_DARK, false)
        setDefault(SettingId.NOTES_SWIPE_DELETE, true)
        setDefault(SettingId.USE_SYSTEM_THEME, true)
        val languageNumber = when (Locale.getDefault().displayLanguage) {
            Locale.GERMAN.displayLanguage -> {
                //german
                1.0
            }
            //english
            else -> 0.0
        }
        setDefault(SettingId.LANGUAGE, languageNumber)
        setDefault(SettingId.BIRTHDAY_SHOW_MONTH, true)
        setDefault(SettingId.BIRTHDAY_COLORS_SOUTH, false)
        setDefault(SettingId.SUGGEST_SIMILAR_ITEMS, true)
        setDefault(SettingId.PREVIEW_BIRTHDAY, true)
        setDefault(SettingId.BIRTHDAY_NOTIFICATION_TIME, "12:00")
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
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val customTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        customTitle.tvDialogTitle.text = getString(titleId)
        myBuilder.setCustomTitle(customTitle)
        val myAlertDialog = myBuilder.create()

        val btnCancelDelete = myDialogView.btnCancelDelete
        val btnDelete = myDialogView.btnDelete

        //onclick to delete
        btnDelete.setOnClickListener {
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

