package com.example.j7_003

import android.os.Bundle
import android.provider.ContactsContract
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.NoteColors
import com.example.j7_003.data.database.database_objects.Birthday
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

class MainActivity : AppCompatActivity(){
    private lateinit var homeFragment: HomeFragment
    private lateinit var dayFragment: DayFragment
    private lateinit var calenderFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var todoFragment: TodoFragment
    private lateinit var modulesFragment: ModulesFragment
    private lateinit var sleepFragment: SleepFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var shoppingFragment: ShoppingFragment
    private lateinit var createNoteFragment: CreateNoteFragment
    private lateinit var createTermFragment: CreateTermFragment


    private lateinit var bottomNavigation: BottomNavigationView

    private var activeFragmentTag = ""

    companion object {
        lateinit var myActivity: MainActivity
        lateinit var sleepView: View
        var editNoteHolder: NoteAdapter.NoteViewHolder? = null
        var editTerm: CalendarAppointment? = null
        var myMenu: Menu? = null
        var noteColor: NoteColors = NoteColors.YELLOW
        var fromHome: Boolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        sleepView = layoutInflater.inflate(R.layout.fragment_sleep, null, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)
        myActivity = this
        //initializes the time api
        AndroidThreeTen.init(this)
        Database.init()
        SettingsManager.init()
        myMenu = null

        bottomNavigation = findViewById(R.id.btm_nav)

        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.notes -> changeToNotes()
                R.id.todolist -> changeToToDo()
                R.id.home -> changeToHome()
                R.id.calendar -> changeToDayView()
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
            "SReminder" -> TODO("Decide where sleep reminder notification onclick should lead")
            else -> changeToHome()
        }

    }

    fun titleDebug(debugMsg: String){
        supportActionBar?.title = debugMsg
    }

    fun changeToBirthdays(){
        if(activeFragmentTag!="birthdays") {
            hideMenuIcons()
            birthdayFragment = BirthdayFragment()
            bottomNavigation.selectedItemId = R.id.modules
            supportActionBar?.title = "Birthdays"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, birthdayFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="birthdays"

        }
    }

    fun changeToShopping(){
        if(activeFragmentTag!="shopping") {
            hideMenuIcons()
            shoppingFragment = ShoppingFragment()
            bottomNavigation.selectedItemId = R.id.modules
            supportActionBar?.title = "Shopping"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, shoppingFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="shopping"
        }
    }

     fun changeToToDo(){
        if(activeFragmentTag!="todo") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete_sweep)
            updateDeleteTaskIcon()
            todoFragment = TodoFragment()
            supportActionBar?.title = "To-Do"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, todoFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="todo"
            bottomNavigation.selectedItemId = R.id.todolist
        }
    }

    fun changeToHome(){
        hideMenuIcons()
        if(activeFragmentTag!="home"){
            homeFragment = HomeFragment()
            supportActionBar?.title = "Pocket-Plan"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, homeFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="home"
            bottomNavigation.selectedItemId=R.id.home
        }
    }
    fun changeToDayView(){
        if(activeFragmentTag!="dayView") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_all_terms)
            myMenu?.getItem(0)?.setVisible(true)
            dayFragment = DayFragment()
            supportActionBar?.title = "Day-View"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, dayFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="dayView"
            bottomNavigation.selectedItemId=R.id.calendar
        }
    }
    fun changeToCalendar(){
        if(activeFragmentTag!="calendar") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_calendar)
            myMenu?.getItem(0)?.setVisible(true)
            calenderFragment = CalenderFragment()
            supportActionBar?.title = "Calendar"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, calenderFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="calendar"
        }
    }
     fun changeToCreateTerm(){
        if(activeFragmentTag!="createTerm") {
            hideMenuIcons()
            createTermFragment = CreateTermFragment()
            supportActionBar?.title = "Create Term"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, createTermFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="createTerm"
        }
    }

    private fun changeToModules(){
        if(activeFragmentTag!="modules") {
            hideMenuIcons()
            modulesFragment = ModulesFragment()
            supportActionBar?.title = "Modules"
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
            hideMenuIcons()
            settingsFragment = SettingsFragment()
            supportActionBar?.title = "Settings"
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
            hideMenuIcons()
            sleepFragment = SleepFragment()
            supportActionBar?.title = "Sleep-Reminder"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, sleepFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="sleep"
        }
    }

     fun changeToNotes(){
        if(activeFragmentTag!="notes") {
            hideMenuIcons()
            noteFragment = NoteFragment()
            supportActionBar?.title = "Notes"
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, noteFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="notes"
            bottomNavigation.selectedItemId=R.id.notes
        }
    }

    fun changeToCreateNoteFragment(){
        myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_colorpicker)
        myMenu?.getItem(0)?.setVisible(true)
        myMenu?.getItem(1)?.setVisible(true)
        if(activeFragmentTag!="createNote") {
            supportActionBar?.title="Editor"
            createNoteFragment = CreateNoteFragment()
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_layout, createNoteFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit()
            activeFragmentTag="createNote"
            /**
             * changing initial color of color choose button
             */
            if(editNoteHolder!=null){
                val btnChooserColor = when(noteColor){
                    NoteColors.RED -> R.color.colorNoteRed
                    NoteColors.YELLOW -> R.color.colorNoteYellow
                    NoteColors.GREEN -> R.color.colorNoteGreen
                    NoteColors.BLUE -> R.color.colorNoteBlue
                    NoteColors.PURPLE -> R.color.colorNotePurple
                }
                myMenu?.getItem(0)?.icon?.setTint(ContextCompat.getColor(this, btnChooserColor))
            }else{
                noteColor = NoteColors.YELLOW
                myMenu?.getItem(0)?.icon?.setTint(ContextCompat.getColor(this, R.color.colorNoteYellow))
            }


        }
    }

    fun hideMenuIcons(){
        if(myMenu!=null){
            myMenu!!.getItem(0).setVisible(false)
            myMenu!!.getItem(1).setVisible(false)
        }
    }

    override fun onBackPressed() {

        when {
            activeFragmentTag=="createNote" -> {
                changeToNotes()
            }
            activeFragmentTag!="home" -> {
                changeToHome()
            }
            else -> {
                super.onBackPressed()
            }
        }

    }

    fun openColorChooser(){
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

        val colorList = arrayOf(R.color.colorNoteRed, R.color.colorNoteYellow,
            R.color.colorNoteGreen, R.color.colorNoteBlue, R.color.colorNotePurple)
        val buttonList = arrayOf(myDialogView.btnRed, myDialogView.btnYellow,
            myDialogView.btnGreen, myDialogView.btnBlue, myDialogView.btnPurple)
        /**
         * Onclick-listeners for every specific color button
         */
        buttonList.forEachIndexed(){ i, b ->
            b.setOnClickListener(){
                noteColor = NoteColors.values()[i]
                myMenu?.getItem(0)?.icon?.setTint(ContextCompat.getColor(this, colorList[i]))
                myAlertDialog.dismiss()
            }
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        /**
         * Manages onclick listeners for color picker and submit icon used when
         * editing or writing a note
         */
        return when(item.itemId){
            R.id.item_left -> {
                if(activeFragmentTag=="dayView"){
                    changeToCalendar()
                }else if(activeFragmentTag=="calendar"){
                    changeToDayView()
                }else if(activeFragmentTag=="todo"){
                    TodoFragment.myFragment.manageCheckedTaskDeletion()
                }else if(activeFragmentTag=="createNote"){
                    openColorChooser()
                }else if(activeFragmentTag=="notes"){
                    Database.addFullNote(NoteFragment.deletedNote!!)
                    NoteFragment.deletedNote = null
                    NoteFragment.myAdapter.notifyItemInserted(0)
                    updateUndoNoteIcon()
                }else if(activeFragmentTag=="birthdays"){
                    //TODO fix insert animation when undo
                    val newPos = Database.addFullBirthday(BirthdayFragment.deletedBirthday!!)
                    BirthdayFragment.deletedBirthday = null
                    updateUndoBirthdayIcon()
                    BirthdayFragment.myAdapter.notifyDataSetChanged()
                }
                true
            }

            R.id.item_right -> {
                if(activeFragmentTag=="createNote"){
                    if(editNoteHolder==null){
                        manageAddNote()
                    }else{
                        manageEditNote()
                    }
                    true
                }else if(activeFragmentTag=="todo"){
                    val newPos = Database.addFullTask(TodoFragment.deletedTask!!)
                    TodoFragment.deletedTask = null
                    TodoFragment.myAdapter.notifyItemInserted(newPos)
                    updateUndoTaskIcon()
                    true
                }else{
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    fun updateUndoTaskIcon(){
        if(TodoFragment.deletedTask!=null){
            myMenu?.getItem(1)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(1)?.isVisible = true
        }else{
            myMenu?.getItem(1)?.isVisible = false
        }
    }

    fun updateUndoNoteIcon(){
        if(NoteFragment.deletedNote!=null){
           myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(0)?.isVisible = true
        }else{
            myMenu?.getItem(0)?.isVisible = false
        }
    }

    fun updateUndoBirthdayIcon(){
        if(BirthdayFragment.deletedBirthday!=null){
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
            myMenu?.getItem(0)?.isVisible = true
        }else{
            myMenu?.getItem(0)?.isVisible = false
        }
    }

    fun updateDeleteTaskIcon(){
        val checkedTasks = Database.taskList.filter{ t -> t.isChecked}.size
        myMenu?.getItem(0)?.isVisible = checkedTasks > 0
    }

    private fun manageEditNote(){
        val noteContent = createNoteFragment.etNoteContent.text.toString()
        val noteTitle = createNoteFragment.etNoteTitle.text.toString()
        Database.editNote(editNoteHolder!!.adapterPosition,noteTitle, noteContent, noteColor)
        editNoteHolder = null
        changeToNotes()
    }

    private fun manageAddNote(){
        val noteContent = createNoteFragment.etNoteContent.text.toString()
        val noteTitle = createNoteFragment.etNoteTitle.text.toString()
        Database.addNote(noteTitle, noteContent, noteColor)
        if(!fromHome){
            changeToNotes()
        }else{
            changeToHome()
            fromHome = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.appbar_menu, menu)
        if (menu != null) {
            myMenu = menu
        }
        changeToHome()
        return true
    }

}

