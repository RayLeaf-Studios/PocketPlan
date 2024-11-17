package com.pocket_plan.j7_003.data.todolist

import android.annotation.SuppressLint
import android.graphics.Paint
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_plan.j7_003.MainActivity
import com.pocket_plan.j7_003.R
import com.pocket_plan.j7_003.data.fragmenttags.FT
import com.pocket_plan.j7_003.data.home.HomeFr
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.databinding.DialogAddTaskBinding
import com.pocket_plan.j7_003.databinding.FragmentTodoBinding
import com.pocket_plan.j7_003.databinding.RowTaskBinding
import com.pocket_plan.j7_003.databinding.TitleDialogBinding

/**
 * A simple [Fragment] subclass.
 */

class TodoFr : Fragment() {
    private var _fragmentBinding: FragmentTodoBinding? = null
    private val fragmentBinding get() = _fragmentBinding!!


    private lateinit var myMenu: Menu
    private lateinit var myActivity: MainActivity

    private lateinit var addTaskDialog: AlertDialog
    private lateinit var dialogAddTaskBinding: DialogAddTaskBinding
    lateinit var myFragment: TodoFr

    companion object {
        //Displayed as middle priority 2, (0 indexed)
        var lastUsedTaskPriority = 1
        lateinit var myAdapter: TodoTaskAdapter
        lateinit var myRecycler: RecyclerView

        lateinit var todoListInstance: TodoList
        var deletedTasks = ArrayDeque<Task?>()

        var offsetTop: Int = 0
        var firstPos: Int = 0
        lateinit var layoutManager: LinearLayoutManager

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_tasks, menu)
        myMenu = menu
        myMenu.findItem(R.id.item_tasks_undo)?.icon?.setTint(myActivity.colorForAttr(R.attr.colorOnBackGround))
        updateTodoIcons()
        super.onCreateOptionsMenu(menu, inflater)
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("NotifyDataSetChanged")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.item_tasks_delete_checked -> {
                //delete checked tasks and update the undoTask icon
                val titleId = R.string.tasksDialogClearChecked
                val action: () -> Unit = {
                    myFragment.manageCheckedTaskDeletion()
                    myFragment.updateTodoIcons()
                }
                myActivity.dialogConfirm(titleId, action)
            }

            R.id.item_tasks_undo -> {
                //undo deletion of last task

                val newPos = todoListInstance.addFullTask(deletedTasks.last()!!)
                myAdapter.notifyItemInserted(newPos)
                deletedTasks.removeLast()

            }

            R.id.item_tasks_clear -> {
                //delete ALL tasks in list
                val titleId = R.string.tasksDialogClearList
                val action: () -> Unit = {
                    todoListInstance.clear()
                    myAdapter.notifyDataSetChanged()
                    todoListInstance.save()
                }
                myActivity.dialogConfirm(titleId, action)
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
    ): View {
        _fragmentBinding = FragmentTodoBinding.inflate(inflater, container, false)
        myActivity = activity as MainActivity
        myRecycler = fragmentBinding.recyclerViewTodo
        myFragment = this

        todoListInstance = TodoList()

        /**
         * Connecting Adapter, Layout-Manager and Swipe Detection to UI elements
         */

        myAdapter = TodoTaskAdapter(myActivity, this)
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
                    val currentPosition = viewHolder.bindingAdapterPosition

                    //mark that moving has ended (to allow a new previousPosition when move is detected)
                    moving = false

                    // don't refresh item if
                    // currentPosition == -1   =>  clearView got called due to a swipe to delete
                    // currentPosition == previousPosition   =>  item was moved, but placed back to original position
                    // previousPosition == -1   =>  item was selected but not moved
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
                        previousPosition = viewHolder.bindingAdapterPosition

                        //and prevent new previous positions from being set until this move is over
                        moving = true
                    }

                    //get start and end position of this move
                    val fromPos = viewHolder.bindingAdapterPosition
                    val toPos = target.bindingAdapterPosition

