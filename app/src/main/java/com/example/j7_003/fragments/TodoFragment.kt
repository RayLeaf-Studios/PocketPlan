package com.example.j7_003.fragments

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R.*
import com.example.j7_003.data.database.Database
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFragment : Fragment() {

    companion object{
        lateinit var myFragment: TodoFragment
        lateinit var myAdapter: TodoTaskAdapter
        lateinit var myRecycler: RecyclerView
    }

    fun manageCheckedTaskDeletion(){
        val oldSize = Database.taskList.size
        val newSize = Database.deleteCheckedTasks()
        val delta = oldSize - newSize
        var animationCounter = oldSize
        for(i in newSize until oldSize){
            val v = myRecycler.findViewHolderForAdapterPosition(i) as TodoTaskAdapter.TodoTaskViewHolder
            animationCounter -= 1
            if(i == newSize){
                v.myView.animate().scaleX(0f).setDuration(250).setStartDelay(animationCounter.toLong()*100).withEndAction{
                    myAdapter.notifyDataSetChanged()
                }
            }else{
                v.myView.animate().scaleX(0f).setStartDelay(animationCounter.toLong()*100).duration = 250
            }

        }
        if(delta == 0){
            myAdapter.notifyDataSetChanged()
        }
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {      

        val myView = inflater.inflate(layout.fragment_todo, container, false)

        myRecycler = myView.recycler_view_todo

        myFragment = this

        /**
         * Adding Task via floating action button
         * Onclick-Listener opening the add-task dialog
         */
        myView.btnAddTodoTask.setOnClickListener() {
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(layout.dialog_add_task, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(layout.title_dialog_add_task, null))

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
                    Database.addTask(title, index + 1, false)
                    myRecycler.adapter?.notifyDataSetChanged()
                }
            }

            myDialogView.etxTitleAddTask.requestFocus()
        }

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = TodoTaskAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)


        val swipeHelperLeft = ItemTouchHelper(SwipeLeftToDeleteT(myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)

        val swipeHelperRight = ItemTouchHelper(SwipeRightToDeleteT(myAdapter))
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
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class SwipeLeftToDeleteT(private var adapter: TodoTaskAdapter):
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
    }
}

class TodoTaskAdapter() :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>(){   

    fun deleteItem(position: Int){
        Database.deleteTask(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        val currentTask = Database.getTask(holder.adapterPosition)
        val activity = MainActivity.myActivity

        //changes design of task based on priority and being checked
        holder.tvName.text = currentTask.title
        holder.myView.scaleX = 1f
        if(Database.getTask(holder.adapterPosition).isChecked){
            holder.checkBox.isChecked = true
            holder.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvName.setTextColor(ContextCompat.getColor(MainActivity.myActivity, color.colorHint))
            holder.myView.setBackgroundResource(drawable.round_corner_gray)
        }else{
            holder.checkBox.isChecked = false
            holder.tvName.paintFlags = 0
            holder.tvName.setTextColor(ContextCompat.getColor(MainActivity.myActivity, color.colorOnBackGround))
            when(currentTask.priority){
                1 -> holder.myView.setBackgroundResource(drawable.round_corner1)
                2 -> holder.myView.setBackgroundResource(drawable.round_corner2)
                3 -> holder.myView.setBackgroundResource(drawable.round_corner3)
            }
        }

        //User Interactions with Task List Item below
        /**
         * EDITING task
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        holder.itemView.setOnClickListener(){

            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(layout.dialog_add_task, null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(layout.title_dialog_add_task, null)
            editTitle.tvDialogTitle.text = "Edit task"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
            myAlertDialog.show()

            //write current task to textField
            myDialogView.etxTitleAddTask.requestFocus()
            myDialogView.etxTitleAddTask.setText(Database.getTask(holder.adapterPosition).title)
            myDialogView.etxTitleAddTask.setSelection(myDialogView.etxTitleAddTask.text.length)

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            //Three buttons to create tasks with priorities 1-3
            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener {
                    myAlertDialog.dismiss()
                    val newPos = Database.editTask(holder.adapterPosition, index + 1, myDialogView.etxTitleAddTask.text.toString(), Database.getTask(holder.adapterPosition).isChecked)
                    this.notifyItemChanged(holder.adapterPosition)
                    this.notifyItemMoved(holder.adapterPosition, newPos)
                }
            }

        }

        //reacts to the user checking a task
        holder.checkBox.setOnClickListener{
            val checked = holder.checkBox.isChecked
            val task = Database.getTask(holder.adapterPosition)
            val newPos = Database.editTask(holder.adapterPosition, task.priority, task.title, checked)
            if(checked){
                holder.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
                holder.tvName.setTextColor(ContextCompat.getColor(MainActivity.myActivity, color.colorHint))
            }else{
                holder.tvName.paintFlags = 0
                holder.tvName.setTextColor(ContextCompat.getColor(MainActivity.myActivity, color.colorOnBackGround))
            }
            notifyItemChanged(holder.adapterPosition)
            if(holder.adapterPosition != newPos){
                notifyItemMoved(holder.adapterPosition, newPos)
            }
        }
    }


    override fun getItemCount() = Database.taskList.size

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        /**
         * One instance of this class will contain one "instance" of row_task and meta data
         * like position, it also holds references to views inside of the layout
         */
        var myView = itemView
        val tvName: TextView = itemView.name_textview
        val checkBox: CheckBox = itemView.cbTask
    }
}
