package com.pocket_plan.j7_003.data.shoppinglist.mapper

import com.pocket_plan.j7_003.data.shoppinglist.ShoppingItem
import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingItemDto

/**
 * Maps a [ShoppingItem] to a [ShoppingItemDto].
 */
object ItemMapper {

    /**
     * Converts a [ShoppingItem] to a [ShoppingItemDto].
     * @receiver ShoppingItem The item to convert
     * @return ShoppingItemDto The converted item
     */
    fun ShoppingItem.toDto() = ShoppingItemDto(
        name = this.name,
        suggestedUnit = this.suggestedUnit,
        amount = this.amount?.toFloatOrNull(),
        unit = this.unit,
        checked = this.checked,
    )
}