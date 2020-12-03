package com.pocket_plan.j7_003.data.shoppinglist

import com.google.gson.annotations.SerializedName
import java.util.*

data class Tag(
    @SerializedName(value = "n")
    val name: String,

    @SerializedName(value = "c")
    val color: String
) {
    override fun equals(other: Any?): Boolean {
        if (other !is Tag) {
            return false
        }

        return this.name.equals(other.name, ignoreCase = true)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}