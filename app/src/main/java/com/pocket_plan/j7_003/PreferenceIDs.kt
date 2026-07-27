package com.pocket_plan.j7_003

enum class PreferenceIDs(val id: String) {
    //legacy keys, written on every editor open by older versions; no longer written,
    //kept only so residue from old installs can be purged
    EDIT_NOTE_CONTENT("editNoteContent"),
    EDIT_NOTE_TITLE("editNoteTitle"),
    EDIT_NOTE_COLOR("editNoteColor"),
    EDIT_NOTE_ID("editNoteId"),
    EDIT_NOTE_FOLDER_ID("editNoteFolderId"),

    //self-contained snapshot of the live editor session, written in MainActivity.onStop,
    //cleared in MainActivity.onStart, consumed by MainActivity.manageNoteRestore
    EDIT_NOTE_CONTENT_ON_DESTROY("editNoteContentOnDestroy"),
    EDIT_NOTE_TITLE_ON_DESTROY("editNoteTitleOnDestroy"),
    EDIT_NOTE_COLOR_ON_DESTROY("editNoteColorOnDestroy"),
    EDIT_NOTE_ID_ON_DESTROY("editNoteIdOnDestroy"),
    EDIT_NOTE_FOLDER_ID_ON_DESTROY("editNoteFolderIdOnDestroy")
}
