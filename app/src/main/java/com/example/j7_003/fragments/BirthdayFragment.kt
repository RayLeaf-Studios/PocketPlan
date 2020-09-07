package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.database_objects.Birthday
import kotlinx.android.synthetic.main.dialog_add_birthday.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
import kotlinx.android.synthetic.main.row_birthday.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*
import org.threeten.bp.LocalDate
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */

class BirthdayFragment : Fragment() {

    companion object {
        var deletedBirthday: Birthday? = null

        lateinit var myAdapter: BirthdayAdapter

        var searching: Boolean = false
        lateinit var adjustedList: ArrayList<Birthday>
        lateinit var lastQuery: String

        lateinit var myFragment: BirthdayFragment
    }

    @SuppressLint("InflateParams", "SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adjustedList = arrayListOf()

        val myView = inflater.inflate(R.layout.fragment_birthday, container, false)

        val myRecycler = myView.recycler_view_birthday

        myFragment = this
        //ADDING BIRTHDAY VIA FLOATING ACTION BUTTON
        myView.btnAddBirthday.setOnClickListener {

            //inflate the dialog with custom view
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.dialog_add_birthday, null)

            val nameField = myDialogView.etName

            //configure number pickers
            val npMonth = myDialogView.npMonth
            npMonth.minValue = 1
            npMonth.maxValue = 12

            val npDay = myDialogView.npDay
            npDay.minValue = 1
            npDay.maxValue = 31

            val npReminder = myDialogView.npReminder
            npReminder.minValue = 0
            npReminder.maxValue = 30

            npMonth.setOnValueChangedListener { _, _, _ ->
                when (npMonth.value) {
                    1, 3, 5, 7, 8, 10, 12 -> npDay.maxValue = 31
                    2 -> npDay.maxValue = 29
                    else -> npDay.maxValue = 30
                }
            }

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            val myTitle = layoutInflater.inflate(R.layout.title_dialog_add_task, null)
            myTitle.tvDialogTitle.text = "Add Birthday"
            myBuilder?.setCustomTitle(myTitle)

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()


            //button to confirm adding of birthday
            myDialogView.btnConfirmBirthday.setOnClickListener {
                val name = nameField.text.toString()
                if (name.isEmpty()) {
                    Toast.makeText(
                        MainActivity.act,
                        "Can't create an empty birthday!",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val day = npDay.value
                    val month = npMonth.value
                    val reminderPeriod = npReminder.value
                    Database.addBirthday(name, day, month, reminderPeriod)
                    myRecycler.adapter?.notifyDataSetChanged()
                }
                myAlertDialog?.dismiss()
            }

            nameField.requestFocus()
        }

        //initialize recyclerview and adapter
        myAdapter = BirthdayAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)

        //initialize and attach swipe helpers
        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.LEFT))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteBirthday(myAdapter, ItemTouchHelper.RIGHT))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        return myView
    }

    fun search(query: String){
        lastQuery = query
        adjustedList.clear()
        Database.birthdayList.forEach {
            if (it.name.toLowerCase(Locale.ROOT).startsWith(query.toLowerCase(Locale.ROOT))&& it.daysToRemind >= 0){
                adjustedList.add(it)
            }
        }
        myAdapter.notifyDataSetChanged()
    }

}

