package com.example.j7_003

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.about.AboutFr
import com.example.j7_003.data.birthdaylist.BirthdayFragment
import com.example.j7_003.data.calendar.CalendarAppointment
import com.example.j7_003.data.calendar.CalenderFragment
import com.example.j7_003.data.calendar.CreateTermFr
import com.example.j7_003.data.calendar.DayFr
import com.example.j7_003.data.fragmenttags.FragmentTags
import com.example.j7_003.data.home.HomeFr
import com.example.j7_003.data.notelist.NoteEditorFr
import com.example.j7_003.data.notelist.Note
import com.example.j7_003.data.notelist.NoteColors
import com.example.j7_003.data.notelist.NoteFr
import com.example.j7_003.data.settings.SettingsFr
import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.data.settings.shoppinglist.CustomItemFragment
import com.example.j7_003.data.shoppinglist.*
import com.example.j7_003.data.sleepreminder.SleepFr
import com.example.j7_003.data.todolist.TodoFr
import com.example.j7_003.system_interaction.handler.AlarmHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.actionbar.view.*
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.dialog_delete_note.view.*
import kotlinx.android.synthetic.main.dialog_discard_note_edit.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.fragment_sleep.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.DayOfWeek

class MainActivity : AppCompatActivity() {
    private lateinit var homeFr: HomeFr
    private lateinit var dayFragment: DayFr
    private lateinit var calendarFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFr: SettingsFr
    private lateinit var todoFr: TodoFr
    private lateinit var sleepFr: SleepFr
    private lateinit var noteFr: NoteFr
    private lateinit var shoppingFr: ShoppingFr
    private lateinit var noteEditorFr: NoteEditorFr
    private lateinit var createTermFr: CreateTermFr
    private lateinit var aboutFr: AboutFr
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var customItemFragment: CustomItemFragment

    //contents for shopping list
    private lateinit var tagList: TagList
    private lateinit var tagNames: Array<String?>
    private lateinit var itemTemplateList: ItemTemplateList
    private lateinit var userItemTemplateList: UserItemTemplateList
    private lateinit var itemNameList: ArrayList<String>
    private var addItemDialog: AlertDialog? = null
    private var addItemDialogView: View? = null