                    // animate move of task from `fromPos` to `toPos` in adapter.
                    myAdapter.notifyItemMoved(fromPos, toPos)

                    //indicates successful move
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    //get index where task should be deleted
                    val deletedAtIndex = viewHolder.bindingAdapterPosition

                    //save task at that index
                    deletedTasks.add(todoListInstance.getTask(deletedAtIndex))

                    //delete this task form todoListInstance
                    todoListInstance.deleteTask(deletedAtIndex)

                    //animate remove in adapter
                    myAdapter.notifyItemRemoved(deletedAtIndex)

                    //update menu icons
                    myFragment.updateTodoIcons()
                }
            })

        itemTouchHelper.attachToRecyclerView(myRecycler)

        return fragmentBinding.root
    }


    fun updateTodoIcons() {
        updateUncheckTaskListIcon()
        updateClearTaskListIcon()
        updateUndoTaskIcon()
        updateDeleteCheckedTasksIcon()
    }

    fun updateUndoTaskIcon() {
        myMenu.findItem(R.id.item_tasks_undo)?.isVisible = deletedTasks.isNotEmpty()
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

    private fun updateDeleteTaskIcon() {
        val checkedTasks = todoListInstance.filter { t -> t.isChecked }.size
        myMenu.findItem(R.id.item_tasks_delete_checked)?.isVisible = checkedTasks > 0
    }

    fun prepareForMove() {
        firstPos = layoutManager.findFirstVisibleItemPosition()
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

    //Deletes all checked tasks and animates the deletion
    private fun manageCheckedTaskDeletion() {
        deletedTasks.clear()
        val oldSize = todoListInstance.size
        val newSize = todoListInstance.deleteCheckedTasks()
        myAdapter.notifyItemRangeRemoved(newSize, oldSize)
        updateDeleteTaskIcon()
    }

    fun preloadAddTaskDialog(passedActivity: MainActivity, myLayoutInflater: LayoutInflater){
        myActivity = passedActivity
        //inflate the dialog with custom view
        dialogAddTaskBinding = DialogAddTaskBinding.inflate(myLayoutInflater)

        //AlertDialogBuilder
        val myBuilder =
            myActivity.let { it1 -> AlertDialog.Builder(it1).setView(dialogAddTaskBinding.root) }
        val titleDialogBinding = TitleDialogBinding.inflate(myLayoutInflater)
        myBuilder?.setCustomTitle(titleDialogBinding.root)

        //show dialog
        addTaskDialog = myBuilder?.create()!!
        addTaskDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        //adds listeners to confirmButtons in addTaskDialog
        val taskConfirmButtons = arrayListOf(
            dialogAddTaskBinding.btnConfirm1,
            dialogAddTaskBinding.btnConfirm2,
            dialogAddTaskBinding.btnConfirm3
        )

        dialogAddTaskBinding.etTitleAddTask.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                taskConfirmButtons[lastUsedTaskPriority].performClick()
                true
            } else false
        }

        taskConfirmButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                val title = dialogAddTaskBinding.etTitleAddTask.text.toString()
                dialogAddTaskBinding.etTitleAddTask.setText("")
                if (title.trim().isEmpty()) {
                    val animationShake =
                        AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                    dialogAddTaskBinding.etTitleAddTask.startAnimation(animationShake)
                    return@setOnClickListener
                }
                lastUsedTaskPriority = index
                val newPos =
                    todoListInstance.addFullTask(
                        Task(
                            title,
                            index + 1,
                            false
                        )
                    )

                addTaskDialog.dismiss()

                if(MainActivity.previousFragmentStack.peek() == FT.HOME){
                    val homeFr = myActivity.getFragment(FT.HOME) as HomeFr
                    homeFr.updateTaskPanel(false)
                    myActivity.toast(myActivity.getString(R.string.homeNotificationTaskAdded))
                    return@setOnClickListener
                }

                myRecycler.adapter?.notifyItemInserted(newPos)
                myRecycler.scrollToPosition(newPos)
                myFragment.updateTodoIcons()
            }
        }
    }

    fun dialogAddTask() {
        addTaskDialog.show()
        dialogAddTaskBinding.etTitleAddTask.requestFocus()
    }
}

