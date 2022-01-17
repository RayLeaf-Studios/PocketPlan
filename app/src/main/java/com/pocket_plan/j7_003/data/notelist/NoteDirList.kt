package com.pocket_plan.j7_003.data.notelist

import java.util.*
import kotlin.collections.ArrayList

class NoteDirList {
    val rootDir: NoteDir = NoteDir("root", ArrayList(), NoteColors.BLUE)
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

    //todo
    fun deleteCurrentFolder(){

    }

    //todo
    fun renameCurrentFolder(newName: String): Boolean{
       return true
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

    fun addNoteDir(noteDir: NoteDir):Boolean {
        if(noteDir.name == "root"){
            return false
        }
        currentList.add(noteDir)
        return true
    }

    fun getNoteObjCount(): Int{
       return currentList.size
    }

    fun getCurrentPathName(): String{
        var path = ""
        //todo, ide marks this as error, but code compiles?! (and works!)
        folderStack.stream().skip(1).forEach {
            path += it.name + " / "
        }
        if(path.length > 26){
            path = ".."+path.takeLast(26)
        }
        return path
    }



}