package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for creating a new shopping item.
 *
 * @property name The name of the shopping item.
 * @property suggestedUnit The suggested unit for the shopping item (optional).
 * @property amount The amount of the shopping item (optional).
 * @property unit The unit of the shopping item (optional).
 * @property checked Whether the shopping item is checked or not.
 */
@Serializable
data class NewShoppingItemDto(
    val name: String,
    val suggestedUnit: String?,
    val amount: Float?,
    val unit: String?,
    val checked: Boolean,
)
