package com.example.j7_003.data.birthdaylist
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.dialog_add_birthday.view.*
import kotlinx.android.synthetic.main.dialog_add_item.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
import kotlinx.android.synthetic.main.fragment_sleep.view.*
import kotlinx.android.synthetic.main.row_birthday.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */

class BirthdayFragment : Fragment() {

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

        lateinit var myFragment: BirthdayFragment

        val birthdayListInstance: BirthdayList = BirthdayList()
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val myView = inflater.inflate(R.layout.fragment_birthday, container, false)
        adjustedList = arrayListOf()
        myRecycler = myView.recycler_view_birthday
        myFragment = this

        //ADDING BIRTHDAY VIA FLOATING ACTION BUTTON
        myView.btnAddBirthday.setOnClickListener {
            editBirthdayHolder = null
            openBirthdayDialog()
        }

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
        }else{
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

        //initialize "Remind me" .. "Days prior" Text views
        val tvRemindMe = myDialogView.tvRemindMe
        val tvDaysPrior = myDialogView.tvDaysPrior

        //set right color for RemindMe DaysPrior
        if(!editing||(editing&&editBirthdayHolder!!.daysToRemind==0)){
            tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
            etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
            tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
        }

        if(editing){
            val addition = when(editBirthdayHolder!!.daysToRemind==1){
                true -> ""
                false -> "s"
            }
            tvDaysPrior.text = "day"+addition+" prior"
        }

        //initialize name field if editing
        if (editing) {
            etName.setText(editBirthdayHolder!!.name)
            etName.setSelection(etName.text.length)
        }

        //initialize date text
        val chooseDateText = when (editBirthdayHolder != null) {
            true -> {
                var yearString = ""
                if (editBirthdayHolder!!.year != 0) {
                    yearString = "."+ editBirthdayHolder!!.year.toString()
                }
                editBirthdayHolder!!.day.toString().padStart(2, '0') + "." +
                        editBirthdayHolder!!.month.toString()
                            .padStart(2, '0') + yearString
            }
            false -> "Choose date"
        }
        tvBirthdayDate.text = chooseDateText


        tvSaveYear.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))

        //initialize value of save year checkbox
        if (editBirthdayHolder != null) {
            if (editBirthdayHolder!!.year != 0) {
                cbSaveBirthdayYear.isChecked = true
            }
        }
        else{
            cbSaveBirthdayYear.isChecked = false
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
                if(enteredText==""||enteredText.toInt()==0){
                    tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                    etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                    tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                }else{
                    tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
                    etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
                    tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
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
            }
            else{
                val enteredText = etDaysToRemind.text.toString()
                if(enteredText==""||enteredText.toInt()==0){
                   etDaysToRemind.setText("0")
                    tvRemindMe.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                    etDaysToRemind.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                    tvDaysPrior.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                }
                val addition = when(enteredText=="1"){
                    true -> ""
                    false -> "s"
                }
                tvDaysPrior.text = "day"+addition+" prior"

            }
        }

        //textWatcher to reset color of nameField after typing again
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                nameField.setHintTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorHint))
                nameField.background.mutate().setColorFilter(
                    resources.getColor(R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                );
            }

            override fun afterTextChanged(s: Editable?) {
            }
        }
        nameField.addTextChangedListener(textWatcher)

        //on click listener to open date picker
        tvBirthdayDate.setOnClickListener {
            val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                tvBirthdayDate.setTextColor(ContextCompat.getColor(MainActivity.act, R.color.colorOnBackGround))
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
            if(editing&&cbSaveBirthdayYear.isChecked){
                yearToDisplay = editBirthdayHolder?.year!!
            }
            else if(editing){
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
            if(cbSaveBirthdayYear.isChecked){
                if (editing && date.year==0) {
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
            true -> "Edit Birthday"
            else -> "Add Birthday"
        }
        myBuilder?.setCustomTitle(myTitle)


        //initialize button text
        myDialogView.btnConfirmBirthday.text = when (editing) {
            true -> "Confirm edit"
            else -> "Add Birthday"
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

            if(!anyDateSet){
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
                if (editing) {
                    editBirthdayHolder!!.name = name
                    editBirthdayHolder!!.day = day
                    editBirthdayHolder!!.month = month
                    editBirthdayHolder!!.year = year
                    editBirthdayHolder!!.daysToRemind = daysToRemind
                    birthdayListInstance.sortAndSaveBirthdays()
                } else {
                    birthdayListInstance.addBirthday(name, date.dayOfMonth, date.monthValue,
                        year, daysToRemind, false)
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
        return if (parsed.birthday.daysToRemind < 0) {
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
    private val listInstance = BirthdayFragment.birthdayListInstance

    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        BirthdayFragment.deletedBirthday = listInstance.getBirthday(viewHolder.adapterPosition)
        listInstance.deleteBirthdayObject(parsed.birthday)
        if (BirthdayFragment.searching) {
            BirthdayFragment.myFragment.search(BirthdayFragment.lastQuery)
        }
        notifyDataSetChanged()
        MainActivity.act.updateUndoBirthdayIcon()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {


        val currentBirthday = when (BirthdayFragment.searching) {
            true -> BirthdayFragment.adjustedList[position]
            false -> listInstance.getBirthday(position)
        }

        holder.birthday = currentBirthday


        //
        if (currentBirthday.daysToRemind < 0) {
            //initialize month divider design
            holder.tvMonthLabel.text = currentBirthday.name
            holder.tvMonthLabel.textSize = 22F
            holder.txvBirthdayLabelName.text = ""
            holder.myView.setBackgroundResource(R.color.colorBackground)
            holder.itemView.setOnLongClickListener {true}
            holder.itemView.setOnClickListener{true}
            holder.itemView.cvBirthdayInfo.visibility = View.GONE
        } else {
            //display bell if birthday has a reminder
            if (currentBirthday.hasReminder()) {
                holder.iconBell.visibility = View.VISIBLE
            } else {
                holder.iconBell.visibility = View.INVISIBLE
            }

            //display info if birthday is expanded
            if(currentBirthday.expanded&&currentBirthday.year!=0){
                holder.itemView.cvBirthdayInfo.visibility = View.VISIBLE
                if(holder.birthday.year!=0){
                    //todo do this properly, whole if is only prototype
                    val birthday = holder.birthday
                    val age = LocalDate.of(birthday.year, birthday.month, birthday.day).until(LocalDate.now()).years
                    holder.itemView.tvBirthdayInfo.text = age.toString()+" years old, born in "+
                            holder.birthday.year.toString()
                }
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
            holder.txvBirthdayLabelName.text =
                dayAddition + currentBirthday.day.toString() + "." +
                        monthAddition + currentBirthday.month.toString() + "      " + currentBirthday.name

            // Red background if birthday is today
            if (LocalDate.now().month.value == currentBirthday.month && LocalDate.now().dayOfMonth == currentBirthday.day) {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_winered)
            } else {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_gray)
            }

            //opens dialog to edit this birthday
            holder.itemView.setOnLongClickListener {
                BirthdayFragment.editBirthdayHolder = holder.birthday
                BirthdayFragment.myFragment.openBirthdayDialog()
                true
            }

            //expands info
            holder.itemView.setOnClickListener{
                holder.birthday.expanded = !holder.birthday.expanded
                listInstance.sortAndSaveBirthdays()
                notifyItemChanged(holder.adapterPosition)
            }


        }


    }

    override fun getItemCount(): Int {
        return when (BirthdayFragment.searching) {
            true -> BirthdayFragment.adjustedList.size
            false -> listInstance.size
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
