package com.pocket_plan.j7_003.data.birthdaylist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_birthday.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
import kotlinx.android.synthetic.main.main_panel.*
import kotlinx.android.synthetic.main.row_birthday.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.math.abs


/**
 * A simple [Fragment] subclass.
 */

class BirthdayFr(mainActivity: MainActivity) : Fragment() {
    private val myActivity = mainActivity

    //initialize recycler view
    lateinit var myRecycler: RecyclerView

    private val darkMode:Boolean = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    //Current date to properly initialize date picker
    private var date: LocalDate = LocalDate.now()

    //reference to birthday options menu
    private lateinit var myMenu: Menu

    //Adapter for recycler view
    lateinit var myAdapter: BirthdayAdapter

    //boolean to signal if a search is currently being performed
    var searching: Boolean = false

    //instance of birthday list, containing all the displayed birthdays
    var birthdayListInstance: BirthdayList = BirthdayList(myActivity)

    //reference to searchView in toolbar
    lateinit var searchView: SearchView


    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        myAdapter = BirthdayAdapter(this, myActivity)
        super.onCreate(savedInstanceState)
    }

    companion object {
        //Holder for deleted birthday to make undo possible
        var deletedBirthday: Birthday? = null

        //Holder for birthday currently being edited
        var editBirthdayHolder: Birthday? = null

        //List containing birthdays that correspond to current search pattern
        lateinit var adjustedList: ArrayList<Birthday>

        //Last used searchPattern
        lateinit var lastQuery: String
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //initialize deletedBirthday with null to signal that nothing can be undone
        deletedBirthday = null

        //inflate menu and save reference to it
        inflater.inflate(R.menu.menu_birthdays, menu)
        myMenu = menu

        //set correct color to undo icon
        myMenu.findItem(R.id.item_birthdays_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

        //set reference to searchView from menu
        searchView = menu.findItem(R.id.item_birthdays_search).actionView as SearchView

        //create textListener, to listen to keyboard input when a birthday search is performed
        val textListener = object : SearchView.OnQueryTextListener {

            //hide keyboard when search is submitted
            override fun onQueryTextSubmit(query: String?): Boolean {
                myActivity.hideKeyboard()
                return true
            }

            //start a new search whenever input text has changed
            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    search(newText.toString())
                }
                return true
            }
        }

        //apply textListener to SearchView
        searchView.setOnQueryTextListener(textListener)

        //set onClose listener, that resets birthdayFragment whenever the searchView gets closed
        searchView.setOnCloseListener {
            myActivity.btnAdd.visibility = View.VISIBLE
            //reset title
            myActivity.toolBar.title = getString(R.string.menuTitleBirthdays)
            //collapse searchView
            searchView.onActionViewCollapsed()
            //signal that no search is being performed
            searching = false
            //reload menu icons
            updateUndoBirthdayIcon()
            //reload list elements by notifying data set change to adapter
            reloadAdapter()
            true
        }

        //set onSearchClickListener that initializes searching
        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            //removes title from toolbar
            myActivity.toolBar.title = ""
            //sets searching to true, which results in the recyclerViewAdapter reading its elements from
            //adjusted list instead of birthdayList
            searching = true
            //clear adjusted list
            adjustedList.clear()
            //reload adapter dataSet
            myAdapter.notifyDataSetChanged()
        }

        //initialize editBirthdayHolder with null to signal that no birthday is being edited
        editBirthdayHolder = null

        //refresh option menus
        updateUndoBirthdayIcon()
        updateBirthdayMenu()

        super.onCreateOptionsMenu(menu, inflater)
    }

    fun reloadAdapter() {
        val newAdapter = BirthdayAdapter(this, myActivity)
        myAdapter = newAdapter
        myRecycler.adapter = newAdapter
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //disable all reminders and refresh adapter
            R.id.item_birthdays_disable_reminders -> {
                birthdayListInstance.disableAllReminders()
                reloadAdapter()
            }

            //enable all reminders and refresh adapter
            R.id.item_birthdays_enable_reminders -> {
                birthdayListInstance.enableAllReminders()
                reloadAdapter()
            }

            R.id.item_birthdays_search -> {/* no-op, listeners for this searchView are set
             in on create options menu*/
            }

            R.id.item_birthdays_undo -> {
                //undo the last deletion
                //re-add previously deleted birthday and get its new position
                val addInfo = birthdayListInstance.addFullBirthday(
                    deletedBirthday!!
                )

                //set deletedBirthday to null no signal that no undo is possible
                deletedBirthday = null

                //update menu content due to new possibilities
                updateUndoBirthdayIcon()
                updateBirthdayMenu()
                reloadAdapter()
                myRecycler.scrollToPosition(addInfo.first)
            }
        }

        return super.onOptionsItemSelected(item)
    }


    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_birthday, container, false)
        myRecycler = myView.recycler_view_birthday

        adjustedList = arrayListOf()

        birthdayListInstance = BirthdayList(myActivity)
        myAdapter = BirthdayAdapter(this, myActivity)


        //collapse all birthdays when reentering fragment
        birthdayListInstance.collapseAll()

        //initialize recyclerview and attach its adapter
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        //initialize and attach swipe helpers
        val swipeHelperLeft =
            ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.LEFT, this))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight =
            ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.RIGHT, this))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }


    /**
     * Refreshes the birthdayList to ensure correct order of birthdays, in case a user leaves
     * the app without closing it (before 00:00) and then re-enters (after 00:00)
     */
    override fun onResume() {
        birthdayListInstance.sortAndSaveBirthdays()
        myAdapter.notifyDataSetChanged()
        super.onResume()
    }

    fun updateBirthdayMenu() {
        myMenu.findItem(R.id.item_birthdays_search).isVisible = birthdayListInstance.size > 0
        myMenu.findItem(R.id.item_birthdays_enable_reminders).isVisible =
            birthdayListInstance.size > 0
        myMenu.findItem(R.id.item_birthdays_disable_reminders).isVisible =
            birthdayListInstance.size > 0
    }

    fun updateUndoBirthdayIcon() {
        if (deletedBirthday != null && !searching) {
            myMenu.findItem(R.id.item_birthdays_undo)?.setIcon(R.drawable.ic_action_undo)
            myMenu.findItem(R.id.item_birthdays_undo)?.isVisible = true
        } else {
            myMenu.findItem(R.id.item_birthdays_undo)?.isVisible = false
        }
    }

    @SuppressLint("InflateParams")
    fun openEditBirthdayDialog() {
        var yearChanged = false

        //set date to birthdays date
        date = LocalDate.of(
            editBirthdayHolder!!.year,
            editBirthdayHolder!!.month,
            editBirthdayHolder!!.day
        )

        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_add_birthday, null)

        //initialize references to ui elements
        val etName = myDialogView.etName
        val etDaysToRemind = myDialogView.etDaysToRemind

        val tvBirthdayDate = myDialogView.tvBirthdayDate
        val tvSaveYear = myDialogView.tvSaveYear
        val tvNotifyMe = myDialogView.tvNotifyMe
        val tvRemindMe = myDialogView.tvRemindMe
        val tvDaysPrior = myDialogView.tvDaysPrior

        val cbSaveBirthdayYear = myDialogView.cbSaveBirthdayYear
        val cbNotifyMe = myDialogView.cbNotifyMe

        /**
         * INITIALIZE DISPLAY VALUES
         */

        //initialize name text field with birthday name
        etName.setText(editBirthdayHolder!!.name)
        etName.setSelection(etName.text.length)

        //initialize color of tvNotifyMe depending on notification status
        if (editBirthdayHolder!!.notify) {
            tvNotifyMe.setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
        } else {
            tvNotifyMe.setTextColor(
                myActivity.colorForAttr(R.attr.colorHint)
            )
        }

        //initialize cbNotifyMe with correct checked state
        cbNotifyMe.isChecked = editBirthdayHolder!!.notify

        //initialize date text
        var yearString = ""
        if (editBirthdayHolder!!.year != 0) {
            yearString = "." + editBirthdayHolder!!.year.toString()
        }
        val dateText = editBirthdayHolder!!.day.toString().padStart(2, '0') + "." +
                editBirthdayHolder!!.month.toString()
                    .padStart(2, '0') + yearString
        tvBirthdayDate.text = dateText

        //initialize color of tvSaveYear
        val hasYear = editBirthdayHolder!!.year != 0
        val tvSaveYearColor = when (hasYear) {
            true -> R.attr.colorOnBackGround
            else -> R.attr.colorHint
        }
        tvSaveYear.setTextColor(myActivity.colorForAttr(tvSaveYearColor))

        //initialize value of save year checkbox
        cbSaveBirthdayYear.isChecked = hasYear

        //set correct color for RemindMe DaysPrior
        val remindMeColor = when (editBirthdayHolder!!.daysToRemind) {
            0 -> R.attr.colorHint
            else -> R.attr.colorOnBackGround
        }
        tvRemindMe.setTextColor(myActivity.colorForAttr(remindMeColor))
        etDaysToRemind.setTextColor(myActivity.colorForAttr(remindMeColor))
        tvDaysPrior.setTextColor(myActivity.colorForAttr(remindMeColor))

        //set correct text to tvDaysPrior
        val daysToRemind = when (etDaysToRemind.text.toString() == "") {
            true -> 0
            else -> etDaysToRemind.text.toString().toInt()
        }
        val daysPriorTextEdit =
            myActivity.resources.getQuantityText(R.plurals.day, daysToRemind)
                .toString() + " " + myActivity.resources.getString(R.string.birthdaysDaysPrior)
        tvDaysPrior.text = daysPriorTextEdit

        //set the correct daysToRemind text
        var cachedRemindText = editBirthdayHolder!!.daysToRemind.toString()
        etDaysToRemind.setText(cachedRemindText)


        //correct focusable state of etDaysToRemind
        if (editBirthdayHolder!!.notify) {
            etDaysToRemind.isFocusableInTouchMode = true
        } else {
            etDaysToRemind.isFocusable = false
        }

        //initialize button text
        myDialogView.btnConfirmBirthday.text = resources.getText(R.string.birthdayDialogEdit)

        /**
         * INITIALIZE LISTENERS
         */

        //listener for cbNotifyMe to change color of tvNotifyMe depending on checked state
        cbNotifyMe.setOnClickListener {
            tvNotifyMe.setTextColor(
                if (cbNotifyMe.isChecked) {
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                } else {
                    myActivity.colorForAttr(R.attr.colorHint)
                }
            )
            when (cbNotifyMe.isChecked) {
                true -> etDaysToRemind.isFocusableInTouchMode = true
                else -> etDaysToRemind.isFocusable = false
            }
            when (cbNotifyMe.isChecked) {
                true -> etDaysToRemind.setText(cachedRemindText)
                else -> {
                    val temp = etDaysToRemind.text.toString()
                    etDaysToRemind.setText("0")
                    cachedRemindText = temp
                }
            }
        }

        //onclick listener for tvBirthday
        tvBirthdayDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, pickedYear, month, day ->
                yearChanged = true

                if (abs(
                        date.withYear(LocalDate.now().year).until(date.withYear(pickedYear)).toTotalMonths()
                    ) >= 12 && !cbSaveBirthdayYear.isChecked
                ) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                    )
                }

                date = date.withYear(pickedYear).withMonth(month + 1).withDayOfMonth(day)

                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }

            var yearToDisplay = if (cbSaveBirthdayYear.isChecked) {
                date.year
            } else {
                LocalDate.now().year
            }

            if (cbSaveBirthdayYear.isChecked && yearChanged) {
                yearToDisplay = date.year
            }

            val dpd = when (darkMode) {
                true ->
                    DatePickerDialog(
                        myActivity,
                        R.style.MyDatePickerStyle,
                        dateSetListener,
                        yearToDisplay,
                        date.monthValue - 1,
                        date.dayOfMonth
                    )
                else ->
                    DatePickerDialog(
                        myActivity,
                        R.style.DialogTheme,
                        dateSetListener,
                        yearToDisplay,
                        date.monthValue - 1,
                        date.dayOfMonth
                    )
            }
            dpd.show()
            dpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
            dpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
        }

        //checkbox to include year
        cbSaveBirthdayYear.setOnClickListener {
            if (!yearChanged) {
                date = date.withYear(LocalDate.now().year)
                yearChanged = true
            }
            //set correct text of tvBirthdayDate (add / remove year)
            val dayMonthString =
                date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                    .padStart(2, '0')

            tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                false -> dayMonthString
                else -> dayMonthString + "." + date.year.toString()
            }


            //color tvSaveYear gray or white depending on checkedState
            val colorTvSaveYear = when (cbSaveBirthdayYear.isChecked) {
                true -> R.attr.colorOnBackGround
                false -> R.attr.colorHint
            }
            tvSaveYear.setTextColor(myActivity.colorForAttr(colorTvSaveYear))
        }


        val textWatcherReminder = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cachedRemindText = etDaysToRemind.text.toString()
                val color = if (cachedRemindText == "" || cachedRemindText.toInt() == 0) {
                    myActivity.colorForAttr(R.attr.colorHint)
                } else {
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                }
                tvRemindMe.setTextColor(color)
                etDaysToRemind.setTextColor(color)
                tvDaysPrior.setTextColor(color)
                val daysToRemindTc = when (etDaysToRemind.text.toString() == "") {
                    true -> 0
                    else -> etDaysToRemind.text.toString().toInt()
                }

                val daysPriorTextEditTc =
                    myActivity.resources.getQuantityText(R.plurals.day, daysToRemindTc)
                        .toString() + " " + myActivity.resources.getString(R.string.birthdaysDaysPrior)
                tvDaysPrior.text = daysPriorTextEditTc

            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        //attach a listener that adjusts the color and text of surrounding text views
        etDaysToRemind.addTextChangedListener(textWatcherReminder)

        //clear text in etDaysToRemind when it is clicked on
        etDaysToRemind.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDaysToRemind.setText("")
            } else {
                if (etDaysToRemind.text.toString() == "") {
                    etDaysToRemind.setText("0")
                }
            }
        }

        etDaysToRemind.setOnClickListener {
            if (!cbNotifyMe.isChecked) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                cbNotifyMe.startAnimation(animationShakeCb)
            }
        }


        //AlertDialogBuilder
        val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val myTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        myTitle.tvDialogTitle.text = resources.getText(R.string.birthdayDialogEditTitle)
        myBuilder?.setCustomTitle(myTitle)


        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //button to confirm edit of birthday
        myDialogView.btnConfirmBirthday.setOnClickListener {
            val name = etName.text.toString()

            //tell user to enter a name if none is entered
            if (name == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                etName.startAnimation(animationShake)
                return@setOnClickListener
            }

            //get day and month from date
            val month = date.monthValue
            val day = date.dayOfMonth
            val year = when (cbSaveBirthdayYear.isChecked) {
                true -> date.year
                else -> 0
            }

            //get value of daysToRemind, set it to 0 if the text field is empty
            val daysToRemindConfirm = when (etDaysToRemind.text.toString()) {
                "" -> 0
                else -> etDaysToRemind.text.toString().toInt()
            }
            val notifyMe = cbNotifyMe.isChecked

            editBirthdayHolder!!.name = name
            editBirthdayHolder!!.day = day
            editBirthdayHolder!!.month = month
            editBirthdayHolder!!.year = year
            editBirthdayHolder!!.daysToRemind = daysToRemindConfirm
            editBirthdayHolder!!.notify = notifyMe

            birthdayListInstance.sortAndSaveBirthdays()
            myRecycler.adapter?.notifyDataSetChanged()
            myAlertDialog?.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    fun openAddBirthdayDialog() {
        var yearChanged = false
        var chosenYear = LocalDate.now().year

        //set date to today
        date = LocalDate.now()

        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_add_birthday, null)

        //initialize references to ui elements
        val etName = myDialogView.etName
        val etDaysToRemind = myDialogView.etDaysToRemind

        val tvBirthdayDate = myDialogView.tvBirthdayDate
        val tvSaveYear = myDialogView.tvSaveYear
        val tvNotifyMe = myDialogView.tvNotifyMe
        val tvRemindMe = myDialogView.tvRemindMe
        val tvDaysPrior = myDialogView.tvDaysPrior

        val cbSaveBirthdayYear = myDialogView.cbSaveBirthdayYear
        val cbNotifyMe = myDialogView.cbNotifyMe

        /**
         * INITIALIZE VALUES
         */

        val initPriorText = myActivity.resources.getQuantityString(
            R.plurals.day,
            0
        ) + " " + myActivity.resources.getString(
            R.string.birthdaysDaysPrior
        )
        tvDaysPrior.text = initPriorText

        /**
         * INITIALIZE LISTENERS
         */

        var cachedRemindText = "0"
        //listener for cbNotifyMe to change color of tvNotifyMe depending on checked state
        cbNotifyMe.setOnClickListener {
            tvNotifyMe.setTextColor(
                if (cbNotifyMe.isChecked) {
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                } else {
                    myActivity.colorForAttr(R.attr.colorHint)
                }
            )
            when (cbNotifyMe.isChecked) {
                true -> etDaysToRemind.isFocusableInTouchMode = true
                else -> etDaysToRemind.isFocusable = false
            }
            when (cbNotifyMe.isChecked) {
                true -> etDaysToRemind.setText(cachedRemindText)
                else -> {
                    val temp = etDaysToRemind.text.toString()
                    etDaysToRemind.setText("0")
                    cachedRemindText = temp
                }
            }
        }

        //onclick listener for tvBirthday
        tvBirthdayDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, pickedYear, month, day ->
                yearChanged = true
                chosenYear = pickedYear

                date = date.withYear(pickedYear).withMonth(month + 1).withDayOfMonth(day)

                if (abs(
                        LocalDate.now().until(date).toTotalMonths()
                    ) >= 12 && !cbSaveBirthdayYear.isChecked
                ) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                    )
                }

                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }

            var yearToDisplay = LocalDate.now().year
            if (cbSaveBirthdayYear.isChecked && yearChanged) {
                yearToDisplay = date.year
            } else if (cbSaveBirthdayYear.isChecked) {
                yearToDisplay = LocalDate.now().year
            }
            val dpd = when (darkMode) {
                true ->
                    DatePickerDialog(
                        myActivity,
                        R.style.MyDatePickerStyle,
                        dateSetListener,
                        yearToDisplay,
                        date.monthValue - 1,
                        date.dayOfMonth
                    )
                else ->
                    DatePickerDialog(
                        myActivity,
                        R.style.DialogTheme,
                        dateSetListener,
                        yearToDisplay,
                        date.monthValue - 1,
                        date.dayOfMonth
                    )
            }
            dpd.show()
        }

        //checkbox to include year
        cbSaveBirthdayYear.setOnClickListener {
            //set correct text of tvBirthdayDate (add / remove year)
            val dayMonthString =
                date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                    .padStart(2, '0')

            if (yearChanged) {
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }


            //color tvSaveYear gray or white depending on checkedState
            val color = when (cbSaveBirthdayYear.isChecked) {
                true -> R.attr.colorOnBackGround
                false -> R.attr.colorHint
            }
            tvSaveYear.setTextColor(myActivity.colorForAttr(color))
        }


        val textWatcherReminder = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cachedRemindText = etDaysToRemind.text.toString()
                var color =
                    myActivity.colorForAttr(R.attr.colorHint)

                val amount: Int
                if (cachedRemindText == "" || cachedRemindText.toInt() == 0) {
                    amount = 0
                } else {
                    color =
                        myActivity.colorForAttr(R.attr.colorOnBackGround)

                    amount = cachedRemindText.toInt()
                }
                tvRemindMe.setTextColor(color)
                etDaysToRemind.setTextColor(color)
                tvDaysPrior.setTextColor(color)

                val result = myActivity.resources.getQuantityString(
                    R.plurals.day,
                    amount
                ) + " " + myActivity.resources.getString(
                    R.string.birthdaysDaysPrior
                )
                tvDaysPrior.text = result

            }

            override fun afterTextChanged(s: Editable?) {
            }
        }

        //attach a listener that adjusts the color and text of surrounding text views
        etDaysToRemind.addTextChangedListener(textWatcherReminder)


        //clear text in etDaysToRemind when it is clicked on
        etDaysToRemind.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDaysToRemind.setText("")
            } else {
                if (etDaysToRemind.text.toString() == "") {
                    etDaysToRemind.setText("0")
                }
            }
        }

        etDaysToRemind.setOnClickListener {
            if (!cbNotifyMe.isChecked) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                cbNotifyMe.startAnimation(animationShakeCb)
            }
        }


        //AlertDialogBuilder
        val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val myTitle = layoutInflater.inflate(R.layout.title_dialog, null)
        myTitle.tvDialogTitle.text = resources.getText(R.string.birthdayDialogAddTitle)
        myBuilder?.setCustomTitle(myTitle)


        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //button to confirm adding of birthday
        myDialogView.btnConfirmBirthday.setOnClickListener {
            val name = etName.text.toString()

            //tell user to enter a name if none is entered
            if (name == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                etName.startAnimation(animationShake)
                return@setOnClickListener
            }


            if (!yearChanged) {
                val animationShake =
                    AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                tvBirthdayDate.startAnimation(animationShake)
                return@setOnClickListener
            }

            //get value of daysToRemind, set it to 0 if the text field is empty
            val daysToRemind = when (etDaysToRemind.text.toString()) {
                "" -> 0
                else -> etDaysToRemind.text.toString().toInt()
            }

            //boolean to determine if notifications for this birthday should be activated
            val notifyMe = cbNotifyMe.isChecked

            //determine which year should be saved for the birthday (0 signals, no year being saved)
            val yearToSave = when (cbSaveBirthdayYear.isChecked) {
                true -> date.year
                else -> 0
            }

            //add birthday and get info about position and range of added elements (month labels, year labels etc)
            val addInfo = birthdayListInstance.addBirthday(
                name, date.dayOfMonth, date.monthValue,
                yearToSave, daysToRemind, false, notifyMe
            )

            reloadAdapter()

            //todo reintroduce animations
//            myRecycler.adapter?.notifyItemRangeInserted(addInfo.first, addInfo.second)
//            if (round) {
//                if (addInfo.second == 1 && birthdayListInstance[addInfo.first - 1].daysToRemind >= 0) {
//                    myAdapter.notifyItemChanged(addInfo.first - 1)
//                }
//            }

            //scroll to added birthday
            myRecycler.scrollToPosition(addInfo.first)

            //update options menu
            updateBirthdayMenu()

            //close dialog
            myAlertDialog?.dismiss()
        }
        etName.requestFocus()
    }

    fun search(query: String) {
        if (query == "") {
            adjustedList.clear()
        } else {
            lastQuery = query
            adjustedList.clear()
            birthdayListInstance.forEach {
                val nameString = it.name.toLowerCase(Locale.ROOT)
                val dateFull = it.day.toString() + "." + it.month.toString()
                val dateLeftPad = it.day.toString().padStart(2, '0') + "." + it.month.toString()
                val dateRightPad = it.day.toString() + "." + it.month.toString().padStart(2, '0')
                val dateFullPad =
                    it.day.toString().padStart(2, '0') + "." + it.month.toString().padStart(2, '0')
                val queryLower = query.toLowerCase(Locale.ROOT)

                //only check birthdays (not month or year dividers with daysToRemind < 0)
                if (it.daysToRemind >= 0) {
                    if (nameString.contains(queryLower) ||
                        dateFull.contains(queryLower) ||
                        dateLeftPad.contains(queryLower) ||
                        dateRightPad.contains(queryLower) ||
                        dateFullPad.contains(queryLower)
                    ) {
                        adjustedList.add(it)
                    }
                }
            }
        }
        reloadAdapter()
    }

}

