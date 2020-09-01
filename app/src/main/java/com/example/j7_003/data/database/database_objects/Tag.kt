package com.example.j7_003.data.database.database_objects

import java.util.*

data class Tag(val name: String, val color: String) {
    override fun equals(other: Any?): Boolean {
        if (other !is Tag) {
            return false
        }

        return this.name.toLowerCase(Locale.ROOT) == other.name.toLowerCase(Locale.ROOT)
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}