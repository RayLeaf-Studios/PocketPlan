package com.pocket_plan.j7_003.data.todolist

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.R.*
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import kotlinx.android.synthetic.main.dialog_add_task.view.*
import kotlinx.android.synthetic.main.fragment_todo.view.*
import kotlinx.android.synthetic.main.row_task.view.*
import kotlinx.android.synthetic.main.title_dialog.view.*
import java.util.*

/**
 * A simple [Fragment] subclass.
 */

class TodoFr : Fragment() {
    private lateinit var myMenu: Menu

    companion object {
        lateinit var myFragment: TodoFr
        lateinit var myAdapter: TodoTaskAdapter
        lateinit var myRecycler: RecyclerView

        lateinit var todoListInstance: TodoList
        var deletedTask: Task? = null

        var offsetTop: Int = 0
        var firstPos: Int = 0
        lateinit var layoutManager: LinearLayoutManager

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks, menu)
        myMenu = menu
        myMenu.findItem(R.id.item_tasks_undo)?.icon?.setTint(MainActivity.act.colorForAttr(R.attr.colorOnBackGround))
        updateTodoIcons()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.item_tasks_delete_checked -> {
                //delete checked tasks and update the undoTask icon
                val titleId = string.todo_dialog_clear_checked_title
                val action: () -> Unit = {
                    myFragment.manageCheckedTaskDeletion()
                }
                MainActivity.act.dialogConfirmDelete(titleId, action)
            }

            R.id.item_tasks_undo -> {
//                undo deletion of last task


//                val newPos = todoListInstance.addFullTask(deletedTask!!)
//                myAdapter.notifyItemInserted(newPos)

                todoListInstance.addFullTask(deletedTask!!)
                myAdapter.notifyDataSetChanged()

                deletedTask = null
            }

            R.id.item_tasks_clear -> {
                //delete ALL tasks in list
                val titleId = string.todo_dialog_clear_title
                val action: () -> Unit = {
                    todoListInstance.clear()
                    myAdapter.notifyDataSetChanged()
                    todoListInstance.save()
                }
                MainActivity.act.dialogConfirmDelete(titleId, action)
            }

