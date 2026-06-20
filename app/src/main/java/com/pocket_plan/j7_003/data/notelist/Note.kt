package com.pocket_plan.j7_003.data.notelist

import com.google.gson.annotations.SerializedName
import java.util.UUID

data class Note(
    @SerializedName(value = "t")
    var title: String,

    @SerializedName(value = "ct")
    var content: String?,

    @SerializedName(value = "cl")
    var color: NoteColors,

    @SerializedName(value = "nl")
    var noteList: NoteList,

    @SerializedName(value = "id")
    var id: String? = UUID.randomUUID().toString()
    ) {
    constructor(name: String, color: NoteColors, noteList: NoteList)
            : this(name, null, color, noteList)
    constructor(name: String, content: String, color: NoteColors)
            : this(name, content, color, NoteList())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Note) return false
        return id != null && id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: System.identityHashCode(this)
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}
