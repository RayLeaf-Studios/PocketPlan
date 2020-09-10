package com.example.j7_003.data.shoppinglist

data class ShoppingItem(var name: String?, var tag: Tag,
                        var s: String?, var amount: String?,
                        var unit: String?, var checked: Boolean)
{
    constructor(tag: Tag, checked: Boolean): this(null, tag, null, null, null, checked)
}
