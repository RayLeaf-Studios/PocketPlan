package com.pocket_plan.j7_003.data.birthdaylist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.DialogAddBirthdayBinding
import com.pocket_plan.j7_003.databinding.FragmentBirthdayBinding
import com.pocket_plan.j7_003.databinding.RowBirthdayBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding
import org.threeten.bp.LocalDate
import java.util.*
import java.util.regex.Pattern
import kotlin.math.abs


/**
 * A simple [Fragment] subclass.
 */

class BirthdayFr : Fragment() {
    //instance of birthday list, containing all the displayed birthdays
    lateinit var myActivity: MainActivity
    lateinit var birthdayListInstance: BirthdayList

    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private var _frBinding: FragmentBirthdayBinding? = null
    private val frBinding get() = _frBinding!!

    //initialize recycler view
    private lateinit var myRecycler: RecyclerView

    private val darkMode: Boolean = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean

    //Current date to properly initialize date picker
    private lateinit var date: LocalDate

    //reference to birthday options menu
    private lateinit var myMenu: Menu

    //Adapter for recycler view
    lateinit var myAdapter: BirthdayAdapter

    //boolean to signal if a search is currently being performed
    var searching: Boolean = false

    //List containing birthdays that correspond to current search pattern
    private lateinit var searchList: ArrayList<Birthday>

    //reference to searchView in toolbar
    lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    companion object {
        //Holder for deleted birthday to make undo possible
        var deletedBirthdays = ArrayDeque<Birthday?>()

        //Holder for birthday currently being edited
        var editBirthdayHolder: Birthday? = null

        //Last used searchPattern
        lateinit var lastQuery: String
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        //inflate menu and save reference to it
        inflater.inflate(R.menu.menu_birthdays, menu)
        myMenu = menu

        searching = false
        updateBirthdayMenu()
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
            myActivity.myBtnAdd.visibility = View.VISIBLE
            //reset title
            myActivity.toolbar.title = getString(R.string.menuTitleBirthdays)
            //collapse searchView
            searchView.onActionViewCollapsed()
            //signal that no search is being performed
            searching = false
            updateBirthdayMenu()
            //reload menu icons
            updateUndoBirthdayIcon()
            //reload list elements by notifying data set change to adapter
            myAdapter.notifyDataSetChanged()
            true
        }

        //set onSearchClickListener that initializes searching
        searchView.setOnSearchClickListener {
            myActivity.myBtnAdd.visibility = View.GONE
            //removes title from toolbar
            myActivity.toolbar.title = ""
            //sets searching to true, which results in the recyclerViewAdapter reading its elements from
            //adjusted list instead of birthdayList
            searching = true
            myMenu.findItem(R.id.item_birthdays_undo)?.isVisible = false
            hideMenuExceptSearch()

            //clear adjusted list
            searchList.clear()
            //reload adapter dataSet
            myAdapter.notifyDataSetChanged()
            searchView.requestFocus()
        }

        //initialize editBirthdayHolder with null to signal that no birthday is being edited
        editBirthdayHolder = null

        //refresh option menus
        updateUndoBirthdayIcon()
        updateBirthdayMenu()

        super.onCreateOptionsMenu(menu, inflater)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            //disable all reminders and refresh adapter
            R.id.item_birthdays_disable_reminders -> {
                birthdayListInstance.disableAllReminders()
                myAdapter.notifyDataSetChanged()
            }

            //enable all reminders and refresh adapter
            R.id.item_birthdays_enable_reminders -> {
                birthdayListInstance.enableAllReminders()
                myAdapter.notifyDataSetChanged()
            }

            R.id.item_birthdays_search -> {/* no-op, listeners for this searchView are set
             in on create options menu*/
            }

            R.id.item_birthdays_undo -> {
                //undo the last deletion
                //re-add previously deleted birthday and get its new position

                val addInfo = birthdayListInstance.addFullBirthday(deletedBirthdays.last()!!)

                //set deletedBirthday to null no signal that no undo is possible
                deletedBirthdays.removeLast()

                //update menu content due to new possibilities
                updateUndoBirthdayIcon()
                updateBirthdayMenu()
                myAdapter.notifyItemRangeInserted(addInfo.first, addInfo.second)
                if (round) {
                    if (addInfo.second == 1 && birthdayListInstance[addInfo.first - 1].daysToRemind >= 0) {
                        myAdapter.notifyItemChanged(addInfo.first - 1)
                    }
                }
                myRecycler.scrollToPosition(addInfo.first)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _frBinding = null
    }

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _frBinding = FragmentBirthdayBinding.inflate(inflater, container, false)
        val view = frBinding.root

