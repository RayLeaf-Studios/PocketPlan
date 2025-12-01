package com.pocket_plan.j7_003.system_interaction.handler.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shopping_lists")
data class ListEntity(
    @PrimaryKey(autoGenerate = false) val name: String,
    val position: Int,
)
