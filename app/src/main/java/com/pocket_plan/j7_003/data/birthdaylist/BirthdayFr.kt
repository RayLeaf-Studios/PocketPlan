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
    private lateinit var myRecycler: RecyclerView

    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean

    private var date: LocalDate = LocalDate.now()
    private lateinit var myMenu: Menu
    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK)
    lateinit var myAdapter: BirthdayAdapter
    var searching: Boolean = false
    lateinit var searchView: SearchView

    var birthdayListInstance: BirthdayList = BirthdayList(myActivity)


    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        myAdapter = BirthdayAdapter(this, myActivity)
        super.onCreate(savedInstanceState)
    }

    companion object {
        var deletedBirthday: Birthday? = null

        var editBirthdayHolder: Birthday? = null

        lateinit var adjustedList: ArrayList<Birthday>
        lateinit var lastQuery: String

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        deletedBirthday = null
        inflater.inflate(R.menu.menu_birthdays, menu)
        myMenu = menu
        myMenu.findItem(R.id.item_birthdays_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))

        searchView = menu.findItem(R.id.item_birthdays_search).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo fix this
                //close keyboard?
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    search(newText.toString())
                }
                return true
            }
        }

        searchView.setOnQueryTextListener(textListener)

        val onCloseListener = SearchView.OnCloseListener {
            myActivity.toolBar.title = getString(R.string.menuTitleBirthdays)
            searchView.onActionViewCollapsed()
            searching = false
            updateUndoBirthdayIcon()
            myAdapter.notifyDataSetChanged()
            true
        }

        searchView.setOnCloseListener(onCloseListener)

        searchView.setOnSearchClickListener {
            myActivity.toolBar.title = ""
            searching = true
            adjustedList.clear()
            myAdapter.notifyDataSetChanged()
        }

        editBirthdayHolder = null
        updateUndoBirthdayIcon()
        updateBirthdayMenu()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_birthdays_disable_reminders -> {
                birthdayListInstance.disableAllReminders()
                myAdapter.notifyDataSetChanged()
            }

            R.id.item_birthdays_enable_reminders -> {
                birthdayListInstance.enableAllReminders()
                myAdapter.notifyDataSetChanged()
            }

            R.id.item_birthdays_search -> {/* no-op, listeners for this searchView are set
             in on create options menu*/
            }
            R.id.item_birthdays_undo -> {
                //undo the last deletion
                val addInfo = birthdayListInstance.addFullBirthday(
                    deletedBirthday!!
                )
                deletedBirthday = null
                updateUndoBirthdayIcon()
                updateBirthdayMenu()
                if (round) {
                    if (addInfo.second == 1 && birthdayListInstance[addInfo.first - 1].daysToRemind >= 0) {
                        myAdapter.notifyItemChanged(addInfo.first - 1)
                    }
                }
                myAdapter.notifyItemRangeInserted(addInfo.first, addInfo.second)
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
        adjustedList = arrayListOf()
        myRecycler = myView.recycler_view_birthday

        //collapse all birthdays when reentering fragment
        birthdayListInstance.collapseAll()

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
        //Mark that user changed year
        var yearChanged = false
        var chosenYear = -1

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

        //initialize name edit text with birthday name
        etName.setText(editBirthdayHolder!!.name)
        etName.setSelection(etName.text.length)

        //initialize color of tvNotifyMe with
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
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                tvBirthdayDate.setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                )
                yearChanged = true
                if (year != 2020 && !cbSaveBirthdayYear.isChecked && year != chosenYear) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                    )
                }

                chosenYear = year
                if (date.year != 0 && date.year != year && !cbSaveBirthdayYear.isChecked) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                    )
                }
                date = when (cbSaveBirthdayYear.isChecked) {
                    true -> date.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                    else -> date.withYear(0).withMonth(month + 1).withDayOfMonth(day)
                }
                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }

            var yearToDisplay = 2020
            if (cbSaveBirthdayYear.isChecked && yearChanged) {
                yearToDisplay = date.year
            }

            val dpd = when (dark) {
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
            //if year is supposed to be included,
            date = if (cbSaveBirthdayYear.isChecked) {
                //if user wants so include year, set it to 2020 as default or chosenYear if he changed the year in the date setter before
                if (!yearChanged) {
                    if (editBirthdayHolder!!.year != 0) {
                        LocalDate.of(editBirthdayHolder!!.year, date.month, date.dayOfMonth)
                    } else {
                        LocalDate.of(LocalDate.now().year, date.month, date.dayOfMonth)
                    }
                } else {
                    LocalDate.of(chosenYear, date.month, date.dayOfMonth)
                }
            } else {
                LocalDate.of(2020, date.month, date.dayOfMonth)
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
                tvBirthdayDate.setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGroundTask)
                )
                yearChanged = true

                val prevChecked = cbSaveBirthdayYear.isChecked
                if (pickedYear != 2020 && !cbSaveBirthdayYear.isChecked &&
                    (date.year != 0 || pickedYear != chosenYear)
                ) {

                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        myActivity.colorForAttr(R.attr.colorOnBackGround)
                    )
                }
                chosenYear = pickedYear

                date = date.withYear(pickedYear).withMonth(month + 1).withDayOfMonth(day)

                when {
                    abs(LocalDate.now().until(date).toTotalMonths()) < 12 -> {
                        if (!prevChecked) {
                            cbSaveBirthdayYear.isChecked = false

                            tvSaveYear.setTextColor(
                                myActivity.colorForAttr(R.attr.colorHint)
                            )
                        }
                    }
                }

                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }

            var yearToDisplay = 2020
            if (cbSaveBirthdayYear.isChecked && yearChanged) {
                yearToDisplay = date.year
            } else if (cbSaveBirthdayYear.isChecked) {
                yearToDisplay = LocalDate.now().year
            }
            val dpd = when (dark) {
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
            val notifyMe = cbNotifyMe.isChecked

            val yearToSave = when (cbSaveBirthdayYear.isChecked) {
                true -> date.year
                else -> 0
            }
            val addInfo = birthdayListInstance.addBirthday(
                name, date.dayOfMonth, date.monthValue,
                yearToSave, daysToRemind, false, notifyMe
            )

            myRecycler.adapter?.notifyItemRangeInserted(addInfo.first, addInfo.second)
            if (round) {
                if (addInfo.second == 1 && birthdayListInstance[addInfo.first - 1].daysToRemind >= 0) {
                    myAdapter.notifyItemChanged(addInfo.first - 1)
                }
            }
            updateBirthdayMenu()
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
        myAdapter.notifyDataSetChanged()
    }

}

