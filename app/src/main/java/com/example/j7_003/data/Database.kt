package com.example.j7_003.data

import android.content.Context
import com.example.j7_003.data.database_objects.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

class Database(context: Context) : Serializable {
    private val storageHandler = StorageHandler(context)

    companion object {
        lateinit var taskList: ArrayList<Task>
        lateinit var birthdayList: ArrayList<Birthday>
        lateinit var noteList: ArrayList<Note>
    }

    var taskList: ArrayList<Task>
    var birthdayList: ArrayList<Birthday>
    var noteList: ArrayList<Note>

    private val TLIST = "TASKLIST"
    private val BLIST = "BIRTHDAYLIST"
    private val NLIST = "NOTELIST"

    init {
        initStorage()
        initLists()
        taskList = Companion.taskList
        birthdayList = Companion.birthdayList
        noteList = Companion.noteList
        sortTasks()
        sortBirthday()
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//


    /**
     * Adds a task to the tasklist and saves the tasklist.
     * @param title The title of the created task
     * @param priority The priority the task will be set to
     */
    fun addTask(title: String, priority: Int) {
        taskList.add(
            Task(
                title,
                priority
            )
        )
        sortTasks()
        storageHandler.saveToFile(StorageHandler.files["TASKLIST"], taskList)
    }

    /**
     * Deletes a task at a given index.
     * @param index The index of the list, which will be removed.
     */
    fun deleteTask(index: Int) {
        taskList.removeAt(index)
        storageHandler.saveToFile(StorageHandler.files["TASKLIST"], taskList)
    }

    fun editTask(position: Int, index: Int, title: String) {
        val editableTask: Task = getTask(position)
        editableTask.title = title
        editableTask.priority = index + 1
        sortTasks()
        storageHandler.saveToFile(StorageHandler.files["TASKLIST"], taskList)
    }

    /**
     * Returns a task at a given index in the tasklist.
     * @param index The index the task is at.
     * @return Returns the requested task.
     */
    fun getTask(index: Int): Task = taskList[index]

    fun sortTasks() {
        taskList.sortWith(compareBy({ it.priority }, { it.priority }))
    }

    private fun fetchTaskList() : ArrayList<Task> {
        val jsonString = StorageHandler.files[TLIST]?.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be the birthday functionality
    /**
     * Adds a birthday to the birthdaylist and saves the birthdaylist.
     * @param name The name of the created birthday
     * @param parMonth The month of the birthday
     * @param parDay The day of the birthday
     */
    fun addBirthday(name: String, parDay: Int, parMonth: Int, parReminder: Int) {
        birthdayList.add(Birthday(name, parMonth, parDay, parReminder))

        sortBirthday()

        storageHandler.saveToFile(StorageHandler.files[BLIST], birthdayList)
    }

    /**
     * Deletes the Birthday at a given index in the birthdaylist
     * @param index The position of the birthday in the array list
     */
    fun deleteBirthday(index: Int) {
        birthdayList.removeAt(index)
        storageHandler.saveToFile(StorageHandler.files[BLIST], birthdayList)
    }

    /**
     * To be implemented...
     * Will edit a given birthday object
     */

    fun editBirthday(name: String, parDay: Int, parMonth: Int, parReminder: Int, parPosition: Int) {
        val editableBirthday: Birthday = getBirthday(parPosition)

        editableBirthday.name = name
        editableBirthday.day = parDay
        editableBirthday.month = parMonth
        editableBirthday.daysToRemind = parReminder

        sortBirthday()
        storageHandler.saveToFile(StorageHandler.files[BLIST], birthdayList)
    }

    /**
     * Returns a birthday from arraylist at given index
     * @return Returns requested birthday object
     */
    fun getBirthday(position: Int): Birthday = birthdayList[position]

    private fun sortBirthday() {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH) + 1
        val cacheList = ArrayList<Birthday>()
        var i = 0

        birthdayList.sortWith(compareBy({ it.month }, { it.day }, { it.name }))

        while(i < birthdayList.size) {
            if (getBirthday(i).month < month || (getBirthday(i).month == month && getBirthday(i).day < day)) {
                cacheList.add(getBirthday(i))
                birthdayList.remove(getBirthday(i))
            } else {
                i++
            }
        }

        birthdayList.sortWith(compareBy(
            { it.month < month },
            { it.month },
            { it.day < day },
            { it.day },
            { it.name })
        )

        cacheList.forEach { n ->
            birthdayList.add(n)
        }
    }

    fun getXNextBirthdays(index: Int): ArrayList<Birthday> {
        var min = index

        if (index > birthdayList.size) {
            min = birthdayList.size
        }

        val xNextBirthdays = ArrayList<Birthday>()

        for (i in 0..min) {
            xNextBirthdays.add(getBirthday(i))
        }

        return xNextBirthdays
    }

    private fun fetchBList() : ArrayList<Birthday> {
        val jsonString = StorageHandler.files[BLIST]?.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be note handling

    fun addNote(title: String, note: String, color: NoteColors) {
        noteList.add(Note(title, note, color))
        storageHandler.saveToFile(StorageHandler.files[NLIST], noteList)
    }

    fun editNote(index: Int, title: String, note: String, color: NoteColors) {
        val editableNote = getNote(index)
        editableNote.title = title
        editableNote.note = note
        editableNote.color = color
        storageHandler.saveToFile(StorageHandler.files[NLIST], noteList)
    }

    fun deleteNote(index: Int) {
        noteList.removeAt(index)
        storageHandler.saveToFile(StorageHandler.files[NLIST], noteList)
    }

    fun getNote(index: Int): Note = noteList[index]

    private fun fetchNoteList() : ArrayList<Note> {
        val jsonString = StorageHandler.files[NLIST]?.readText()

        return GsonBuilder().create()
            .fromJson(jsonString, object : TypeToken<ArrayList<Note>>() {}.type)
    }

    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //--------------------------------------------------------------------------------------------//
    //debug here will be database handling

    private fun initStorage() {
        storageHandler.addCollToFiles(TLIST, "TaskList.json")
        storageHandler.addCollToFiles(BLIST, "BirthdayList.json")
        storageHandler.addCollToFiles(NLIST, "NoteFile.json")
    }

    private fun initLists() {
        Companion.birthdayList = fetchBList()
        Companion.taskList = fetchTaskList()
        Companion.noteList = fetchNoteList()
    }
}