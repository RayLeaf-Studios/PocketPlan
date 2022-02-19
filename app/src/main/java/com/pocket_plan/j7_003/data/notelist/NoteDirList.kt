package com.pocket_plan.j7_003.data.notelist

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.pocket_plan.j7_003.data.Checkable
import com.pocket_plan.j7_003.data.settings.SettingId
import com.pocket_plan.j7_003.data.settings.SettingsManager
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageHandler
import com.pocket_plan.j7_003.system_interaction.handler.storage.StorageId
import java.util.*

class NoteDirList: Checkable {
    private val rootDirName = "groot"
    var rootDir: Note = Note(rootDirName, NoteColors.GREEN, NoteList())
    var currentList : () -> NoteList = {folderStack.peek().noteList}
    var folderStack: Stack<Note> = Stack()

    init {
        StorageHandler.createJsonFile(StorageId.NOTES)
        try {   // Todo - part of the compatibility layer; remove try, catch soon
            fetchFromFile()
        } catch (_: Exception) {/* no-op */}
        folderStack.push(rootDir)

        try {   // Todo - main part of the comp. layer; also remove soon
            val jsonString = StorageHandler.files[StorageId.NOTES]?.readText()
            GsonBuilder().create()
                .fromJson<LinkedList<Note>>(jsonString, object : TypeToken<LinkedList<Note>>() {}.type)
                .forEach {
                    if (it.noteList == null) {
                        it.noteList = NoteList()
                    }
                    currentList().add(it)
                }
            save()
        } catch (_: Exception) {/* no-op */}

    }

    /**
     * Creates a note in the current folder with the given parameters and saves it to file.
     * @param title Displayed title of the note.
     * @param content Contents of the note.
     * @param color Color of the note.
     * @see NoteList.addNote
     */
    fun addNote(title: String, content: String, color: NoteColors) {
        currentList().addNote(title, content, color)
        save()
    }

    fun remove(note: Note) {
        currentList().remove(note)
        save()
    }

    /**
     * Small helper function to add a note object, used for undoing deletions
     * @see NoteList.addFullNote
     */
    fun addFullNote(note: Note) {
        currentList().addFullNote(note)
        save()
    }

    fun removeNoteAt(index: Int) {
        currentList().removeAt(index)
        save()
    }

    fun addNote(index: Int, note: Note) {
        currentList().add(index, note)
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

    fun getParentDirectory(dir: Note): Note {
        return getDirPathsWithRef().find {it.second.noteList.contains(dir)}!!.second
    }

    fun moveDir(noteToMove: Note, toIndex: Int) : Boolean {
        //Get containing directories
        val containingDirs = containingDirs(noteToMove)
        containingDirs.add(noteToMove)
        //Get all dirs, that are not contained in the current dir
        val validWithParent = getDirPathsWithRef().filter {!containingDirs.contains(it.second)}

        //get new parent directory
        val newParent = validWithParent[toIndex].second

        //Return false if trying to move to parent index
        if (newParent == getParentDirectory(noteToMove))
            return false

        //remove from current parent directory
        getParentDirectory(noteToMove).noteList.remove(noteToMove)

        //Add to new parent directory
        newParent.noteList.add(noteToMove)

        adjustStackAbove(noteToMove)
        save()
        return true
    }

    fun adjustStackAbove(note: Note){
        //Adjust folder stack
        folderStack.clear()
        var currentDir = note

        while (getParentDirectory(currentDir) != rootDir) {
            if (currentDir.content==null) folderStack.push(currentDir)
            currentDir = getParentDirectory(currentDir)
        }
        //Add last parent directory and root directory to stack
        //This is necessary, since the loop above stops once the parent directory is the root directory
        //todo improve loop
        if (currentDir.content == null) folderStack.push(currentDir)
        folderStack.push(rootDir)
        folderStack.reverse()
    }

    fun getNoteByTitleAndContent(title: String, content: String, directory: Note = rootDir): Note? {
        for (note in directory.noteList) {
            if(note.content!=null){
                //Check note
               if(note.content == content && note.title == title){
                   return note
               }
            }else{
                //Check subDirectory val
                val subResult = getNoteByTitleAndContent(title, content, note)
                if(subResult != null) return subResult
            }
        }
        return null
    }


    fun getSuperordinatePaths(dir: Note, passedRootName: String): ArrayList<String> {
        val paths = arrayListOf<String>()
        if (dir.content == null) {
            val containingDirs = containingDirs(dir)
            containingDirs.add(dir)
            getDirPathsWithRef(passedRootName).filter {
                !containingDirs.contains(it.second)
            }.forEach { paths.add(it.first) }
        } else paths.addAll(getDirPaths(passedRootName))
        return paths
    }

    private fun getDirPaths(passedRootName: String): ArrayList<String> {
        val paths = arrayListOf<String>()
        getDirPathsWithRef(passedRootName).forEach { paths.add(it.first) }
        return paths
    }

    private fun getDirPathsWithRef(passedRootName: String = rootDirName): ArrayList<Pair<String, Note>> {
        val pathsAndDirs = arrayListOf(Pair(passedRootName, rootDir))
        containingDirs(rootDir).forEach {
            if (rootDir.noteList.contains(it))
                pathsAndDirs.add(Pair("$passedRootName   ›   ${it.title}", it))
            else
                pathsAndDirs.add(Pair("...   ›   ${it.title}", it))
        }
        return pathsAndDirs
    }

    /**
     * Creates a string representing the current work directory.
     * The Format is as follows: root   ›   dirName   ›   ...
     * If the path is longer than 26 characters only the cwd name is shown.
     * @return A string representing the current work directory
     */
    fun getCurrentPathName(rootDirName: String): String {
        var path = ""
        folderStack.forEachIndexed { index, noteDir ->
            path += when (index) {
                0 -> rootDirName
                else -> "   ›   " + noteDir.title
            }
        }

        if(path.length > 26) {
            path = "...   ›   " + path.split("   ›   ").last()
        }
        return path
    }

    //todo add doc
    private fun containingDirs(dir: Note): ArrayList<Note> {
        val dirs = arrayListOf<Note>()
        if (dir.content != null) return dirs
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
    fun getNote(index: Int): Note = currentList()[index]

    /**
     * Navigates into the given folder and updates the currentlist.
     * @param dir The directory to open.
     */
    fun openFolder(dir: Note) {
        folderStack.add(dir)
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
        return true
    }

    /**
     * Deletes the currently opened folder, except for the root folder which can't
     * be deleted. Also saves the notes to file.
     */
    fun deleteCurrentFolder(): Note? {
        if (folderStack.size == 1) return null

        val deletedDir = folderStack.pop()
        currentList().remove(deletedDir)
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
     * @see NoteList.addNote
     */
    fun addNote(note: Note) {
        if(SettingsManager.getSetting(SettingId.NOTES_MOVE_UP_CURRENT) as Boolean){
            currentList().add(0, note)
        }else{
            currentList().add(note)
        }
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
        if(SettingsManager.getSetting(SettingId.NOTES_MOVE_UP_CURRENT) as Boolean){
            currentList().add(0, noteDir)
        }else{
            currentList().add(noteDir)
        }
        save()
        return true
    }

    /**
     * The count of notes and folders in the current directory.
     * @return The aforementioned count notes and folders.
     */
    fun getNoteObjCount(): Int{
       return currentList().size
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