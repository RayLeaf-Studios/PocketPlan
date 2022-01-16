package com.pocket_plan.j7_003.data.notelist

import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.lang.NullPointerException
import java.util.*

class NoteList : LinkedList<Note>(), Checkable {
    init {
        StorageHandler.createJsonFile(StorageId.NOTES)
        fetchFromFile()

        // TODO - compatibility layer for old nodes to conform to new note hierarchy
        //  they are placed in the top/start directory
        this.forEach {
            if (it.dir == null)
                it.dir = "/"
        }
        save()
    }

    /**
     * Creates a note with the given parameters and saves it to file.
     * @param title Displayed title of the note.
     * @param content Contents of the note.
     * @param color Color of the note.
     */
    fun addNote(title: String, content: String, color: NoteColors) {
        this.push(Note(title, content, color, "/"))
        save()
    }

    /**
     * Creates a note with the given parameters and saves it to file.
     * @param title Displayed title of the note.
     * @param content Contents of the note.
     * @param color Color of the note.
     * @param dir Directory the note is placed in.
     */
    fun addNote(title: String, content: String, color: NoteColors, dir: String) {
        this.push(Note(title, content, color, dir))
        save()
    }

    /**
     * Small helper function to add a note object, used for undoing deletions
     */
    fun addFullNote(note: Note): Int {
        addNote(note.title, note.content, note.color)
        return this.indexOf(note)
    }

    /**
     * Gets the requested note from the list.
     * @return The requested noteObject.
     */
    fun getNote(index: Int): Note = this[index]

    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.NOTES], this
        )
    }

    private fun fetchFromFile() {
        val jsonString = StorageHandler.files[StorageId.NOTES]?.readText()

        this.addAll(
            GsonBuilder().create().fromJson(
                jsonString, object : TypeToken<LinkedList<Note>>() {}.type
            )
        )
    }

    override fun check() {
        this.forEach {
            if(it.color == null || it.title == null || it.content== null){
                throw NullPointerException()
            }
        }
    }

}