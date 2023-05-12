package com.pocket_plan.j7_003

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
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
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayList
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.notelist.NoteColors
import com.pocket_plan.j7_003.data.notelist.NoteDirList
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr
import com.pocket_plan.j7_003.data.notelist.NoteFr
import com.pocket_plan.j7_003.data.settings.Languages
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsMainFr
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.settings.sub_categories.*
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.CustomItemFr
import com.pocket_plan.j7_003.data.settings.sub_categories.shoppinglist.SettingsShoppingFr
import com.pocket_plan.j7_003.data.shoppinglist.ItemTemplateList
import com.pocket_plan.j7_003.data.shoppinglist.MultiShoppingFr
import com.pocket_plan.j7_003.data.shoppinglist.ShoppingListWrapper
import com.pocket_plan.j7_003.data.shoppinglist.UserItemTemplateList
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.todolist.TodoFr
import com.pocket_plan.j7_003.data.todolist.TodoList
import com.pocket_plan.j7_003.data.todolist.TodoTaskAdapter
import com.pocket_plan.j7_003.databinding.DialogConfirmBinding
import com.pocket_plan.j7_003.databinding.DrawerLayoutBinding
import com.pocket_plan.j7_003.databinding.HeaderNavigationDrawerBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import java.util.Locale
import java.util.Stack

class MainActivity : AppCompatActivity() {

    lateinit var toolbar: Toolbar
    private lateinit var drawerLayoutBinding: DrawerLayoutBinding

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    private var birthdayFr: BirthdayFr? = null
    private var homeFr: HomeFr? = null
    lateinit var multiShoppingFr: MultiShoppingFr
    private var noteEditorFr: NoteEditorFr? = null
    private var noteFr: NoteFr? = null
    private var sleepFr: SleepFr? = null
    var todoFr: TodoFr? = null

    lateinit var titleDialogBinding: TitleDialogBinding

    lateinit var itemTemplateList: ItemTemplateList
    lateinit var userItemTemplateList: UserItemTemplateList

    lateinit var myBtnAdd: FloatingActionButton

