package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.AchievementDao
import com.example.data.local.dao.GameSessionDao
import com.example.data.local.dao.ProfileDao
import com.example.data.local.model.AchievementEntity
import com.example.data.local.model.GameSessionEntity
import com.example.data.local.model.ProfileEntity

@Database(
    entities = [
        ProfileEntity::class,
        GameSessionEntity::class,
        AchievementEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun gameSessionDao(): GameSessionDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "brain_booster_kids.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
