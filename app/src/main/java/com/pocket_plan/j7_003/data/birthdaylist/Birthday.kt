package com.pocket_plan.j7_003.data.birthdaylist

import com.google.gson.annotations.SerializedName
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

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

    fun daysUntil(): Int {
        return ChronoUnit.DAYS.between(LocalDate.now(), asLocalDate()).toInt()
    }

    private fun asLocalDate(): LocalDate {
        return if (LocalDate.of(LocalDate.now().year, month, day).isBefore(LocalDate.now())) {
            LocalDate.of(LocalDate.now().year.plus(1), month, day)
        } else {
            LocalDate.of(LocalDate.now().year, month, day)
        }
    }
}