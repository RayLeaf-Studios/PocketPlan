package com.pocket_plan.j7_003.system_interaction.handler.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pocket_plan.j7_003.system_interaction.handler.db.dao.ShoppingListDao
import com.pocket_plan.j7_003.system_interaction.handler.db.model.ItemEntity
import com.pocket_plan.j7_003.system_interaction.handler.db.model.ItemNameEntity
import com.pocket_plan.j7_003.system_interaction.handler.db.model.ItemTemplateEntity
import com.pocket_plan.j7_003.system_interaction.handler.db.model.ListEntity
import org.koin.core.annotation.Single


@Database(
    entities = [ListEntity::class, ItemEntity::class, ItemTemplateEntity::class, ItemNameEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shoppingListDao(): ShoppingListDao
}

@Single
fun provideDatabase(context: Context): AppDatabase = Room
    .databaseBuilder(context, AppDatabase::class.java, "pocket_plan_database")
    .fallbackToDestructiveMigration(true)
    .build()