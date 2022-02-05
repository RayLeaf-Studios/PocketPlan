package com.pocket_plan.j7_003.data.notelist

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

class NoteDirList: Checkable {
    private val rootDirName = "groot"
    var rootDir: Note = Note(rootDirName, NoteColors.GREEN, NoteList())
    var currentList: NoteList = rootDir.noteList
    var folderStack: Stack<Note> = Stack()

    init {
        StorageHandler.createJsonFile(StorageId.NOTES)
        try {   // Todo - part of the compatibility layer; remove try, catch soon
            fetchFromFile()
        } catch (_: Exception) {/* no-op */}
        currentList = rootDir.noteList

        try {   // Todo - main part of the comp. layer; also remove soon
            val jsonString = StorageHandler.files[StorageId.NOTES]?.readText()
            GsonBuilder().create()
                .fromJson<LinkedList<Note>>(jsonString, object : TypeToken<LinkedList<Note>>() {}.type)
                .forEach { currentList.add(it) }
            save()
        } catch (_: Exception) {/* no-op */}

        folderStack.push(rootDir)
    }

    /**
     * Creates a note in the current folder with the given parameters and saves it to file.
     * @param title Displayed title of the note.
     * @param content Contents of the note.
     * @param color Color of the note.
     * @see NoteList.addNote
     */
    fun addNote(title: String, content: String, color: NoteColors) {
        currentList.addNote(title, content, color)
        save()
    }

    fun remove(note: Note) {
        currentList.remove(note)
        save()
    }

    /**
     * Small helper function to add a note object, used for undoing deletions
     * @see NoteList.addFullNote
     */
    fun addFullNote(note: Note) {
        currentList.addFullNote(note)
        save()
    }

    fun removeNoteAt(index: Int) {
        currentList.removeAt(index)
        save()
    }

    fun addNote(index: Int, note: Note) {
        currentList.add(index, note)
        save()
    }

    /**
     * From superordinatepaths
     */
    fun getParentFolderIndex(dir: Note): Int {
        val parentDir = getDirPathsWithRef().find {it.second.noteList.contains(dir)}!!.second
        val containingDirs = containingDirs(dir)
        containingDirs.add(dir)
        val supPairs = getDirPathsWithRef().filter {
            !containingDirs.contains(it.second)
        }
        supPairs.forEachIndexed { index, pair ->
            if (pair.second == parentDir) return index
        }
        return 0
    }

    private fun getParentDirectory(dir: Note): Note {
        return getDirPathsWithRef().find {it.second.noteList.contains(dir)}!!.second
    }

    //Todo, adjust stack, put user to goal?
    //todo, dont move if its already where it should be
    fun moveDir(noteToMove: Note, toIndex: Int) {
        getParentDirectory(noteToMove).noteList.remove(noteToMove)
        val containingDirs = containingDirs(noteToMove)
        containingDirs.add(noteToMove)
        getDirPathsWithRef().filter {
            !containingDirs.contains(it.second)}[toIndex].second.noteList.add(noteToMove)
        save()
    }

    fun getSuperordinatePaths(dir: Note): ArrayList<String> {
        val paths = arrayListOf<String>()
        val containingDirs = containingDirs(dir)
        containingDirs.add(dir)
        getDirPathsWithRef().filter {
            !containingDirs.contains(it.second)
        }.forEach { paths.add(it.first) }
        return paths
    }

    fun getDirPaths(): ArrayList<String> {
        val paths = arrayListOf<String>()
        getDirPathsWithRef().forEach { paths.add(it.first) }
        return paths
    }

    private fun getDirPathsWithRef(): ArrayList<Pair<String, Note>> {
        val pathsAndDirs = arrayListOf(Pair(rootDirName, rootDir))
        containingDirs(rootDir).forEach {
            if (rootDir.noteList.contains(it))
                pathsAndDirs.add(Pair("$rootDirName   ›   ${it.title}", it))
            else
                pathsAndDirs.add(Pair("...   ›   ${it.title}", it))
        }
        return pathsAndDirs
    }

