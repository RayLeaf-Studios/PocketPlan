package com.example.j7_003.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database_objects.Task
import kotlinx.android.synthetic.main.addtask_dialog.view.*
import kotlinx.android.synthetic.main.addtask_dialog_title.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val database = MainActivity.database

        val myView = inflater.inflate(R.layout.fragment_todo, container, false)

        val myRecycler = myView.recycler_view_todo

        //ADDING TASK VIA FLOATING ACTION BUTTON
        myView.btnAddTodoTask.setOnClickListener() {
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(R.layout.addtask_dialog_title, null))

            //show dialog
            val myAlertDialog = myBuilder?.create()
            myAlertDialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog?.show()

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
                    myRecycler.adapter?.notifyDataSetChanged()
                }
            }

            myDialogView.etxTitleAddTask.requestFocus()
        }

        myRecycler.adapter = TodoTaskAdapter()

        myRecycler.layoutManager = LinearLayoutManager(activity)

        myRecycler.setHasFixedSize(true)


        return myView
    }

}

private class TodoTaskAdapter() :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>(){
    private val database = MainActivity.database
    private val taskList = database.taskList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskAdapter.TodoTaskViewHolder {
        //parent is Recyclerview the view holder will be placed in
        //context is activity that the recyclerview is placed in
        //parent in inflate tells the inflater where the layout will be placed
        //so it can be inflated to the right size
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        val currentTask = taskList[position]
        val activity = MainActivity.myActivity


        //EDITING BIRTHDAY VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener(){

            //inflate the dialog with custom view
            //todo, passing null here probably causes problem with keyboard below
            val myDialogView = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(R.layout.addtask_dialog_title, null)
            editTitle.tvDialogTitle.text = "Edit Task"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            //write current task to textField
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

        holder.name_textview.text = currentTask.title

        when(currentTask.priority){
            1 -> holder.btnDelete.setBackgroundResource(R.color.colorPriority1)
            2 -> holder.btnDelete.setBackgroundResource(R.color.colorPriority2)
            3 -> holder.btnDelete.setBackgroundResource(R.color.colorPriority3)
        }
        holder.btnDelete.setOnClickListener(){
            database.deleteTask(position)
            notifyDataSetChanged()
            //todo sort birthdays!? when and where

        }
    }

    override fun getItemCount() = taskList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name_textview: TextView = itemView.name_textview
        val btnDelete: ImageButton = itemView.btnDelete
    }
}