    companion object {
        var previousFragmentTag: FragmentTags = FragmentTags.EMPTY
        var activeFragmentTag: FragmentTags = FragmentTags.EMPTY
        lateinit var act: MainActivity
        lateinit var sleepView: View
        lateinit var actionbarContent: View
        lateinit var searchView: SearchView
        var editNoteHolder: Note? = null
        var editTerm: CalendarAppointment? = null
        var myMenu: Menu? = null
        var noteColor: NoteColors = NoteColors.YELLOW
        var fromHome: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Set a reference to this activity so its accessible in the companion object
        act = this

        preloadAddItemDialog()

        //Initialize Settings Manager and Time api and Alarmhandler
        SettingsManager.init()
        AndroidThreeTen.init(this)
        AlarmHandler.setBirthdayAlarms(context = this)

        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        //Check if layout should be right-handed or left-handed
        val leftHandedMode = SettingsManager.getSetting("drawerLeftSide") as Boolean
        if (leftHandedMode) {
            setContentView(R.layout.main_panel_lefthanded)
        } else {
            setContentView(R.layout.main_panel)
        }


        //initialize actionbar content
        actionbarContent = layoutInflater.inflate(R.layout.actionbar, null, false)
        supportActionBar?.title = ""
        supportActionBar?.customView = actionbarContent
        supportActionBar?.setDisplayShowCustomEnabled(true)

        //initialize navigation drawer
        nav_drawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToSettings()
                R.id.menuItemBirthdays -> changeToBirthdays()
                R.id.menuItemAbout -> changeToAbout()
            }
            if (leftHandedMode) {
                drawer_layout.closeDrawer(GravityCompat.START)
            } else {
                drawer_layout.closeDrawer(GravityCompat.END)
            }
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
                R.id.notes -> changeToNotes()
                R.id.todolist -> changeToToDo()
                R.id.home -> changeToHome()
                R.id.shopping -> changeToShopping()
                R.id.sleepReminder -> changeToSleepReminder()
            }
            true
        }

        //inflate sleepView for faster loading time
        sleepView = layoutInflater.inflate(R.layout.fragment_sleep, null, false)

    }

    /**
     * DEBUG FUNCTIONS
     */

    fun titleDebug(debugMsg: String) {
        supportActionBar?.title = debugMsg
    }


    fun sadToast(msg: String) {
        Toast.makeText(act, msg + " :(", Toast.LENGTH_LONG).show()
    }

    private fun setNavBarUnchecked() {
        bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigation.menu.size()) {
            bottomNavigation.menu.getItem(i).isChecked = false
        }
        bottomNavigation.menu.setGroupCheckable(0, true, true)
    }

    /**
     * CHANGE FRAGMENT METHODS
     */
    fun changeToBirthdays() {
        if (activeFragmentTag != FragmentTags.BIRTHDAYS) {
            hideMenuIcons()
            myMenu?.getItem(3)?.isVisible = true
            birthdayFragment = BirthdayFragment()
            changeToFragment(birthdayFragment, FragmentTags.BIRTHDAYS, "Birthdays", -1)
            searchView.onActionViewCollapsed()
        }
    }

    fun changeToShopping() {
        if (activeFragmentTag != FragmentTags.SHOPPING) {
            hideMenuIcons()
            shoppingFr = ShoppingFr()
            changeToFragment(shoppingFr, FragmentTags.SHOPPING, "Shopping", R.id.shopping)
        }
    }

    fun changeToCustomItems() {
        if (activeFragmentTag != FragmentTags.CUSTOM_ITEMS) {
            hideMenuIcons()
            customItemFragment = CustomItemFragment()
            changeToFragment(customItemFragment, FragmentTags.CUSTOM_ITEMS, "Custom Items", -1)
        }
    }

    fun changeToToDo() {
        if (activeFragmentTag != FragmentTags.TASKS) {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete_sweep)
            updateDeleteTaskIcon()
            todoFr = TodoFr()
            changeToFragment(todoFr, FragmentTags.TASKS, "To-Do", R.id.todolist)
        }
    }

    fun changeToHome() {
        hideMenuIcons()
        if (activeFragmentTag != FragmentTags.HOME) {
            homeFr = HomeFr()
            changeToFragment(homeFr, FragmentTags.HOME, "Pocket Plan", R.id.home)
        }
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }

    fun changeToCreateTerm() {
        if (activeFragmentTag != FragmentTags.CREATE_TERM) {
            hideMenuIcons()
            createTermFr = CreateTermFr()
            changeToFragment(
                createTermFr,
                FragmentTags.CREATE_TERM,
                "Create Appointment",
                R.id.home
            )
        }
    }

    fun changeToSettings() {
        if (activeFragmentTag != FragmentTags.SETTINGS) {
            hideMenuIcons()
            settingsFr = SettingsFr()
            changeToFragment(settingsFr, FragmentTags.SETTINGS, "Settings", -1)
        }
        setNavBarUnchecked()
    }

    fun changeToSleepReminder() {
        if (activeFragmentTag != FragmentTags.SLEEP) {
            hideMenuIcons()
            sleepFr = SleepFr()
            changeToFragment(sleepFr, FragmentTags.SLEEP, "Sleep-Reminder", -1)
        }
    }

    private fun changeToNotes() {
        if (activeFragmentTag != FragmentTags.NOTES) {
            hideMenuIcons()
            noteFr = NoteFr()
            changeToFragment(noteFr, FragmentTags.NOTES, "Notes", R.id.notes)
        }
    }

    fun changeToCreateNoteFragment() {
        if (activeFragmentTag != FragmentTags.NOTE_EDITOR) {
            if (editNoteHolder != null) {
                myMenu?.getItem(0)?.isVisible = true
                myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete)
            }
            myMenu?.getItem(1)?.setIcon(R.drawable.ic_action_colorpicker)
            myMenu?.getItem(2)?.setIcon(R.drawable.ic_check_mark)
            myMenu?.getItem(1)?.isVisible = true
            myMenu?.getItem(2)?.isVisible = true

            noteEditorFr = NoteEditorFr()
            changeToFragment(noteEditorFr, FragmentTags.NOTE_EDITOR, "Editor", -1)

            //initialize button with color of note that is currently being edited
            if (editNoteHolder != null) {
                val btnChooserColor = when (noteColor) {
                    NoteColors.RED -> R.color.colorNoteRed
                    NoteColors.YELLOW -> R.color.colorNoteYellow
                    NoteColors.GREEN -> R.color.colorNoteGreen
                    NoteColors.BLUE -> R.color.colorNoteBlue
                    NoteColors.PURPLE -> R.color.colorNotePurple
                }
                myMenu?.getItem(1)?.icon?.setTint(ContextCompat.getColor(this, btnChooserColor))
            } else {
                noteColor = NoteColors.YELLOW
                myMenu?.getItem(1)?.icon?.setTint(
                    ContextCompat.getColor(
                        this,
                        R.color.colorNoteYellow
                    )
                )
            }
        }
    }

    fun changeToAbout() {
        if (activeFragmentTag != FragmentTags.ABOUT) {
            hideMenuIcons()
            aboutFr = AboutFr()
            changeToFragment(aboutFr, FragmentTags.ABOUT, "About", -1)
        }
    }

    fun changeToDayView() {
        if (activeFragmentTag != FragmentTags.DAY_VIEW) {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_all_terms)
            myMenu?.getItem(0)?.isVisible = true
            dayFragment = DayFr()
            changeToFragment(dayFragment, FragmentTags.DAY_VIEW, "Day-View", R.id.home)
        }
    }

    private fun changeToCalendar() {
        if (activeFragmentTag != FragmentTags.CALENDAR) {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_calendar)
            myMenu?.getItem(0)?.isVisible = true
            calendarFragment = CalenderFragment()
            changeToFragment(calendarFragment, FragmentTags.CALENDAR, "Calendar", R.id.home)
        }
    }

    /**
     * Manages the change to a different fragment
     * @param fragment the fragment that will be displayed
     * @param activeFragmentTag String tag that will be saved to check which fragment is active
     * @param actionBarTitle the title that will be displayed in the action bar, once this
     * fragment is visible
     * @param bottomNavigationId the id of the element that will be selected in the bottom
     * navigation bar, if it is -1, the currently selected id will not change
     */

    private fun changeToFragment(
        fragment: Fragment, fragmentTag: FragmentTags,
        actionBarTitle: String, bottomNavigationId: Int
    ) {
        if (activeFragmentTag == FragmentTags.NOTE_EDITOR) {
            if(relevantNoteChanges()){
                dialogDiscardNoteChanges(fragmentTag)
                return
            }
        }
        actionbarContent.tvActionbarTitle.text = actionBarTitle
        previousFragmentTag = activeFragmentTag
        activeFragmentTag = fragmentTag



        if (bottomNavigationId != -1) {
            bottomNavigation.selectedItemId = bottomNavigationId
        }
        else{
            setNavBarUnchecked()
        }
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, fragment)
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .commit()
    }

    /**
     * UI FUNCTIONS
     */

    private fun hideMenuIcons() {
        if (myMenu != null) {
            myMenu!!.getItem(0).setVisible(false)
            myMenu!!.getItem(1).setVisible(false)
            myMenu!!.getItem(2).setVisible(false)
            myMenu!!.getItem(3).setVisible(false)
        }
    }

    private fun openColorChooser() {
        //inflate the dialog with custom view
        val myDialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = "Choose color"
        myBuilder.setCustomTitle(editTitle)

        //show dialog
        val myAlertDialog = myBuilder.create()
        myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog.show()

        val colorList = arrayOf(
            R.color.colorNoteRed, R.color.colorNoteYellow,
            R.color.colorNoteGreen, R.color.colorNoteBlue, R.color.colorNotePurple
        )
        val buttonList = arrayOf(
            myDialogView.btnRed, myDialogView.btnYellow,
            myDialogView.btnGreen, myDialogView.btnBlue, myDialogView.btnPurple
        )
        /**
         * Onclick-listeners for every specific color button
         */
        buttonList.forEachIndexed() { i, b ->
            b.setOnClickListener() {
                noteColor = NoteColors.values()[i]
                myMenu?.getItem(1)?.icon?.setTint(ContextCompat.getColor(this, colorList[i]))
                myAlertDialog.dismiss()
            }
        }
    }

    fun updateUndoTaskIcon() {
        if (TodoFr.deletedTask != null || TodoFr.deletedTaskList.size > 0) {
            myMenu?.getItem(1)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(1)?.isVisible = true
        } else {
            myMenu?.getItem(1)?.isVisible = false
        }
    }

    fun updateUndoNoteIcon() {
        if (NoteFr.deletedNote != null) {
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(0)?.isVisible = true
        } else {
            myMenu?.getItem(0)?.isVisible = false
        }
    }

    fun updateUndoBirthdayIcon() {
        if (BirthdayFragment.deletedBirthday != null && !BirthdayFragment.searching) {
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(0)?.isVisible = true
        } else {
            myMenu?.getItem(0)?.isVisible = false
        }
    }

    fun updateUndoItemIcon() {
        //TODO uncomment this
//        if(ShoppingFragment.deletedItem!=null){
//            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
//            myMenu?.getItem(0)?.isVisible = true
//        }else{
//            myMenu?.getItem(0)?.isVisible = false
//        }
    }

    fun updateDeleteTaskIcon() {
        val checkedTasks = TodoFr.todoListInstance.filter { t -> t.isChecked }.size
        myMenu?.getItem(0)?.isVisible = checkedTasks > 0
    }


    /**
     * DATA MANAGEMENT FUNCTIONS
     */

    private fun manageEditNote() {
        hideKeyboard()
        val noteContent = noteEditorFr.etNoteContent.text.toString()
        val noteTitle = noteEditorFr.etNoteTitle.text.toString()
        editNoteHolder!!.title = noteTitle
        editNoteHolder!!.content = noteContent
        editNoteHolder!!.color = noteColor
        editNoteHolder = null
//        activeFragmentTag=FragmentTags.EMPTY
    }

    private fun manageNoteConfirm() {

        if (editNoteHolder == null) {
            manageAddNote()
        } else {
            manageEditNote()
        }
    }

    private fun manageAddNote() {
        hideKeyboard()
        val noteContent = noteEditorFr.etNoteContent.text.toString()
        val noteTitle = noteEditorFr.etNoteTitle.text.toString()
        NoteFr.noteListInstance.addNote(noteTitle, noteContent, noteColor)
//        activeFragmentTag=FragmentTags.EMPTY
//        if (previousFragmentTag==FragmentTags.NOTES) {
//            changeToNotes()
//        } else {
//            changeToHome()
//            Toast.makeText(act, "Note was added!", Toast.LENGTH_SHORT).show()
//            fromHome = false
//        }
    }

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

    /**
     * OVERRIDE FUNCTIONS
     */

    override fun onBackPressed() {

        if (activeFragmentTag == FragmentTags.NOTE_EDITOR) {
            if(relevantNoteChanges()){
                dialogDiscardNoteChanges(previousFragmentTag)
                return
            }
        }

        when (previousFragmentTag) {
            FragmentTags.HOME -> changeToHome()
            FragmentTags.NOTES -> changeToNotes()
            FragmentTags.SHOPPING -> changeToShopping()
            FragmentTags.SETTINGS -> changeToSettings()
            FragmentTags.TASKS -> changeToToDo()
            FragmentTags.SLEEP -> changeToSleepReminder()
            else -> super.onBackPressed()
        }


    }

    private fun relevantNoteChanges():Boolean{

        var result = true
        //check if note was edited, return otherwise
        if (editNoteHolder != null && editNoteHolder!!.title == noteEditorFr.etNoteTitle.text.toString() &&
            editNoteHolder!!.content == noteEditorFr.etNoteContent.text.toString() &&
            editNoteHolder!!.color == noteColor) {
            //no relevant note changes if the title, content and color did not get changed
            result = false
        }

        //check if anything was written when adding new note, return otherwise
        if(editNoteHolder==null && noteEditorFr.etNoteTitle.text.toString()=="" &&
            noteEditorFr.etNoteContent.text.toString()==""){
            //no relevant note changes if its a new empty note
            result = false
        }
        return result
    }

    private fun dialogDiscardNoteChanges(gotoFragment: FragmentTags) {

        val myDialogView = LayoutInflater.from(act).inflate(R.layout.dialog_discard_note_edit, null)

        //AlertDialogBuilder
        val myBuilder = act?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        customTitle.tvDialogTitle.text = "Save changes?"
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            setNavBarUnchecked()
        }

        myDialogView.btnDiscardChanges.setOnClickListener {
            activeFragmentTag = FragmentTags.EMPTY
            myAlertDialog?.dismiss()
            when (gotoFragment) {
                FragmentTags.HOME -> changeToHome()
                FragmentTags.NOTES -> changeToNotes()
                FragmentTags.SHOPPING -> changeToShopping()
                FragmentTags.SETTINGS -> changeToSettings()
                FragmentTags.TASKS -> changeToToDo()
                FragmentTags.SLEEP -> changeToSleepReminder()
                FragmentTags.ABOUT -> changeToAbout()
                FragmentTags.BIRTHDAYS -> changeToBirthdays()
            }
        }
        myDialogView.btnSaveChanges.setOnClickListener {
            activeFragmentTag=FragmentTags.EMPTY
            manageNoteConfirm()
            when (gotoFragment) {
                FragmentTags.HOME -> changeToHome()
                FragmentTags.NOTES -> changeToNotes()
                FragmentTags.SHOPPING -> changeToShopping()
                FragmentTags.SETTINGS -> changeToSettings()
                FragmentTags.TASKS -> changeToToDo()
                FragmentTags.SLEEP -> changeToSleepReminder()
                FragmentTags.ABOUT -> changeToAbout()
                FragmentTags.BIRTHDAYS -> changeToBirthdays()
            }
            myAlertDialog?.dismiss()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * Manages onclick listeners for color picker and submit icon used when
         * editing or writing a note
         */
        return when (item.itemId) {
            R.id.item_left -> {
                if (activeFragmentTag == FragmentTags.DAY_VIEW) {
                    changeToCalendar()
                } else if (activeFragmentTag == FragmentTags.CALENDAR) {
                    changeToDayView()
                } else if (activeFragmentTag == FragmentTags.TASKS) {
                    TodoFr.myFragment.manageCheckedTaskDeletion()
                    updateUndoTaskIcon()
                } else if (activeFragmentTag == FragmentTags.NOTE_EDITOR) {
                    //act as delete button to delete current note
                    openDeleteNoteDialog()
                } else if (activeFragmentTag == FragmentTags.NOTES) {
                    NoteFr.noteListInstance.addFullNote(NoteFr.deletedNote!!)
                    NoteFr.deletedNote = null
                    NoteFr.noteAdapter.notifyItemInserted(0)
                    updateUndoNoteIcon()
                } else if (activeFragmentTag == FragmentTags.BIRTHDAYS) {
                    BirthdayFragment.birthdayListInstance.addFullBirthday(
                        BirthdayFragment.deletedBirthday!!
                    )
                    BirthdayFragment.deletedBirthday = null
                    updateUndoBirthdayIcon()
                    BirthdayFragment.myAdapter.notifyDataSetChanged()
                }
                true
            }

            R.id.item_middle -> {
                when (activeFragmentTag) {
                    FragmentTags.NOTE_EDITOR -> {
                        //open color chooser to change color of current note
                        openColorChooser()
                        true
                    }
                    FragmentTags.TASKS -> {
                        if (TodoFr.deletedTaskList.size > 0) {
                            TodoFr.deletedTaskList.forEach { task ->
                                val newPos = TodoFr.todoListInstance.addFullTask(task)
                                TodoFr.myAdapter.notifyItemInserted(newPos)
                            }
                            TodoFr.deletedTaskList.clear()
                        } else {
                            val newPos = TodoFr.todoListInstance.addFullTask(TodoFr.deletedTask!!)
                            TodoFr.deletedTask = null
                            TodoFr.myAdapter.notifyItemInserted(newPos)
                        }
                        updateUndoTaskIcon()
                        updateDeleteTaskIcon()
                        true
                    }
                    else -> {
                        true
                    }
                }
            }
            R.id.item_right -> {
                if (activeFragmentTag == FragmentTags.NOTE_EDITOR) {
                    //act as check mark to add / confirm note edit
                    manageNoteConfirm()
                    activeFragmentTag=FragmentTags.EMPTY
                    when(previousFragmentTag==FragmentTags.NOTES){
                        true -> changeToNotes()
                        else -> changeToHome()
                    }

                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        searchView = menu!!.getItem(3).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo fix this
                //close keyboard?
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (BirthdayFragment.searching) {
                    BirthdayFragment.myFragment.search(newText.toString())
                }
                return true
            }
        }
        searchView.setOnQueryTextListener(textListener)
        val onCloseListener = SearchView.OnCloseListener {
            actionbarContent.tvActionbarTitle.text = "Birthdays"
            searchView.onActionViewCollapsed()
            BirthdayFragment.searching = false
            updateUndoBirthdayIcon()
            BirthdayFragment.myAdapter.notifyDataSetChanged()
            true
        }
        searchView.setOnCloseListener(onCloseListener)

        searchView.setOnSearchClickListener {
            actionbarContent.tvActionbarTitle.text = ""
            BirthdayFragment.searching = true
            BirthdayFragment.adjustedList.clear()
            myMenu?.getItem(0)?.isVisible = false
            BirthdayFragment.myAdapter.notifyDataSetChanged()
        }

        myMenu = menu
        activeFragmentTag = FragmentTags.EMPTY

        /**
         * Checks intent for passed String-Value, indicating required switching into fragment
         * that isn't the home fragment
         */

        when (intent.extras?.get("NotificationEntry").toString()) {
            "birthdays" -> changeToBirthdays()
            "SReminder" -> changeToHome()
            "settings" -> changeToSettings()
            else -> changeToHome()
        }
        return true
    }

    private fun openDeleteNoteDialog() {
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete_note, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = "Swipe right to delete this note"
        myBuilder.setCustomTitle(editTitle)
        val myAlertDialog = myBuilder.create()

        val btnCancelNew = myDialogView.btnCancelNew
        val btnDeleteNote = myDialogView.btnDelete
        val mySeekbar = myDialogView.sbDeleteNote

        var allowDelete = false

        mySeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // TODO Auto-generated method stub
            }

            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (progress == 100) {
                    allowDelete = true
                    btnDeleteNote.setBackgroundResource(R.drawable.round_corner_red)
                    btnDeleteNote.setTextColor(
                        ContextCompat.getColor(
                            act,
                            R.color.colorOnBackGround
                        )
                    )
                } else {
                    if (allowDelete) {
                        allowDelete = false
                        btnDeleteNote.setBackgroundResource(R.drawable.round_corner_gray)
                        btnDeleteNote.setTextColor(ContextCompat.getColor(act, R.color.colorHint))
                    }

                }

            }
        })

        btnDeleteNote.setOnClickListener {
            if (!allowDelete) {
                val animationShake =
                    AnimationUtils.loadAnimation(act, R.anim.shake)
                mySeekbar.startAnimation(animationShake)
                return@setOnClickListener
            }

            NoteFr.noteListInstance.remove(editNoteHolder)
            editNoteHolder = null
            NoteFr.noteListInstance.save()
            hideKeyboard()
            myAlertDialog.dismiss()
            activeFragmentTag = FragmentTags.EMPTY
            changeToNotes()
        }

        btnCancelNew.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }

    private fun loadDefaultSettings() {
        setDefault("noteColumns", "2")
        setDefault("expandOneCategory", false)
        setDefault("collapseCheckedSublists", false)
        setDefault("noteLines", "All")
        setDefault("drawerLeftSide", false)
    }

    private fun setDefault(setting: String, value: Any) {
        if (SettingsManager.getSetting(setting) == null) {
            SettingsManager.addSetting(setting, value)
        }
    }

    private fun preloadAddItemDialog() {

        //initialize shopping list data
        tagList = TagList()
        tagNames = tagList.getTagNames()
        itemTemplateList = ItemTemplateList()
        userItemTemplateList = UserItemTemplateList()
        ShoppingFr.shoppingListInstance = ShoppingList()

        //initialize itemNameList
        itemNameList = ArrayList()

        userItemTemplateList.forEach {
            itemNameList.add(it.n)
        }

        itemTemplateList.forEach {
            if (!itemNameList.contains(it.n)) {
                itemNameList.add(it.n)
            }
        }

        //inflate view for this dialog
        addItemDialogView = LayoutInflater.from(act).inflate(R.layout.dialog_add_item, null)

        //Initialize dialogBuilder and set its title
        val myBuilder = act.let { it1 -> AlertDialog.Builder(it1).setView(addItemDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        customTitle.tvDialogTitle.text = "Add Item"
        myBuilder?.setCustomTitle(customTitle)
        addItemDialog = myBuilder?.create()

        val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        myTitle.tvDialogTitle.text = "Add Item"
        myBuilder?.setCustomTitle(myTitle)

        //initialize autocompleteTextView and spinner for item unit
        val actvItem = addItemDialogView!!.actvItem
        val spItemUnit = addItemDialogView!!.spItemUnit


        //initialize spinner for categories
        val spCategory = addItemDialogView!!.spCategory
        val categoryAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_list_item_1, tagNames
        )

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = addItemDialogView!!.spItemUnit
        val myAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.units)
        )

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter


        //initialize autocompleteTextView and its adapter
        val autoCompleteTv = addItemDialogView!!.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_spinner_dropdown_item, itemNameList
        )

        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                addItemDialogView!!.actvItem.hint = ""
                addItemDialogView!!.actvItem.background.mutate().setColorFilter(
                    resources.getColor(R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                );
                //check for existing user template
                var template = userItemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(tagNames.indexOf(template.c.n))

                    //display correct unit
                    val unitPointPos = resources.getStringArray(R.array.units).indexOf(template.s)
                    spItemUnit.setSelection(unitPointPos)
                    return
                }

                //check for existing item template
                template = itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(tagNames.indexOf(template.c.n))

                    //display correct unit
                    val unitPointPos = resources.getStringArray(R.array.units).indexOf(template.s)
                    spItemUnit.setSelection(unitPointPos)
                } else {
                    spCategory.setSelection(0)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        autoCompleteTv.addTextChangedListener(textWatcher)

        //initialize edit text for item amount string
        val etItemAmount = addItemDialogView!!.etItemAmount
        etItemAmount.setText("1")

        var firstTap = true
        etItemAmount.setOnFocusChangeListener { _, _ ->
            if (firstTap) {
                etItemAmount.setText("")
                firstTap = false
            }
        }

        //Button to Confirm adding Item to list
        addItemDialogView!!.btnAddItemToList.setOnClickListener {
            if (actvItem.text.toString() == "") {
                //animation
                val animationShake =
                    AnimationUtils.loadAnimation(act, R.anim.shake)
                addItemDialogView!!.actvItem.startAnimation(animationShake)
                return@setOnClickListener
            }
            val tagList = TagList()
            val tag = tagList.getTagByName(spCategory.selectedItem as String)
            //check if user template exists
            var template = userItemTemplateList.getTemplateByName(actvItem.text.toString())

            if (template == null) {
                //no user item with this name => check for regular template
                template = itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template == null || tag != template!!.c) {
                    //item unknown, use selected category, add item, and save it to userTemplate list
                    userItemTemplateList.add(
                        ItemTemplate(
                            actvItem.text.toString(), tag,
                            spItemUnit.selectedItem.toString()
                        )
                    )
                    val item = ShoppingItem(
                        actvItem.text.toString(), tag,
                        spItemUnit.selectedItem.toString(),
                        etItemAmount.text.toString(),
                        spItemUnit.selectedItem.toString(),
                        false
                    )
                    ShoppingFr.shoppingListInstance.add(item)
                    if (activeFragmentTag == FragmentTags.SHOPPING) {
                        ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(act, "Item was added!", Toast.LENGTH_SHORT).show()
                    }
                    itemNameList.add(actvItem.text.toString())
                    val autoCompleteTvAdapter = ArrayAdapter<String>(
                        act, android.R.layout.simple_spinner_dropdown_item, itemNameList
                    )
                    autoCompleteTv.setAdapter(autoCompleteTvAdapter)
                    addItemDialog?.dismiss()
                    return@setOnClickListener
                }
            }

            if (tag != template!!.c) {
                //known as user item but with different tag
                userItemTemplateList.removeItem(actvItem.text.toString())
                userItemTemplateList.add(
                    ItemTemplate(
                        actvItem.text.toString(), tag,
                        spItemUnit.selectedItem.toString()
                    )
                )
            }
            //add already known item to list
            val item = ShoppingItem(
                template!!.n,
                tag,
                template!!.s,
                etItemAmount!!.text.toString(),
                spItemUnit.selectedItem.toString(),
                false
            )
            ShoppingFr.shoppingListInstance.add(item)
            if (activeFragmentTag == FragmentTags.SHOPPING) {
                ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(act, "Item was added!", Toast.LENGTH_SHORT).show()
            }

            addItemDialog?.dismiss()
        }

        val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

    fun openAddItemDialog() {
        addItemDialogView!!.actvItem.setText("")
        //show dialog
        addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        addItemDialog?.show()
    }

}

