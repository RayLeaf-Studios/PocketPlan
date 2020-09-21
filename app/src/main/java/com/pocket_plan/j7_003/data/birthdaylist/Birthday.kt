package com.pocket_plan.j7_003.data.birthdaylist

import com.google.gson.annotations.SerializedName

data class Birthday constructor(
    @SerializedName(value = "n")
    var name: String,

    @SerializedName(value = "d")
    var day: Int,

    @SerializedName(value = "m")
    var month: Int,

    @SerializedName(value = "y")
    var year: Int,

    @SerializedName(value = "dr")
    var daysToRemind: Int,

    @SerializedName(value = "e")
    var expanded: Boolean,

    @SerializedName(value = "nt")
    var notify: Boolean) {

    fun hasReminder(): Boolean = daysToRemind != 0
}