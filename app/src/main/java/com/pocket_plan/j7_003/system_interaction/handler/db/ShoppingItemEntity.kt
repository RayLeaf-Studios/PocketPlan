package com.pocket_plan.j7_003.system_interaction.handler.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_items")
data class ShoppingItemEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val amount: String,
    val unit: String,
    val category: String,
    val checked: Boolean,
    val position: Int,
)
