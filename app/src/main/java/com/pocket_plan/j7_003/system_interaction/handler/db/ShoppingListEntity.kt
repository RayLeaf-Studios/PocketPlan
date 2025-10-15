package com.pocket_plan.j7_003.system_interaction.handler.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val position: Int,
    val items: List<ShoppingItemEntity>,
)
