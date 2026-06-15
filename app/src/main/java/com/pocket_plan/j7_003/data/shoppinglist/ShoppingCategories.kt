package com.pocket_plan.j7_003.data.shoppinglist

import android.content.res.Resources
import com.pocket_plan.j7_003.R

object ShoppingCategories {
    const val DEFAULT_TAG = "So"

    private val validTags = setOf(
        "So", "Ob", "Gt", "Nu", "Bw", "Km", "Kf", "Ve", "Tk",
        "Ko", "Fr", "Gw", "Ha", "Sn", "Bz", "Dr", "Al"
    )

    fun normalizeTag(tag: String?): String {
        return if (tag != null && validTags.contains(tag)) tag else DEFAULT_TAG
    }

    fun indexForTag(resources: Resources, tag: String?): Int {
        val codes = resources.getStringArray(R.array.categoryCodes)
        val index = codes.indexOf(normalizeTag(tag))
        return when (index in codes.indices) {
            true -> index
            false -> 0
        }
    }

    fun nameForTag(resources: Resources, tag: String): String {
        val names = resources.getStringArray(R.array.categoryNames)
        val index = indexForTag(resources, tag)
        return when (index in names.indices) {
            true -> names[index]
            false -> names[0]
        }
    }

    fun codeForName(resources: Resources, name: String?): String {
        val names = resources.getStringArray(R.array.categoryNames)
        val codes = resources.getStringArray(R.array.categoryCodes)
        val index = names.indexOf(name)
        return when (index in codes.indices) {
            true -> codes[index]
            false -> DEFAULT_TAG
        }
    }
}
