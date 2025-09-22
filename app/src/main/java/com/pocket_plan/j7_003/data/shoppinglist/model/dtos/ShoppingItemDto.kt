package com.pocket_plan.j7_003.data.shoppinglist.model.dtos

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItemDto(
    val name: String?,
    val suggestedUnit: String?,
    val amount: Float?,
    val unit: String?,
    val checked: Boolean,
)