class TodoTaskAdapter(activity: MainActivity, private var myFragment: TodoFr) :
    RecyclerView.Adapter<TodoTaskAdapter.TodoTaskViewHolder>() {
    private val myActivity = activity
    private val listInstance = TodoFr.todoListInstance
    private val round = SettingsManager.getSetting(SettingId.SHAPES_ROUND) as Boolean
    private val dark = SettingsManager.getSetting(SettingId.THEME_DARK) as Boolean
    private val cr = myActivity.resources.getDimension(R.dimen.cornerRadius)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoTaskViewHolder {
        val rowTaskBinding = RowTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TodoTaskViewHolder(rowTaskBinding)
    }


    override fun onBindViewHolder(holder: TodoTaskViewHolder, position: Int) {

        holder.binding.root.visibility = View.VISIBLE

        val currentTask = listInstance.getTask(holder.bindingAdapterPosition)

        //Set text of task to be visible
        holder.binding.tvName.text = currentTask.title

        //Set Long click listener to initiate re-sorting
        holder.binding.tvName.setOnLongClickListener {
            val animationShake =
                AnimationUtils.loadAnimation(myActivity, R.anim.shake_small)
            holder.binding.root.startAnimation(animationShake)
            true
        }

        if (currentTask.isChecked) {
            //Display the task as checked: check checkbox, strike through text, use gray colors for text and background
            holder.binding.cbTask.isChecked = true
            holder.binding.tvName.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.binding.tvName.setTextColor(
                myActivity.colorForAttr(R.attr.colorHint)
            )
            holder.binding.crvTask.setCardBackgroundColor(myActivity.colorForAttr(R.attr.colorCheckedTask))
            holder.binding.crvBg.setCardBackgroundColor(myActivity.colorForAttr(R.attr.colorCheckedTask))
        } else {
            //Display the task as unchecked: Uncheck checkbox, remove strike-through of text, initialize correct colors
            holder.binding.cbTask.isChecked = false
            holder.binding.tvName.paintFlags = 0

            val taskTextColor = if (dark) {
                //colored task text when in dark theme
                if (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE) == 3.0)
                    R.attr.colorOnBackGround
                else when (listInstance.getTask(holder.bindingAdapterPosition).priority) {
                        1 -> R.attr.colorPriority1
                        2 -> R.attr.colorPriority2
                        else -> R.attr.colorPriority3
                    }
            } else {
                //white text when in light theme
                R.attr.colorBackground
            }

            val taskBackgroundColor = if (dark) {
                //dark background in dark theme
                if (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE) != 3.0)
                    R.attr.colorBackgroundElevated
                else when (listInstance.getTask(holder.bindingAdapterPosition).priority) {
                        1 -> R.attr.colorPriority1darker
                        2 -> R.attr.colorPriority2darker
                        else -> R.attr.colorPriority3darker
                    }
            } else {
                //colored background in light theme
                when (listInstance.getTask(holder.bindingAdapterPosition).priority) {
                    1 -> R.attr.colorPriority1
                    2 -> R.attr.colorPriority2
                    else -> R.attr.colorPriority3
                }
            }

            val taskBorderColor = if (dark) {
                when (SettingsManager.getSetting(SettingId.DARK_BORDER_STYLE)) {
                    1.0 -> R.attr.colorBackgroundElevated
                    2.0 -> taskTextColor
                    else -> taskBackgroundColor
                }
            } else {
                taskBackgroundColor
            }

            holder.binding.tvName.setTextColor(myActivity.colorForAttr(taskTextColor))
            holder.binding.crvTask.setCardBackgroundColor(myActivity.colorForAttr(taskBackgroundColor))
            holder.binding.crvBg.setCardBackgroundColor(myActivity.colorForAttr(taskBorderColor))
        }

        //set corner radius to be round if style is set to round
        holder.binding.crvTask.radius = if (round) cr else 0f
        holder.binding.crvBg.radius = if (round) cr else 0f

        /**
         * EDITING task
         * Onclick-Listener on List items, opening the edit-task dialog
         */

        holder.binding.tvName.setOnClickListener {

            //inflate the dialog with custom view
            val dialogAddTaskBinding = DialogAddTaskBinding.inflate(LayoutInflater.from(myActivity))

            //AlertDialogBuilder
            val myBuilder = AlertDialog.Builder(myActivity).setView(dialogAddTaskBinding.root)

            val titleDialogBinding = TitleDialogBinding.inflate(LayoutInflater.from(myActivity))
            titleDialogBinding.tvDialogTitle.text = myActivity.resources.getText(R.string.tasksEditTitle)
            myBuilder.setCustomTitle(titleDialogBinding.root)

            //show dialog
            val myAlertDialog = myBuilder.create()
            myAlertDialog.window?.setSoftInputMode(
                WindowManager
                    .LayoutParams.SOFT_INPUT_STATE_VISIBLE
            )
            myAlertDialog.show()

            //write current task to textField
            dialogAddTaskBinding.etTitleAddTask.requestFocus()
            dialogAddTaskBinding.etTitleAddTask.setText(listInstance.getTask(holder.bindingAdapterPosition).title)
            dialogAddTaskBinding.etTitleAddTask.setSelection(dialogAddTaskBinding.etTitleAddTask.text.length)

            //adds listeners to confirmButtons in addTaskDialog
            val taskConfirmButtons = arrayListOf(
                dialogAddTaskBinding.btnConfirm1,
                dialogAddTaskBinding.btnConfirm2,
                dialogAddTaskBinding.btnConfirm3
            )

            dialogAddTaskBinding.etTitleAddTask.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
                    taskConfirmButtons[listInstance.getTask(holder.bindingAdapterPosition).priority-1].performClick()
                    true
                } else false
            }

            //Three buttons to create tasks with priorities 1-3
            taskConfirmButtons.forEachIndexed { index, button ->
                button.setOnClickListener Button@{
                    if (dialogAddTaskBinding.etTitleAddTask.text.toString().trim() == "") {
                        val animationShake =
                            AnimationUtils.loadAnimation(myActivity, R.anim.shake)
                        dialogAddTaskBinding.etTitleAddTask.startAnimation(animationShake)
                        return@Button
                    }
                    val newPos = listInstance.editTask(
                        holder.bindingAdapterPosition, index + 1,
                        dialogAddTaskBinding.etTitleAddTask.text.toString(),
                        listInstance.getTask(holder.bindingAdapterPosition).isChecked
                    )
                    this.notifyItemChanged(holder.bindingAdapterPosition)
                    myFragment.prepareForMove()
                    this.notifyItemMoved(holder.bindingAdapterPosition, newPos)
                    myFragment.reactToMove()
                    myAlertDialog.dismiss()

                }
            }
        }

        //reacts to the user checking a task
        holder.binding.tapField.setOnClickListener {
            val checkedStatus = !listInstance.getTask(holder.bindingAdapterPosition).isChecked
            holder.binding.cbTask.isChecked = checkedStatus
            val task = listInstance.getTask(holder.bindingAdapterPosition)
            val newPos = listInstance.editTask(
                holder.bindingAdapterPosition, task.priority,
                task.title, checkedStatus
            )
            myFragment.updateUndoTaskIcon()

            notifyItemChanged(holder.bindingAdapterPosition)
            if (holder.bindingAdapterPosition != newPos) {
                myFragment.prepareForMove()
                notifyItemMoved(holder.bindingAdapterPosition, newPos)
                myFragment.reactToMove()
            }
            myFragment.updateTodoIcons()

        }
    }

    override fun getItemCount() = TodoFr.todoListInstance.size

    class TodoTaskViewHolder(rowTaskBinding: RowTaskBinding) : RecyclerView.ViewHolder(rowTaskBinding.root){
        var binding = rowTaskBinding
    }
}



