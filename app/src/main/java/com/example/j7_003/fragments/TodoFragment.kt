package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.j7_003.data.Database
import kotlinx.android.synthetic.main.row_task.view.*
import android.view.*
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import kotlinx.android.synthetic.main.addtask_dialog.view.*
import kotlinx.android.synthetic.main.addtask_dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_todo, container, false)

        val listView = view.listView
        val btnAddTodoTask = view.btnAddTodoTask

        val database = MainActivity.database
        val listAdapter = MyAdapter(
            inflater.context,
            database,
            layoutInflater
        )

        listView.adapter = listAdapter

        btnAddTodoTask.setOnClickListener {

            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(R.layout.addtask_dialog_title, null))

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

            myDialogView.etxTitleAddTask.requestFocus()

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog?.dismiss()
                    val title = myDialogView.etxTitleAddTask.text.toString()
                    database.addTask(title, index + 1)
                    listAdapter.notifyDataSetChanged()
                }
            }
        }

        // Inflate the layout for this fragment
        return view
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
            val rowSimple = layoutInflater.inflate(R.layout.row_task, parent, false)

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
