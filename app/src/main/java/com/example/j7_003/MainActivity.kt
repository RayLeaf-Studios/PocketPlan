package com.example.j7_003

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.j7_003.logic.Database
import com.example.j7_003.logic.Task
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.addtask_dialog.view.*
import kotlinx.android.synthetic.main.row_simple.view.*


class MainActivity : AppCompatActivity() {

    @SuppressLint("InflateParams")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val database = Database(this)

        val listAdapter = MyAdapter(this, database)
        listView.adapter = listAdapter

        btnAddTodoTask.setOnClickListener {

            //inflate the dialog with custom view
            //todo, passing null here probably causes problem with keyboard below
            val myDialogView = LayoutInflater.from(this).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(this).setView(myDialogView).setTitle("Add Task")

            //show dialog
            val myAlertDialog = myBuilder.show()

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

    private class MyAdapter(context: Context, database: Database) : BaseAdapter() {

        private val tasks = database.getTaskList()
        private val mContext: Context = context

        override fun getCount(): Int {
            return tasks.size
        }

        fun sortTasks() {
            tasks.sortBy { t ->
                t.priority
            }
        }

        //todo replace entire list with recylcer view (newer / faster version of list)
        @SuppressLint("ViewHolder")
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            sortTasks()
            val layoutInflater = LayoutInflater.from(mContext)
            val rowSimple = layoutInflater.inflate(R.layout.row_simple, parent, false)

            //set displayed title
            rowSimple.name_textview.text = tasks[position].title

            //onclick action
            rowSimple.setOnClickListener {
                //todo onclick should edit task
                tasks.add(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            //delete button action
            rowSimple.btnDelete.setOnClickListener {
                tasks.remove(tasks[position])
                notifyDataSetChanged()
                sortTasks()
            }

            return rowSimple
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