    companion object {
        val previousFragmentStack: Stack<FT> = Stack()
        lateinit var bottomNavigation: BottomNavigationView

        lateinit var mainNoteListDir: NoteDirList
        lateinit var birthdayList: BirthdayList

        lateinit var itemNameList: ArrayList<String>
        lateinit var shoppingListWrapper: ShoppingListWrapper
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
        FT.HOME -> homeFr as HomeFr
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

        //Initialize fragment stack that enables onBackPress behavior
        previousFragmentStack.clear()
        previousFragmentStack.push(FT.EMPTY)

        //Initialize StorageHandler and SettingsManager
        StorageHandler.path = this.filesDir.absolutePath
        SettingsManager.init()

        //set correct language depending on setting
        val languageCode = when (SettingsManager.getSetting(SettingId.LANGUAGE)) {
            Languages.ITALIAN.index -> Languages.ITALIAN.code
            Languages.RUSSIAN.index -> Languages.RUSSIAN.code
            Languages.SPANISH.index -> Languages.SPANISH.code
            Languages.FRENCH.index -> Languages.FRENCH.code
            Languages.GERMAN.index -> Languages.GERMAN.code
            else -> Languages.ENGLISH.code
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

        //create drawer_layout
        super.onCreate(savedInstanceState)
        drawerLayoutBinding = DrawerLayoutBinding.inflate(layoutInflater)
        setContentView(drawerLayoutBinding.root)

        //IMPORTANT: ORDER IS CRITICAL HERE
        //Initialize Time api and AlarmHandler
        AndroidThreeTen.init(this)
        val time = SettingsManager.getSetting(SettingId.BIRTHDAY_NOTIFICATION_TIME) as String
        AlarmHandler.setBirthdayAlarms(time, context = this)

        //Initialize toolbar
        toolbar = drawerLayoutBinding.tbMain
        setSupportActionBar(toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu)

        //Initialize header and icon in side drawer, show current version name
        val headerView = drawerLayoutBinding.navDrawer.getHeaderView(0)
        val headerBinding = HeaderNavigationDrawerBinding.bind(headerView)
        val versionString = "v " + packageManager.getPackageInfo(packageName, 0).versionName
        headerBinding.tvVersion.text = versionString

        //Initialize adapters and necessary list instances
        todoFr = TodoFr()
        TodoFr.todoListInstance = TodoList()
        TodoFr.myAdapter = TodoTaskAdapter(this, todoFr!!)

        //Initialize fragment classes necessary for home
        sleepFr = SleepFr()
        birthdayFr = BirthdayFr()
        birthdayList = BirthdayList(resources.getStringArray(R.array.months))
        homeFr = HomeFr()

        //spinning app Icon
        val myLogo = headerBinding.ivLogo
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
        mDrawerToggle =
            ActionBarDrawerToggle(this, drawerLayoutBinding.drawerLayout, R.string.generalOpen, R.string.generalClose)
        drawerLayoutBinding.drawerLayout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        //initialize bottom navigation
        bottomNavigation = findViewById(R.id.btm_nav)

        multiShoppingFr = MultiShoppingFr()
        shoppingListWrapper = ShoppingListWrapper(getString(R.string.menuTitleShopping))

        //Initialize remaining fragments
        noteFr = NoteFr()
        mainNoteListDir = NoteDirList()
        noteFr!!.noteListDirs = mainNoteListDir

    //When activity is entered via special intent, change to respective fragment
        if(intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain"){
            if(handleTextViaIntent(intent)) changeToFragment(FT.NOTES)
        }


        when (intent.extras?.get("NotificationEntry").toString()) {
        "birthdays" -> changeToFragment(FT.BIRTHDAYS)
        "SReminder" -> changeToFragment(FT.HOME)
        "settings" -> changeToFragment(FT.SETTINGS)
        "general" -> {
            previousFragmentStack.push(FT.HOME)
            previousFragmentStack.push(FT.SETTINGS)
            changeToFragment(FT.SETTINGS_GENERAL)
        }

        "backup" -> {
            previousFragmentStack.push(FT.HOME)
            changeToFragment(FT.SETTINGS)
        }

        else -> {
            if (previousFragmentStack.peek() == FT.EMPTY) {
                changeToFragment(FT.HOME)
            } else {
                changeToFragment(previousFragmentStack.pop())
            }
        }
    }

    multiShoppingFr.preloadAddItemDialog(this, layoutInflater)
    todoFr!!.preloadAddTaskDialog(this, layoutInflater)


    try {
            //1000 things can go wrong here
            manageNoteRestore()
        } catch (e: Exception) {
            /* no-op */
        }

        //initialize navigation drawer listener
        drawerLayoutBinding.navDrawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToFragment(FT.SETTINGS)
                R.id.menuSleepReminder -> changeToFragment(FT.SLEEP)
                R.id.menuHelp -> changeToFragment(FT.SETTINGS_HOWTO)
            }
            drawerLayoutBinding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        drawerLayoutBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
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
        bottomNavigation.setOnItemSelectedListener { item ->
            //If selecting "notes" element, while already in notes, and not in root folder, go back to root folder
            if (item.itemId == R.id.bottom1 && previousFragmentStack.peek() == FT.NOTES && noteFr!!.noteListDirs.folderStack.size > 1) {
                noteFr!!.noteListDirs.resetStack()
                changeToFragment(FT.NOTES, exitingFragment = true)
                return@setOnItemSelectedListener true
            }
            if (!navList.contains(previousFragmentStack.peek()) || bottomNavigation.selectedItemId != item.itemId) {
                when (item.itemId) {
                    R.id.bottom1 -> changeToFragment(FT.NOTES)
                    R.id.bottom2 -> changeToFragment(FT.TASKS)
                    R.id.bottom3 -> changeToFragment(FT.HOME)
                    R.id.bottom4 -> changeToFragment(FT.SHOPPING)
                    R.id.bottom5 -> changeToFragment(FT.BIRTHDAYS)
                }
            }
            return@setOnItemSelectedListener true
        }

        this.myBtnAdd = drawerLayoutBinding.btnAdd

        //initialize btn to add elements, depending on which fragment is active
        drawerLayoutBinding.btnAdd.setOnClickListener {
            when (previousFragmentStack.peek()) {
                FT.BIRTHDAYS -> {
                    BirthdayFr.editBirthdayHolder = null
                    birthdayFr!!.openAddBirthdayDialog()
                }

                FT.TASKS -> {
                    todoFr!!.myFragment.dialogAddTask()
                }

                FT.NOTES -> {
                    NoteFr.editNoteHolder = null
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

    private fun handleTextViaIntent(intent: Intent): Boolean{
        try {
            val content = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (content != null) {
                mainNoteListDir.addNote(title = "", content = content, color = NoteColors.BLUE)
                Toast.makeText(this, getString(R.string.notesNotificationNoteAdded), Toast.LENGTH_SHORT).show()
                return true
            }
        }catch (e: Exception){
            Toast.makeText(this, getString(R.string.settingsBackupImportFailed), Toast.LENGTH_SHORT).show()
        }
        return false
    }


    private fun manageNoteRestore() {

        val editNoteContentOnDestroy = getPreferences(Context.MODE_PRIVATE).getString(
            PreferenceIDs.EDIT_NOTE_CONTENT_ON_DESTROY.id,
            ""
        )
        val editNoteTitleOnDestroy = getPreferences(Context.MODE_PRIVATE).getString(
            PreferenceIDs.EDIT_NOTE_TITLE_ON_DESTROY.id,
            ""
        )
        val editNoteColorOnDestroy = getPreferences(Context.MODE_PRIVATE).getInt(
            PreferenceIDs.EDIT_NOTE_COLOR_ON_DESTROY.id,
            -1
        )

        if (editNoteContentOnDestroy == null || editNoteTitleOnDestroy == null || noteFr == null || editNoteColorOnDestroy == -1) return

        if (editNoteContentOnDestroy == "" && editNoteTitleOnDestroy == "") {
            //App was closed when editor window was empty, do nothing
            getPreferences(Context.MODE_PRIVATE).edit()
                .putString(PreferenceIDs.EDIT_NOTE_CONTENT.id, "").apply()
            getPreferences(Context.MODE_PRIVATE).edit()
                .putString(PreferenceIDs.EDIT_NOTE_TITLE.id, "").apply()
            return
        }

        //Get saved editNoteContent (this gets written when editor is opened (content of note to edit)
        val editNoteContent =
            getPreferences(Context.MODE_PRIVATE).getString(PreferenceIDs.EDIT_NOTE_CONTENT.id, "")
        val editNoteTitle =
            getPreferences(Context.MODE_PRIVATE).getString(PreferenceIDs.EDIT_NOTE_TITLE.id, "")
        val editNoteColor =
            getPreferences(Context.MODE_PRIVATE).getInt(PreferenceIDs.EDIT_NOTE_COLOR.id, -1)

        if (editNoteContent == null || editNoteTitle == null || editNoteColor == -1) return

        resetNotePreferenceStorage()

        if (editNoteContent == "" && editNoteTitle == "") {
            //App was closed after editor was opened for a new note, and app was closed with non-empty editor (see if above)
            //add new note with content of editor saved onDestroy
            noteFr!!.noteListDirs.rootDir.noteList.addNote(
                editNoteTitleOnDestroy,
                editNoteContentOnDestroy, NoteColors.values()[editNoteColorOnDestroy]
            )
            noteFr!!.noteListDirs.save()
            return
        }

        if (editNoteContent != editNoteContentOnDestroy || editNoteTitle != editNoteTitleOnDestroy || editNoteColor != editNoteColorOnDestroy) {
            //App was closed, after editor got initialized with text, and this text was modified before the app close, but not saved
            val editedNote = noteFr!!.noteListDirs.getNoteByTitleAndContent(
                title = editNoteTitle,
                content = editNoteContent
            ) ?: return
            NoteFr.editNoteHolder = editedNote

            NoteFr.displayContent = editNoteContentOnDestroy
            NoteFr.displayTitle = editNoteTitleOnDestroy
            NoteFr.displayColor = editNoteColorOnDestroy

            previousFragmentStack.push(FT.NOTES)
            noteFr!!.noteListDirs.adjustStackAbove(editedNote)

            changeToFragment(FT.NOTE_EDITOR)
            return
        }
    }

    private fun resetNotePreferenceStorage() {
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString(PreferenceIDs.EDIT_NOTE_CONTENT.id, "").apply()
        getPreferences(Context.MODE_PRIVATE).edit().putString(PreferenceIDs.EDIT_NOTE_TITLE.id, "")
            .apply()
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString(PreferenceIDs.EDIT_NOTE_CONTENT_ON_DESTROY.id, "").apply()
        getPreferences(Context.MODE_PRIVATE).edit()
            .putString(PreferenceIDs.EDIT_NOTE_TITLE_ON_DESTROY.id, "").apply()
        getPreferences(Context.MODE_PRIVATE).edit().putInt(PreferenceIDs.EDIT_NOTE_COLOR.id, -1)
            .apply()
        getPreferences(Context.MODE_PRIVATE).edit()
            .putInt(PreferenceIDs.EDIT_NOTE_COLOR_ON_DESTROY.id, -1).apply()
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

    fun changeToFragment(fragmentTag: FT, exitingFragment: Boolean = false): Fragment? {
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
        drawerLayoutBinding.btnAdd.visibility = when (fragmentTag) {
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
        toolbar.title = when (fragmentTag) {
            FT.HOME -> resources.getText(R.string.menuTitleHome)
            FT.TASKS -> resources.getText(R.string.menuTitleTasks)
            FT.SETTINGS_ABOUT -> resources.getText(R.string.menuTitleAbout)
            FT.SHOPPING, FT.SETTINGS_SHOPPING -> resources.getText(R.string.menuTitleShopping)
            FT.SETTINGS_NOTES -> resources.getText(R.string.menuTitleNotes)
            FT.NOTES -> mainNoteListDir.getCurrentPathName(getString(R.string.menuTitleNotes))
            FT.SETTINGS -> resources.getText(R.string.menuTitleSettings)
            FT.NOTE_EDITOR -> resources.getText(R.string.menuTitleNotesEditor)
            FT.BIRTHDAYS -> resources.getText(R.string.menuTitleBirthdays)
            FT.CUSTOM_ITEMS -> resources.getText(R.string.menuTitleCustomItem)
            FT.SLEEP -> resources.getText(R.string.menuTitleSleep)
            FT.SETTINGS_BACKUP -> resources.getText(R.string.settingsBackupTitle)
            FT.SETTINGS_GENERAL -> resources.getText(R.string.settingsGeneralTitle)
            FT.SETTINGS_HOWTO -> resources.getText(R.string.settingsHowToTitle)
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
            FT.TASKS -> todoFr
            FT.SHOPPING -> {
                multiShoppingFr
            }
            FT.NOTES -> {
                NoteFr.searching = false
                noteFr = NoteFr()
                noteFr!!.noteListDirs = mainNoteListDir
                noteFr
            }
            FT.NOTE_EDITOR -> {
                noteEditorFr = NoteEditorFr()
                noteEditorFr
            }
            FT.BIRTHDAYS -> {
                birthdayFr
            }
            FT.SETTINGS_ABOUT -> SettingsAboutFr()
            FT.SETTINGS_NOTES -> SettingsNotesFr()
            FT.SETTINGS_SHOPPING -> SettingsShoppingFr()
            FT.SETTINGS_GENERAL -> SettingsGeneralFr()
            FT.SETTINGS -> SettingsMainFr()
            FT.CUSTOM_ITEMS -> CustomItemFr()
            FT.SLEEP -> sleepFr
            FT.SETTINGS_HOWTO -> SettingsHowTo()
            FT.SETTINGS_BIRTHDAYS -> SettingsBirthdays()
            else -> homeFr
        }

        val fragmentTransition = when (exitingFragment) {
            true -> FragmentTransaction.TRANSIT_FRAGMENT_CLOSE
            else -> FragmentTransaction.TRANSIT_FRAGMENT_OPEN
        }

        //animate fragment change
        if (fragment != null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, fragment, fragmentTag.name)
                .setTransition(fragmentTransition)
                .commit()
        }
        return fragment
    }

    fun setToolbarTitle(msg: String) {
       toolbar.title = msg
    }


    /**
     * OVERRIDE FUNCTIONS
     */
    @SuppressLint("NotifyDataSetChanged")
    override fun onBackPressed() {
        //close drawer when its open
        if (drawerLayoutBinding.drawerLayout.isDrawerOpen(drawerLayoutBinding.navDrawer)) {
            drawerLayoutBinding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        //When in birthdayFragment and searching, close search and restore fragment to normal mode
        if (previousFragmentStack.peek() == FT.BIRTHDAYS && birthdayFr!!.searching) {
            myBtnAdd.visibility = View.VISIBLE
            toolbar.title = getString(R.string.menuTitleBirthdays)
            birthdayFr!!.searchView.onActionViewCollapsed()
            birthdayFr!!.searching = false
            birthdayFr!!.updateBirthdayMenu()
            birthdayFr!!.updateUndoBirthdayIcon()
            birthdayFr!!.myAdapter.notifyDataSetChanged()
            return
        }

        //When in noteFragment and searching, close search and restore fragment to normal mode
        if (previousFragmentStack.peek() == FT.NOTES && NoteFr.searching) {
            myBtnAdd.visibility = View.VISIBLE
            toolbar.title = getString(R.string.menuTitleNotes)
            noteFr!!.searchView.onActionViewCollapsed()
            NoteFr.searching = false
            NoteFr.myAdapter.notifyDataSetChanged()
            noteFr!!.setMenuAccessibility(true)
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

        //When in note fragment, back press should go upwards in folder structure
        if (previousFragmentStack.peek() == FT.NOTES) {
            val result = noteFr!!.noteListDirs.goBack()
            if (result) {
                changeToFragment(FT.NOTES, exitingFragment = true)
                return
            }
        }

        previousFragmentStack.pop()
        if (previousFragmentStack.isNotEmpty() && previousFragmentStack.peek() != FT.EMPTY) {
            changeToFragment(previousFragmentStack.peek())
        } else super.onBackPressed()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStop() {
        if (previousFragmentStack.peek() == FT.NOTE_EDITOR) {
            getPreferences(Context.MODE_PRIVATE).edit().putString(
                PreferenceIDs.EDIT_NOTE_CONTENT_ON_DESTROY.id,
                noteEditorFr!!.getEditorContent()
            ).apply()
            getPreferences(Context.MODE_PRIVATE).edit().putString(
                PreferenceIDs.EDIT_NOTE_TITLE_ON_DESTROY.id,
                noteEditorFr!!.getEditorTitle()
            ).apply()
            getPreferences(Context.MODE_PRIVATE).edit()
                .putInt(PreferenceIDs.EDIT_NOTE_COLOR_ON_DESTROY.id, noteEditorFr!!.getNoteColor())
                .apply()
        }
        super.onStop()
    }

    /**
     * Opens a dialog, asking the user to confirm a deletion by pressing a button.
     * The action to be executed when the button is pressed can be passed as a lambda.
     * @param titleId Resource id pointing to the String that will be displayed as dialog title
     * @param action Lambda that will be executed when btnDelete is pressed
     */

    @SuppressLint("InflateParams")
    fun dialogConfirm(titleId: Int, action: () -> Unit, hint: String = "") {
        dialogConfirm(getString(titleId), action, hint)
    }

    fun dialogConfirm(title: String, action: () -> Unit, hint: String = "") {
        val dialogConfirmBinding = DialogConfirmBinding.inflate(layoutInflater)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(dialogConfirmBinding.root)
        val titleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        titleDialogBinding.tvDialogTitle.text = title
        myBuilder.setCustomTitle(titleDialogBinding.tvDialogTitle)
        val myAlertDialog = myBuilder.create()

        //show or hide hint
        val tvConfirmHint = dialogConfirmBinding.tvConfirmHint
        tvConfirmHint.visibility = when (hint == "") {
            true -> View.GONE
            else -> View.VISIBLE
        }

        //set correct text for hint
        if (hint != "") {
            tvConfirmHint.text = hint
        }

        val btnCancel = dialogConfirmBinding.btnCancel
        val btnConfirm = dialogConfirmBinding.btnConfirm

        //onclick to confirm
        btnConfirm.setOnClickListener {
            action()
            myAlertDialog.dismiss()
        }

        //hide dialog when "Cancel" is pressed
        btnCancel.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }
}