class SwipeToDeleteBirthday(private var adapter: BirthdayAdapter, direction: Int, birthdayFr: BirthdayFr) :
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

    //calculate corner radius
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)
    private val elevation = myActivity.resources.getDimension(R.dimen.elevation)


    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        BirthdayFr.deletedBirthday = listInstance.getBirthday(viewHolder.adapterPosition)
        val deleteInfo = listInstance.deleteBirthdayObject(parsed.birthday)
        if (myFragment.searching) {
            myFragment.search(BirthdayFr.lastQuery)
        }
        if (round) {
            if (deleteInfo.second == 1 && myFragment.birthdayListInstance[deleteInfo.first - 1].daysToRemind >= 0) {
                myFragment.myAdapter.notifyItemChanged(deleteInfo.first - 1)
            }
        }
        notifyItemRangeRemoved(deleteInfo.first, deleteInfo.second)
        myFragment.updateUndoBirthdayIcon()
        myFragment.updateBirthdayMenu()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        //Last birthday is spacer birthday
        if (position == myFragment.birthdayListInstance.size) {
            val density = myActivity.resources.displayMetrics.density
            holder.itemView.layoutParams.height = (100 * density).toInt()
            holder.itemView.visibility = View.INVISIBLE
            holder.cvBirthday.elevation = 0f
            holder.itemView.setOnLongClickListener { true }
            holder.itemView.setOnClickListener {}
            return
        }

        //reset parameters visibility and height, to undo spacer birthday values (recycler view)
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE
        holder.cvBirthday.elevation = elevation

        //get birthday for this view holder, from BirthdayFr.adjustedList if this Fragment currently is
        //in search mode, or from listInstance.getBirthday(position) if its in regular display mode
        val currentBirthday = when (myFragment.searching) {
            true -> BirthdayFr.adjustedList[position]
            false -> listInstance.getBirthday(position)
        }

        //save a reference for the birthday saved in this holder
        holder.birthday = currentBirthday

        if (currentBirthday.daysToRemind < 0) {
            //hide everything not related to year or month divider
            holder.tvRowBirthdayDivider.visibility = View.VISIBLE
            holder.tvRowBirthdayDivider.text = currentBirthday.name
            holder.tvRowBirthdayDate.text = ""
            holder.tvRowBirthdayName.text = ""
            holder.itemView.setOnLongClickListener { true }
            holder.itemView.setOnClickListener { }
            holder.itemView.tvBirthdayInfo.visibility = View.GONE
            holder.itemView.icon_bell.visibility = View.GONE

            if (currentBirthday.daysToRemind == -200) {
                //YEAR
                holder.cvBirthday.elevation = 0f
                holder.itemView.layoutParams.height = (70 * density).toInt()
                holder.tvRowBirthdayDivider.textSize = 22f
                holder.tvRowBirthdayDivider.setTextColor(
                    myActivity.colorForAttr(R.attr.colorOnBackGround)
                )

                val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, marginSide, 0, (0 * density).toInt())

                holder.cvBirthday.setBackgroundColor(
                    myActivity.colorForAttr(R.attr.colorBackground)
                )


            } else {
                val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(marginSide, marginSide, marginSide, (2 * density).toInt())

                //MONTH
                holder.tvRowBirthdayDivider.textSize = 20f
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                holder.tvRowBirthdayDivider.setTextColor(
                    myActivity.colorForAttr(R.attr.colorCategory)
                )

                //determine the background color of the card
                val gradientPair: Pair<Int, Int> =
                    when (southColors) {
                        true ->
                            when (currentBirthday.daysToRemind) {
                                -7 -> Pair(R.attr.colorMonth2, R.attr.colorMonth1)
                                -8 -> Pair(R.attr.colorMonth3, R.attr.colorMonth2)
                                -9 -> Pair(R.attr.colorMonth4, R.attr.colorMonth3)
                                -10 -> Pair(R.attr.colorMonth5, R.attr.colorMonth4)
                                -11 -> Pair(R.attr.colorMonth6, R.attr.colorMonth5)
                                -12 -> Pair(R.attr.colorMonth7, R.attr.colorMonth6)
                                -1 -> Pair(R.attr.colorMonth8, R.attr.colorMonth7)
                                -2 -> Pair(R.attr.colorMonth9, R.attr.colorMonth8)
                                -3 -> Pair(R.attr.colorMonth10, R.attr.colorMonth9)
                                -4 -> Pair(R.attr.colorMonth11, R.attr.colorMonth10)
                                -5 -> Pair(R.attr.colorMonth12, R.attr.colorMonth11)
                                else -> Pair(R.attr.colorMonth1, R.attr.colorMonth12)
                            }
                        else ->
                            when (currentBirthday.daysToRemind) {
                                -1 -> Pair(R.attr.colorMonth2, R.attr.colorMonth1)
                                -2 -> Pair(R.attr.colorMonth3, R.attr.colorMonth2)
                                -3 -> Pair(R.attr.colorMonth4, R.attr.colorMonth3)
                                -4 -> Pair(R.attr.colorMonth5, R.attr.colorMonth4)
                                -5 -> Pair(R.attr.colorMonth6, R.attr.colorMonth5)
                                -6 -> Pair(R.attr.colorMonth7, R.attr.colorMonth6)
                                -7 -> Pair(R.attr.colorMonth8, R.attr.colorMonth7)
                                -8 -> Pair(R.attr.colorMonth9, R.attr.colorMonth8)
                                -9 -> Pair(R.attr.colorMonth10, R.attr.colorMonth9)
                                -10 -> Pair(R.attr.colorMonth11, R.attr.colorMonth10)
                                -11 -> Pair(R.attr.colorMonth12, R.attr.colorMonth11)
                                else -> Pair(R.attr.colorMonth1, R.attr.colorMonth12)
                            }
                    }


                val myGradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        myActivity.colorForAttr(gradientPair.second),
                        myActivity.colorForAttr(gradientPair.first)
                    )
                )
                if (round) myGradientDrawable.cornerRadii =
                    floatArrayOf(cr, cr, cr, cr, 0f, 0f, 0f, 0f)
                holder.cvBirthday.background = myGradientDrawable
            }

            //check if its a year divider, and display divider lines if its the case
            val dividerVisibility = when (currentBirthday.daysToRemind == -200) {
                true -> View.VISIBLE
                else -> View.GONE
            }
            holder.myDividerRight.visibility = dividerVisibility
            holder.myDividerLeft.visibility = dividerVisibility
            return
        }

        //set regular birthday background
        val colorA =
            myActivity.colorForAttr(R.attr.colorHomePanel)

        val colorB =
            myActivity.colorForAttr(R.attr.colorHomePanel)

        val myGradientDrawable =
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(colorA, colorB))


        //reset margin
        val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        if (myFragment.searching) {
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


        var dateString =
            currentBirthday.day.toString().padStart(2, '0')


        if (SettingsManager.getSetting(SettingId.BIRTHDAY_SHOW_MONTH) as Boolean) {
            dateString += "." + currentBirthday.month.toString()
                .padStart(2, '0')
        }


        //Display name and date
        holder.tvRowBirthdayDate.text = dateString
        val daysUntilString = when (currentBirthday.daysUntil()) {
            0 -> myActivity.resources.getString(R.string.birthdayToday)
            1 -> myActivity.resources.getString(R.string.birthdayTomorrow)
            else -> if (currentBirthday.daysUntil() < 30) {
                myActivity.resources.getString(R.string.birthdayIn) + " " + currentBirthday.daysUntil()
                    .toString() + " " + myActivity.resources.getQuantityString(
                    R.plurals.dayIn,
                    currentBirthday.daysUntil()
                )
            } else {
                ""
            }
        }
        val birthdayText = currentBirthday.name + " " + daysUntilString
        holder.tvRowBirthdayName.text = birthdayText

        //set icon / text color to blue if birthday is today, to pink if its daysToRemind < days.Until, to white otherwise
        val today = LocalDate.now()
        if (holder.birthday.day == today.dayOfMonth && holder.birthday.month == today.monthValue) {
            holder.tvRowBirthdayDate.setTextColor(
                myActivity.colorForAttr(R.attr.colorBirthdayToday)
            )
            holder.tvRowBirthdayName.setTextColor(
                myActivity.colorForAttr(R.attr.colorBirthdayToday)
            )
            holder.iconBell.setColorFilter(
                myActivity.colorForAttr(R.attr.colorBirthdayToday)
            )
        } else if (holder.birthday.daysToRemind > 0 && holder.birthday.daysUntil() <= holder.birthday.daysToRemind) {
            holder.tvRowBirthdayDate.setTextColor(
                myActivity.colorForAttr(R.attr.colorBirthdaySoon)
            )
            holder.tvRowBirthdayName.setTextColor(
                myActivity.colorForAttr(R.attr.colorBirthdaySoon)
            )
            holder.iconBell.setColorFilter(
                myActivity.colorForAttr(R.attr.colorBirthdaySoon)
            )
        } else {
            holder.tvRowBirthdayDate.setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
            holder.tvRowBirthdayName.setTextColor(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
            holder.iconBell.setColorFilter(
                myActivity.colorForAttr(R.attr.colorOnBackGround)
            )
        }

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

        //opens dialog to edit this birthday
        holder.itemView.tvRowBirthdayName.setOnLongClickListener {
            BirthdayFr.editBirthdayHolder = holder.birthday
            myFragment.openEditBirthdayDialog()
            true
        }
        holder.itemView.tvRowBirthdayDate.setOnLongClickListener {
            BirthdayFr.editBirthdayHolder = holder.birthday
            myFragment.openEditBirthdayDialog()
            true
        }
        holder.itemView.icon_bell.setOnLongClickListener {
            BirthdayFr.editBirthdayHolder = holder.birthday
            myFragment.openEditBirthdayDialog()
            true
        }

        //expands info
        holder.itemView.tvRowBirthdayDate.setOnClickListener {
            holder.birthday.expanded = !holder.birthday.expanded
            listInstance.sortAndSaveBirthdays()
            notifyItemChanged(holder.adapterPosition)
        }
        holder.itemView.tvRowBirthdayName.setOnClickListener {
            holder.birthday.expanded = !holder.birthday.expanded
            listInstance.sortAndSaveBirthdays()
            notifyItemChanged(holder.adapterPosition)
        }

        holder.itemView.icon_bell.setOnClickListener {
            holder.birthday.notify = !holder.birthday.notify
            myFragment.myAdapter.notifyItemChanged(holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return when (myFragment.searching) {
            true -> BirthdayFr.adjustedList.size
            false -> listInstance.size + 1
        }
    }

    class BirthdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_task and meta data
         * like position, it also holds references to views inside of the layout
         */
        lateinit var birthday: Birthday
        val tvRowBirthdayName: TextView = itemView.tvRowBirthdayName
        val tvRowBirthdayDate: TextView = itemView.tvRowBirthdayDate
        val iconBell: ImageView = itemView.icon_bell
        val myView: View = itemView
        val tvRowBirthdayDivider: TextView = itemView.tvRowBirthdayDivider
        val cvBirthday: ConstraintLayout = itemView.cvBirthday
        val myDividerLeft: View = itemView.viewDividerLeft
        val myDividerRight: View = itemView.viewDividerRight
    }

}
