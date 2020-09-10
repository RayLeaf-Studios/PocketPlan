package com.example.j7_003.data.shoppinglist

import java.util.*

data class Tag(val n: String, val c: String) {
    override fun equals(other: Any?): Boolean {
        if (other !is Tag) {
            return false
        }

        return this.n.toLowerCase(Locale.ROOT) == other.n.toLowerCase(Locale.ROOT)
    }

    override fun hashCode(): Int {
        return n.hashCode()
    }
}