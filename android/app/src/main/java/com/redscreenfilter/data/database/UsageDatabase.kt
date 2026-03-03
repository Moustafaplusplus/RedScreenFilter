package com.redscreenfilter.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * UsageDatabase
 * Room database for storing usage events and analytics data
 */
@Database(
    entities = [UsageEvent::class],
    version = 1,
    exportSchema = false
)
abstract class UsageDatabase : RoomDatabase() {
    
    abstract fun usageEventDao(): UsageEventDao
    
    companion object {
        private const val DATABASE_NAME = "usage_database.db"
        
        @Volatile
        private var instance: UsageDatabase? = null
        
        fun getInstance(context: Context): UsageDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    UsageDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