    private fun containingDirs(dir: Note): ArrayList<Note> {
        val dirs = arrayListOf<Note>()
        dir.noteList.forEach {
            if (it.content == null)
                dirs.add(it)
        }

        val childDirs = arrayListOf<Note>()
        if (dirs.size > 0) {
            dirs.forEach {
                childDirs.addAll(containingDirs(it))
            }
            dirs.addAll(childDirs)
        }

        return dirs
    }

    /**
     * Returns the note at the given position of this folder. Also includes folder notes.
     * @param index The position of the requested note.
     */
    fun getNote(index: Int): Note = currentList[index]

    /**
     * Navigates into the given folder and updates the currentlist.
     * @param dir The directory to open.
     */
    fun openFolder(dir: Note) {
        currentList = dir.noteList
        folderStack.add(dir)
    }

    /**
     * Deletes the currently opened folder, except for the root folder which can't
     * be deleted. Also saves the notes to file.
     */
    fun deleteCurrentFolder(): Note? {
        if (folderStack.size == 1) return null

        val deletedDir = folderStack.pop()
        currentList = folderStack.peek().noteList
        currentList.remove(deletedDir)
        save()

        return deletedDir
    }

    /**
     * Tries to rename the current folder. Returns a boolean depending
     * on the outcome of the renaming process.
     * @param newName The new name the directory will get.
     * @return True on success, false otherwise.
     */
    fun editFolder(newName: String, newColor: NoteColors): Boolean{
        if (folderStack.size == 1 || newName.trim() == "" || newName == rootDirName) return false
        folderStack.peek().title = newName
        folderStack.peek().color = newColor
        save()
        return true
    }

    /**
     * Steps one directory back, if possible. Returns a true if
     * moving was possible.
     * @return True if the move worked, false otherwise.
     */
    fun goBack(): Boolean{
        if(folderStack.size <= 1){
            return false
        }
        folderStack.pop()
        currentList = folderStack.peek().noteList
        return true
    }

    /**
     * @see NoteList.addNote
     */
    fun addNote(note: Note) {
        currentList.add(note)
        save()
    }

    /**
     * Tries to add a directory in the current one, while forbidding to set the rootDirName.
     * @param noteDir The directory to be created.
     * @return True if the directory was added, false otherwise.
     */
    fun addNoteDir(noteDir: Note): Boolean {
        if (noteDir.title == rootDirName || noteDir.title.trim() == ""){
            return false
        }
        currentList.add(noteDir)
        save()
        return true
    }

    /**
     * Todo - update doc
     * Tries to add a directory in the current one, while forbidding to set the rootDirName.
     * @param noteDir The directory to be created.
     * @return True if the directory was added, false otherwise.
     */
    fun addNoteDir(noteDir: Note, index: Int): Boolean {
        if (noteDir.title == rootDirName){
            return false
        }
        getDirPathsWithRef()[index].second.noteList.add(noteDir)
        save()
        return true
    }

    /**
     * The count of notes and folders in the current directory.
     * @return The aforementioned count notes and folders.
     */
    fun getNoteObjCount(): Int{
       return currentList.size
    }

    /**
     * Saves all notes to file.
     */
    fun save() {
        StorageHandler.saveAsJsonToFile(
            StorageHandler.files[StorageId.NOTES], rootDir)
    }

    private fun fetchFromFile() {
        val jsonString = StorageHandler.files[StorageId.NOTES]?.readText()

        rootDir = GsonBuilder().create().fromJson(jsonString, object : TypeToken<Note>() {}.type)
    }

    override fun check() {
        getDirPathsWithRef().forEach {
            it.second.noteList.check()
        }
    }

    fun search(query: String): Collection<Note> {
        val dirs = getDirPathsWithRef()
        val results = ArrayList<Note>()

        dirs.forEach {
            it.second.noteList.forEach { note ->
                if (note.title.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT))
                    || note.content != null && note.content!!.toLowerCase(Locale.ROOT).contains(query.toLowerCase(Locale.ROOT)))
                        results.add(note)
            }
        }

        return results
    }
}