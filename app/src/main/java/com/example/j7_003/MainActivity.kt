package com.example.j7_003

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.NoteColors
import com.example.j7_003.data.database.database_objects.CalendarAppointment
import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.fragments.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import kotlin.random.Random

class MainActivity : AppCompatActivity(){
    private lateinit var homeFragment: HomeFragment
    private lateinit var dayFragment: DayFragment
    private lateinit var calendarFragment: CalenderFragment
    private lateinit var birthdayFragment: BirthdayFragment
    private lateinit var settingsFragment: SettingsFragment
    private lateinit var todoFragment: TodoFragment
    private lateinit var modulesFragment: ModulesFragment
    private lateinit var sleepFragment: SleepFragment
    private lateinit var noteFragment: NoteFragment
    private lateinit var shoppingFragment: ShoppingFragment
    private lateinit var createNoteFragment: CreateNoteFragment
    private lateinit var createTermFragment: CreateTermFragment
    private lateinit var aboutFragment: AboutFragment
    private lateinit var addItemFragment: AddItemFragment
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var customItemFragment: CustomItemFragment


    private var activeFragmentTag = ""
    private var previousFragmentTag = ""

    companion object {
        lateinit var act: MainActivity
        lateinit var sleepView: View
        var editNoteHolder: NoteAdapter.NoteViewHolder? = null
        var editTerm: CalendarAppointment? = null
        var myMenu: Menu? = null
        var noteColor: NoteColors = NoteColors.YELLOW
        var fromHome: Boolean = false
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)
        act = this

        //inflate sleepView for faster loading time
        sleepView = layoutInflater.inflate(R.layout.fragment_sleep, null, false)

        //initialization of time-API, Database and SettingsManager
        AndroidThreeTen.init(this)
        Database.init()
        SettingsManager.init()