class SwipeToDeleteBirthday(var adapter: BirthdayAdapter, direction: Int) :
        ItemTouchHelper.SimpleCallback(0, direction){
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

    fun deleteItem(viewHolder: RecyclerView.ViewHolder) {
        val parsed = viewHolder as BirthdayViewHolder
        BirthdayFragment.deletedBirthday = Database.getBirthday(viewHolder.adapterPosition)
        Database.deleteBirthdayObject(parsed.birthday)
        if(BirthdayFragment.searching){
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


        val currentBirthday = when(BirthdayFragment.searching){
            true -> BirthdayFragment.adjustedList[position]
            false -> Database.getBirthday(position)
        }

        holder.birthday = currentBirthday

        val activity = MainActivity.act

        /**
         * Editing birthday via floating action button
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        if (currentBirthday.daysToRemind < 0) {
            //initialize month divider design
            holder.tvMonthLabel.text = currentBirthday.name
            holder.tvMonthLabel.textSize = 22F
            holder.txvBirthdayLabelName.text = ""
            holder.myView.setBackgroundResource(R.color.colorBackground)
            holder.itemView.setOnClickListener {}
        } else {
            //initialize regular birthday design
            holder.tvMonthLabel.textSize = 20F
            holder.tvMonthLabel.text = ""
            holder.myView.setBackgroundResource(R.drawable.round_corner_gray)
            holder.itemView.setOnClickListener {

                //inflate the dialog with custom view
                val myDialogView =
                    LayoutInflater.from(activity).inflate(R.layout.dialog_add_birthday, null)

                //configuring number pickers
                val npMonth = myDialogView.npMonth
                npMonth.minValue = 1
                npMonth.maxValue = 12

                val npDay = myDialogView.npDay
                npDay.minValue = 1
                npDay.maxValue = 31

                val npReminder = myDialogView.npReminder
                npReminder.minValue = 0
                npReminder.maxValue = 30

                npMonth.setOnValueChangedListener { _, _, _ ->
                    when (npMonth.value) {
                        1, 3, 5, 7, 8, 10, 12 -> npDay.maxValue = 31
                        2 -> npDay.maxValue = 29
                        else -> npDay.maxValue = 30
                    }
                }

                val etName = myDialogView.etName

                //AlertDialogBuilder
                val myBuilder =
                    activity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
                val myTitle =
                    LayoutInflater.from(activity).inflate(R.layout.title_dialog_add_task, null)
                myTitle.tvDialogTitle.text = "Edit Birthday"
                myBuilder?.setCustomTitle(myTitle)

                //write current values to edit Text fields
                etName.setText(currentBirthday.name)
                npMonth.value = currentBirthday.month
                npDay.value = currentBirthday.day
                npReminder.value = currentBirthday.daysToRemind

                when (npMonth.value) {
                    1, 3, 5, 7, 8, 10, 12 -> npDay.maxValue = 31
                    2 -> npDay.maxValue = 29
                    else -> npDay.maxValue = 30
                }

                myDialogView.btnConfirmBirthday.text = "CONFIRM EDIT"

                //show dialog
                val myAlertDialog = myBuilder?.create()
                myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
                myAlertDialog?.show()

                //button to confirm editing of birthday
                myDialogView.btnConfirmBirthday.setOnClickListener {

                    holder.birthday.name = etName.text.toString()
                    holder.birthday.day = npDay.value
                    holder.birthday.month = npMonth.value
                    holder.birthday.daysToRemind = npReminder.value
                    Database.sortAndSaveBirthdays()
                    notifyItemChanged(position)
                    myAlertDialog?.dismiss()
                }

                etName.requestFocus()
                etName.setSelection(etName.text.length)
            }

            //formatting date
            var monthAddition = ""
            if (currentBirthday.month < 10) monthAddition = "0"

            var dayAddition = ""
            if (currentBirthday.day < 10) dayAddition = "0"

            holder.txvBirthdayLabelName.text =
                dayAddition + currentBirthday.day.toString() + "." +
                        monthAddition + currentBirthday.month.toString() + "      " + currentBirthday.name

            if (LocalDate.now().month.value == currentBirthday.month && LocalDate.now().dayOfMonth == currentBirthday.day) {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_winered)
            } else {
                holder.myConstraintLayout.setBackgroundResource(R.drawable.round_corner_gray)
            }

            //display bell if birthday has a reminder
            if (currentBirthday.hasReminder()) {
                holder.iconBell.visibility = View.VISIBLE
            } else {
                holder.iconBell.visibility = View.INVISIBLE
            }
        }


    }

    override fun getItemCount(): Int{
         return when(BirthdayFragment.searching){
             true -> BirthdayFragment.adjustedList.size
             false -> Database.birthdayList.size
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
