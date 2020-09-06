package com.example.j7_003.fragments

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.j7_003.MainActivity
import com.example.j7_003.R
import com.example.j7_003.data.database.Database
import com.example.j7_003.data.database.ItemTemplate
import com.example.j7_003.data.database.database_objects.Task
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*
import kotlinx.android.synthetic.main.title_dialog_add_task.view.*

class CustomItemFragment : Fragment() {

    companion object{
        lateinit var myFragment: CustomItemFragment
        lateinit var myAdapter: CustomItemAdapter
        lateinit var myRecycler: RecyclerView

        var deletedItem: ItemTemplate? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(R.layout.fragment_todo, container, false)
        myRecycler = myView.recycler_view_todo
        myFragment = this

        /**
         * Adding Task via floating action button
         * Onclick-Listener opening the add-task dialog
         */
        myView.btnAddTodoTask.setOnClickListener {
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_add_task, null)

            //AlertDialogBuilder
            val myBuilder = activity?.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
            myBuilder?.setCustomTitle(layoutInflater.inflate(R.layout.title_dialog_add_task, null))

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
                    if(title.isEmpty()){
                        Toast.makeText(MainActivity.act, "Can't create an empty task!", Toast.LENGTH_SHORT).show()
                    }else{
                        myRecycler.adapter?.notifyItemInserted(Database.addFullTask(Task(title, index+1, false)))
                    }
                }
            }

            myDialogView.etxTitleAddTask.requestFocus()
        }

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = CustomItemAdapter()
        myRecycler.adapter = myAdapter
        myRecycler.layoutManager = LinearLayoutManager(activity)
        myRecycler.setHasFixedSize(true)


        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.LEFT, myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteCustomItem(ItemTouchHelper.RIGHT, myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)


        return myView
    }

    //Deletes all checked tasks and animates the deletion

}
class SwipeToDeleteCustomItem(direction: Int,  val adapter: CustomItemAdapter): ItemTouchHelper
.SimpleCallback(0, direction){
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                        target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.deleteItem(position)
        MainActivity.act.updateDeleteTaskIcon()
    }
}

class CustomItemAdapter :
    RecyclerView.Adapter<CustomItemAdapter.CustomItemViewHolder>(){

    fun deleteItem(position: Int){
        TodoFragment.deletedTaskList.clear()
        TodoFragment.deletedTask = Database.getTask(position)
        MainActivity.act.updateUndoTaskIcon()
        Database.deleteTask(position)
        notifyItemRemoved(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomItemViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_task, parent, false)
        return CustomItemViewHolder(itemView)
    }


    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: CustomItemViewHolder, position: Int) {

        val currentTask = Database.getTask(holder.adapterPosition)
        val activity = MainActivity.act

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentTask.title

        //resets scale, that got animated
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f
        if(Database.getTask(holder.adapterPosition).isChecked){
            holder.itemView.cbTask.isChecked = true
            holder.itemView.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvName.setTextColor(
                ContextCompat.getColor(MainActivity.act,
                R.color.colorHint))
            holder.itemView.setBackgroundResource(R.drawable.round_corner_gray)
        }else{
            holder.itemView.cbTask.isChecked = false
            holder.itemView.tvName.paintFlags = 0
            holder.itemView.tvName.setTextColor(
                ContextCompat.getColor(MainActivity.act,
                R.color.colorOnBackGround))
            when(currentTask.priority){
                1 -> holder.itemView.setBackgroundResource(R.drawable.round_corner1)
                2 -> holder.itemView.setBackgroundResource(R.drawable.round_corner2)
                3 -> holder.itemView.setBackgroundResource(R.drawable.round_corner3)
            }
        }

        //User Interactions with Task List Item below
        /**
         * EDITING task
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        holder.itemView.tvName.setOnClickListener {

            if(!TodoFragment.allowSwipe){
                return@setOnClickListener
            }
            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(
                R.layout.dialog_add_task,
                null)

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(
                R.layout.title_dialog_add_task,
                null)
            editTitle.tvDialogTitle.text = "Edit task"
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(WindowManager
                .LayoutParams.SOFT_INPUT_STATE_VISIBLE)
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
                    val newPos = Database.editTask(holder.adapterPosition, index + 1,
                        myDialogView.etxTitleAddTask.text.toString(),
                        Database.getTask(holder.adapterPosition).isChecked)
                    this.notifyItemChanged(holder.adapterPosition)
                    this.notifyItemMoved(holder.adapterPosition, newPos)
                }
            }

        }

        //reacts to the user checking a task
        holder.itemView.tapField.setOnClickListener{
            if(!TodoFragment.allowSwipe){
                return@setOnClickListener
            }
            val checkedStatus = !Database.getTask(holder.adapterPosition).isChecked
            holder.itemView.cbTask.isChecked = checkedStatus
            val task = Database.getTask(holder.adapterPosition)
            val newPos = Database.editTask(holder.adapterPosition, task.priority,
                task.title, checkedStatus)
            MainActivity.act.updateDeleteTaskIcon()

            notifyItemChanged(holder.adapterPosition)
            if(holder.adapterPosition != newPos){
                notifyItemMoved(holder.adapterPosition, newPos)
            }
            //delays item change until list is reordered
        }
    }


    override fun getItemCount() = Database.taskList.size

    class CustomItemViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView)
}
