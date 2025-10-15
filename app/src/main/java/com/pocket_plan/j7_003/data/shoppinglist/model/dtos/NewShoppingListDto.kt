package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

/**
 * Data Transfer Object for creating a new shopping list.
 *
 * @property name The name of the shopping list.
 * @property content The list of shopping categories in this shopping list.
 */
@Serializable
data class NewShoppingListDto(
    val name: String,
    val content: List<NewShoppingCategoryDto>,
)
