package com.pocket_plan.j7_003.data.shoppinglist.mapper

import com.pocket_plan.j7_003.data.shoppinglist.ShoppingItem
import com.pocket_plan.j7_003.data.shoppinglist.model.dtos.ShoppingItemDto
import kotlin.math.ceil

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

    /**
     * Converts a [ShoppingItemDto] to a [ShoppingItem].
     * @receiver ShoppingItemDto The item to convert
     * @param tag String The tag to assign to the item
     * @return ShoppingItem The converted item
     */
    fun ShoppingItemDto.toCore(tag: String) = ShoppingItem(
        name = this.name,
        suggestedUnit = this.suggestedUnit,
        amount = this.amount?.let {
            if (ceil(it) == this.amount) this.amount.toInt().toString()
            else this.amount.toString()
        },
        unit = this.unit,
        checked = this.checked,
        tag = tag,
    )
}