package com.pocket_plan.j7_003.data.notelist

class NoteDirList {
    val rootDir: NoteDir = NoteDir("/", arrayListOf(Note("test title", "test content", NoteColors.BLUE)))
    var currentList: ArrayList<NoteObj>
    init {
        this.currentList = rootDir.notes
    }

    fun getNode(index: Int): NoteObj = this.currentList[index]

    fun folderOpened(dir: NoteDir) {
        currentList = dir.notes
    }

    fun addNote(note: Note) {
        currentList.add(note)
    }

    fun addNoteDir(noteDir: NoteDir) {
        currentList.add(noteDir)
    }
}