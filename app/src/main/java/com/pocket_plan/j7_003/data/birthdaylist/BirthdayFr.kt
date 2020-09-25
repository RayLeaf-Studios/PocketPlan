package com.pocket_plan.j7_003.data.birthdaylist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
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
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.dialog_add_birthday.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
import kotlinx.android.synthetic.main.new_app_bar.*
import kotlinx.android.synthetic.main.row_birthday.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.LocalDate
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

class BirthdayFr : Fragment() {

    private lateinit var myRecycler: RecyclerView

    var date: LocalDate = LocalDate.now()
    lateinit var myMenu: Menu


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

        val birthdayListInstance: BirthdayList = BirthdayList(MainActivity.act)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.appbar_menu, menu)
        myMenu = menu

        //hide all icons
        for(i in 0 until menu.size()){
            menu.getItem(i).isVisible = false
        }

        //make search item visible
        menu.findItem(R.id.item_sv_birthday).isVisible = true

        searchView = menu.findItem(R.id.item_sv_birthday).actionView as SearchView
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

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.item_sv_birthday -> MainActivity.act.toast("lol")
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

        birthdayListInstance.collapseAll()

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

    fun updateUndoBirthdayIcon() {
        if (deletedBirthday != null && !searching) {
            myMenu.findItem(R.id.item_left)?.setIcon(R.drawable.ic_action_undo)
            myMenu.findItem(R.id.item_left)?.isVisible = true
        } else {
            myMenu.findItem(R.id.item_left)?.isVisible = false
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

        //todo fix this with plurals
        //Set text of reminder (day/ days)
        val addition = when (editBirthdayHolder!!.daysToRemind == 1) {
            true -> ""
            false -> "s"
        }
        tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)


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
                    tvBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
                }

                chosenYear = year
                if (date.year != 0 && date.year != year && !cbSaveBirthdayYear.isChecked) {
                    cbSaveBirthdayYear.isChecked = true
                    tvBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
                }
                date = date.withYear(year).withMonth(month + 1).withDayOfMonth(day)
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
                    if (editBirthdayHolder!!.year != 0) {
                        LocalDate.of(editBirthdayHolder!!.year, date.month, date.dayOfMonth)
                    } else {
                        LocalDate.of(2020, date.month, date.dayOfMonth)
                    }
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

                //todo fix this with plurals
                val dayAddition = when (cachedRemindText == "") {
                    true -> ""
                    false -> "s"
                }
                tvDaysPrior.text = "day" + dayAddition + " prior"
                tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)

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
        val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
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
            val year = date.year

            //get value of daysToRemind, set it to 0 if the text field is empty
            val daysToRemind = when (etDaysToRemind.text.toString()) {
                "" -> 0
                else -> etDaysToRemind.text.toString().toInt()
            }
            val notifyMe = cbNotifyMe.isChecked

            editBirthdayHolder!!.name = name
            editBirthdayHolder!!.day = day
            editBirthdayHolder!!.month = month
            editBirthdayHolder!!.year = year
            editBirthdayHolder!!.daysToRemind = daysToRemind
            editBirthdayHolder!!.notify = notifyMe

            birthdayListInstance.sortAndSaveBirthdays()
            myRecycler.adapter?.notifyDataSetChanged()
            myAlertDialog?.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    fun openAddBirthdayDialog() {
        var yearChanged = false
        var chosenYear = -1

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
                    tvBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
                }

                chosenYear = year
                if (date.year != 0 && date.year != year && !cbSaveBirthdayYear.isChecked) {
                    cbSaveBirthdayYear.isChecked = true
                }
                date = date.withYear(year).withMonth(month + 1).withDayOfMonth(day)
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
                    LocalDate.of(2020, date.month, date.dayOfMonth)
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

                //todo fix this with plurals
                val addition = when (cachedRemindText == "") {
                    true -> ""
                    false -> "s"
                }
                tvDaysPrior.text = "day" + addition + " prior"
                tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)

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
        val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
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

            birthdayListInstance.addBirthday(
                name, date.dayOfMonth, date.monthValue,
                date.year, daysToRemind, false, notifyMe
            )

            myRecycler.adapter?.notifyDataSetChanged()
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
                if (it.name.toLowerCase(Locale.ROOT)
                        .contains(query.toLowerCase(Locale.ROOT)) && it.daysToRemind >= 0
                ) {
                    adjustedList.add(it)
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

    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        BirthdayFr.deletedBirthday = listInstance.getBirthday(viewHolder.adapterPosition)
        listInstance.deleteBirthdayObject(parsed.birthday)
        if (BirthdayFr.searching) {
            BirthdayFr.myFragment.search(BirthdayFr.lastQuery)
        }
        notifyDataSetChanged()
        BirthdayFr.myFragment.updateUndoBirthdayIcon()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        if (position == BirthdayFr.birthdayListInstance.size) {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.layoutParams.height = 280
            holder.itemView.setOnLongClickListener { true }
            holder.itemView.setOnClickListener {}
            return
        }

        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE

        val currentBirthday = when (BirthdayFr.searching) {
            true -> BirthdayFr.adjustedList[position]
            false -> listInstance.getBirthday(position)
        }

        holder.birthday = currentBirthday

        if (currentBirthday.daysToRemind < 0) {
            //initialize month divider design
            holder.tvMonthLabel.text = currentBirthday.name
            holder.tvMonthLabel.textSize = 22F
            holder.txvBirthdayLabelName.text = ""
            holder.myView.setBackgroundResource(R.color.colorBackground)
            holder.itemView.setOnLongClickListener { true }
            holder.itemView.setOnClickListener { }
            holder.itemView.cvBirthdayInfo.visibility = View.GONE
            holder.itemView.icon_bell.visibility = View.GONE
        } else {
            //display bell if birthday has a reminder
            if (currentBirthday.notify) {
                holder.iconBell.visibility = View.VISIBLE
            } else {
                holder.iconBell.visibility = View.INVISIBLE
            }

            //display info if birthday is expanded
            if (currentBirthday.expanded) {
                holder.itemView.cvBirthdayInfo.visibility = View.VISIBLE
                var ageText = MainActivity.act.resources.getString(R.string.birthdayAgeUnknown)
                if (holder.birthday.year != 0) {
                    val birthday = holder.birthday
                    val age = LocalDate.of(birthday.year, birthday.month, birthday.day)
                        .until(LocalDate.now()).years
                    ageText = MainActivity.act.resources.getString(
                        R.string.birthdayAge, age, holder.birthday.year
                    )
                }
                var dayExtension = ""
                if (currentBirthday.daysToRemind != 1) {
                    dayExtension = "s"
                }
                val reminderText =
                    MainActivity.act.resources.getString(
                        R.string.birthdayReminder, currentBirthday.daysToRemind, dayExtension
                    )
                holder.itemView.tvBirthdayInfo.text = ageText + reminderText
            } else {
                holder.itemView.cvBirthdayInfo.visibility = View.GONE
            }

            //initialize regular birthday design
            holder.tvMonthLabel.textSize = 20F
            holder.tvMonthLabel.text = ""
            holder.myView.setBackgroundResource(R.drawable.round_corner_gray)

            //formatting date
            var monthAddition = ""
            if (currentBirthday.month < 10) monthAddition = "0"

            var dayAddition = ""
            if (currentBirthday.day < 10) dayAddition = "0"

            //Display name and date
            holder.txvBirthdayLabelName.text = MainActivity.act.resources.getString(
                R.string.birthdayLabelName, dayAddition, currentBirthday.day.toString(),
                monthAddition, currentBirthday.month.toString(), currentBirthday.name
            )

            // Red background if birthday is today
            if (LocalDate.now().month.value == currentBirthday.month && LocalDate.now().dayOfMonth == currentBirthday.day) {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_winered)
            } else {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_gray)
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
        val txvBirthdayLabelName: TextView = itemView.txvBirthdayLabelName
        val iconBell: ImageView = itemView.icon_bell
        val myView: View = itemView
        val tvMonthLabel: TextView = itemView.tvMonthLabel
        val myConstraintLayout: ConstraintLayout = itemView.constr
    }

}
