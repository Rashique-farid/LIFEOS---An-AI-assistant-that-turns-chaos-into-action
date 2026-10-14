package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TaskDao
import com.example.data.model.TaskEntity

@Database(entities = [TaskEntity::class], version = 1, exportSchema = false)
abstract class LifeOsDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: LifeOsDatabase? = null

        fun getDatabase(context: Context): LifeOsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LifeOsDatabase::class.java,
                    "lifeos_database"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
