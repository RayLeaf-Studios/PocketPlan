package com.pocket_plan.j7_003

import SettingsNavigationFr
import android.annotation.SuppressLint

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.calendar.CalendarAppointment
import com.pocket_plan.j7_003.data.calendar.CalenderFr
import com.pocket_plan.j7_003.data.calendar.CreateTermFr
import com.pocket_plan.j7_003.data.calendar.DayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr
import com.pocket_plan.j7_003.data.notelist.Note
import com.pocket_plan.j7_003.data.notelist.NoteColors
import com.pocket_plan.j7_003.data.notelist.NoteFr
import com.pocket_plan.j7_003.data.settings.shoppinglist.CustomItemFr
import com.pocket_plan.j7_003.data.shoppinglist.*
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.todolist.TodoFr
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.settings.*
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsAboutFr
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsBackupFr
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsShoppingFr
import com.pocket_plan.j7_003.data.todolist.Task
import com.pocket_plan.j7_003.data.todolist.TodoFr.Companion.myRecycler
import com.pocket_plan.j7_003.data.todolist.TodoFr.Companion.todoListInstance
import kotlinx.android.synthetic.main.actionbar.view.*
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.dialog_delete_note.view.*
import kotlinx.android.synthetic.main.dialog_discard_note_edit.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.main_panel.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var noteEditorFr: NoteEditorFr

    //contents for shopping list
    private lateinit var tagList: TagList
    private lateinit var tagNames: Array<String?>
    private lateinit var itemTemplateList: ItemTemplateList
    private lateinit var userItemTemplateList: UserItemTemplateList
    private lateinit var itemNameList: ArrayList<String>
    private var addItemDialog: AlertDialog? = null
    private var addItemDialogView: View? = null
    private var dialogOpened = false
    private lateinit var birthdayFr: BirthdayFr

    companion object {
        var previousFragmentTag: FT = FT.EMPTY
        var activeFragmentTag: FT = FT.EMPTY
        lateinit var act: MainActivity
        lateinit var sleepView: View
        lateinit var actionbarContent: View
        lateinit var searchView: SearchView
        var drawerGravity = 0
        var editNoteHolder: Note? = null
        var editTerm: CalendarAppointment? = null
        var myMenu: Menu? = null
        var noteColor: NoteColors = NoteColors.YELLOW
        var fromHome: Boolean = false
        lateinit var bottomNavigation: BottomNavigationView
        lateinit var params: DrawerLayout.LayoutParams
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        //Set a reference to this activity so its accessible in the companion object
        act = this

        preloadAddItemDialog()

        //Initialize Settings Manager and Time api and AlarmHandler
        SettingsManager.init()
        AndroidThreeTen.init(this)
        AlarmHandler.setBirthdayAlarms(context = this)

        //load default values for settings in case none have been set yet
        loadDefaultSettings()
        setContentView(R.layout.main_panel)

        params = drawer_layout.nav_drawer.layoutParams as DrawerLayout.LayoutParams
        //Check if layout should be right-handed or left-handed
        when (SettingsManager.getSetting(SettingId.DRAWER_SIDE)) {
            true -> drawerGravity = Gravity.END
            false -> {
                params.gravity = Gravity.START
                drawerGravity = Gravity.START
            }
        }

        //initialize actionbar content
        actionbarContent = layoutInflater.inflate(R.layout.actionbar, null, false)
        supportActionBar?.title = ""
        supportActionBar?.customView = actionbarContent
        supportActionBar?.setDisplayShowCustomEnabled(true)

        //initialize navigation drawer
        nav_drawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToFragment(FT.SETTINGS)
                R.id.menuItemBirthdays -> changeToFragment(FT.BIRTHDAYS)
                R.id.menuSleepReminder -> changeToFragment(FT.SLEEP)
            }
            drawer_layout.closeDrawer(drawerGravity)
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
                R.id.notes -> changeToFragment(FT.NOTES)
                R.id.todolist -> changeToFragment(FT.TASKS)
                R.id.home -> changeToFragment(FT.HOME)
                R.id.shopping -> changeToFragment(FT.SHOPPING)
                R.id.menu -> drawer_layout.openDrawer(drawerGravity)
            }
            true
        }

        //inflate sleepView for faster loading time
        sleepView = layoutInflater.inflate(R.layout.fragment_sleep, null, false)

        btnAdd.setOnClickListener {
            when (activeFragmentTag) {
                FT.BIRTHDAYS -> {
                    BirthdayFr.editBirthdayHolder = null
                    birthdayFr.openAddBirthdayDialog()
                }

                FT.TASKS -> {
                    //inflate the dialog with custom view
                    val myDialogView =
                        LayoutInflater.from(act).inflate(R.layout.dialog_add_task, null)

                    //AlertDialogBuilder
                    val myBuilder =
                        act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
                    myBuilder?.setCustomTitle(
                        layoutInflater.inflate(
                            R.layout.title_dialog_add_task,
                            null
                        )
                    )

                    //show dialog
                    val myAlertDialog = myBuilder?.create()
                    myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                    myAlertDialog?.show()

                    //adds listeners to confirmButtons in addTaskDialog
                    val taskConfirmButtons = arrayListOf<Button>(
                        myDialogView.btnConfirm1,
                        myDialogView.btnConfirm2,
                        myDialogView.btnConfirm3
                    )

                    taskConfirmButtons.forEachIndexed { index, button ->
                        button.setOnClickListener {
                            val title = myDialogView.etxTitleAddTask.text.toString()
                            if (title.isEmpty()) {
                                val animationShake =
                                    AnimationUtils.loadAnimation(this, R.anim.shake)
                                myDialogView.etxTitleAddTask.startAnimation(animationShake)
                                @Suppress("LABEL_NAME_CLASH")
                                return@setOnClickListener
                            } else {
                                val newPos =
                                    todoListInstance.addFullTask(
                                        Task(
                                            title,
                                            index + 1,
                                            false
                                        )
                                    )
                                if (newPos == todoListInstance.size - 1) {
                                    myRecycler.adapter?.notifyDataSetChanged()
                                } else {
                                    myRecycler.adapter?.notifyItemInserted(newPos)
                                }
                            }
                            myAlertDialog?.dismiss()
                        }
                    }

                    myDialogView.etxTitleAddTask.requestFocus()
                }

                FT.NOTES -> {
                    editNoteHolder = null
                    changeToFragment(FT.NOTE_EDITOR)
                }

                FT.SHOPPING -> {
                    openAddItemDialog()
                }

                else -> {/* no-op */
                }
            }
        }

    }

    /**
     * DEBUG FUNCTIONS
     */

    fun toast(msg: String) {
        Toast.makeText(act, msg, Toast.LENGTH_SHORT).show()
    }

    private fun setNavBarUnchecked() {
        bottomNavigation.menu.setGroupCheckable(0, true, false)
        for (i in 0 until bottomNavigation.menu.size()) {
            bottomNavigation.menu.getItem(i).isChecked = false
        }
        bottomNavigation.menu.setGroupCheckable(0, true, true)
    }


    //change to fragment of specified tag
    fun changeToFragment(fragmentTag: FT) {

        if (activeFragmentTag == fragmentTag) {
            return
        }

        //Check if the currently requested fragment change comes from note editor, if yes
        //check if there are relevant changes to the note, if yes, open the "Keep changes?"
        //dialog and return
        if (activeFragmentTag == FT.NOTE_EDITOR) {
            if (relevantNoteChanges()) {
                dialogDiscardNoteChanges(fragmentTag)
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
        actionbarContent.tvActionbarTitle.text = when (fragmentTag) {
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

        //save the current activeFragmentTag as previousFragmentTag
        previousFragmentTag = activeFragmentTag

        //set the new activeFragment tag
        activeFragmentTag = fragmentTag

        //deselect bottom nav items if current item does not have an icon there
        when (fragmentTag) {
            FT.HOME,
            FT.SHOPPING,
            FT.TASKS,
            FT.NOTES
            -> {/* no-op */
            }
            else -> setNavBarUnchecked()
        }

        //hide all icons
        hideMenuIcons()

        //set all icons to showAsAction if not in Shopping
        if (activeFragmentTag != FT.SHOPPING) {
            for (i in 0 until myMenu!!.size()) {
                myMenu?.getItem(i)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM)
            }
        }

        //set the correct items to visible and set their correct icons, depending on current fragment
        when (activeFragmentTag) {
            FT.NOTE_EDITOR -> {
                if (editNoteHolder != null) {
                    myMenu?.getItem(0)?.isVisible = true
                    myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete)
                }
                myMenu?.getItem(1)?.setIcon(R.drawable.ic_action_colorpicker)
                myMenu?.getItem(2)?.setIcon(R.drawable.ic_check_mark)
                myMenu?.getItem(1)?.isVisible = true
                myMenu?.getItem(2)?.isVisible = true
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
            FT.BIRTHDAYS -> {
                myMenu?.getItem(3)?.isVisible = true
            }
            FT.TASKS -> {
                myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete_sweep)
                updateDeleteTaskIcon()
            }
            FT.CALENDAR -> {
                myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_calendar)
                myMenu?.getItem(0)?.isVisible = true
            }
            FT.DAY_VIEW -> {
                myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_all_terms)
                myMenu?.getItem(0)?.isVisible = true
            }
            FT.SHOPPING -> {
                myMenu?.getItem(0)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                myMenu?.getItem(0)?.isVisible = true
                myMenu?.getItem(1)?.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
                myMenu?.getItem(1)?.isVisible = true
            }
            else -> {/* no-op, activeFragment does not have any icons*/
            }

        }

        //create fragment object
        val fragment = when (fragmentTag) {
            FT.HOME -> HomeFr()
            FT.TASKS -> TodoFr()
            FT.SHOPPING -> ShoppingFr()
            FT.NOTES -> NoteFr()
            FT.NOTE_EDITOR -> {
                noteEditorFr = NoteEditorFr()
                noteEditorFr
            }
            FT.BIRTHDAYS -> {
                birthdayFr = BirthdayFr()
                birthdayFr
            }
            FT.SETTINGS_ABOUT -> SettingsAboutFr()
            FT.SETTINGS_NAVIGATION -> SettingsNavigationFr()
            FT.SETTINGS_NOTES -> SettingsNotesFr()
            FT.SETTINGS_SHOPPING -> SettingsShoppingFr()
            FT.SETTINGS_BACKUP -> SettingsBackupFr()
            FT.SETTINGS -> SettingsMainFr()
            FT.CUSTOM_ITEMS -> CustomItemFr()
            FT.SLEEP -> SleepFr()
            FT.CALENDAR -> CalenderFr()
            FT.CREATE_TERM -> CreateTermFr()
            FT.DAY_VIEW -> DayFr()
            else -> HomeFr()
        }

        //animate fragment change
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
            myMenu!!.getItem(0).isVisible = false
            myMenu!!.getItem(1).isVisible = false
            myMenu!!.getItem(2).isVisible = false
            myMenu!!.getItem(3).isVisible = false
        }
    }

    @SuppressLint("InflateParams")
    private fun openColorChooser() {
        //inflate the dialog with custom view
        val myDialogView = layoutInflater.inflate(R.layout.dialog_choose_color, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = getString(R.string.menuTitleColorChoose)
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
        buttonList.forEachIndexed { i, b ->
            b.setOnClickListener {
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
        if (BirthdayFr.deletedBirthday != null && !BirthdayFr.searching) {
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(0)?.isVisible = true
        } else {
            myMenu?.getItem(0)?.isVisible = false
        }
    }

    fun updateUndoItemIcon() {
        if (ShoppingFr.deletedItem != null) {
            myMenu?.getItem(2)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(2)?.isVisible = true
        } else {
            myMenu?.getItem(2)?.isVisible = false
        }
    }

    fun updateDeleteTaskIcon() {
        val checkedTasks = todoListInstance.filter { t -> t.isChecked }.size
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
        if (previousFragmentTag == FT.HOME) {
            Toast.makeText(act, "Note was added!", Toast.LENGTH_SHORT).show()
        }
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
        if (drawer_layout.isDrawerOpen(nav_drawer)) {
            drawer_layout.closeDrawer(drawerGravity)
            return
        }

        if (activeFragmentTag == FT.NOTE_EDITOR) {
            if (relevantNoteChanges()) {
                dialogDiscardNoteChanges(previousFragmentTag)
                return
            }
        }

        when (previousFragmentTag) {
            FT.HOME -> changeToFragment(FT.HOME)
            FT.NOTES -> changeToFragment(FT.NOTES)
            FT.SHOPPING -> changeToFragment(FT.SHOPPING)
            FT.SETTINGS -> changeToFragment(FT.SETTINGS)
            FT.TASKS -> changeToFragment(FT.TASKS)
            FT.SLEEP -> changeToFragment(FT.SLEEP)
            else -> super.onBackPressed()
        }


    }

    private fun relevantNoteChanges(): Boolean {

        var result = true
        //check if note was edited, return otherwise
        if (editNoteHolder != null && editNoteHolder!!.title == noteEditorFr.etNoteTitle.text.toString() &&
            editNoteHolder!!.content == noteEditorFr.etNoteContent.text.toString() &&
            editNoteHolder!!.color == noteColor
        ) {
            //no relevant note changes if the title, content and color did not get changed
            result = false
        }

        //check if anything was written when adding new note, return otherwise
        if (editNoteHolder == null && noteEditorFr.etNoteTitle.text.toString() == "" &&
            noteEditorFr.etNoteContent.text.toString() == ""
        ) {
            //no relevant note changes if its a new empty note
            result = false
        }
        return result
    }

    @SuppressLint("InflateParams")
    private fun dialogDiscardNoteChanges(gotoFragment: FT) {

        if (dialogOpened) {
            return
        }
        dialogOpened = true

        val myDialogView = LayoutInflater.from(act).inflate(R.layout.dialog_discard_note_edit, null)

        //AlertDialogBuilder
        val myBuilder = act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        customTitle.tvDialogTitle.text = resources.getText(R.string.noteDiscardDialogTitle)
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.show()
        myAlertDialog?.setOnCancelListener {
            setNavBarUnchecked()
            dialogOpened = false
        }

        myDialogView.btnDiscardChanges.setOnClickListener {
            activeFragmentTag = FT.EMPTY
            dialogOpened = false
            myAlertDialog?.dismiss()
            changeToFragment(gotoFragment)
        }
        myDialogView.btnSaveChanges.setOnClickListener {
            activeFragmentTag = FT.EMPTY
            manageNoteConfirm()
            changeToFragment(gotoFragment)
            dialogOpened = false
            myAlertDialog?.dismiss()
        }
    }


    //Reacts to the user clicking an options icon in the actionbar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            //behavior for the item on the left
            R.id.item_left -> {
                //actions for this item depending on the current fragment
                when (activeFragmentTag) {
                    FT.DAY_VIEW -> {
                        //takes the user to the calendar fragment
                        changeToFragment(FT.CALENDAR)
                    }
                    FT.TASKS -> {
                        //delete checked tasks and update the undoTask icon
                        TodoFr.myFragment.manageCheckedTaskDeletion()
                        updateUndoTaskIcon()
                    }
                    FT.NOTE_EDITOR -> {
                        //ote Editor open the Delete note dialog
                        openDeleteNoteDialog()
                    }
                    FT.NOTES -> {
                        //undo the last deletion
                        NoteFr.noteListInstance.addFullNote(NoteFr.deletedNote!!)
                        NoteFr.deletedNote = null
                        NoteFr.noteAdapter.notifyItemInserted(0)
                        updateUndoNoteIcon()
                    }
                    FT.BIRTHDAYS -> {
                        //undo the last deletion
                        BirthdayFr.birthdayListInstance.addFullBirthday(
                            BirthdayFr.deletedBirthday!!
                        )
                        BirthdayFr.deletedBirthday = null
                        updateUndoBirthdayIcon()
                        BirthdayFr.myAdapter.notifyDataSetChanged()
                    }
                    FT.SHOPPING -> {
                        //clear shopping list
                        if (!ShoppingFr.shoppingListInstance.isEmpty()) {
                            dialogShoppingClear()
                        } else {
                            toast("List is already empty!")
                        }
                    }
                    else -> {/* no-op, this icon should not be visible / clickable in this fragment*/
                    }
                }
                true
            }

            R.id.item_middle -> {
                when (activeFragmentTag) {
                    FT.NOTE_EDITOR -> {
                        //open color chooser to change color of current note
                        openColorChooser()
                    }
                    FT.TASKS -> {
                        //undo deletion of last deleted task (or multiple deleted tasks, if
                        //sweep delete button was used
                        if (TodoFr.deletedTaskList.size > 0) {
                            TodoFr.deletedTaskList.forEach { task ->
                                val newPos = todoListInstance.addFullTask(task)
                                TodoFr.myAdapter.notifyItemInserted(newPos)
                            }
                            TodoFr.deletedTaskList.clear()
                        } else {
                            val newPos = todoListInstance.addFullTask(TodoFr.deletedTask!!)
                            TodoFr.deletedTask = null
                            TodoFr.myAdapter.notifyItemInserted(newPos)
                        }
                        updateUndoTaskIcon()
                        updateDeleteTaskIcon()
                    }
                    FT.SHOPPING -> {
                        //uncheck all shopping items
                        if (!ShoppingFr.shoppingListInstance.allItemUnchecked()) {
                            ShoppingFr.shoppingListInstance.uncheckAll()
                            ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
                        } else {
                            toast("Nothing to uncheck!")
                        }
                    }
                    else -> {/* no-op, this item should not be clickable in current fragment */
                    }
                }
                true
            }
            R.id.item_right -> {
                when (activeFragmentTag) {
                    FT.NOTE_EDITOR -> {
                        //act as check mark to add / confirm note edit
                        manageNoteConfirm()
                        activeFragmentTag = FT.EMPTY
                        when (previousFragmentTag == FT.NOTES) {
                            true -> changeToFragment(FT.NOTES)
                            else -> changeToFragment(FT.HOME)
                        }
                    }
                    FT.SHOPPING -> {
                        //undo the last deletion of a shopping item
                        ShoppingFr.shoppingListInstance.add(ShoppingFr.deletedItem!!)
                        ShoppingFr.deletedItem = null
                        ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
                        updateUndoItemIcon()
                    }
                    else -> {/* no-op, this item should not be clickable in current fragment */
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
                if (BirthdayFr.searching) {
                    BirthdayFr.myFragment.search(newText.toString())
                }
                return true
            }
        }
        searchView.setOnQueryTextListener(textListener)
        val onCloseListener = SearchView.OnCloseListener {
            actionbarContent.tvActionbarTitle.text = getString(R.string.menuTitleBirthdays)
            searchView.onActionViewCollapsed()
            BirthdayFr.searching = false
            updateUndoBirthdayIcon()
            BirthdayFr.myAdapter.notifyDataSetChanged()
            true
        }
        searchView.setOnCloseListener(onCloseListener)

        searchView.setOnSearchClickListener {
            actionbarContent.tvActionbarTitle.text = ""
            BirthdayFr.searching = true
            BirthdayFr.adjustedList.clear()
            myMenu?.getItem(0)?.isVisible = false
            BirthdayFr.myAdapter.notifyDataSetChanged()
        }

        myMenu = menu
        activeFragmentTag = FT.EMPTY

        /**
         * Checks intent for passed String-Value, indicating required switching into fragment
         * that isn't the home fragment
         */

        when (intent.extras?.get("NotificationEntry").toString()) {
            "birthdays" -> changeToFragment(FT.BIRTHDAYS)
            "SReminder" -> changeToFragment(FT.HOME)
            "settings" -> changeToFragment(FT.SETTINGS)
            else -> bottomNavigation.selectedItemId = R.id.home
        }
        return true
    }

    @SuppressLint("InflateParams")
    private fun dialogShoppingClear() {
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete_note, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = getString(R.string.noteDeleteDialogText)
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
            ShoppingFr.shoppingListInstance.clear()
            ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
            myAlertDialog.dismiss()
        }

        btnCancelNew.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }

    @SuppressLint("InflateParams")
    private fun openDeleteNoteDialog() {
        val myDialogView = layoutInflater.inflate(R.layout.dialog_delete_note, null)

        //AlertDialogBuilder
        val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
        val editTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        editTitle.tvDialogTitle.text = resources.getText(R.string.noteDeleteDialogText)
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
            activeFragmentTag = FT.EMPTY
            changeToFragment(FT.NOTES)
        }

        btnCancelNew.setOnClickListener {
            myAlertDialog.dismiss()
        }

        //show dialog
        myAlertDialog.show()
    }

    private fun loadDefaultSettings() {
        setDefault(SettingId.NOTE_COLUMNS, "2")
        setDefault(SettingId.NOTE_LINES, -1.0)
        setDefault(SettingId.FONT_SIZE, "18")
        setDefault(SettingId.CLOSE_ITEM_DIALOG, false)
        setDefault(SettingId.EXPAND_ONE_CATEGORY, false)
        setDefault(SettingId.COLLAPSE_CHECKED_SUBLISTS, false)
        setDefault(SettingId.DRAWER_SIDE, true)
    }

    private fun setDefault(setting: SettingId, value: Any) {
        if (SettingsManager.getSetting(setting) == null) {
            SettingsManager.addSetting(setting, value)
        }
    }

    @SuppressLint("InflateParams")
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
        customTitle.tvDialogTitle.text = getString(R.string.shoppingAddItemTitle)
        myBuilder?.setCustomTitle(customTitle)
        addItemDialog = myBuilder?.create()

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
                //check for existing user template
                var template = userItemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(tagNames.indexOf(template.c.name))

                    //display correct unit
                    val unitPointPos = resources.getStringArray(R.array.units).indexOf(template.s)
                    spItemUnit.setSelection(unitPointPos)
                    return
                }

                //check for existing item template
                template = itemTemplateList.getTemplateByName(actvItem.text.toString())
                if (template != null) {
                    //display correct category
                    spCategory.setSelection(tagNames.indexOf(template.c.name))

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

        addItemDialogView!!.btnCancelItem.setOnClickListener {
            addItemDialog?.dismiss()
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
                    if (activeFragmentTag == FT.SHOPPING) {
                        ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(act, "Item was added!", Toast.LENGTH_SHORT).show()
                    }
                    itemNameList.add(actvItem.text.toString())
                    val autoCompleteTvAdapter2 = ArrayAdapter<String>(
                        act, android.R.layout.simple_spinner_dropdown_item, itemNameList
                    )
                    autoCompleteTv.setAdapter(autoCompleteTvAdapter2)
                    actvItem.setText("")
                    if (activeFragmentTag == FT.HOME || SettingsManager.getSetting(SettingId.CLOSE_ITEM_DIALOG) as Boolean) {
                        addItemDialog?.dismiss()
                    }
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
            if (activeFragmentTag == FT.SHOPPING) {
                ShoppingFr.shoppingListAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(act, "Item was added!", Toast.LENGTH_SHORT).show()
            }
            actvItem.setText("")
            if (activeFragmentTag == FT.HOME) {
                addItemDialog?.dismiss()
            }
        }

        val imm = act.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

    fun openAddItemDialog() {
        addItemDialogView!!.actvItem.setText("")
        addItemDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        addItemDialog?.show()
    }

}

