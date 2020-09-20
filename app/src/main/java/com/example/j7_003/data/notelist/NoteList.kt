package com.example.j7_003.data.notelist

import com.example.j7_003.system_interaction.handler.StorageHandler
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

class NoteList: LinkedList<Note>() {
    init {
        StorageHandler.createJsonFile(
            "NOTELIST",
            "NoteFile.json"
        )
        fetchFromFile()
    }

    /**
     * Creates a note with the given parameters and saves it to file.
     * @param title Displayed title of the note.
     * @param content Contents of the note.
     * @param color Color of the note.
     */
    fun addNote(title: String, content: String, color: NoteColors) {
        this.push(Note(title, content, color))
        save()
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
        save()
    }

    /**
     * Deletes the requested note.
     * @param index Position of the note in the noteList.
     */
    fun deleteNote(index: Int) {
        this.removeAt(index)
        save()
    }

    /**
     * Gets the requested note from the list.
     * @return The requested noteObject.
     */
    fun getNote(index: Int): Note = this[index]

    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files["NOTELIST"],
            this
        )
    }

    private fun fetchFromFile() {
        val jsonString = StorageHandler.files["NOTELIST"]?.readText()

        this.addAll(
            GsonBuilder().create().fromJson(
                jsonString, object : TypeToken<LinkedList<Note>>() {}.type))
    }
}