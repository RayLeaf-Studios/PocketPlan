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
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.pocket_plan.j7_003.data.birthdaylist.BirthdayFr
import com.pocket_plan.j7_003.data.calendar.CalendarAppointment
import com.pocket_plan.j7_003.data.calendar.CalenderFr
import com.pocket_plan.j7_003.data.calendar.CreateTermFr
import com.pocket_plan.j7_003.data.calendar.DayFr
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.notelist.Note
import com.pocket_plan.j7_003.data.notelist.NoteEditorFr
import com.pocket_plan.j7_003.data.notelist.NoteFr
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsMainFr
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.data.settings.SettingsNotesFr
import com.pocket_plan.j7_003.data.settings.shoppinglist.CustomItemFr
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsAboutFr
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsBackupFr
import com.pocket_plan.j7_003.data.settings.sub_fragments.SettingsShoppingFr
import com.pocket_plan.j7_003.data.shoppinglist.*
import com.pocket_plan.j7_003.data.sleepreminder.SleepFr
import com.pocket_plan.j7_003.data.todolist.Task
import com.pocket_plan.j7_003.data.todolist.TodoFr
import com.pocket_plan.j7_003.data.todolist.TodoFr.Companion.myRecycler
import com.pocket_plan.j7_003.data.todolist.TodoFr.Companion.todoListInstance
import com.pocket_plan.j7_003.system_interaction.handler.notifications.AlarmHandler
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_note_editor.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.main_panel.view.*
import kotlinx.android.synthetic.main.new_app_bar.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

class MainActivity : AppCompatActivity() {

    private lateinit var mDrawerToggle: ActionBarDrawerToggle

    //contents for shopping list
    private lateinit var tagList: TagList
    private lateinit var tagNames: Array<String?>
    private lateinit var itemTemplateList: ItemTemplateList
    private lateinit var userItemTemplateList: UserItemTemplateList
    private lateinit var itemNameList: ArrayList<String>
    private var addItemDialog: AlertDialog? = null
    private var addItemDialogView: View? = null
    private lateinit var birthdayFr: BirthdayFr

    companion object {
        lateinit var noteEditorFr: NoteEditorFr
        var previousFragmentTag: FT = FT.EMPTY
        var activeFragmentTag: FT = FT.EMPTY
        lateinit var act: MainActivity
        lateinit var sleepView: View
        lateinit var toolBar: Toolbar
        var drawerGravity = 0
        var editNoteHolder: Note? = null
        var editTerm: CalendarAppointment? = null
        var fromHome: Boolean = false
        lateinit var bottomNavigation: BottomNavigationView
    }

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_panel)

        //Set a reference to this activity so its accessible in the companion object
        act = this

        preloadAddItemDialog()

        //Initialize Settings Manager and Time api and AlarmHandler
        SettingsManager.init()
        AndroidThreeTen.init(this)
        AlarmHandler.setBirthdayAlarms(context = this)

        //load default values for settings in case none have been set yet
        loadDefaultSettings()

        //initialize toolbar
        setSupportActionBar(myNewToolbar)
        toolBar = myNewToolbar
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_action_menu)


        //initialize drawer toggle button
        mDrawerToggle = ActionBarDrawerToggle(this, drawer_layout, R.string.open, R.string.close)
        drawer_layout.addDrawerListener(mDrawerToggle)
        mDrawerToggle.syncState()

        //initialize navigation drawer
        nav_drawer.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menuItemSettings -> changeToFragment(FT.SETTINGS)
//                R.id.menuItemBirthdays -> changeToFragment(FT.BIRTHDAYS)
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
                R.id.bottom1 -> changeToFragment(FT.NOTES)
                R.id.bottom2 -> changeToFragment(FT.TASKS)
                R.id.bottom3 -> changeToFragment(FT.HOME)
                R.id.bottom4 -> changeToFragment(FT.SHOPPING)
                R.id.bottom5 -> changeToFragment(FT.BIRTHDAYS)
            }
            true
        }

        //inflate sleepView for faster loading time
        sleepView = layoutInflater.inflate(R.layout.fragment_sleep, null, false)

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
                    changeToFragment(FT.NOTE_EDITOR)
                }

                FT.SHOPPING -> {
                    openAddItemDialog()
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
                bottomNavigation.menu.getItem(2).isChecked = true
                changeToFragment(FT.HOME)
            }
        }

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

        if (activeFragmentTag == fragmentTag) {
            return
        }

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
     * DATA MANAGEMENT FUNCTIONS
     */


    /**
     * OVERRIDE FUNCTIONS
     */

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(nav_drawer)) {
            drawer_layout.closeDrawer(drawerGravity)
            return
        }

        if (activeFragmentTag == FT.NOTE_EDITOR) {
            if (NoteEditorFr.myFragment.relevantNoteChanges()) {
                NoteEditorFr.myFragment.dialogDiscardNoteChanges(previousFragmentTag)
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



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(mDrawerToggle.onOptionsItemSelected(item)){
           return true
        }
        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("InflateParams")

    private fun loadDefaultSettings() {
        setDefault(SettingId.NOTE_COLUMNS, "2")
        setDefault(SettingId.NOTE_LINES, -1.0)
        setDefault(SettingId.FONT_SIZE, "18")
        setDefault(SettingId.CLOSE_ITEM_DIALOG, false)
        setDefault(SettingId.EXPAND_ONE_CATEGORY, false)
        setDefault(SettingId.COLLAPSE_CHECKED_SUBLISTS, false)
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

