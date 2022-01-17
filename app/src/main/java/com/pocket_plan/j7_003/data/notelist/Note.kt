package com.pocket_plan.j7_003.data.notelist

import com.google.gson.annotations.SerializedName

data class Note(
    @SerializedName(value = "t")
    var title: String,

    @SerializedName(value = "ct")
    var content: String,

    @SerializedName(value = "cl")
    override var color: NoteColors): NoteObj()