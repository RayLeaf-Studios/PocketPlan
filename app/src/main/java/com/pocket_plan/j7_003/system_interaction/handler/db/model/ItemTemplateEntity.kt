package com.pocket_plan.j7_003.system_interaction.handler.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_item_templates")
data class ItemTemplateEntity(
    @PrimaryKey val id: Long,
    val category: CategoryEntity,
    val unit: String,
)
