package com.example.j7_003.data.shoppinglist

import com.google.gson.annotations.SerializedName

data class ShoppingItem(
    @SerializedName(value = "n")
    var name: String?,

    @SerializedName(value = "t")
    var tag: Tag,

    @SerializedName(value = "s")
    var suggestedUnit: String?,

    @SerializedName(value = "a")
    var amount: String?,

    @SerializedName(value = "u")
    var unit: String?,

    @SerializedName(value = "c")
    var checked: Boolean)
{
    constructor(tag: Tag, checked: Boolean): this(null, tag, null, null, null, checked)
}
