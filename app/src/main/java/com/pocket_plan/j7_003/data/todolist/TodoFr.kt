package com.pocket_plan.j7_003.data.todolist

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
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

class TodoFr(mainActivity: MainActivity) : Fragment() {
    private lateinit var myMenu: Menu
    private val myActivity = mainActivity

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
        myMenu.findItem(R.id.item_tasks_undo)?.icon?.setTint(myActivity.colorForAttr(attr.colorOnBackGround))
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
                    myFragment.updateTodoIcons()
                }
                myActivity.dialogConfirmDelete(titleId, action)
            }

            R.id.item_tasks_undo -> {
//                undo deletion of last task

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
                myActivity.dialogConfirmDelete(titleId, action)
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

        myAdapter = TodoTaskAdapter(myActivity)
        myRecycler.adapter = myAdapter

        layoutManager = LinearLayoutManager(activity)
        myRecycler.layoutManager = layoutManager
        myRecycler.setHasFixedSize(true)

        //itemTouchHelper to drag and reorder notes
        val itemTouchHelper = ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                var previousPosition: Int = -1
                var moving = false

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ) {
                    //get current position in adapter
                    val currentPosition = viewHolder.adapterPosition

                    //mark that moving has ended (to allow a new previousPosition when move is detected)
                    moving = false

                    // don't refresh item if
                    // adapterPosition == -1   =>  clearView got called due to a swipe to delete
                    // adapterPosition == lastMovePos   =>  item was moved, but placed back to original position
                    // lastMovePos == -1   =>  item was selected but not moved
                    if (currentPosition == -1 || currentPosition == previousPosition || previousPosition == -1) {
                        previousPosition = -1
                        super.clearView(recyclerView, viewHolder)
                        return
                    }

                    //save task that was moved
                    val movedTask = todoListInstance[previousPosition]
                    //remove it from its previous position
                    todoListInstance.removeAt(previousPosition)
                    //re-add it at the current adapter position
                    todoListInstance.add(currentPosition, movedTask)

                    //save old values
                    val oldPriority = movedTask.priority
                    val oldCheckedState = movedTask.isChecked

                    //initialize new values
                    val newPriority: Int
                    val newCheckedState: Boolean

                    //get new values for priority and checked state
                    if (currentPosition > previousPosition) {
                        //if moved down, take values from above
                        newPriority = todoListInstance[currentPosition - 1].priority
                        newCheckedState = todoListInstance[currentPosition - 1].isChecked
                    } else {
                        //if moved up, take values from below
                        newPriority = todoListInstance[currentPosition + 1].priority
                        newCheckedState = todoListInstance[currentPosition + 1].isChecked
                    }

                    //apply changes
                    movedTask.priority = newPriority
                    movedTask.isChecked = newCheckedState

                    //save changes
                    todoListInstance.save()

                    //notify change if priority or checkedState changed
                    if (oldPriority != newPriority || oldCheckedState != newCheckedState) {
                        myAdapter.notifyItemChanged(currentPosition)
                    }

                    //reset previousPosition to -1 to mark that nothing is moving
                    previousPosition = -1

                    //clear view
                    super.clearView(recyclerView, viewHolder)
                }


                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder
                ): Boolean {

                    if (!moving) {
                        //if not moving, save new previous position
                        previousPosition = viewHolder.adapterPosition

                        //and prevent new previous positions from being set until this move is over
                        moving = true
                    }

                    //get start and end position of this move
                    val fromPos = viewHolder.adapterPosition
                    val toPos = target.adapterPosition

                    // move item in `fromPos` to `toPos` in adapter.
                    myAdapter.notifyItemMoved(fromPos, toPos)

                    //indicates successful move
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    //get index where task should be deleted
                    val deletedAtIndex = viewHolder.adapterPosition

                    //save task at that index
                    deletedTask = todoListInstance.getTask(deletedAtIndex)

                    //delete this task form todoListInstance
                    todoListInstance.deleteTask(deletedAtIndex)

                    //animate remove in adapter
                    myAdapter.notifyItemRemoved(deletedAtIndex)

                    //update menu icons
                    myFragment.updateTodoIcons()
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
            LayoutInflater.from(myActivity).inflate(layout.dialog_add_task, null)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(myDialogView) }
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
                        AnimationUtils.loadAnimation(myActivity, anim.shake)
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
                    myFragment.updateTodoIcons()
                }
                myAlertDialog?.dismiss()
            }
        }

        myDialogView.etxTitleAddTask.requestFocus()
    }
}

class TodoTaskAdapter(activity: MainActivity) :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>() {
    private val myActivity = activity
    private val listInstance = TodoFr.todoListInstance
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val cr = myActivity.resources.getDimension(dimen.cornerRadius)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout.row_task, parent, false)
        return TodoTaskViewHolder(itemView)
    }


    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        holder.itemView.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        holder.itemView.visibility = View.VISIBLE

        val currentTask = listInstance.getTask(holder.adapterPosition)

        //changes design of task based on priority and being checked
        holder.itemView.tvName.text = currentTask.title

        holder.itemView.tvName.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, anim.shake_small)
            holder.itemView.startAnimation(animationShake)
            true
        }

        if (listInstance.getTask(holder.adapterPosition).isChecked) {
            holder.itemView.cbTask.isChecked = true
            holder.itemView.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.tvName.setTextColor(
                myActivity.colorForAttr(attr.colorHint)
            )
            holder.itemView.crvTask.setCardBackgroundColor(myActivity.colorForAttr(attr.colorCheckedTask))
        } else {
            holder.itemView.cbTask.isChecked = false
            holder.itemView.tvName.paintFlags = 0
            holder.itemView.tvName.setTextColor(
                myActivity.colorForAttr(attr.colorOnBackGroundTask)
            )
            val backgroundColor = when (listInstance.getTask(holder.adapterPosition).priority) {
                1 -> attr.colorPriority1
                2 -> attr.colorPriority2
                else -> attr.colorPriority3
            }
            holder.itemView.crvTask.setCardBackgroundColor(
                myActivity.colorForAttr(
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
            val myDialogView = LayoutInflater.from(myActivity).inflate(
                layout.dialog_add_task,
                null
            )

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(myActivity).setView(myDialogView)
            val editTitle = LayoutInflater.from(myActivity).inflate(
                layout.title_dialog,
                null
            )
            editTitle.tvDialogTitle.text = myActivity.resources.getText(string.tasksEditTitle)
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
                            AnimationUtils.loadAnimation(myActivity, anim.shake)
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

    override fun getItemCount() = TodoFr.todoListInstance.size

    class TodoTaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}



