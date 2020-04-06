package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database_objects.Birthday
import kotlinx.android.synthetic.main.addbirthday_dialog.view.*
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
            val monthField = myDialogView.etMonth
            val dayField = myDialogView.etDay

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            val myTitle = layoutInflater.inflate(R.layout.addtask_dialog_title, null)
            myTitle.tvDialogTitle.text = "Add Birthday"
            myBuilder?.setCustomTitle(myTitle)

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

            nameField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dayField.requestFocus()
                }
                false
            }

            dayField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    monthField.requestFocus()
                }
                false
            }

            monthField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    database.addBirthday(
                        nameField.text.toString(),
                        monthField.text.toString().toInt(), dayField.text.toString().toInt()
                    )
                    myRecycler.adapter?.notifyDataSetChanged()
                    myAlertDialog?.dismiss()
                }
                false
            }

            nameField.requestFocus()


        }

        myRecycler.adapter = BirthdayAdapter()

        myRecycler.layoutManager = LinearLayoutManager(activity)

        //performance optimization, not necessary
        myRecycler.setHasFixedSize(true)

        return myView
    }

}

private class BirthdayAdapter() :
    RecyclerView.Adapter<BirthdayAdapter.BirthdayViewHolder>() {
    val birthdayList = MainActivity.database.birthdayList
    val mydatabase = MainActivity.database


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

        val currentBirthday = birthdayList[position]
        val activity = MainActivity.myActivity

        // EDITING BIRTHDAY VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener() {

            //inflate the dialog with custom view
            val myDialogView =
                LayoutInflater.from(activity).inflate(R.layout.addbirthday_dialog, null)

            val nameField = myDialogView.etName
            val monthField = myDialogView.etMonth
            val dayField = myDialogView.etDay

            //AlertDialogBuilder
            val myBuilder = activity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            val myTitle = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog_title, null)
            myTitle.tvDialogTitle.text = "Edit Birthday"
            myBuilder?.setCustomTitle(myTitle)

            //write current values to edit Text fields
            nameField.setText(birthdayList[position].name)
            monthField.setText(birthdayList[position].month.toString())
            dayField.setText(birthdayList[position].day.toString())

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

            //detect next / done press on phone keyboard, send focus to next text field
            nameField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dayField.requestFocus()
                    dayField.setSelection(monthField.text.toString().length)
                }
                false
            }

            dayField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    monthField.requestFocus()
                    monthField.setSelection(monthField.text.toString().length)
                }
                false
            }

            monthField.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mydatabase.editBirthday(nameField.text.toString(),
                        monthField.text.toString().toInt(),
                        dayField.text.toString().toInt(), position)
                    notifyDataSetChanged()
                    myAlertDialog?.dismiss()
                }
                false
            }

            nameField.requestFocus()
            nameField.setSelection(nameField.text.length)

        }

        //formatting date
        var monthAddition = ""
        if (currentBirthday.month < 10) monthAddition = "0"

        var dayAddition = ""
        if (currentBirthday.day < 10) dayAddition = "0"

        holder.txvBirthdayLabelName.text =
            dayAddition + currentBirthday.day.toString() + "." +
                    monthAddition + currentBirthday.month.toString() + "      " + currentBirthday.name

    }

    override fun getItemCount() = birthdayList.size

    //one instance of this class will contain one instance of row_birthday and meta data like position
    //also holds references to views inside the layout

    class BirthdayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txvBirthdayLabelName: TextView = itemView.txvBirthdayLabelName
    }

}
