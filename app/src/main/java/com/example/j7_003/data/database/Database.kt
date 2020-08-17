package com.example.j7_003.data.database

import android.animation.TimeAnimator
import android.provider.ContactsContract
import com.example.j7_003.data.NoteColors
import com.example.j7_003.data.database.database_objects.Birthday
import com.example.j7_003.data.database.database_objects.Note
import com.example.j7_003.data.database.database_objects.Task
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.threeten.bp.LocalDate
import java.util.*
import java.util.concurrent.BlockingDeque
import kotlin.collections.ArrayList

/**
 * A simple handler to manage the interaction of different objects
 * with a similar structure.
 */
class Database {
    companion object {
        lateinit var taskList: ArrayList<Task>
        lateinit var birthdayList: ArrayList<Birthday>
        lateinit var noteList: LinkedList<Note>

        private const val TLIST = "TASKLIST"
        private const val BLIST = "BIRTHDAYLIST"
        private const val NLIST = "NOTELIST"

        /**
         * Used to initialize the Database, i.e. setting up the storage
         * and loading existing data from the file. Also sorts the taskList and
         * birthdayList.
         */
        fun init() {
            initStorage()
            initLists()
            sortTasks()
            sortBirthday()
        }

        /**
         * Adds a task to the taskList and saves the taskList.
         * @param title The title of the created task
         * @param priority The priority the task will be set to
         */
        fun addTask(
            title: String,
            priority: Int,
            isChecked: Boolean
        ) {
            taskList.add(Task(title, priority, isChecked))
            sortTasks()
            save(TLIST, taskList)
        }

        /**
         * Helper function to add a task object, used for undoing deletions
         */
        fun addFullTask(task: Task): Int{
            taskList.add(task)
            sortTasks()
            save(TLIST, taskList)
            return taskList.indexOf(task)
        }

        /**
         * Deletes a task at a given index.
         * @param index The index of the list, which will be removed.
         */
        fun deleteTask(index: Int) {
            taskList.removeAt(index)
            save(TLIST, taskList)
        }

        /**
         * Edits the requested task to have a new title and priority.
         * @param position The tasks position in the list.
         * @param priority The tasks new priority.
         * @param title The new title of the task.
         */
        fun editTask(position: Int, priority: Int, title: String, isChecked: Boolean) : Int{
            val editableTask: Task =
                getTask(
                    position
                )
            editableTask.title = title
            editableTask.priority = priority
            editableTask.isChecked = isChecked
            sortTasks()
            save(TLIST, taskList)
            return taskList.indexOf(editableTask)
        }

        /**
         * Returns a task at a given index in the taskList.
         * @param index The index the task is at.
         * @return Returns the requested task.
         */
        fun getTask(index: Int): Task = taskList[index]

        private fun sortTasks() {
            taskList.sortWith(compareBy({ it.isChecked }, { it.priority }))
        }

        fun deleteCheckedTasks(): Int{
            val toBeDeleted = ArrayList<Task>()
            taskList.forEach { n ->
                if (n.isChecked) toBeDeleted.add(n)
            }

            toBeDeleted.forEach { n ->
                taskList.remove(n)
            }

            save(TLIST, taskList)
            return taskList.size
        }

        private fun fetchTaskList() : ArrayList<Task> {
            val jsonString = StorageHandler.files[TLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Task>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //-----------------------------------birthdayList handling------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //debug here will be the birthday functionality
        /**
         * Adds a birthday to the birthdayList and saves the birthdayList.
         * @param name The name of the created birthday
         * @param parMonth The month of the birthday
         * @param parDay The day of the birthday
         */
        fun addBirthday(name: String, day: Int, month: Int, daysToRemind: Int) {
            birthdayList.add(Birthday(name, month, day, daysToRemind))

            sortBirthday()

            save(BLIST, birthdayList)
        }

        /**
         * Helper Function to add a full birthday object, used for undoing deletions
          */
        fun addFullBirthday(birthday: Birthday): Int{
            birthdayList.add(birthday)
            sortBirthday()
            save(BLIST, birthdayList)
            return birthdayList.indexOf(birthday)
        }

        /**
         * Deletes the Birthday at a given index in the birthdayList
         * @param index The position of the birthday in the array list
         */
        fun deleteBirthday(index: Int) {
            birthdayList.removeAt(index)
            sortBirthday()
            save(BLIST, birthdayList)
        }

        /**
         * Grabs a birthday object and changes its attributes according to the parameters.
         * @param name Name of the Person.
         * @param parDay Day of the birthday.
         * @param parMonth Month of the birthday.
         * @param parReminder Days to be reminded at prior to the birthday.
         * @param parPosition Position of the birthday object int he list.
         */
        fun editBirthday(name: String, parDay: Int, parMonth: Int, parReminder: Int, parPosition: Int) {
            val editableBirthday: Birthday =
                getBirthday(
                    parPosition
                )

            editableBirthday.name = name
            editableBirthday.day = parDay
            editableBirthday.month = parMonth
            editableBirthday.daysToRemind = parReminder

            sortBirthday()
            save(BLIST, birthdayList)
        }

        /**
         * Returns a birthday from arraylist at given index
         * @return Returns requested birthday object
         */
        fun getBirthday(position: Int): Birthday = birthdayList[position]

        private fun manageLabels() {
            val months = arrayListOf<Int>()
            var n = 0
            while (n < birthdayList.size) {
                if (birthdayList[n].daysToRemind < 0) birthdayList.remove(birthdayList[n])
                else n++
            }

            val today = LocalDate.now()
            var beforeMonth = false
            var afterMonth = false
            birthdayList.forEach { m ->
                if(!months.contains(m.month) && m.month != today.monthValue){
                    months.add(m.month)
                }

                if (m.month == today.monthValue && m.day < today.dayOfMonth) beforeMonth = true
                else if (m.month == today.monthValue && m.day >= today.dayOfMonth) afterMonth = true
            }

            if (beforeMonth) {
                birthdayList.add(
                    Birthday(
                        today.month.toString().toLowerCase().capitalize(),
                        today.monthValue,
                        1,
                        -1 * today.monthValue
                    )
                )
            }

            if (afterMonth) {
                birthdayList.add(
                    Birthday(
                        today.month.toString().toLowerCase().capitalize(),
                        today.monthValue,
                        today.dayOfMonth,
                        -1 * today.monthValue
                    )
                )
            }

            months.forEach { m ->
                val name = LocalDate.of(2020, m, 1).month.toString()
                birthdayList.add(Birthday(name.toLowerCase().capitalize(), m, 0, -1*m))
            }
        }

        private fun sortBirthday() {
            manageLabels()
            val localDate = LocalDate.now()
            val day = localDate.dayOfMonth
            val month = localDate.month.value
            val cacheList = ArrayList<Birthday>()
            birthdayList.sortWith(compareBy({ it.month }, { it.day }, {it.daysToRemind >= 0}, { it.name }))

            var i = 0
            val spacerBirthday = Birthday("---    ${localDate.year + 1}    ---", 1, 1, -200)
            cacheList.add(spacerBirthday)
            while(i < birthdayList.size) {
                if (getBirthday(i).month < month ||
                    (getBirthday(i).month == month && getBirthday(i).day < day)) {
                    cacheList.add(getBirthday(i))
                    birthdayList.remove(getBirthday(i))
                } else {
                    i++
                }
            }

            birthdayList.sortWith(compareBy(
                { it.month },
                { it.day },
                { it.daysToRemind >= 0},
                { it.name })
            )

            if (cacheList.size == 1) {
                cacheList.remove(spacerBirthday)
            }

            cacheList.forEach { n ->
                birthdayList.add(n)
            }
        }

        /**
         * Collects all birthdays that are happening on the current day and returns
         * them as an list.
         * @return List of today's birthdays.
         */
        fun getRelevantCurrentBirthdays(): ArrayList<Birthday> {
            val currentBirthdays = ArrayList<Birthday>()
            val localDate = LocalDate.now()
            birthdayList.forEach { n ->
                if (n.month == localDate.monthValue  &&
                    n.day == localDate.dayOfMonth &&
                    n.daysToRemind == 0
                ) {
                    currentBirthdays.add(n)
                }
            }
            return currentBirthdays
        }

        /**
         * Collects all birthdays which's reminder corresponds to the current day
         * @see getRelevantCurrentBirthdays for current birthdays.
         * @return List of birthdays to be reminded of on the current day.
         */
        fun getRelevantUpcomingBirthdays(): ArrayList<Birthday> {
            val upcomingBirthdays = ArrayList<Birthday>()
            val localDate = LocalDate.now()
            birthdayList.forEach { n ->
                if (n.month == localDate.monthValue + 1 && (n.day - n.daysToRemind) ==
                    localDate.dayOfMonth && n.daysToRemind != 0)
                {
                    upcomingBirthdays.add(n)
                }
            }
            return upcomingBirthdays
        }

        /**
         * Returns the x next birthdays from the birthdayList as a list. If the requested
         * size is larger than the birthdayList size the whole list is returned.
         * @param index Amount of birthdays to return.
         * @return The requested amount of birthdays or the whole birthdayList.
         */
        fun getXNextBirthdays(index: Int): ArrayList<Birthday> {
            var min = index

            if (index > birthdayList.size) {
                min = birthdayList.size
            }

            val xNextBirthdays = ArrayList<Birthday>()

            for (i in 0..min) {
                xNextBirthdays.add(
                    getBirthday(
                        i
                    )
                )
            }

            return xNextBirthdays
        }

        /**
         * Fetches the data from the birthdayList's file and returns it as a list.
         * @return The loaded version of the birthdayList.
         */
        fun fetchBList() : ArrayList<Birthday> {
            val jsonString = StorageHandler.files[BLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //---------------------------------noteList handling------------------------------------------//
        //--------------------------------------------------------------------------------------------//

        /**
         * Creates a note with the given parameters and saves it to file.
         * @param title Displayed title of the note.
         * @param content Contents of the note.
         * @param color Color of the note.
         */
        fun addNote(title: String, content: String, color: NoteColors) {
            noteList.push(Note(title, content, color))
            save(NLIST, noteList)
        }

        /**
         * Small helper function to add a note object, used for undoing deletions
          */
        fun addFullNote(note: Note){
            addNote(note.title, note.content, note.color)
        }

        /**
         * Changes the requested notes properties.
         * @param index Position of the note in the noteList.
         * @param title Title of the note.
         * @param content Contents of the note color.
         * @param color Color of the note.
         */
        fun editNote(index: Int, title: String, content: String, color: NoteColors) {
            val editableNote =
                getNote(
                    index
                )
            editableNote.title = title
            editableNote.content = content
            editableNote.color = color
            save(NLIST, noteList)
        }

        /**
         * Deletes the requested note.
         * @param index Position of the note in the noteList.
         */
        fun deleteNote(index: Int) {
            noteList.removeAt(index)
            save(NLIST, noteList)
        }

        /**
         * Gets the requested note from the list.
         * @return The requested noteObject.
         */
        fun getNote(index: Int): Note = noteList[index]

        private fun fetchNoteList() : LinkedList<Note> {
            val jsonString = StorageHandler.files[NLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<LinkedList<Note>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //-------------------------------Database internal handling-----------------------------------//
        //--------------------------------------------------------------------------------------------//

        private fun initStorage() {
            StorageHandler.createJsonFile(
                TLIST,
                "TaskList.json"
            )
            StorageHandler.createJsonFile(
                BLIST,
                "BirthdayList.json"
            )
            StorageHandler.createJsonFile(
                NLIST,
                "NoteFile.json"
            )
        }

        private fun initLists() {
            birthdayList =
                fetchBList()
            taskList =
                fetchTaskList()
            noteList =
                fetchNoteList()
        }

        private fun save(identifier: String, collection: Collection<Any>) {
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[identifier],
                collection
            )
        }
    }
}