            R.id.item_tasks_uncheck_all -> {
                //uncheck all tasks
                todoListInstance.uncheckAll()
                myAdapter.notifyDataSetChanged()
            }

        }
        updateTodoIcons()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val myView = inflater.inflate(layout.fragment_todo, container, false)
        myRecycler = myView.recycler_view_todo
        myFragment = this

        deletedTask = null

        /**
         * Adding Task via floating action button
         * Onclick-Listener opening the add-task dialog
         */

        todoListInstance = TodoList()
        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = TodoTaskAdapter()
        myRecycler.adapter = myAdapter

        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)

        val swipeHelperLeft = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.LEFT, myAdapter))
        swipeHelperLeft.attachToRecyclerView(myRecycler)
        val swipeHelperRight = ItemTouchHelper(SwipeToDeleteTask(ItemTouchHelper.RIGHT, myAdapter))
        swipeHelperRight.attachToRecyclerView(myRecycler)

        //itemTouchHelper to drag and reorder notes
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            ) {
                var lastMovePos: Int = -1
                var moving = false
                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    if (!moving) {
                        return
                    }

                    if (viewHolder.adapterPosition == todoListInstance.size) {
                        return
                    }

                    //indicate the current move is over
                    moving = false

                    //don't refresh or change anything when the position wasn't changed
                    if (viewHolder.adapterPosition == lastMovePos || lastMovePos == -1) {
                        lastMovePos = -1
                        return
                    }

                    //save the old checked state
                    val oldCheckedState = todoListInstance[viewHolder.adapterPosition].isChecked

                    //get new checkedState from item below, or leave it unchanged
                    //if item is the last in the list, take checkedState of item above
                    val newCheckedState: Boolean
                    newCheckedState = if (!oldCheckedState) {
                        when (viewHolder.adapterPosition) {
                            todoListInstance.size - 1 -> todoListInstance[viewHolder.adapterPosition - 1].isChecked
                            0 -> oldCheckedState
                            else -> todoListInstance[viewHolder.adapterPosition - 1].isChecked
                        }

                    } else {
                        when (viewHolder.adapterPosition) {
                            todoListInstance.size - 1 -> todoListInstance[viewHolder.adapterPosition - 1].isChecked
                            else -> todoListInstance[viewHolder.adapterPosition + 1].isChecked
                        }

                    }

                    //set new checkedState
                    todoListInstance[viewHolder.adapterPosition].isChecked = newCheckedState

                    //save old priority
                    val oldPriority = todoListInstance[viewHolder.adapterPosition].priority

                    //get new priority from item below or from item above if item is last item
                    val newPriority: Int
                    if (viewHolder.adapterPosition > lastMovePos) {
                        newPriority = when (viewHolder.adapterPosition) {
                            todoListInstance.size - 1 -> todoListInstance[viewHolder.adapterPosition - 1].priority
                            0 -> oldPriority
                            else -> {
                                todoListInstance[viewHolder.adapterPosition - 1].priority
                            }
                        }
                    } else {
                        newPriority = when (viewHolder.adapterPosition) {
                            todoListInstance.size - 1 -> todoListInstance[viewHolder.adapterPosition - 1].priority
                            else -> {
                                todoListInstance[viewHolder.adapterPosition + 1].priority
                            }
                        }

                    }

                    //Set new priority
                    todoListInstance[viewHolder.adapterPosition].priority = newPriority
                    todoListInstance.save()

                    //if priority or checkedState did change, refresh adapter
//                    myAdapter.notifyDataSetChanged()
                    if (oldPriority != newPriority || oldCheckedState != newCheckedState) {
                        myAdapter.notifyItemChanged(viewHolder.adapterPosition)
                    }


                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean {

                    val fromPos = viewHolder.adapterPosition
                    if (fromPos == todoListInstance.size) {
                        return true
                    }

                    var toPos = target.adapterPosition

                    if (toPos == todoListInstance.size) toPos--

                    if (!moving) {
                        moving = true
                        lastMovePos = viewHolder.adapterPosition
                    }

                    //swap items in list
                    Collections.swap(
                        todoListInstance, fromPos, toPos
                    )


                    // move item in `fromPos` to `toPos` in adapter.
                    myAdapter.notifyItemMoved(fromPos, toPos)
                    return true // true if moved, false otherwise
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // remove from adapter
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)

        return myView
    }


    fun updateTodoIcons() {
        updateUncheckTaskListIcon()
        updateClearTaskListIcon()
        updateUndoTaskIcon()
        updateDeleteCheckedTasksIcon()
    }

    fun updateUndoTaskIcon() {
        myMenu.findItem(R.id.item_tasks_undo)?.isVisible = deletedTask != null
    }

    private fun updateClearTaskListIcon() {
        myMenu.findItem(R.id.item_tasks_clear)?.isVisible = todoListInstance.size > 0
    }

    private fun updateUncheckTaskListIcon() {
        myMenu.findItem(R.id.item_tasks_uncheck_all)?.isVisible =
            todoListInstance.somethingIsChecked()
    }

    private fun updateDeleteCheckedTasksIcon() {
        myMenu.findItem(R.id.item_tasks_delete_checked)?.isVisible =
            todoListInstance.somethingIsChecked()
    }

    fun prepareForMove() {
        firstPos = layoutManager.findFirstCompletelyVisibleItemPosition()
        offsetTop = 0
        if (firstPos >= 0) {
            val firstView = layoutManager.findViewByPosition(firstPos)
            offsetTop =
                layoutManager.getDecoratedTop(firstView!!) - layoutManager.getTopDecorationHeight(
                    firstView
                )
        }
    }

    fun reactToMove() {
        layoutManager.scrollToPositionWithOffset(
            firstPos,
            offsetTop
        )
    }

    private fun updateDeleteTaskIcon() {
        val checkedTasks = todoListInstance.filter { t -> t.isChecked }.size
        myMenu.findItem(R.id.item_tasks_delete_checked)?.isVisible = checkedTasks > 0
    }

    //Deletes all checked tasks and animates the deletion
    private fun manageCheckedTaskDeletion() {
        deletedTask = null
        val oldSize = todoListInstance.size
        val newSize = todoListInstance.deleteCheckedTasks()
        myAdapter.notifyItemRangeRemoved(newSize, oldSize)
        updateDeleteTaskIcon()
    }

    @SuppressLint("InflateParams")
    fun dialogAddTask() {
        //inflate the dialog with custom view
        val myDialogView =
            LayoutInflater.from(MainActivity.act).inflate(layout.dialog_add_task, null)

        //AlertDialogBuilder
        val myBuilder =
            MainActivity.act.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
        myBuilder?.setCustomTitle(
            layoutInflater.inflate(
                layout.title_dialog,
                null
            )
        )

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
                val title = myDialogView.etxTitleAddTask.text.toString()
                if (title.isEmpty()) {
                    val animationShake =
                        AnimationUtils.loadAnimation(MainActivity.act, anim.shake)
                    myDialogView.etxTitleAddTask.startAnimation(animationShake)
                    @Suppress("LABEL_NAME_CLASH")
                    return@setOnClickListener
                } else {
                    val newPos =
                        todoListInstance.addFullTask(
                            Task(
                                title,
                                index + 1,
                                false
                            )
                        )
                    myRecycler.adapter?.notifyItemInserted(newPos)
                    myRecycler.scrollToPosition(newPos)
                }
                myAlertDialog?.dismiss()
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
    }
}

class SwipeToDeleteTask(direction: Int, val adapter: TodoTaskAdapter) : ItemTouchHelper
.SimpleCallback(0, direction) {
    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        if (viewHolder.adapterPosition == TodoFr.todoListInstance.size) {
            return 0
        }
        return super.getSwipeDirs(recyclerView, viewHolder)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        TodoFr.deletedTask = TodoFr.todoListInstance.getTask(position)
        TodoFr.todoListInstance.deleteTask(position)
        adapter.notifyItemRemoved(position)
        TodoFr.myFragment.updateTodoIcons()
    }

}