        date = LocalDate.now()
        myActivity = activity as MainActivity

        myRecycler = frBinding.recyclerViewBirthday
        birthdayListInstance = MainActivity.birthdayList

        searchList = arrayListOf()

        myAdapter = BirthdayAdapter(this, myActivity, searchList)

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

        return view
    }


    /**
     * Refreshes the birthdayList to ensure correct order of birthdays, in case a user leaves
     * the app without closing it (before 00:00) and then re-enters (after 00:00)
     */
    @SuppressLint("NotifyDataSetChanged")
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
        if (deletedBirthdays.isNotEmpty() && !searching) {
            myMenu.findItem(R.id.item_birthdays_undo)?.setIcon(R.drawable.ic_action_undo)
            myMenu.findItem(R.id.item_birthdays_undo)?.isVisible = true
        } else {
            myMenu.findItem(R.id.item_birthdays_undo)?.isVisible = false
        }
    }

    private fun hideMenuExceptSearch() {
        val size = myMenu.size()
        for (i in 0 until size) myMenu.getItem(i).isVisible = false
        myMenu.findItem(R.id.item_birthdays_search)?.isVisible = true
    }

    @SuppressLint("InflateParams", "NotifyDataSetChanged")
    fun openEditBirthdayDialog() {
        var yearChanged = false

        //set date to birthdays date
        date = LocalDate.of(
            editBirthdayHolder!!.year, editBirthdayHolder!!.month, editBirthdayHolder!!.day
        )

        //inflate the dialog with custom view
        val myDialogBinding = DialogAddBirthdayBinding.inflate(layoutInflater)

        //initialize references to ui elements
        val etName = myDialogBinding.etName
        val etDaysToRemind = myDialogBinding.etDaysToRemind

        val tvBirthdayDate = myDialogBinding.tvBirthdayDate
        val tvSaveYear = myDialogBinding.tvSaveYear
        val tvNotifyMe = myDialogBinding.tvNotifyMe
        val tvRemindMe = myDialogBinding.tvRemindMe
        val tvDaysPrior = myDialogBinding.tvDaysPrior

        val cbSaveBirthdayYear = myDialogBinding.cbSaveBirthdayYear
        val cbNotifyMe = myDialogBinding.cbNotifyMe

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
        val dateText = editBirthdayHolder!!.day.toString()
            .padStart(2, '0') + "." + editBirthdayHolder!!.month.toString()
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
        val daysPriorTextEdit = myActivity.resources.getQuantityText(R.plurals.day, daysToRemind)
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
        myDialogBinding.btnConfirmBirthday.text = resources.getText(R.string.birthdayDialogEdit)

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
                        date.withYear(LocalDate.now().year).until(date.withYear(pickedYear))
                            .toTotalMonths()
                    ) >= 12 && !cbSaveBirthdayYear.isChecked
                ) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGround)
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
                true -> DatePickerDialog(
                    myActivity,
                    R.style.MyDatePickerStyle,
                    dateSetListener,
                    yearToDisplay,
                    date.monthValue - 1,
                    date.dayOfMonth
                )

                else -> DatePickerDialog(
                    myActivity,
                    R.style.DialogTheme,
                    dateSetListener,
                    yearToDisplay,
                    date.monthValue - 1,
                    date.dayOfMonth
                )
            }
            dpd.show()
            dpd.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
            dpd.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
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

        //TextWatcher, adjusting the text colors of "Remind me in x days" in the add / edit birthday dialog
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
                val animationShake = AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb = AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                cbNotifyMe.startAnimation(animationShakeCb)
            }
        }


        //AlertDialogBuilder
        val myBuilder =
            activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogBinding.root) }
        val myTitleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        myTitleDialogBinding.tvDialogTitle.text =
            resources.getText(R.string.birthdayDialogEditTitle)
        myBuilder?.setCustomTitle(myTitleDialogBinding.root)


        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //button to confirm edit of birthday
        myDialogBinding.btnConfirmBirthday.setOnClickListener {
            val name = etName.text.toString()

            //tell user to enter a name if none is entered
            if (name.trim() == "") {
                val animationShake = AnimationUtils.loadAnimation(myActivity, R.anim.shake)
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

        //set date to today
        date = LocalDate.now()

        //inflate the dialog with custom view
        val myDialogBinding = DialogAddBirthdayBinding.inflate(layoutInflater)

        //initialize references to ui elements
        val etName = myDialogBinding.etName
        val etDaysToRemind = myDialogBinding.etDaysToRemind

        val tvBirthdayDate = myDialogBinding.tvBirthdayDate
        val tvSaveYear = myDialogBinding.tvSaveYear
        val tvNotifyMe = myDialogBinding.tvNotifyMe
        val tvRemindMe = myDialogBinding.tvRemindMe
        val tvDaysPrior = myDialogBinding.tvDaysPrior

        val cbSaveBirthdayYear = myDialogBinding.cbSaveBirthdayYear
        val cbNotifyMe = myDialogBinding.cbNotifyMe

        /**
         * INITIALIZE VALUES
         */

        var dateRegistered = false
        var dateRemoved = false
        var dateStringStartIndex = 0
        val initPriorText = myActivity.resources.getQuantityString(
            R.plurals.day, 0
        ) + " " + myActivity.resources.getString(
            R.string.birthdaysDaysPrior
        )
        tvDaysPrior.text = initPriorText

        /**
         * INITIALIZE LISTENERS
         */

        etName.doOnTextChanged { text, _, _, _ ->
            if (text == null) return@doOnTextChanged
            val pattern =
                Pattern.compile("(\\d{1,2})[-/.](\\d{1,2})[-/.](\\d{4})|(\\d{4})[-/.](\\d{1,2})[-/.](\\d{1,2})|(\\d{1,2})[-/.](\\d{1,2})")
            val matcher = pattern.matcher(text)

            // return if no date of valid format is found
            if (!matcher.find()) return@doOnTextChanged

            val yearPresent: Boolean

            val groupIndex = when {
                matcher.group(1) != null -> 1
                matcher.group(4) != null -> 4
                else -> 7
            }

            try {
                dateStringStartIndex = matcher.start(groupIndex)
                val day = matcher.group(groupIndex)!!.toInt()
                val month = matcher.group(groupIndex + 1)!!.toInt()
                val year =
                    if (groupIndex != 7) matcher.group(groupIndex + 2)!!.toInt() else date.year
                yearPresent = groupIndex != 7
                date = LocalDate.of(year, month, day)
            } catch (_: Exception) {
                // return if date not valid, e.g. 35.12.2023
                return@doOnTextChanged
            }

            // construct date string and set it in the "choose date button"
            val dayMonthString = date.dayOfMonth.toString()
                .padStart(2, '0') + "." + (date.monthValue).toString().padStart(2, '0')
            tvBirthdayDate.text = when (yearPresent) {
                false -> dayMonthString
                else -> dayMonthString + "." + date.year.toString()
            }

            // only remove date from name line, if it was a complete date
            if (yearPresent) {
                if (text.filterNot { it.isWhitespace() }.length != text.length) dateStringStartIndex--
                etName.setText(text.substring(0, dateStringStartIndex).trim())
                etName.setSelection(etName.text.length)
                cbSaveBirthdayYear.isChecked = true
                tvSaveYear.setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )
                dateRemoved = true
            } else {
                cbSaveBirthdayYear.isChecked = false
                tvSaveYear.setTextColor(
                    myActivity.colorForAttr(R.attr.colorHint)
                )
                dateRemoved = false
            }

            yearChanged = true
            dateRegistered = true
        }

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

                date = date.withYear(pickedYear).withMonth(month + 1).withDayOfMonth(day)

                if (abs(
                        LocalDate.now().until(date).toTotalMonths()
                    ) >= 12 && !cbSaveBirthdayYear.isChecked
                ) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGround)
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

            val dpd = DatePickerDialog(
                myActivity,
                if (darkMode) R.style.MyDatePickerStyle else R.style.DialogTheme,
                dateSetListener,
                yearToDisplay,
                date.monthValue - 1,
                date.dayOfMonth
            )

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
                var color = myActivity.colorForAttr(R.attr.colorHint)

                val amount: Int
                if (cachedRemindText == "" || cachedRemindText.toInt() == 0) {
                    amount = 0
                } else {
                    color = myActivity.colorForAttr(R.attr.colorOnBackGround)

                    amount = cachedRemindText.toInt()
                }
                tvRemindMe.setTextColor(color)
                etDaysToRemind.setTextColor(color)
                tvDaysPrior.setTextColor(color)

                val result = myActivity.resources.getQuantityString(
                    R.plurals.day, amount
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
                val animationShake = AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb = AnimationUtils.loadAnimation(myActivity, R.anim.shake2)
                cbNotifyMe.startAnimation(animationShakeCb)
            }
        }

        //AlertDialogBuilder
        val myBuilder =
            activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogBinding.root) }
        val myTitleDialogBinding = TitleDialogBinding.inflate(layoutInflater)
        myTitleDialogBinding.tvDialogTitle.text = resources.getText(R.string.birthdayDialogAddTitle)
        myBuilder?.setCustomTitle(myTitleDialogBinding.root)

        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //button to confirm adding of birthday
        myDialogBinding.btnConfirmBirthday.setOnClickListener {
            val name = if (!dateRegistered || (dateRegistered && dateRemoved)){
                etName.text.toString().trim()
            } else {
                etName.text.toString().substring(0, dateStringStartIndex).trim()
            }

            //tell user to enter a name if none is entered
            if (name.trim() == "") {
                val animationShake = AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                etName.startAnimation(animationShake)
                return@setOnClickListener
            }


            if (!yearChanged) {
                val animationShake = AnimationUtils.loadAnimation(myActivity, R.anim.shake)
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
                name, date.dayOfMonth, date.monthValue, yearToSave, daysToRemind, false, notifyMe
            )

            myRecycler.adapter?.notifyItemRangeInserted(addInfo.first, addInfo.second)
            if (round) {
                if (addInfo.second == 1 && birthdayListInstance[addInfo.first - 1].daysToRemind >= 0) {
                    myAdapter.notifyItemChanged(addInfo.first - 1)
                }
            }

            //scroll to added birthday
            myRecycler.scrollToPosition(addInfo.first)

            //update options menu
            updateBirthdayMenu()

            //close dialog
            myAlertDialog?.dismiss()
        }
        etName.requestFocus()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun search(query: String) {
        if (query == "") {
            searchList.clear()
        } else {
            //Save query, so search can be reset after deleting a birthday
            lastQuery = query
            searchList.clear()
            //Create multiple possible strings by which a birthday can be found
            birthdayListInstance.forEach {
                //Find by name
                val nameString = it.name.lowercase(Locale.ROOT)
                //Find by day, no 0 padding e.g. 1.2.
                val dateFull = it.day.toString() + "." + it.month.toString()
                //Find by date, 0 padding the month, e.g. 3.02
                val dateLeftPad = it.day.toString().padStart(2, '0') + "." + it.month.toString()
                //Find by date, 0 padding the day, e.g. 03.2
                val dateRightPad = it.day.toString() + "." + it.month.toString().padStart(2, '0')
                //Find by date, 0 padding both day and month e.g. 03.02
                val dateFullPad =
                    it.day.toString().padStart(2, '0') + "." + it.month.toString().padStart(2, '0')
                val queryLower = query.lowercase(Locale.ROOT)

                //only check birthdays (not month or year dividers with daysToRemind < 0)
                if (it.daysToRemind >= 0) {
                    if (nameString.contains(queryLower) ||
                        dateFull.contains(queryLower) ||
                        dateLeftPad.contains(queryLower) ||
                        dateRightPad.contains(queryLower) ||
                        dateFullPad.contains(queryLower)
                    ) {
                        searchList.add(it)
                    }
                }
            }
        }
        myAdapter.notifyDataSetChanged()
    }

}

