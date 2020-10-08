package com.pocket_plan.j7_003.data.birthdaylist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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


/**
 * A simple [Fragment] subclass.
 */

class BirthdayFr : Fragment() {

    //initialize recycler view
    private lateinit var myRecycler: RecyclerView

    var date: LocalDate = LocalDate.now()
    private lateinit var myMenu: Menu


    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)

    }

    companion object {
        var deletedBirthday: Birthday? = null

        var editBirthdayHolder: Birthday? = null
        lateinit var myAdapter: BirthdayAdapter

        var searching: Boolean = false
        lateinit var adjustedList: ArrayList<Birthday>
        lateinit var lastQuery: String

        lateinit var myFragment: BirthdayFr

        lateinit var searchView: SearchView

        var birthdayListInstance: BirthdayList = BirthdayList(MainActivity.act)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        deletedBirthday = null
        inflater.inflate(R.menu.menu_birthdays, menu)
        myMenu = menu

        searchView = menu.findItem(R.id.item_birthdays_search).actionView as SearchView
        val textListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                //todo fix this
                //close keyboard?
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (searching) {
                    myFragment.search(newText.toString())
                }
                return true
            }
        }

        searchView.setOnQueryTextListener(textListener)

        val onCloseListener = SearchView.OnCloseListener {
            MainActivity.toolBar.title = getString(R.string.menuTitleBirthdays)
            searchView.onActionViewCollapsed()
            searching = false
            updateUndoBirthdayIcon()
            myAdapter.notifyDataSetChanged()
            true
        }

        searchView.setOnCloseListener(onCloseListener)

        searchView.setOnSearchClickListener {
            MainActivity.toolBar.title = ""
            searching = true
            adjustedList.clear()
            myAdapter.notifyDataSetChanged()
        }

        editBirthdayHolder = null
        updateUndoBirthdayIcon()
        updateSearchBirthdaysIcon()

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item_birthdays_search -> {/* no-op, listeners for this searchView are set
             in on create options menu*/
            }
            R.id.item_birthdays_undo -> {
                //undo the last deletion
                birthdayListInstance.addFullBirthday(
                    deletedBirthday!!
                )
                deletedBirthday = null
                updateUndoBirthdayIcon()
                myAdapter.notifyDataSetChanged()
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
        myFragment = this

        //initialize recyclerview and adapter
        myAdapter = BirthdayAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        //initialize and attach swipe helpers
        val swipeHelperLeft =
            ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.LEFT))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight =
            ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.RIGHT))
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

    fun updateSearchBirthdaysIcon() {
        myMenu.findItem(R.id.item_birthdays_search).isVisible = birthdayListInstance.size > 0
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
                ContextCompat.getColor(
                    MainActivity.act,
                    R.color.colorOnBackGround
                )
            )
        } else {
            tvNotifyMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
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
            true -> R.color.colorOnBackGround
            else -> R.color.colorHint
        }
        tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, tvSaveYearColor))

        //initialize value of save year checkbox
        cbSaveBirthdayYear.isChecked = hasYear

        //set correct color for RemindMe DaysPrior
        val remindMeColor = when (editBirthdayHolder!!.daysToRemind) {
            0 -> R.color.colorHint
            else -> R.color.colorOnBackGround
        }
        tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, remindMeColor))
        etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, remindMeColor))
        tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, remindMeColor))

        val daysToRemind = when (etDaysToRemind.text.toString() == "") {
            true -> 0
            else -> etDaysToRemind.text.toString().toInt()
        }

        val daysPriorTextEdit =
            MainActivity.act.resources.getQuantityText(R.plurals.day, daysToRemind)
                .toString() +" "+MainActivity.act.resources.getString(R.string.birthdaysDaysPrior)
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
                    ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround)
                } else {
                    ContextCompat.getColor(MainActivity.act, R.color.colorHint)
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
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
                yearChanged = true
                if (year != 2020 && !cbSaveBirthdayYear.isChecked && year != chosenYear) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                }

                chosenYear = year
                if (date.year != 0 && date.year != year && !cbSaveBirthdayYear.isChecked) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
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
            val dpd = DatePickerDialog(
                MainActivity.act,
                R.style.MyDatePickerStyle,
                dateSetListener,
                yearToDisplay,
                date.monthValue - 1,
                date.dayOfMonth
            )
            dpd.show()
            dpd.getButton(AlertDialog.BUTTON_NEGATIVE)
                .setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
            dpd.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
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
            val color = when (cbSaveBirthdayYear.isChecked) {
                true -> R.color.colorOnBackGround
                false -> R.color.colorHint
            }
            tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, color))
        }


        val textWatcherReminder = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cachedRemindText = etDaysToRemind.text.toString()
                val color = if (cachedRemindText == "" || cachedRemindText.toInt() == 0) {
                    ContextCompat.getColor(MainActivity.act, R.color.colorHint)
                } else {
                    ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround)
                }
                tvRemindMe.setTextColor(color)
                etDaysToRemind.setTextColor(color)
                tvDaysPrior.setTextColor(color)
                val daysToRemindTc = when (etDaysToRemind.text.toString() == "") {
                    true -> 0
                    else -> etDaysToRemind.text.toString().toInt()
                }

                val daysPriorTextEditTc =
                    MainActivity.act.resources.getQuantityText(R.plurals.day, daysToRemindTc)
                        .toString() + " " + MainActivity.act.resources.getString(R.string.birthdaysDaysPrior)
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
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake2)
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
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
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

        val initPriorText = MainActivity.act.resources.getQuantityString(
            R.plurals.day,
            0
        ) + " " + MainActivity.act.resources.getString(
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
                    ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround)
                } else {
                    ContextCompat.getColor(MainActivity.act, R.color.colorHint)
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
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
                yearChanged = true
                if (pickedYear != 2020 && !cbSaveBirthdayYear.isChecked && pickedYear != chosenYear) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                }

                chosenYear = pickedYear
                if (date.year != 0 && pickedYear != 2020 && !cbSaveBirthdayYear.isChecked) {
                    cbSaveBirthdayYear.isChecked = true
                    tvSaveYear.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                }
                date = when (cbSaveBirthdayYear.isChecked) {
                    true -> date.withYear(pickedYear).withMonth(month + 1).withDayOfMonth(day)
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
            } else if (cbSaveBirthdayYear.isChecked) {
                yearToDisplay = LocalDate.now().year
            }
            val dpd = DatePickerDialog(
                MainActivity.act,
                R.style.MyDatePickerStyle,
                dateSetListener,
                yearToDisplay,
                date.monthValue - 1,
                date.dayOfMonth
            )
            dpd.show()
        }

        //checkbox to include year
        cbSaveBirthdayYear.setOnClickListener {
            //if year is supposed to be included,
            date = if (cbSaveBirthdayYear.isChecked) {
                //if user wants so include year, set it to 2020 as default or chosenYear if he changed the year in the date setter before
                if (!yearChanged) {
                    LocalDate.of(LocalDate.now().year, date.month, date.dayOfMonth)
                } else {
                    LocalDate.of(chosenYear, date.month, date.dayOfMonth)
                }
            } else {
                LocalDate.of(0, date.month, date.dayOfMonth)
            }

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
                true -> R.color.colorOnBackGround
                false -> R.color.colorHint
            }
            tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, color))
        }


        val textWatcherReminder = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                cachedRemindText = etDaysToRemind.text.toString()
                var color = ContextCompat.getColor(MainActivity.act, R.color.colorHint)
                val amount: Int
                if (cachedRemindText == "" || cachedRemindText.toInt() == 0) {
                    amount = 0
                } else {
                    color = ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround)
                    amount = cachedRemindText.toInt()
                }
                tvRemindMe.setTextColor(color)
                etDaysToRemind.setTextColor(color)
                tvDaysPrior.setTextColor(color)

                val result = MainActivity.act.resources.getQuantityString(
                    R.plurals.day,
                    amount
                ) + " " + MainActivity.act.resources.getString(
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
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake2)
                tvNotifyMe.startAnimation(animationShake)
                val animationShakeCb =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake2)
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
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                etName.startAnimation(animationShake)
                return@setOnClickListener
            }


            if (!yearChanged) {
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
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
            birthdayListInstance.addBirthday(
                name, date.dayOfMonth, date.monthValue,
                yearToSave, daysToRemind, false, notifyMe
            )

            myRecycler.adapter?.notifyDataSetChanged()
            updateSearchBirthdaysIcon()
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

class SwipeToDeleteBirthday(var adapter: BirthdayAdapter, direction: Int) :
    ItemTouchHelper.SimpleCallback(0, direction) {
    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val parsed = viewHolder as BirthdayAdapter.BirthdayViewHolder
        return if (viewHolder.adapterPosition == BirthdayFr.birthdayListInstance.size || parsed.birthday.daysToRemind < 0) {
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


class BirthdayAdapter :
    RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {
    private val listInstance = BirthdayFr.birthdayListInstance
    private val density = MainActivity.act.resources.displayMetrics.density
    private val marginSide = (density*20).toInt()
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean

    //calculate corner radius
    private val cr = MainActivity.act.resources.getDimension(R.dimen.cornerRadius)


    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        BirthdayFr.deletedBirthday = listInstance.getBirthday(viewHolder.adapterPosition)
        listInstance.deleteBirthdayObject(parsed.birthday)
        if (BirthdayFr.searching) {
            BirthdayFr.myFragment.search(BirthdayFr.lastQuery)
        }
        notifyDataSetChanged()
        BirthdayFr.myFragment.updateUndoBirthdayIcon()
        BirthdayFr.myFragment.updateSearchBirthdaysIcon()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        //Last birthday is spacer birthday
        if (position == BirthdayFr.birthdayListInstance.size) {
            val density = MainActivity.act.resources.displayMetrics.density
            holder.itemView.layoutParams.height = (100*density).toInt()
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.setOnLongClickListener { true }
            holder.itemView.setOnClickListener {}
            return
        }

        //reset parameters visibility and height, to undo spacer birthday values (recycler view)
        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE

        //get birthday for this view holder, from BirthdayFr.adjustedList if this Fragment currently is
        //in search mode, or from listInstance.getBirthday(position) if its in regular display mode
        val currentBirthday = when (BirthdayFr.searching) {
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
                holder.itemView.layoutParams.height = (70*density).toInt()
                holder.tvRowBirthdayDivider.textSize = 22f
                holder.tvRowBirthdayDivider.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )

                val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, marginSide, 0, (0*density).toInt())

                holder.cvBirthday.setBackgroundColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorBackground
                    )
                )


            } else {
                val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
                if(position==0){
                    params.setMargins(marginSide, marginSide, marginSide, (2*density).toInt())
                }else{
                    params.setMargins(marginSide, marginSide, marginSide, (2*density).toInt())
                }

                //MONTH
                holder.tvRowBirthdayDivider.textSize = 20f
                holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
                holder.tvRowBirthdayDivider.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )

                //determine the background color of the card
                val gradientPair: Pair<Int, Int> = when (currentBirthday.daysToRemind) {
                    -1 -> Pair(R.color.colorMonth2, R.color.colorMonth1)
                    -2 -> Pair(R.color.colorMonth3, R.color.colorMonth2)
                    -3 -> Pair(R.color.colorMonth4, R.color.colorMonth3)
                    -4 -> Pair(R.color.colorMonth5, R.color.colorMonth4)
                    -5 -> Pair(R.color.colorMonth6, R.color.colorMonth5)
                    -6 -> Pair(R.color.colorMonth7, R.color.colorMonth6)
                    -7 -> Pair(R.color.colorMonth8, R.color.colorMonth7)
                    -8 -> Pair(R.color.colorMonth9, R.color.colorMonth8)
                    -9 -> Pair(R.color.colorMonth10, R.color.colorMonth9)
                    -10 -> Pair(R.color.colorMonth11, R.color.colorMonth10)
                    -11 -> Pair(R.color.colorMonth12, R.color.colorMonth11)
                    else -> Pair(R.color.colorMonth1, R.color.colorMonth12)
                }

                val myGradientDrawable = GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        ContextCompat.getColor(MainActivity.act, gradientPair.second),
                        ContextCompat.getColor(MainActivity.act, gradientPair.first)
                    )
                )
                if(round) myGradientDrawable.cornerRadii = floatArrayOf(cr,cr,cr,cr,0f,0f,0f,0f)
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
        val colorA = ContextCompat.getColor(MainActivity.act, R.color.colorBackgroundListElement)
        val colorB = ContextCompat.getColor(MainActivity.act, R.color.colorBackgroundListElement)
        val myGradientDrawable =
            GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, intArrayOf(colorA, colorB))


