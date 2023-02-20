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

    fun daysUntil(): Int {
        return ChronoUnit.DAYS.between(LocalDate.now(), asAdjustedLocalDate()).toInt()
    }

    /**
     * Returns this birthday as a LocalDate, which is adjusted for the calculation of the
     * remaining days until the birthday.
     *
     * Year adjustment: If the month - day combination in this year is after the current date, the year
     * is unchanged. If it is before the current date (e.g. Today: 28.12.2022, Birthday 01.02
     * +1 is added to the year, since the required delta is between 28.12.2022 and 01.02.2023)
     *
     * Day adjustment: Special case for leap year. If the year is not a leap year, but the date
     * is 29.02, it is set to 28.02. for the "days until" calculation.
     */
    private fun asAdjustedLocalDate(): LocalDate {
        val leapDay = day == 29 && month == 2
        val today = LocalDate.now()
        var dayToUse = day
        if (leapDay && !today.isLeapYear){
            dayToUse -= 1
        }
        var yearToUse = today.year
        if(LocalDate.of(today.year, month, dayToUse).isBefore(today)){
            yearToUse += 1
        }
        if(leapDay && LocalDate.of(yearToUse, 1, 1).isLeapYear){
            dayToUse = 29
        }
        return LocalDate.of(yearToUse, month, dayToUse)
    }
}