class TodoTaskAdapter : RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>() {
    private val listInstance = TodoFr.todoListInstance
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = MainActivity.act.resources.getDimension(dimen.cornerRadius)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        if (position == TodoFr.todoListInstance.size) {
            holder.itemView.visibility = View.INVISIBLE
            holder.itemView.tvName.setOnLongClickListener { true }
            holder.itemView.tapField.setOnClickListener { }
            val density = MainActivity.act.resources.displayMetrics.density
            holder.itemView.layoutParams.height = (100 * density).toInt()
            return
        }

        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE

        val currentTask = listInstance.getTask(holder.adapterPosition)
        val activity = MainActivity.act

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentTask.title

        holder.itemView.tvName.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(MainActivity.act, anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }

        if (listInstance.getTask(holder.adapterPosition).isChecked) {
            holder.itemView.cbTask.isChecked = true
            holder.itemView.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvName.setTextColor(
                MainActivity.act.colorForAttr(attr.colorHint)
            )
            holder.itemView.crvTask.setCardBackgroundColor(MainActivity.act.colorForAttr(attr.colorCheckedTask))
        } else {
            holder.itemView.cbTask.isChecked = false
            holder.itemView.tvName.paintFlags = 0
            holder.itemView.tvName.setTextColor(
                MainActivity.act.colorForAttr(attr.colorOnBackGroundTask)
            )
            val backgroundColor = when (listInstance.getTask(holder.adapterPosition).priority) {
                1 -> attr.colorPriority1
                2 -> attr.colorPriority2
                else -> attr.colorPriority3
            }
            holder.itemView.crvTask.setCardBackgroundColor(
                MainActivity.act.colorForAttr(
                    backgroundColor
                )
            )
        }
        if (round) {
            holder.itemView.crvTask.radius = cr
        } else {
            holder.itemView.crvTask.radius = 0f
        }

        //User Interactions with Task List Item below
        /**
         * EDITING task
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        holder.itemView.tvName.setOnClickListener {

            //inflate the dialog with custom view
            val myDialogView = LayoutInflater.from(activity).inflate(
                layout.dialog_add_task,
                null
            )

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(activity).setView(myDialogView)
            val editTitle = LayoutInflater.from(activity).inflate(
                layout.title_dialog,
                null
            )
            editTitle.tvDialogTitle.text = MainActivity.act.resources.getText(string.menuTitleTasks)
            myBuilder.setCustomTitle(editTitle)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(
                WindowManager
                    .LayoutParams.SOFT_INPUT_STATE_VISIBLE
            )
            myAlertDialog.show()

            //write current task to textField
            myDialogView.etxTitleAddTask.requestFocus()
            myDialogView.etxTitleAddTask.setText(listInstance.getTask(holder.adapterPosition).title)
            myDialogView.etxTitleAddTask.setSelection(myDialogView.etxTitleAddTask.text.length)

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf<Button>(
                myDialogView.btnConfirm1,
                myDialogView.btnConfirm2,
                myDialogView.btnConfirm3
            )

            //Three buttons to create tasks with priorities 1-3
            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener Button@{
                    if (myDialogView.etxTitleAddTask.text.toString() == "") {
                        val animationShake =
                            AnimationUtils.loadAnimation(MainActivity.act, anim.shake)
                        myDialogView.etxTitleAddTask.startAnimation(animationShake)
                        return@Button
                    }
                    val newPos = listInstance.editTask(
                        holder.adapterPosition, index + 1,
                        myDialogView.etxTitleAddTask.text.toString(),
                        listInstance.getTask(holder.adapterPosition).isChecked
                    )
                    this.notifyItemChanged(holder.adapterPosition)
                    TodoFr.myFragment.prepareForMove()
                    this.notifyItemMoved(holder.adapterPosition, newPos)
                    TodoFr.myFragment.reactToMove()
                    myAlertDialog.dismiss()

                }
            }
        }

        //reacts to the user checking a task
        holder.itemView.tapField.setOnClickListener {
            val checkedStatus = !listInstance.getTask(holder.adapterPosition).isChecked
            holder.itemView.cbTask.isChecked = checkedStatus
            val task = listInstance.getTask(holder.adapterPosition)
            val newPos = listInstance.editTask(
                holder.adapterPosition, task.priority,
                task.title, checkedStatus
            )
            TodoFr.myFragment.updateUndoTaskIcon()

            notifyItemChanged(holder.adapterPosition)
            if (holder.adapterPosition != newPos) {
                TodoFr.myFragment.prepareForMove()
                notifyItemMoved(holder.adapterPosition, newPos)
                TodoFr.myFragment.reactToMove()
            }
            TodoFr.myFragment.updateTodoIcons()

        }

//        holder.itemView.cbTask.
    }

    override fun getItemCount() = TodoFr.todoListInstance.size + 1

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}



