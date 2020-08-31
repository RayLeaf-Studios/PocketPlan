package com.example.j7_003.data.database.database_objects

import java.util.*

data class ShoppingItem(var name: String?, var tag: Tag,
                        var suggestedUnit: String?, var amount: String?,
                        var unit: String?, var checked: Boolean)
{
    constructor(tag: Tag, checked: Boolean): this(null, tag, null, null, null, checked)
}
