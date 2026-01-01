package com.iota.campusX.Feature.UserProfile.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iota.campusX.Feature.UserProfile.data.local.database.Converters
import com.iota.campusX.Feature.UserProfile.data.local.database.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity

@Database(
    entities = [UserProfileEntity::class], // add all your entities here
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class) // optional, if you use custom converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // register all migrations
                    //.addMigrations(MIGRATION_2_3,)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}

// Migration 1 -> 2: add new column
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Example: Add a new non-null column with a default value
        database.execSQL( "ALTER TABLE user_profile ADD COLUMN tagline TEXT NOT NULL DEFAULT ''")
    }
}