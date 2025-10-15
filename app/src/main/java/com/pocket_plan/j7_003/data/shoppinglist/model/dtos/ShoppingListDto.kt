package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingListDto(
    val id: String,
    val name: String,
    val content: List<ShoppingCategoryDto>
)
