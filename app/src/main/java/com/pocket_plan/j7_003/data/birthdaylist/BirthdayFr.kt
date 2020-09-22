package com.pocket_plan.j7_003.data.birthdaylist

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import kotlinx.android.synthetic.main.dialog_add_birthday.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
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
    private var anyDateSet = false


    companion object {
        var deletedBirthday: Birthday? = null

        var editBirthdayHolder: Birthday? = null
        lateinit var myAdapter: BirthdayAdapter

        var searching: Boolean = false
        lateinit var adjustedList: ArrayList<Birthday>
        lateinit var lastQuery: String

        lateinit var myFragment: BirthdayFr

        val birthdayListInstance: BirthdayList = BirthdayList(MainActivity.act)
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


    @SuppressLint("InflateParams")
    fun openBirthdayDialog() {
        val editing = editBirthdayHolder != null
        anyDateSet = false

        if (editing) {
            anyDateSet = true
            date = LocalDate.of(
                editBirthdayHolder!!.year,
                editBirthdayHolder!!.month,
                editBirthdayHolder!!.day
            )
        } else {
            date = LocalDate.now()
        }

        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(activity).inflate(R.layout.dialog_add_birthday, null)

        //initialize instances
        val nameField = myDialogView.etName
        val tvBirthdayDate = myDialogView.tvBirthdayDate
        val cbSaveBirthdayYear = myDialogView.cbSaveBirthdayYear
        val tvSaveYear = myDialogView.tvSaveYear
        val etDaysToRemind = myDialogView.etDaysToRemind
        val etName = myDialogView.etName
        val tvNotifyMe = myDialogView.tvNotifyMe
        val cbNotifyMe = myDialogView.cbNotifyMe

        //initialize name field if editing
        if (editing) {
            etName.setText(editBirthdayHolder!!.name)
            etName.setSelection(etName.text.length)
        }

        //default checkbox for "Notify me" with true, if adding a new birthday
        if (!editing) {
            cbNotifyMe.isChecked = true
        }

        cbNotifyMe.setOnClickListener {
            if (cbNotifyMe.isChecked) {
                tvNotifyMe.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
            } else {
                tvNotifyMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
            }
        }

        //set checkbox and "Notify me" textColor to correct state depending on birthday that is being edited
        if (editing) {
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
            cbNotifyMe.isChecked = editBirthdayHolder!!.notify
        }

        //initialize "Remind me" .. "Days prior" Text views
        val tvRemindMe = myDialogView.tvRemindMe
        val tvDaysPrior = myDialogView.tvDaysPrior

        //set right color for RemindMe DaysPrior
        if (!editing || (editing && editBirthdayHolder!!.daysToRemind == 0)) {
            tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
            etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
            tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
        }

        if (editing) {
            val addition = when (editBirthdayHolder!!.daysToRemind == 1) {
                true -> ""
                false -> "s"
            }
            tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)
//                "day" + addition + " prior"
        }


        //initialize date text
        val chooseDateText = when (editBirthdayHolder != null) {
            true -> {
                var yearString = ""
                if (editBirthdayHolder!!.year != 0) {
                    yearString = "." + editBirthdayHolder!!.year.toString()
                }
                editBirthdayHolder!!.day.toString().padStart(2, '0') + "." +
                        editBirthdayHolder!!.month.toString()
                            .padStart(2, '0') + yearString
            }
            false -> resources.getText(R.string.birthdayChooseDate)
        }
        tvBirthdayDate.text = chooseDateText


        tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))

        //initialize value of save year checkbox
        if (editBirthdayHolder != null) {
            if (editBirthdayHolder!!.year != 0) {
                cbSaveBirthdayYear.isChecked = true
                tvSaveYear.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
            }
        } else {
            cbSaveBirthdayYear.isChecked = false
            tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
        }

        //initialize reminder text
        val daysToRemindText = when (editBirthdayHolder != null) {
            true -> editBirthdayHolder!!.daysToRemind.toString()
            else -> "0"
        }

        val textWatcherReminder = object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val enteredText = etDaysToRemind.text.toString()
                if (enteredText == "" || enteredText.toInt() == 0) {
                    tvRemindMe.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                    etDaysToRemind.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                    tvDaysPrior.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                } else {
                    tvRemindMe.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                    etDaysToRemind.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                    tvDaysPrior.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorOnBackGround
                        )
                    )
                    val addition = when (enteredText.toInt() == 1) {
                        true -> ""
                        false -> "s"
                    }
