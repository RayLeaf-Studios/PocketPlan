package com.example.j7_003.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity

import com.example.j7_003.R.*
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

        val myView = inflater.inflate(layout.fragment_todo, container, false)

        val myRecycler = myView.recycler_view_todo

        //ADDING TASK VIA FLOATING ACTION BUTTON
        myView.btnAddTodoTask.setOnClickListener() {
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(layout.addtask_dialog_title, null))

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
                    database.sortTasks()
                    myRecycler.adapter?.notifyDataSetChanged()
                }
            }

            myDialogView.etxTitleAddTask.requestFocus()
        }

        val myAdapter = TodoTaskAdapter()

        myRecycler.adapter = myAdapter

        myRecycler.layoutManager = LinearLayoutManager(activity)

        myRecycler.setHasFixedSize(true)

        var swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteT(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        var swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteT(myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)


        return myView
    }

}

class SwipeRightToDeleteT(var adapter: TodoTaskAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class SwipeLeftToDeleteT(private var adapter: TodoTaskAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        var position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class TodoTaskAdapter() :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>(){
    private val database = MainActivity.database
    private val taskList = database.taskList

    fun deleteItem(position: Int){
        database.deleteTask(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskAdapter.TodoTaskViewHolder {
        //parent is Recyc^^lerview the view holder will be placed in
        //context is activity that the recyclerview is placed in
        //parent in inflate tells the inflater where the layout will be placed
        //so it can be inflated to the right size
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        val currentTask = database.getTask(holder.adapterPosition)
        val activity = MainActivity.myActivity


        //EDITING BIRTHDAY VIA ONCLICK LISTENER ON RECYCLER ITEMS
        holder.itemView.setOnClickListener(){

            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(layout.addtask_dialog, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(layout.addtask_dialog_title, null)
            editTitle.tvDialogTitle.text = "Edit Task"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            //write current task to textField
            myDialogView.etxTitleAddTask.requestFocus()
            myDialogView.etxTitleAddTask.setText(database.getTask(holder.adapterPosition).title)
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
                    database.editTask(holder.adapterPosition, index, myDialogView.etxTitleAddTask.text.toString())
                    database.sortTasks()
                    this.notifyDataSetChanged()
                }
            }

        }

        holder.name_textview.text = currentTask.title

        when(currentTask.priority){
            1 -> {holder.myView.setBackgroundResource(drawable.round_corner1)
//                holder.name_textview.setTextColor(ContextCompat.getColor(activity, color.colorPriority1))
            }
            2 -> {holder.myView.setBackgroundResource(drawable.round_corner2)
//                holder.name_textview.setTextColor(ContextCompat.getColor(activity, color.colorPriority2))
            }
            3 -> {holder.myView.setBackgroundResource(drawable.round_corner3)
//                holder.name_textview.setTextColor(ContextCompat.getColor(activity, color.colorPriority3))
            }
        }

    }

    override fun getItemCount() = taskList.size

    //one instance of this class will contain one instance of row_task and meta data like position
    //also holds references to views inside the layout

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name_textview: TextView = itemView.name_textview
        var myView = itemView
    }
}