        //load default values for settings in case none have been set yet
        loadDefaultSettings()
        val rowview = layoutInflater.inflate(R.layout.actionbar, null, false)
        supportActionBar?.setCustomView(rowview)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        //initialize bottomNavigation
        bottomNavigation = findViewById(R.id.btm_nav)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when(item.itemId){
                R.id.notes -> changeToNotes()
                R.id.todolist -> changeToToDo()
                R.id.home -> changeToHome()
                R.id.shopping -> changeToShopping()
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

    /**
     * DEBUG FUNCTIONS
     */

    fun titleDebug(debugMsg: String){
        supportActionBar?.title = debugMsg
    }


    fun sadToast(msg: String){
        Toast.makeText(act, msg +" :(", Toast.LENGTH_LONG).show()
    }

    /**
     * CHANGE FRAGMENT METHODS
     */

    fun changeToBirthdays(){
        if(activeFragmentTag!="birthdays") {
            hideMenuIcons()
            birthdayFragment = BirthdayFragment()
            changeToFragment(birthdayFragment, "birthdays", "Birthdays", -1)

        }
    }

    fun changeToShopping(){
        if(activeFragmentTag!="shopping") {
            hideMenuIcons()
            shoppingFragment = ShoppingFragment()
            changeToFragment(shoppingFragment, "shopping", "Shopping", R.id.shopping)
        }
    }

    fun changeToAddItem(){
        if(activeFragmentTag!="addItem") {
            hideMenuIcons()
            addItemFragment = AddItemFragment()
            changeToFragment(addItemFragment, "addItem", "Add Item", -1)
        }
    }

    fun changeToCustomItems(){
        if(activeFragmentTag!="customItems") {
            hideMenuIcons()
            customItemFragment = CustomItemFragment()
            changeToFragment(customItemFragment, "customItems", "Custom Items", -1)
        }
    }

     fun changeToToDo(){
        if(activeFragmentTag!="todo") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_delete_sweep)
            updateDeleteTaskIcon()
            todoFragment = TodoFragment()
            changeToFragment(todoFragment, "todo", "To-Do", R.id.todolist)
        }
    }

    fun changeToHome(){
        hideMenuIcons()
        if(activeFragmentTag!="home"){
            homeFragment = HomeFragment()
            changeToFragment(homeFragment, "home", "", R.id.home)
        }
        supportActionBar?.setDisplayShowCustomEnabled(true)
    }

    fun changeToCreateTerm(){
        if(activeFragmentTag!="createTerm") {
            hideMenuIcons()
            createTermFragment = CreateTermFragment()
            changeToFragment(createTermFragment, "createTerm", "Create Appointment", R.id.modules)
        }
    }

    private fun changeToModules(){
        if(activeFragmentTag!="modules") {
            hideMenuIcons()
            modulesFragment = ModulesFragment()
            changeToFragment(modulesFragment, "modules", "Menu", R.id.modules)
        }
    }

    fun changeToSettings(){
        if(activeFragmentTag!="settings") {
            hideMenuIcons()
            settingsFragment = SettingsFragment()
            changeToFragment(settingsFragment, "settings",
                "Settings", -1)
        }
    }

    fun changeToSleepReminder(){
        if(activeFragmentTag!="sleep") {
            hideMenuIcons()
            sleepFragment = SleepFragment()
            changeToFragment(sleepFragment, "sleep",
                "Sleep-Reminder", -1)
        }
    }

    private fun changeToNotes(){
        if(activeFragmentTag!="notes") {
            hideMenuIcons()
            noteFragment = NoteFragment()
            changeToFragment(noteFragment, "notes",
                "Notes", R.id.notes)
        }
    }

    fun changeToCreateNoteFragment(){
        if(activeFragmentTag!="createNote") {
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_colorpicker)
            myMenu?.getItem(1)?.setIcon(R.drawable.ic_check_mark)
            myMenu?.getItem(0)?.isVisible = true
            myMenu?.getItem(1)?.isVisible = true

            createNoteFragment = CreateNoteFragment()
            changeToFragment(createNoteFragment, "createNote",
                "Editor", -1)

            //initialize button with color of note that is currently being edited
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

    fun changeToAbout(){
        if(activeFragmentTag!="about") {
            hideMenuIcons()
            aboutFragment = AboutFragment()
            changeToFragment(aboutFragment, "about", "About", -1)
        }
    }

    fun changeToDayView(){
        if(activeFragmentTag!="dayView") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_all_terms)
            myMenu?.getItem(0)?.isVisible = true
            dayFragment = DayFragment()
            changeToFragment(dayFragment, "dayView", "Day-View", R.id.modules)
        }
    }

    private fun changeToCalendar(){
        if(activeFragmentTag!="calendar") {
            hideMenuIcons()
            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_calendar)
            myMenu?.getItem(0)?.isVisible = true
            calendarFragment = CalenderFragment()
            changeToFragment(calendarFragment, "calendar", "Calendar", R.id.modules)
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

    private fun changeToFragment(fragment: Fragment, activeFragmentTag: String, actionBarTitle: String, bottomNavigationId: Int){
        supportActionBar?.setDisplayShowCustomEnabled(false)
        previousFragmentTag = this.activeFragmentTag
        this.activeFragmentTag = activeFragmentTag
        supportActionBar?.title = actionBarTitle
        if(bottomNavigationId!=-1){
            bottomNavigation.selectedItemId = bottomNavigationId
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

    private fun hideMenuIcons(){
        if(myMenu!=null){
            myMenu!!.getItem(0).setVisible(false)
            myMenu!!.getItem(1).setVisible(false)
        }
    }

    private fun openColorChooser(){
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

    fun updateUndoTaskIcon(){
        if(TodoFragment.deletedTask!=null || TodoFragment.deletedTaskList.size > 0){
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
    fun updateUndoItemIcon(){
        //TODO uncomment this
//        if(ShoppingFragment.deletedItem!=null){
//            myMenu?.getItem(0)?.setIcon(R.drawable.ic_action_undo)
//            myMenu?.getItem(0)?.isVisible = true
//        }else{
//            myMenu?.getItem(0)?.isVisible = false
//        }
    }

    fun updateDeleteTaskIcon(){
        val checkedTasks = Database.taskList.filter{ t -> t.isChecked}.size
        myMenu?.getItem(0)?.isVisible = checkedTasks > 0
    }


    /**
     * DATA MANAGEMENT FUNCTIONS
     */

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

    /**
     * OVERRIDE FUNCTIONS
     */

    override fun onBackPressed() {

        Log.e("error", activeFragmentTag+" "+previousFragmentTag)
        when (previousFragmentTag) {
            "home" -> {
                changeToHome()
            }
            "notes" -> {
                changeToNotes()
            }
            "shopping" -> {
                changeToShopping()
            }
            "settings" ->{
                changeToSettings()
            }
            else -> {
                super.onBackPressed()
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
                    updateUndoTaskIcon()
                }else if(activeFragmentTag=="createNote"){
                    openColorChooser()
                }else if(activeFragmentTag=="notes"){
                    Database.addFullNote(NoteFragment.deletedNote!!)
                    NoteFragment.deletedNote = null
                    NoteFragment.noteAdapter.notifyItemInserted(0)
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
                    if(TodoFragment.deletedTaskList.size>0){
                        TodoFragment.deletedTaskList.forEach {
                            task ->
                            val newPos = Database.addFullTask(task)
                            TodoFragment.myAdapter.notifyItemInserted(newPos)
                        }
                        TodoFragment.deletedTaskList.clear()
                    }else{
                        val newPos = Database.addFullTask(TodoFragment.deletedTask!!)
                        TodoFragment.deletedTask = null
                        TodoFragment.myAdapter.notifyItemInserted(newPos)
                    }
                    updateUndoTaskIcon()
                    updateDeleteTaskIcon()
                    true
                }else{
                    true
                }
            }
            else -> super.onOptionsItemSelected(item)
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

    private fun loadDefaultSettings(){
        if(SettingsManager.getSetting("noteColumns")==null){
            SettingsManager.addSetting("noteColumns", "2")
        }
        if(SettingsManager.getSetting("noteLines")==null){
            SettingsManager.addSetting("noteLines", "All")
        }
        if(SettingsManager.getSetting("expandOneCategory")==null){
            SettingsManager.addSetting("expandOneCategory", false)
        }
    }

}

