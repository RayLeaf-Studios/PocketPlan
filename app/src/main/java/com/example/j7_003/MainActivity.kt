package com.example.j7_003

import android.content.Context
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.j7_003.data.notelist.NoteColors
import com.example.j7_003.data.about.AboutFragment
import com.example.j7_003.data.birthdaylist.BirthdayFragment
import com.example.j7_003.data.calendar.CalendarAppointment
import com.example.j7_003.data.calendar.CalenderFragment
import com.example.j7_003.data.calendar.CreateTermFragment
import com.example.j7_003.data.calendar.DayFragment
import com.example.j7_003.data.home.HomeFragment
import com.example.j7_003.data.modules.ModulesFragment
import com.example.j7_003.data.notelist.CreateNoteFragment
import com.example.j7_003.data.notelist.NoteAdapter
import com.example.j7_003.data.notelist.NoteFragment
import com.example.j7_003.data.settings.SettingsFragment
import com.example.j7_003.data.settings.SettingsManager
import com.example.j7_003.data.settings.shoppinglist.CustomItemFragment
import com.example.j7_003.data.shoppinglist.*
import com.example.j7_003.data.todolist.TodoFragment
import com.example.j7_003.data.sleepreminder.SleepFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.actionbar.view.*
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.dialog_choose_color.view.*
import kotlinx.android.synthetic.main.fragment_write_note.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

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
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var customItemFragment: CustomItemFragment


    companion object {
        var previousFragmentTag = ""
        var activeFragmentTag = ""
        lateinit var act: MainActivity
        lateinit var sleepView: View
        lateinit var actionbarContent: View
        lateinit var searchView: SearchView
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
        SettingsManager.init()


        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        //initialize actionbar content
        actionbarContent = layoutInflater.inflate(R.layout.actionbar, null, false)
        supportActionBar?.title = ""
        supportActionBar?.customView = actionbarContent
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
            myMenu?.getItem(2)?.isVisible = true
            birthdayFragment = BirthdayFragment()
            changeToFragment(birthdayFragment, "birthdays", "Birthdays", -1)
            searchView.onActionViewCollapsed()

        }
    }

    fun changeToShopping(){
        if(activeFragmentTag!="shopping") {
            hideMenuIcons()
            shoppingFragment = ShoppingFragment()
            changeToFragment(shoppingFragment, "shopping", "Shopping", R.id.shopping)
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
            changeToFragment(homeFragment, "home", "Pocket Plan", R.id.home)
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

    private fun changeToFragment(fragment: Fragment, fragmentTag: String, actionBarTitle: String, bottomNavigationId: Int){

//        actionbarContent.logo.visibility = when(activeFragmentTag=="home"){
//            true -> View.VISIBLE
//            else -> View.GONE
//        }
        actionbarContent.tvActionbarTitle.text = actionBarTitle
        previousFragmentTag = activeFragmentTag
        activeFragmentTag = fragmentTag
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
            myMenu!!.getItem(2).setVisible(false)
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
        if(BirthdayFragment.deletedBirthday!=null&&!BirthdayFragment.searching){
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
        val checkedTasks = TodoFragment.todoListInstance.filter{ t -> t.isChecked}.size
        myMenu?.getItem(0)?.isVisible = checkedTasks > 0
    }


    /**
     * DATA MANAGEMENT FUNCTIONS
     */

    private fun manageEditNote(){
        val noteContent = createNoteFragment.etNoteContent.text.toString()
        val noteTitle = createNoteFragment.etNoteTitle.text.toString()
        NoteFragment.noteListInstance.editNote(editNoteHolder!!.adapterPosition,noteTitle, noteContent, noteColor)
        editNoteHolder = null
        changeToNotes()
    }

    private fun manageAddNote(){
        val noteContent = createNoteFragment.etNoteContent.text.toString()
        val noteTitle = createNoteFragment.etNoteTitle.text.toString()
        NoteFragment.noteListInstance.addNote(noteTitle, noteContent, noteColor)
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
                    NoteFragment.noteListInstance.addFullNote(NoteFragment.deletedNote!!)
                    NoteFragment.deletedNote = null
                    NoteFragment.noteAdapter.notifyItemInserted(0)
                    updateUndoNoteIcon()
                }else if(activeFragmentTag=="birthdays"){
                    //TODO fix insert animation when undo
                    val newPos = BirthdayFragment.birthdayListInstance.addFullBirthday(
                        BirthdayFragment.deletedBirthday!!)
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
                            val newPos = TodoFragment.todoListInstance.addFullTask(task)
                            TodoFragment.myAdapter.notifyItemInserted(newPos)
                        }
                        TodoFragment.deletedTaskList.clear()
                    }else{
                        val newPos = TodoFragment.todoListInstance.addFullTask(TodoFragment.deletedTask!!)
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
        searchView = menu!!.getItem(2).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo fix this
                //close keyboard?
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(BirthdayFragment.searching){
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
        if(SettingsManager.getSetting("collapseCheckedSublists")==null){
            SettingsManager.addSetting("collapseCheckedSublists", false)
        }
    }

    fun openAddItemDialog() {
        val myView = LayoutInflater.from(act).inflate(R.layout.dialog_add_item, null)

        //AlertDialogBuilder
        val myBuilder = act.let { it1 -> AlertDialog.Builder(it1).setView(myView) }
        val customTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        customTitle.tvDialogTitle.text = "Add Item"
        myBuilder?.setCustomTitle(customTitle)

        val myAlertDialog = myBuilder?.create()

        //show dialog
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //initialize autocompleteTextView and spinner for item unit
        val actvItem = myView.actvItem
        val spItemUnit = myView.spItemUnit

        //initialize tagNames and itemTemplateList
        val tagList = TagList()
        val tagNames = tagList.getTagNames()
        val itemTemplateList = ItemTemplateList()
        val userItemTemplateList = UserItemTemplateList()
        ShoppingFragment.shoppingListInstance = ShoppingList()

        val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        myTitle.tvDialogTitle.text = "Add Item"
        myBuilder?.setCustomTitle(myTitle)

        //initialize spinner for categories
        val spCategory = myView.spCategory
        val categoryAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_list_item_1, tagNames
        )

        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCategory.adapter = categoryAdapter

        //Initialize spinner and its adapter to choose its Unit
        val mySpinner = myView.spItemUnit
        val myAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.units)
        )

        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = myAdapter

        //initialize itemNameList
        val itemNameList: ArrayList<String> = ArrayList()

        userItemTemplateList.forEach {
            itemNameList.add(it.n)
        }

        itemTemplateList.forEach {
            if (!itemNameList.contains(it.n)) {
                itemNameList.add(it.n)
            }
        }

        //initialize autocompleteTextView and its adapter
        val autoCompleteTv = myView.actvItem
        val autoCompleteTvAdapter = ArrayAdapter<String>(
            act, android.R.layout.simple_spinner_dropdown_item, itemNameList
        )

        autoCompleteTv.setAdapter(autoCompleteTvAdapter)
        autoCompleteTv.requestFocus()

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                myView.actvItem.hint = ""
                myView.actvItem.background.mutate().setColorFilter(
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
        val etItemAmount = myView.etItemAmount
        etItemAmount.setText("1")

        var firstTap = true
        etItemAmount.setOnFocusChangeListener { _, _ ->
            if (firstTap) {
                etItemAmount.setText("")
                firstTap = false
            }
        }

        //Button to Confirm adding Item to list
        myView.btnAddItemToList.setOnClickListener {
            if (actvItem.text.toString() == "") {
                myView.actvItem.hint = "Enter an item!"
                myView.actvItem.background.mutate().setColorFilter(
                    resources.getColor(R.color.colorGoToSleep),
                    PorterDuff.Mode.SRC_ATOP
                );
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
                    ShoppingFragment.shoppingListInstance.add(item)
                    if(activeFragmentTag=="shopping"){
                        ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()
                    }
                    myAlertDialog?.dismiss()
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
                etItemAmount.text.toString(),
                spItemUnit.selectedItem.toString(),
                false
            )
            ShoppingFragment.shoppingListInstance.add(item)
            ShoppingFragment.shoppingListAdapter.notifyDataSetChanged()
            myAlertDialog?.dismiss()

        }


        val imm = act?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, InputMethodManager.SHOW_FORCED)
    }

}