class SwipeToDeleteBirthday(
    private var adapter: BirthdayAdapter,
    direction: Int,
    birthdayFr: BirthdayFr
) :
    ItemTouchHelper.SimpleCallback(0, direction) {
    private val myFragment = birthdayFr
    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val parsed = viewHolder as BirthdayAdapter.BirthdayViewHolder
        return if (viewHolder.adapterPosition == myFragment.birthdayListInstance.size || parsed.birthday.daysToRemind < 0) {
            0
        } else {
            super.getSwipeDirs(recyclerView, viewHolder)
        }
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) =
        adapter.deleteItem(viewHolder)
}


class BirthdayAdapter(birthdayFr: BirthdayFr, mainActivity: MainActivity) :
    RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {
    private val myFragment = birthdayFr
    private val myActivity = mainActivity
    private val listInstance = myFragment.birthdayListInstance
    private val density = myActivity.resources.displayMetrics.density
    private val marginSide = (density * 20).toInt()
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val southColors = SettingsManager.getSetting(SettingId.BIRTHDAY_COLORS_SOUTH) as Boolean

    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        //get deleted birthday via index
        BirthdayFr.deletedBirthday = when (myFragment.searching) {
            true -> BirthdayFr.adjustedList[viewHolder.adapterPosition]
            else -> listInstance.getBirthday(viewHolder.adapterPosition)
        }

        //delete birthday and get info of deleted range and position back
        val deleteInfo = listInstance.deleteBirthdayObject(parsed.birthday)

        myFragment.reloadAdapter()
        myFragment.myRecycler.scrollToPosition(deleteInfo.first)

        if (myFragment.searching) {
            myFragment.search(BirthdayFr.lastQuery)
        } else {
            //update option menus
            myFragment.updateUndoBirthdayIcon()
            myFragment.updateBirthdayMenu()
            //todo, test and reintroduce animations
//            notifyItemRangeRemoved(deleteInfo.first, deleteInfo.second)
//            if (round) {
//                if (deleteInfo.second == 1 && myFragment.birthdayListInstance[deleteInfo.first - 1].daysToRemind >= 0) {
//                    myFragment.myAdapter.notifyItemChanged(deleteInfo.first - 1)
//                }
//            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        //reset elevation
        holder.cvBirthday.elevation = 10f

        //get birthday for this view holder, from BirthdayFr.adjustedList if this Fragment currently is
        //in search mode, or from listInstance.getBirthday(position) if its in regular display mode
        val currentBirthday = when (myFragment.searching) {
            true -> BirthdayFr.adjustedList[position]
            false -> listInstance.getBirthday(position)
        }

        //save a reference for the birthday saved in this holder
        holder.birthday = currentBirthday

        //manage design of month or year divider (signaled by days to remind being smaller than 0)
        if (currentBirthday.daysToRemind < 0) {
            initializeDividerViewHolder(holder, currentBirthday)
            return
        }

        //set regular birthday background
        val colorA =
            myActivity.colorForAttr(R.attr.colorHomePanel)

        val myGradientDrawable =
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(colorA, colorA))

        //reset margin
        val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        //reset elevation
        holder.cvBirthday.elevation = 10f


        //make name and date visible
        holder.tvRowBirthdayDate.visibility = View.VISIBLE
        holder.tvRowBirthdayName.visibility = View.VISIBLE

        if (myFragment.searching) {
            //display all corners as round if in searching mode
            if (round) {
                myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)
            }

            if (position == 0) {
                params.setMargins(marginSide, marginSide, marginSide, (density * 10).toInt())
            } else {
                params.setMargins(
                    marginSide,
                    (density * 10).toInt(),
                    marginSide,
                    (density * 10).toInt()
                )
            }
        } else {
            params.setMargins(marginSide, (density * 1).toInt(), marginSide, (density * 1).toInt())
            if (round) {
                //if its the last birthday in list, or the following birthday is a monthDivider (daysToRemind < 0) bottom corners become round
                if ((holder.adapterPosition == myFragment.birthdayListInstance.size - 1) || (myFragment.birthdayListInstance[holder.adapterPosition + 1].daysToRemind < 0)) {
                    myGradientDrawable.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, cr, cr, cr, cr)
                }
            }
        }

        holder.cvBirthday.background = myGradientDrawable

        //initialize regular birthday design
        holder.tvRowBirthdayDivider.visibility = View.GONE
        holder.myDividerLeft.visibility = View.GONE
        holder.myDividerRight.visibility = View.GONE


        //display info if birthday is expanded
        if (currentBirthday.expanded) {
            holder.itemView.tvBirthdayInfo.visibility = View.VISIBLE
            var ageText = myActivity.resources.getString(R.string.birthdayAgeUnknown)
            //if a year is saved, calculate age and display it
            if (holder.birthday.year != 0) {
                val birthday = holder.birthday
                val age = LocalDate.of(birthday.year, birthday.month, birthday.day)
                    .until(LocalDate.now()).years
                val ageYearText = myActivity.resources.getQuantityString(R.plurals.year, age)
                ageText =
                    age.toString() + " " + ageYearText + myActivity.resources.getString(R.string.birthdayOldBornIn) + birthday.year
            }
            val reminderDayString = myActivity.resources.getQuantityString(
                R.plurals.day,
                currentBirthday.daysToRemind
            )
            val reminderText = when (currentBirthday.notify) {
                true ->
                    when (currentBirthday.daysToRemind) {
                        0 -> myActivity.resources.getString(R.string.birthdaysReminderActivated)
                        else ->
                            myActivity.resources.getString(
                                R.string.birthdayReminder,
                                currentBirthday.daysToRemind,
                                reminderDayString
                            )
                    }
                else -> myActivity.resources.getString(R.string.birthdaysNoReminder)
            }

            val infoText = ageText + reminderText
            holder.itemView.tvBirthdayInfo.text = infoText
        } else {
            holder.itemView.tvBirthdayInfo.visibility = View.GONE
        }


        //creates initial dateString containing padded dayOfMonth "03" or "12" e.g.
        var dateString =
            currentBirthday.day.toString().padStart(2, '0')

        //adds month to this string if searching, or setting says to show month
        if (myFragment.searching || SettingsManager.getSetting(SettingId.BIRTHDAY_SHOW_MONTH) as Boolean) {
            dateString += "." + currentBirthday.month.toString()
                .padStart(2, '0')
        }

        //Display name and date
        holder.tvRowBirthdayDate.text = dateString
        val daysUntilString = when (currentBirthday.daysUntil()) {
            //"today"
            0 -> myActivity.resources.getString(R.string.birthdayToday)
            //"tomorrow"
            1 -> myActivity.resources.getString(R.string.birthdayTomorrow)
            //"in x days"
            in 2..30 ->
                myActivity.resources.getString(R.string.birthdayIn) + " " + currentBirthday.daysUntil()
                    .toString() + " " + myActivity.resources.getQuantityString(
                    R.plurals.dayIn,
                    currentBirthday.daysUntil()
                )
            //no addition
            else -> ""
        }
        //combines name and daysUntil addition to displayed text and applies it
        val birthdayText = currentBirthday.name + " " + daysUntilString
        holder.tvRowBirthdayName.text = birthdayText

        //signalColor applied to birthday bell icon and text
        val today = LocalDate.now()
        val signalColor =
            if (holder.birthday.day == today.dayOfMonth && holder.birthday.month == today.monthValue) {
                //if birthday is today
                myActivity.colorForAttr(R.attr.colorBirthdayToday)
            } else if (holder.birthday.daysToRemind > 0 && holder.birthday.daysUntil() <= holder.birthday.daysToRemind) {
                //if birthday is soon
                myActivity.colorForAttr(R.attr.colorBirthdaySoon)
            } else {
                //regular color otherwise
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            }

        //apply signal color
        holder.tvRowBirthdayDate.setTextColor(signalColor)
        holder.tvRowBirthdayName.setTextColor(signalColor)
        holder.iconBell.setColorFilter(signalColor)

        //display bell if birthday has a reminder
        holder.iconBell.visibility = View.VISIBLE
        if (currentBirthday.notify) {
            holder.iconBell.setImageResource(R.drawable.ic_bell)
        } else {
            holder.iconBell.setColorFilter(
                myActivity.colorForAttr(R.attr.colorHint),
                android.graphics.PorterDuff.Mode.MULTIPLY
            )
            holder.iconBell.setImageResource(R.drawable.ic_action_no_notification)
        }

        //lambda that initializes edit, by saving birthday to editBirthdayHolder and opening dialog
        val initializeEdit: () -> Boolean = {
            BirthdayFr.editBirthdayHolder = holder.birthday
            myFragment.openEditBirthdayDialog()
            true
        }

        //onLonClickListeners on name, date and icon to initialize edit
        holder.itemView.tvRowBirthdayName.setOnLongClickListener {
            initializeEdit()
        }
        holder.itemView.tvRowBirthdayDate.setOnLongClickListener {
            initializeEdit()
        }
        holder.itemView.icon_bell.setOnLongClickListener {
            initializeEdit()
        }

        //expands info
        val switchExpandState: () -> Unit = {
            holder.birthday.expanded = !holder.birthday.expanded
            listInstance.sortAndSaveBirthdays()
            notifyItemChanged(holder.adapterPosition)
        }

        //onClickListener on name and Date to switch expansion state
        holder.itemView.tvRowBirthdayDate.setOnClickListener {
            switchExpandState()
        }

        holder.itemView.tvRowBirthdayName.setOnClickListener {
            switchExpandState()
        }
        holder.itemView.tvBirthdayInfo.setOnClickListener {
            switchExpandState()
        }

        //onClickListener on bell icon to enable / disable reminder
        holder.itemView.icon_bell.setOnClickListener {
            holder.birthday.notify = !holder.birthday.notify
            notifyItemChanged(holder.adapterPosition)
        }


    }

    private fun initializeYearViewHolder(holder: BirthdayViewHolder) {
        //YEAR DIVIDER
        holder.cvBirthday.elevation = 0f
        holder.itemView.layoutParams.height = (70 * density).toInt()
        holder.tvRowBirthdayDivider.textSize = 22f
        holder.tvRowBirthdayDivider.setTextColor(
            myActivity.colorForAttr(R.attr.colorOnBackGround)
        )

        //removes margin at sides so year divider spans parent width
        val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0, marginSide, 0, (0 * density).toInt())

        //change backgroundColor to app background color and show year dividers
        holder.cvBirthday.setBackgroundColor(
            myActivity.colorForAttr(R.attr.colorBackground)
        )
        holder.myDividerRight.visibility = View.VISIBLE
        holder.myDividerLeft.visibility = View.VISIBLE
    }

    private fun initializeMonthViewHolder(
        holder: BirthdayViewHolder,
        currentBirthday: Birthday
    ) {
        //initialize values specific to month divider
        // reintroduces margin at sides
        val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(marginSide, marginSide, marginSide, (2 * density).toInt())

        //sets correct text size, height and text color
        holder.tvRowBirthdayDivider.textSize = 20f
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.tvRowBirthdayDivider.setTextColor(
            myActivity.colorForAttr(R.attr.colorCategory)
        )

        val monthColors = listOf(
            Pair(R.attr.colorMonth2, R.attr.colorMonth1),
            Pair(R.attr.colorMonth3, R.attr.colorMonth2),
            Pair(R.attr.colorMonth4, R.attr.colorMonth3),
            Pair(R.attr.colorMonth5, R.attr.colorMonth4),
            Pair(R.attr.colorMonth6, R.attr.colorMonth5),
            Pair(R.attr.colorMonth7, R.attr.colorMonth6),
            Pair(R.attr.colorMonth8, R.attr.colorMonth7),
            Pair(R.attr.colorMonth9, R.attr.colorMonth8),
            Pair(R.attr.colorMonth10, R.attr.colorMonth9),
            Pair(R.attr.colorMonth11, R.attr.colorMonth10),
            Pair(R.attr.colorMonth12, R.attr.colorMonth11),
            Pair(R.attr.colorMonth1, R.attr.colorMonth12)
        )

        //determine the background color of the card
        //daysToRemind acts as a marker in mothDividers to mark the month they display
        // -1 = january, -2 february etc
        val gradientPair: Pair<Int, Int> =
            when (southColors) {
                true ->
                    //month colors for southern hemisphere, days to remind gets turned positive, then + 7 % 12, to shift the colors by 7 month
                    //now warm colors are represented in months 8 - 2 etc
                    monthColors[((currentBirthday.daysToRemind * -1) + 7) % 12]
                else ->
                    //regular month colors, days to remind gets turned positive, then -1 so january => index 0
                    monthColors[(currentBirthday.daysToRemind * -1) - 1]
            }

        //create a gradient drawable as a background for the month divider
        val myGradientDrawable = GradientDrawable(
            GradientDrawable.Orientation.TL_BR,
            intArrayOf(
                myActivity.colorForAttr(gradientPair.second),
                myActivity.colorForAttr(gradientPair.first)
            )
        )

        //check if setting says to use round design, apply correct corner angles
        if (round) myGradientDrawable.cornerRadii =
            floatArrayOf(cr, cr, cr, cr, 0f, 0f, 0f, 0f)

        holder.cvBirthday.background = myGradientDrawable

        //hide dividers that are specific to year divider
        holder.myDividerRight.visibility = View.GONE
        holder.myDividerLeft.visibility = View.GONE

    }

    /**
     * This gets called when currentBirthday.daysToRemind was < 0. This signals that the ViewHolder is
     * either a month or a year divider. At first all elements only necessary for a regular birthday
     * will be hidden or reset, then the proper year or month divider will be initialized.
     * @param holder the BirthdayViewHolder being modified
     * @param currentBirthday the birthday being displayed by this BirthdayViewHolder
     */
    private fun initializeDividerViewHolder(
        holder: BirthdayViewHolder,
        currentBirthday: Birthday
    ) {
        //show tvRowBirthdayDivider and set its text to the correct month or year name
        holder.tvRowBirthdayDivider.visibility = View.VISIBLE
        holder.tvRowBirthdayDivider.text = currentBirthday.name

        //hide elements only used for a regular birthday
        holder.tvRowBirthdayDate.visibility = View.GONE
        holder.tvRowBirthdayName.visibility = View.GONE
        holder.itemView.tvBirthdayInfo.visibility = View.GONE
        holder.itemView.icon_bell.visibility = View.GONE

        //reset onLongClickListener to prevent editDialog
        holder.itemView.setOnLongClickListener { true }

        //reset onClickListener to prevent expansion
        holder.itemView.setOnClickListener { }

        if (currentBirthday.daysToRemind == -200) {
            initializeYearViewHolder(holder)
        } else {
            initializeMonthViewHolder(holder, currentBirthday)
        }
        return

    }

    //returns number of items that should be displayed in the recyclerView
    override fun getItemCount(): Int {
        return when (myFragment.searching) {
            //if a search is currently being performed, the number of items is derived from the adjustedList
            true -> BirthdayFr.adjustedList.size

            //otherwise the number of items will be derived from the regular list instance
            false -> listInstance.size
        }
    }

    class BirthdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_task and meta data
         * like position, it also holds references to views inside of the layout
         */

        //birthday instance represented by this viewHolder
        lateinit var birthday: Birthday

        //textViews for name, date and monthNames
        val tvRowBirthdayName: TextView = itemView.tvRowBirthdayName
        val tvRowBirthdayDate: TextView = itemView.tvRowBirthdayDate
        val tvRowBirthdayDivider: TextView = itemView.tvRowBirthdayDivider

        //reminder bell icon
        val iconBell: ImageView = itemView.icon_bell

        //dividers used to display a year element
        val myDividerLeft: View = itemView.viewDividerLeft
        val myDividerRight: View = itemView.viewDividerRight

        //reference to the constraintLayout containing the elements above
        val cvBirthday: ConstraintLayout = itemView.cvBirthday
    }

}
