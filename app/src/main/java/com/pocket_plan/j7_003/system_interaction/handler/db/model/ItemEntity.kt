package com.pocket_plan.j7_003.system_interaction.handler.db.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "shopping_items",
    primaryKeys = ["id", "templateId", "listName"],
    foreignKeys = [
        ForeignKey(
            entity = ListEntity::class,
            parentColumns = ["name"],
            childColumns = ["listName"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE,
        )
    ]
)
data class ItemEntity(
    val id: Long,
    val templateId: Long,
    val listName: String,
    val category: CategoryEntity,
    val amount: Double,
    val checked: Boolean,
    val unit: String,
)
