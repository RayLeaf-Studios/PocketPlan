package com.pocket_plan.j7_003.data.notelist

import java.util.*
import kotlin.collections.ArrayList

class NoteDirList {
    val rootDir: NoteDir = NoteDir("/", arrayListOf(Note("test title", "test content", NoteColors.BLUE)))
    var currentList: ArrayList<NoteObj>
    var folderStack: Stack<NoteDir> = Stack()

    init {
        this.currentList = rootDir.notes
        folderStack.push(rootDir)
    }

    fun getNode(index: Int): NoteObj = this.currentList[index]

    fun folderOpened(dir: NoteDir) {
        currentList = dir.notes
        folderStack.add(dir)
    }

    fun goBack(): Boolean{
        if(folderStack.size <= 1){
            return false
        }
        folderStack.pop()
        currentList = folderStack.peek().notes
        return true
    }

    fun addNote(note: Note) {
        currentList.add(note)
    }

    fun addNoteDir(noteDir: NoteDir) {
        currentList.add(noteDir)
    }

    fun getNoteObjCount(): Int{
       return currentList.size
    }


}