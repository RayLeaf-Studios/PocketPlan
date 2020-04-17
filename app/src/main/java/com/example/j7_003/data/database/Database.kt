package com.example.j7_003.data.database.database_objects

import com.example.j7_003.data.NoteColors
import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.ArrayList

class Database {
    companion object {
        lateinit var taskList: ArrayList<Task>
        lateinit var birthdayList: ArrayList<Birthday>
        lateinit var noteList: LinkedList<Note>

        private const val TLIST = "TASKLIST"
        private const val BLIST = "BIRTHDAYLIST"
        private const val NLIST = "NOTELIST"

        fun init() {
            initStorage()
            initLists()
            sortTasks()
            sortBirthday()
        }

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
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[TLIST],
                taskList
            )
        }

        /**
         * Deletes a task at a given index.
         * @param index The index of the list, which will be removed.
         */
        fun deleteTask(index: Int) {
            taskList.removeAt(index)
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[TLIST],
                taskList
            )
        }

        fun editTask(position: Int, index: Int, title: String) {
            val editableTask: Task =
                getTask(
                    position
                )
            editableTask.title = title
            editableTask.priority = index + 1
            sortTasks()
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[TLIST],
                taskList
            )
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

            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[BLIST],
                birthdayList
            )
        }

        /**
         * Deletes the Birthday at a given index in the birthdaylist
         * @param index The position of the birthday in the array list
         */
        fun deleteBirthday(index: Int) {
            birthdayList.removeAt(index)
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[BLIST],
                birthdayList
            )
        }

        /**
         * To be implemented...
         * Will edit a given birthday object
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
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[BLIST],
                birthdayList
            )
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
                if (getBirthday(
                        i
                    ).month < month || (getBirthday(
                        i
                    ).month == month && getBirthday(
                        i
                    ).day < day)) {
                    cacheList.add(
                        getBirthday(
                            i
                        )
                    )
                    birthdayList.remove(
                        getBirthday(
                            i
                        )
                    )
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
                xNextBirthdays.add(
                    getBirthday(
                        i
                    )
                )
            }

            return xNextBirthdays
        }

        fun fetchBList() : ArrayList<Birthday> {
            val jsonString = StorageHandler.files[BLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<ArrayList<Birthday>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //debug here will be note handling

        fun addNote(title: String, note: String, color: NoteColors) {
            noteList.push(Note(title, note, color))
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[NLIST],
                noteList
            )
        }

        fun editNote(index: Int, title: String, note: String, color: NoteColors) {
            val editableNote =
                getNote(
                    index
                )
            editableNote.title = title
            editableNote.note = note
            editableNote.color = color
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[NLIST],
                noteList
            )
        }

        fun deleteNote(index: Int) {
            noteList.removeAt(index)
            StorageHandler.saveAsJsonToFile(
                StorageHandler.files[NLIST],
                noteList
            )
        }

        fun getNote(index: Int): Note = noteList[index]

        private fun fetchNoteList() : LinkedList<Note> {
            val jsonString = StorageHandler.files[NLIST]?.readText()

            return GsonBuilder().create()
                .fromJson(jsonString, object : TypeToken<LinkedList<Note>>() {}.type)
        }

        //--------------------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------------------//
        //debug here will be database handling

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
    }
}