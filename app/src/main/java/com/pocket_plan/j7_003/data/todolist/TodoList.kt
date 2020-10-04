package com.pocket_plan.j7_003.data.todolist

import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

class TodoList: ArrayList<Task>() {
   init {
       StorageHandler.createJsonFile(StorageId.TASKS)
       fetchFromFile()
   }

    /**
     * Adds a task to the taskList and saves the taskList.
     * @param title The title of the created task
     * @param priority The priority the task will be set to
     */
    fun addTask(title: String, priority: Int, isChecked: Boolean) {
        this.add(Task(title, priority, isChecked))
        sortTasks()
        save()
    }

    /**
     * Helper function to add a task object, used for undoing deletions
     */
    fun addFullTask(task: Task): Int{
        this.add(task)
        sortTasks()
        save()
        return this.indexOf(task)
    }

    /**
     * Deletes a task at a given index.
     * @param index The index of the list, which will be removed.
     */
    fun deleteTask(index: Int) {
        this.removeAt(index)
        save()
    }

    /**
     * Edits the requested task to have a new title and priority.
     * @param position The tasks position in the list.
     * @param priority The tasks new priority.
     * @param title The new title of the task.
     */
    fun editTask(position: Int, priority: Int, title: String, isChecked: Boolean) : Int{
        val editableTask: Task = getTask(position)

        editableTask.title = title
        editableTask.priority = priority
        editableTask.isChecked = isChecked

        sortTasks()
        save()

        return this.indexOf(editableTask)
    }

    /**
     * Returns a task at a given index in the taskList.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = this[index]

    private fun sortTasks() {
        this.sortWith(compareBy({ it.isChecked }, { it.priority }))
    }

    fun somethingIsChecked(): Boolean{
        this.forEach { task ->
            if(task.isChecked){
                return true
            }
        }
        return false
    }

    fun uncheckAll(){
        this.forEach { task ->
            task.isChecked = false
        }
        sortTasks()
        save()
    }

    fun deleteCheckedTasks(): Int{
        val toBeDeleted = ArrayList<Task>()

        this.forEach { n ->
            if (n.isChecked) {
                toBeDeleted.add(n)
                TodoFr.deletedTaskList.add(n)
            }
        }

        toBeDeleted.forEach { n ->
            this.remove(n)
        }

        save()
        return this.size
    }

    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.TASKS],
            this
        )
    }

    private fun fetchFromFile() {
        val jsonString = StorageHandler.files[StorageId.TASKS]?.readText()

        this.addAll(
            GsonBuilder().create().fromJson(
                jsonString, object : TypeToken<ArrayList<Task>>() {}.type))
    }
}