package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingCategoryDto(
    val name: String,
    val items: List<ShoppingItemDto>,
)