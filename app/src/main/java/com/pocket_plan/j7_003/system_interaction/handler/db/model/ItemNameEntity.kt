package com.pocket_plan.j7_003.system_interaction.handler.db.model

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "shopping_item_names",
    primaryKeys = ["templateId", "value", "lang"],
    foreignKeys = [ForeignKey(
        entity = ItemTemplateEntity::class,
        parentColumns = ["id"],
        childColumns = ["templateId"],
        onDelete = ForeignKey.CASCADE,
        onUpdate = ForeignKey.CASCADE,
    )]
)
data class ItemNameEntity(
    val templateId: Long,
    val value: String,
    val lang: LanguageEntity,
)