//        myGradientDrawable.cornerRadius = 0f

        //reset margin
        val params = holder.cvBirthday.layoutParams as ViewGroup.MarginLayoutParams
        if(BirthdayFr.searching){
            if(position==0){
                params.setMargins(marginSide, marginSide, marginSide, (density*10).toInt())
            }
            else{
                params.setMargins(marginSide, (density*10).toInt(), marginSide, (density*10).toInt())
            }
        }
        else{
            params.setMargins(marginSide, (density*1).toInt(), marginSide, (density*1).toInt())
            if(round){
                if((holder.adapterPosition==BirthdayFr.birthdayListInstance.size-1)||(BirthdayFr.birthdayListInstance[holder.adapterPosition+1].daysToRemind<0)){
                    myGradientDrawable.cornerRadii = floatArrayOf(0f,0f,0f,0f,cr,cr,cr,cr)
                }
            }
        }

        holder.cvBirthday.background = myGradientDrawable

        //initialize regular birthday design
        holder.tvRowBirthdayDivider.visibility = View.GONE
        holder.myDividerLeft.visibility = View.GONE
        holder.myDividerRight.visibility = View.GONE

        //display bell if birthday has a reminder
        if (currentBirthday.notify) {
            holder.iconBell.visibility = View.VISIBLE
        } else {
            holder.iconBell.visibility = View.INVISIBLE
        }

        //display info if birthday is expanded
        if (currentBirthday.expanded) {
            holder.itemView.tvBirthdayInfo.visibility = View.VISIBLE
            var ageText = MainActivity.act.resources.getString(R.string.birthdayAgeUnknown)
            if (holder.birthday.year != 0) {
                val birthday = holder.birthday
                val age = LocalDate.of(birthday.year, birthday.month, birthday.day)
                    .until(LocalDate.now()).years
                val ageYearText = MainActivity.act.resources.getQuantityString(R.plurals.year, age)
                ageText =
                    age.toString() + " " + ageYearText + MainActivity.act.resources.getString(R.string.birthdayOldBornIn) + birthday.year
            }
            val reminderDayString = MainActivity.act.resources.getQuantityString(
                R.plurals.day,
                currentBirthday.daysToRemind
            )
            val reminderText =
                MainActivity.act.resources.getString(
                    R.string.birthdayReminder, currentBirthday.daysToRemind, reminderDayString
                )
            val infoText = ageText + reminderText
            holder.itemView.tvBirthdayInfo.text = infoText
        } else {
            holder.itemView.tvBirthdayInfo.visibility = View.GONE
        }


        val dateString =
            currentBirthday.day.toString().padStart(2, '0') + "." + currentBirthday.month.toString()
                .padStart(2, '0')

        //Display name and date
        holder.tvRowBirthdayDate.text = dateString
        holder.tvRowBirthdayName.text = currentBirthday.name


        //todo figure a way out to display this in another way, blue under blue month label => low contrast
        //maybe animation?
        // Blue background if birthday is today

        val today = LocalDate.now()
        if(holder.birthday.day == today.dayOfMonth && holder.birthday.month == today.monthValue){
           holder.tvRowBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorTiefkühlL))
           holder.tvRowBirthdayName.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorTiefkühlL))
           holder.iconBell.setColorFilter(ContextCompat.getColor(MainActivity.act, R.color.colorTiefkühlL))
        }else{
            holder.tvRowBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
            holder.tvRowBirthdayName.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
            holder.iconBell.setColorFilter(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
        }

        //opens dialog to edit this birthday
        holder.itemView.setOnLongClickListener {
            BirthdayFr.editBirthdayHolder = holder.birthday
            BirthdayFr.myFragment.openEditBirthdayDialog()
            true
        }

        //expands info
        holder.itemView.setOnClickListener {
            holder.birthday.expanded = !holder.birthday.expanded
            listInstance.sortAndSaveBirthdays()
            notifyItemChanged(holder.adapterPosition)
        }


    }

    override fun getItemCount(): Int {
        return when (BirthdayFr.searching) {
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