class SwipeToDeleteBirthday(
    private var adapter: BirthdayAdapter, direction: Int, birthdayFr: BirthdayFr
) : ItemTouchHelper.SimpleCallback(0, direction) {
    private val myFragment = birthdayFr
    override fun getSwipeDirs(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ): Int {
        val parsed = viewHolder as BirthdayAdapter.BirthdayViewHolder
        return if (viewHolder.bindingAdapterPosition == myFragment.birthdayListInstance.size || parsed.birthday.daysToRemind < 0) {
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


class BirthdayAdapter(
    birthdayFr: BirthdayFr, mainActivity: MainActivity, private var searchList: ArrayList<Birthday>
) : RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {
    private val myFragment = birthdayFr
    private val myActivity = mainActivity
    private val listInstance = myFragment.birthdayListInstance
    private val density = myActivity.resources.displayMetrics.density
    private val marginSide = (density * 20).toInt()
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val southColors = SettingsManager.getSetting(SettingId.BIRTHDAY_COLORS_SOUTH) as Boolean

    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    private val monthColors = listOf(
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

    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        //get deleted birthday via index
        BirthdayFr.deletedBirthdays.add(
            when (myFragment.searching) {
                true -> searchList[viewHolder.bindingAdapterPosition]
                else -> listInstance.getBirthday(viewHolder.bindingAdapterPosition)
            }
        )

        //delete birthday and get info of deleted range and position back
        val deleteInfo = listInstance.deleteBirthdayObject(parsed.birthday)

        if (myFragment.searching) {
            //Reset search after deleting an item
            myFragment.search(BirthdayFr.lastQuery)
        } else {
            //Update option menus
            myFragment.updateUndoBirthdayIcon()
            myFragment.updateBirthdayMenu()
            notifyItemRangeRemoved(deleteInfo.first, deleteInfo.second)
            if (round) {
                if (deleteInfo.second == 1 && myFragment.birthdayListInstance[deleteInfo.first - 1].daysToRemind >= 0) {
                    myFragment.myAdapter.notifyItemChanged(deleteInfo.first - 1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
//        val itemView =
//            LayoutInflater.from(parent.context).inflate(R.layout.row_birthday, parent, false)
        val rowBirthdayBinding =
            RowBirthdayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BirthdayViewHolder(rowBirthdayBinding)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        //reset elevation
        holder.binding.cvBirthday.elevation = 10f

        //get birthday for this view holder, from BirthdayFr.adjustedList if this Fragment currently is
        //in search mode, or from listInstance.getBirthday(position) if its in regular display mode
        val currentBirthday = when (myFragment.searching) {
            true -> searchList[position]
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
        val colorA = myActivity.colorForAttr(R.attr.colorHomePanel)

        val myGradientDrawable =
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(colorA, colorA))

        //reset margin
        val params = holder.binding.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT

        //reset elevation
        holder.binding.cvBirthday.elevation = 10f

        //make birthday specific elements visible
        holder.binding.tvRowBirthdayDate.visibility = View.VISIBLE
        holder.binding.tvRowBirthdayName.visibility = View.VISIBLE
        holder.binding.tvRowBirthdayDays.visibility = View.VISIBLE
        holder.binding.icRowBirthdayNotification.visibility = View.VISIBLE

        if (myFragment.searching) {
            //display all corners as round if in searching mode
            if (round) {
                myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, cr, cr, cr, cr)
            }

            if (position == 0) {
                params.setMargins(marginSide, marginSide, marginSide, (density * 10).toInt())
            } else {
                params.setMargins(
                    marginSide, (density * 10).toInt(), marginSide, (density * 10).toInt()
                )
            }
        } else {
            params.setMargins(marginSide, (density * 1).toInt(), marginSide, (density * 1).toInt())
            if (round) {
                //if its the last birthday in list, or the following birthday is a monthDivider (daysToRemind < 0) bottom corners become round
                if ((holder.bindingAdapterPosition == myFragment.birthdayListInstance.size - 1) || (myFragment.birthdayListInstance[holder.bindingAdapterPosition + 1].daysToRemind < 0)) {
                    myGradientDrawable.cornerRadii = floatArrayOf(0f, 0f, 0f, 0f, cr, cr, cr, cr)
                }
            }
        }

        holder.binding.cvBirthday.background = myGradientDrawable

        //initialize regular birthday design
        holder.binding.tvRowBirthdayDivider.visibility = View.GONE
        holder.binding.viewDividerLeft.visibility = View.GONE
        holder.binding.viewDividerRight.visibility = View.GONE

        //display info if birthday is expanded
        var expanded = currentBirthday.expanded
        if (myFragment.searching && itemCount == 1) {
            // expand birthday if it is the only search result
            expanded = true
        }
        if (expanded) {
            holder.binding.tvBirthdayInfo.visibility = View.VISIBLE
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
                R.plurals.day, currentBirthday.daysToRemind
            )
            val reminderText = when (currentBirthday.notify) {
                true -> when (currentBirthday.daysToRemind) {
                    0 -> myActivity.resources.getString(R.string.birthdaysReminderActivated)
                    else -> myActivity.resources.getString(
                        R.string.birthdayReminder, currentBirthday.daysToRemind, reminderDayString
                    )
                }

                else -> myActivity.resources.getString(R.string.birthdaysNoReminder)
            }

            val infoText = ageText + reminderText
            holder.binding.tvBirthdayInfo.text = infoText
        } else {
            holder.binding.tvBirthdayInfo.visibility = View.GONE
        }

        //creates initial dateString containing padded dayOfMonth "03" or "12" e.g.
        var dateString = currentBirthday.day.toString().padStart(2, '0')

        //adds month to this string if searching, or setting says to show month
        if (myFragment.searching || SettingsManager.getSetting(SettingId.BIRTHDAY_SHOW_MONTH) as Boolean) {
            dateString += "." + currentBirthday.month.toString().padStart(2, '0')
        }

        //Display name and date
        holder.binding.tvRowBirthdayDate.text = dateString
        val daysUntilString = when (currentBirthday.daysUntil()) {
            //"today"
            0 -> myActivity.resources.getString(R.string.birthdayToday)
            //"tomorrow"
            1 -> myActivity.resources.getString(R.string.birthdayTomorrow)
            //"in x days"
            in 2..14 -> myActivity.resources.getString(R.string.birthdayIn) + " " + currentBirthday.daysUntil()
                .toString() + " " + myActivity.resources.getQuantityString(
                R.plurals.dayIn, currentBirthday.daysUntil()
            )
            //no addition
            else -> ""
        }

        // display name and daysUntil string
        holder.binding.tvRowBirthdayName.text = currentBirthday.name
        holder.binding.tvRowBirthdayDays.text = daysUntilString

        //show or hide daysUntil text
        holder.binding.tvRowBirthdayDays.visibility = when (daysUntilString == "") {
            true -> View.GONE
            else -> View.VISIBLE
        }

        // Adjust distance between birthday name and expanded info, depending on if there is a "daysUntil" string below the name
        if (daysUntilString == "") {
            val layoutParams =
                holder.binding.tvBirthdayInfo.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, (-12 * density).toInt(), 0, 0)
        } else {
            val layoutParams =
                holder.binding.tvBirthdayInfo.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(0, (-2 * density).toInt(), 0, 0)
        }

        //determine signalColor applied to birthday notification icon and text
        val today = LocalDate.now()
        val birthdayIsToday =
            holder.birthday.day == today.dayOfMonth && holder.birthday.month == today.monthValue
        val signalColor =
            if (birthdayIsToday) {
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
        holder.binding.tvRowBirthdayDate.setTextColor(signalColor)
        holder.binding.tvRowBirthdayName.setTextColor(signalColor)

        // determine and apply daysUntilColor for "in 3 days" string, signalColor if there is anything to signal, hint color otherwise
        val daysUntilColor =
            when (signalColor == myActivity.colorForAttr(R.attr.colorOnBackGround)) {
                true -> myActivity.colorForAttr(R.attr.colorHint)
                else -> signalColor
            }
        holder.binding.tvRowBirthdayDays.setTextColor(daysUntilColor)

        // change color of notification circle
        val notificationColor = if (currentBirthday.notify) {
            // if notification is enabled, color red if it's today, or month color otherwise
            if (birthdayIsToday) {
                R.attr.colorBirthdayToday
            } else {
                // month colored circle if not today, but notification enabled
                when (southColors) {
                    true -> monthColors[(currentBirthday.month + 7) % 12].first
                    else -> monthColors[currentBirthday.month - 1].first
                }
            }
        } else {
            // gray circle if notification disabled
            R.attr.colorBirthdayNotifyDisabled
        }
        holder.binding.icRowBirthdayNotification.setColorFilter(
            myActivity.colorForAttr(
                notificationColor
            )
        )

        // edit birthday via long click on main body
        holder.binding.clRowBirthdayMain.setOnLongClickListener {
            BirthdayFr.editBirthdayHolder = holder.birthday
            myFragment.openEditBirthdayDialog()
            true
        }

        // switch expand state via tap on main body
        holder.binding.clRowBirthdayMain.setOnClickListener {
            holder.birthday.expanded = !holder.birthday.expanded
            listInstance.sortAndSaveBirthdays()
            notifyItemChanged(holder.bindingAdapterPosition)
        }

        // enable / disable reminder via tap on notification symbol
        holder.binding.clRowBirthdayDotField.setOnClickListener {
            holder.birthday.notify = !holder.birthday.notify
            myFragment.birthdayListInstance.save()
            notifyItemChanged(holder.bindingAdapterPosition)
        }
    }

    private fun initializeYearViewHolder(holder: BirthdayViewHolder) {
        //YEAR DIVIDER
        holder.binding.cvBirthday.elevation = 0f
        holder.binding.root.layoutParams.height = (70 * density).toInt()
        holder.binding.tvRowBirthdayDivider.textSize = 22f
        holder.binding.tvRowBirthdayDivider.setTextColor(
            myActivity.colorForAttr(R.attr.colorOnBackGround)
        )

        //removes margin at sides so year divider spans parent width
        val params = holder.binding.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(0, marginSide, 0, (0 * density).toInt())

        //change backgroundColor to app background color and show year dividers
        holder.binding.cvBirthday.setBackgroundColor(
            myActivity.colorForAttr(R.attr.colorBackground)
        )
        holder.binding.viewDividerRight.visibility = View.VISIBLE
        holder.binding.viewDividerLeft.visibility = View.VISIBLE
    }

    private fun initializeMonthViewHolder(
        holder: BirthdayViewHolder, currentBirthday: Birthday
    ) {
        //initialize values specific to month divider
        // reintroduces margin at sides
        val params = holder.binding.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        params.setMargins(marginSide, marginSide, marginSide, (2 * density).toInt())

        //sets correct text size, height and text color
        holder.binding.tvRowBirthdayDivider.textSize = 20f
        holder.binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.binding.tvRowBirthdayDivider.setTextColor(
            myActivity.colorForAttr(R.attr.colorCategory)
        )

        //determine the background color of the card
        //daysToRemind acts as a marker in mothDividers to mark the month they display
        // -1 = january, -2 february etc
        val gradientPair: Pair<Int, Int> = when (southColors) {
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
            GradientDrawable.Orientation.TL_BR, intArrayOf(
                myActivity.colorForAttr(gradientPair.second),
                myActivity.colorForAttr(gradientPair.first)
            )
        )

        //check if setting says to use round design, apply correct corner angles
        if (round) myGradientDrawable.cornerRadii = floatArrayOf(cr, cr, cr, cr, 0f, 0f, 0f, 0f)

        holder.binding.cvBirthday.background = myGradientDrawable

        //hide dividers that are specific to year divider
        holder.binding.viewDividerRight.visibility = View.GONE
        holder.binding.viewDividerLeft.visibility = View.GONE
    }

    /**
     * This gets called when currentBirthday.daysToRemind was < 0. This signals that the ViewHolder is
     * either a month or a year divider. At first all elements only necessary for a regular birthday
     * will be hidden or reset, then the proper year or month divider will be initialized.
     * @param holder the BirthdayViewHolder being modified
     * @param currentBirthday the birthday being displayed by this BirthdayViewHolder
     */
    private fun initializeDividerViewHolder(
        holder: BirthdayViewHolder, currentBirthday: Birthday
    ) {
        //show tvRowBirthdayDivider and set its text to the correct month or year name
        holder.binding.tvRowBirthdayDivider.visibility = View.VISIBLE
        holder.binding.tvRowBirthdayDivider.text = currentBirthday.name

        //hide elements only used for a regular birthday
        holder.binding.tvRowBirthdayDate.visibility = View.GONE
        holder.binding.tvRowBirthdayName.visibility = View.GONE
        holder.binding.tvBirthdayInfo.visibility = View.GONE
        holder.binding.icRowBirthdayNotification.visibility = View.GONE
        holder.binding.tvRowBirthdayDays.visibility = View.GONE

        //reset onLongClickListener to prevent editDialog
        holder.itemView.setOnLongClickListener { true }

        //reset onClickListener to prevent expansion
        holder.itemView.setOnClickListener { }

        //daysToRemind==-200 signals a year divider
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
            true -> searchList.size

            //otherwise the number of items will be derived from the regular list instance
            false -> listInstance.size
        }
    }

    class BirthdayViewHolder(itemBinding: RowBirthdayBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        //birthday instance represented by this viewHolder
        lateinit var birthday: Birthday
        var binding = itemBinding
    }

}