//                    tvDaysPrior.text = "day" + addition + " prior"
                    tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        etDaysToRemind.addTextChangedListener(textWatcherReminder)

        etDaysToRemind.setText(daysToRemindText)
        etDaysToRemind.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                etDaysToRemind.setText("")
            } else {
                val enteredText = etDaysToRemind.text.toString()
                if (enteredText == "" || enteredText.toInt() == 0) {
                    etDaysToRemind.setText("0")
                    tvRemindMe.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                    etDaysToRemind.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                    tvDaysPrior.setTextColor(
                        ContextCompat.getColor(
                            MainActivity.act,
                            R.color.colorHint
                        )
                    )
                }
                val addition = when (enteredText == "1") {
                    true -> ""
                    false -> "s"
                }
//                tvDaysPrior.text = "day" + addition + " prior"
                tvDaysPrior.text = resources.getText(R.string.birthdaysDaysPrior, addition)

            }
        }

        //textWatcher to reset color of nameField after typing again
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nameField.setHintTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorHint
                    )
                )
                nameField.background.mutate().setColorFilter(
                    resources.getColor(R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                )
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        nameField.addTextChangedListener(textWatcher)

        //on click listener to open date picker
        tvBirthdayDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                tvBirthdayDate.setTextColor(
                    ContextCompat.getColor(
                        MainActivity.act,
                        R.color.colorOnBackGround
                    )
                )
                anyDateSet = true
                date = date.withYear(year).withMonth(month + 1).withDayOfMonth(day)
                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }

            var yearToDisplay = date.year
            if (editing && cbSaveBirthdayYear.isChecked) {
                yearToDisplay = editBirthdayHolder?.year!!
            } else if (editing) {
                yearToDisplay = 2020
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
            if (cbSaveBirthdayYear.isChecked) {
                if (editing && date.year == 0) {
                    date = LocalDate.of(LocalDate.now().year, date.month, date.dayOfMonth)
                }
            }

            if (anyDateSet) {
                val dayMonthString =
                    date.dayOfMonth.toString().padStart(2, '0') + "." + (date.monthValue).toString()
                        .padStart(2, '0')
                tvBirthdayDate.text = when (cbSaveBirthdayYear.isChecked) {
                    false -> dayMonthString
                    else -> dayMonthString + "." + date.year.toString()
                }
            }
            val color = when (cbSaveBirthdayYear.isChecked) {
                true -> R.color.colorOnBackGround
                false -> R.color.colorHint
            }
            tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, color))
        }

        //AlertDialogBuilder
        val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
        myTitle.tvDialogTitle.text = when (editing) {
            true -> resources.getText(R.string.birthdayDialogEditTitle)
            else -> resources.getText(R.string.birthdayDialogAddTitle)
        }
        myBuilder?.setCustomTitle(myTitle)


        //initialize button text
        myDialogView.btnConfirmBirthday.text = when (editing) {
            true -> resources.getText(R.string.birthdayDialogEdit)
            else -> resources.getText(R.string.birthdayDialogAdd)
        }


        //show dialog
        val myAlertDialog = myBuilder?.create()
        myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        myAlertDialog?.show()

        //button to confirm adding of birthday
        myDialogView.btnConfirmBirthday.setOnClickListener {
            val name = nameField.text.toString()

            //tell user to enter a name if none is entered
            if (name == "") {
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                nameField.startAnimation(animationShake)
                return@setOnClickListener
            }

            if (!anyDateSet) {
                val animationShake =
                    AnimationUtils.loadAnimation(MainActivity.act, R.anim.shake)
                tvBirthdayDate.startAnimation(animationShake)
                return@setOnClickListener
            }


            if (!anyDateSet) {
                Toast.makeText(
                    MainActivity.act,
                    "No date entered!",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (name.isEmpty()) {
                Toast.makeText(
                    MainActivity.act,
                    "No name entered!",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                //get day and month from date
                val month = date.monthValue
                val day = date.dayOfMonth

                //set year to 0 if the year is not supposed to be saved, or to the actual value otherwise
                val year = when (cbSaveBirthdayYear.isChecked) {
                    false -> 0
                    true -> date.year
                }

                //get value of daysToRemind, set it to 0 if the text field is empty
                val daysToRemind = when (etDaysToRemind.text.toString()) {
                    "" -> 0
                    else -> etDaysToRemind.text.toString().toInt()
                }
                val notifyMe = cbNotifyMe.isChecked
                if (editing) {
                    editBirthdayHolder!!.name = name
                    editBirthdayHolder!!.day = day
                    editBirthdayHolder!!.month = month
                    editBirthdayHolder!!.year = year
                    editBirthdayHolder!!.daysToRemind = daysToRemind
                    editBirthdayHolder!!.notify = notifyMe
                    birthdayListInstance.sortAndSaveBirthdays()
                } else {
                    birthdayListInstance.addBirthday(
                        name, date.dayOfMonth, date.monthValue,
                        year, daysToRemind, false, notifyMe
                    )
                }
                myRecycler.adapter?.notifyDataSetChanged()
            }
            myAlertDialog?.dismiss()
        }


        nameField.requestFocus()
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
        return if (viewHolder.adapterPosition==BirthdayFr.birthdayListInstance.size || parsed.birthday.daysToRemind < 0) {
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
        MainActivity.act.updateUndoBirthdayIcon()
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
            holder.itemView.setOnClickListener{}
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
            holder.itemView.setOnClickListener { true }
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
                        R.string.birthdayAge, age, holder.birthday.year)
                }
                var dayExtension = ""
                if (currentBirthday.daysToRemind != 1) {
                    dayExtension = "s"
                }
                val reminderText =
                    MainActivity.act.resources.getString(
                        R.string.birthdayReminder, currentBirthday.daysToRemind, dayExtension)
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
                monthAddition, currentBirthday.month.toString(), currentBirthday.name)

            // Red background if birthday is today
            if (LocalDate.now().month.value == currentBirthday.month && LocalDate.now().dayOfMonth == currentBirthday.day) {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_winered)
            } else {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_gray)
            }

            //opens dialog to edit this birthday
            holder.itemView.setOnLongClickListener {
                BirthdayFr.editBirthdayHolder = holder.birthday
                BirthdayFr.myFragment.openBirthdayDialog()
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
