package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ActivityDao
import com.example.data.dao.ChallengeDao
import com.example.data.dao.ProjectDao
import com.example.data.dao.UserStatsDao
import com.example.data.model.ChallengeEntity
import com.example.data.model.DailyActivityEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.UserStatsEntity

@Database(
    entities = [
        ProjectEntity::class,
        ChallengeEntity::class,
        DailyActivityEntity::class,
        UserStatsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao
    abstract fun challengeDao(): ChallengeDao
    abstract fun activityDao(): ActivityDao
    abstract fun userStatsDao(): UserStatsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "c_compiler_database"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
