package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for creating a new shopping category.
 *
 * @property name The name of the shopping category.
 * @property items The list of shopping items in this category.
 */
@Serializable
data class NewShoppingCategoryDto(
    val name: String,
    val items: List<NewShoppingItemDto>,
)
