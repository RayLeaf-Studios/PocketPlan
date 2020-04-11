package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.addbirthday_dialog.*
import kotlinx.android.synthetic.main.addbirthday_dialog.view.*
import kotlinx.android.synthetic.main.addbirthday_dialog.view.npMonth
import kotlinx.android.synthetic.main.addbirthday_dialog.view.etName
import kotlinx.android.synthetic.main.addtask_dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_birthday.view.*
import kotlinx.android.synthetic.main.row_birthday.view.*

/**
 * A simple [Fragment] subclass.
 */
class BirthdayFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val database = MainActivity.database

        val myView = inflater.inflate(R.layout.fragment_birthday, container, false)

        val myRecycler = myView.recycler_view_birthday

        //ADDING BIRTHDAY VIA FLOATING ACTION BUTTON
        myView.btnAddBirthday.setOnClickListener() {

            //inflate the dialog with custom view
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.addbirthday_dialog, null)

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

            npMonth.setOnValueChangedListener{ _, _, _ ->
                when(npMonth.value){
                    1,3,5,7,8,10,12 -> npDay.maxValue = 31
                    2 -> npDay.maxValue = 29
                    else -> npDay.maxValue = 30
                }
            }

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            val myTitle = layoutInflater.inflate(R.layout.addtask_dialog_title, null)
            myTitle.tvDialogTitle.text = "Add Birthday"
            myBuilder?.setCustomTitle(myTitle)

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()


            //button to confirm adding of birthday
            myDialogView.btnConfirmBirthday.setOnClickListener(){
                val name = nameField.text.toString()
                val day = npDay.value
                val month = npMonth.value
                val reminderPeriod = npReminder.value
                if(!(database.addBirthday(name, day, month, reminderPeriod))){
                    Toast.makeText(activity, "Invalid Input", Toast.LENGTH_SHORT).show()
                }
                myRecycler.adapter?.notifyDataSetChanged()
                myAlertDialog?.dismiss()
            }

            nameField.requestFocus()
        }

        val myAdapter = BirthdayAdapter()

        myRecycler.adapter = myAdapter

        myRecycler.layoutManager = LinearLayoutManager(activity)

        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDelete(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDelete(myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        //performance optimization, not necessary
        myRecycler.setHasFixedSize(true)

        return myView
    }

}

class SwipeRightToDelete(var adapter: BirthdayAdapter):ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        Log.e("debug", "im moving")
        return false
            }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)

    }
}

class SwipeLeftToDelete(var adapter: BirthdayAdapter):ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        Log.e("debug", "im moving")
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class BirthdayAdapter() :
    RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {
    val mydatabase = MainActivity.database

    fun deleteItem(position: Int){
        mydatabase.deleteBirthday(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BirthdayViewHolder {
        //parent is Recyclerview the view holder will be placed in
        //context is activity that the recyclerview is placed in
        //parent in inflate tells the inflater where the layout will be placed
        //so it can be inflated to the right size
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_birthday, parent, false)
        return BirthdayViewHolder(itemView)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: BirthdayViewHolder, position: Int) {

        val currentBirthday = mydatabase.getBirthday(position)
        val activity = MainActivity.myActivity

        // EDITING BIRTHDAY VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener() {


            //inflate the dialog with custom view
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.addbirthday_dialog, null)


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

            npMonth.setOnValueChangedListener{ _, _, _ ->
                when(npMonth.value){
                    1,3,5,7,8,10,12 -> npDay.maxValue = 31
                    2 -> npDay.maxValue = 29
                    else -> npDay.maxValue = 30
                }
            }

            val etName = myDialogView.etName

            //AlertDialogBuilder
            val myBuilder = activity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            val myTitle = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog_title, null)
            myTitle.tvDialogTitle.text = "Edit Birthday"
            myBuilder?.setCustomTitle(myTitle)

            //write current values to edit Text fields
            etName.setText(currentBirthday.name)
            npMonth.value = currentBirthday.month
            npDay.value = currentBirthday.day
            npReminder.value = currentBirthday.daysToRemind

            when(npMonth.value){
                1,3,5,7,8,10,12 -> npDay.maxValue = 31
                2 -> npDay.maxValue = 29
                else -> npDay.maxValue = 30
            }

            myDialogView.btnConfirmBirthday.text ="CONFIRM EDIT"

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

            //button to confirm editing of birthday
            myDialogView.btnConfirmBirthday.setOnClickListener(){

                val name = etName.text.toString()
                val day = npDay.value
                val month = npMonth.value
                val reminderPeriod = npReminder.value
                if(!(mydatabase.editBirthday(name, day, month, reminderPeriod, holder.adapterPosition))){
                    Toast.makeText(activity, "Invalid Input", Toast.LENGTH_SHORT).show()
                }


                notifyDataSetChanged()
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

        if(currentBirthday.hasReminder()) {
            holder.iconBell.visibility = View.VISIBLE
        }else{
            holder.iconBell.visibility = View.INVISIBLE
        }

    }

    override fun getItemCount() = mydatabase.birthdayList.size

    //one instance of this class will contain one instance of row_birthday and meta data like position
    //also holds references to views inside the layout

    class BirthdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txvBirthdayLabelName: TextView = itemView.txvBirthdayLabelName
        val iconBell: ImageView = itemView.icon_bell
    }

}
