package com.example.j7_003

import android.annotation.SuppressLint
import android.app.ActionBar
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.BaseAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.logic.Database
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.addtask_dialog.view.*
import kotlinx.android.synthetic.main.addtask_dialog_title.view.*
import kotlinx.android.synthetic.main.row_simple.view.*


class MainActivity : AppCompatActivity() {

    @SuppressLint("InflateParams", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        supportActionBar?.title = "To-Do List"
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val database = Database(this)

        val listAdapter = MyAdapter(this, database, layoutInflater)
        listView.adapter = listAdapter



        btnAddTodoTask.setOnClickListener {


            //inflate the dialog with custom view
            //todo, passing null here probably causes problem with keyboard below
            val myDialogView = LayoutInflater.from(this).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(this).setView(myDialogView)
            myBuilder.setCustomTitle(layoutInflater.inflate(R.layout.addtask_dialog_title, null))

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            //todo, show keyboard after this
            myDialogView.etxTitleAddTask.requestFocus()

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog.dismiss()
                    val title = myDialogView.etxTitleAddTask.text.toString()
                    database.addTask(title, index + 1)
                    listAdapter.notifyDataSetChanged()
                }
            }

        }

    }

    private class MyAdapter(context: Context, val database: Database, val layoutInflater: LayoutInflater) : BaseAdapter() {
        private val mContext: Context = context

        override fun getCount(): Int {
            return database.taskList.size
        }

        fun sortTasks() {
            database.taskList.sortBy { t ->
                t.priority
            }
        }

        //todo replace entire list with recylcer view (newer / faster version of list)
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            sortTasks()
            val rowSimple = layoutInflater.inflate(R.layout.row_simple, parent, false)

            //set displayed title
            rowSimple.name_textview.text = database.getTask(position).title

            //onclick action
            rowSimple.setOnClickListener {
                editTaskDialog(position)
            }

            when(database.getTask(position).priority){
                1 -> rowSimple.btnDelete.setBackgroundResource(R.color.colorPriority1)
                2 -> rowSimple.btnDelete.setBackgroundResource(R.color.colorPriority2)
                3 -> rowSimple.btnDelete.setBackgroundResource(R.color.colorPriority3)
            }

            //delete button action
            rowSimple.btnDelete.setOnClickListener{
                database.deleteTask(position)
                notifyDataSetChanged()
                sortTasks()
            }

            return rowSimple
        }

        fun editTaskDialog(position: Int) {
            //inflate the dialog with custom view
            //todo, passing null here probably causes problem with keyboard below
            val myDialogView = LayoutInflater.from(mContext).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(mContext).setView(myDialogView)
            val editTitle = layoutInflater.inflate(R.layout.addtask_dialog_title, null)
            editTitle.tvDialogTitle.text = "Edit Task"
            myBuilder.setCustomTitle(editTitle)


            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            //todo, show keyboard after this
            myDialogView.etxTitleAddTask.requestFocus()
            myDialogView.etxTitleAddTask.setText(database.getTask(position).title)
            myDialogView.etxTitleAddTask.setSelection(myDialogView.etxTitleAddTask.text.length)

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog.dismiss()
                    //val title = myDialogView.etxTitleAddTask.text.toString()
                    database.editTask(position, index, myDialogView.etxTitleAddTask.text.toString())
                    this.notifyDataSetChanged()
                }
            }

        }

        //this can be ignored for now
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        //this can be ignored for now
        override fun getItem(position: Int): Any {
            return "TEST STRING"
        }
    }


}


