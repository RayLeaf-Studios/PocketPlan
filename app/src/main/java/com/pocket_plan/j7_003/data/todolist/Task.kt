package com.pocket_plan.j7_003.data.todolist

import com.google.gson.annotations.SerializedName

class Task(
    @SerializedName(value = "t")
    var title: String,

    @SerializedName(value = "p")
    var priority: Int,

    @SerializedName(value = "i")
    var isChecked: Boolean
)