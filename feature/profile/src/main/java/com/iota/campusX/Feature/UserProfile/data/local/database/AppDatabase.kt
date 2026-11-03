package com.iota.campusX.Feature.UserProfile.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.iota.campusX.Feature.UserProfile.data.local.dao.AuraTransactionDao
import com.iota.campusX.Feature.UserProfile.data.local.dao.UserProfileDao
import com.iota.campusX.Feature.UserProfile.data.local.entities.AuraTransactionEntity
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        AuraTransactionEntity::class
    ],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class) // optional, if you use custom converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun auraTransactionDao(): AuraTransactionDao


    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

}

// Migration 1 -> 2: add new column
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_profile ADD COLUMN tagline TEXT NOT NULL DEFAULT ''")
    }
}

// Migration 3 -> 4: add aura points + level columns
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_profile ADD COLUMN aura_points INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE user_profile ADD COLUMN aura_level  TEXT    NOT NULL DEFAULT 'NEWCOMER'")
    }
}

// Migration 4 -> 5: create aura_transactions table
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("""
            CREATE TABLE IF NOT EXISTS aura_transactions (
                id TEXT NOT NULL PRIMARY KEY,
                user_id TEXT NOT NULL,
                type TEXT NOT NULL,
                amount INTEGER NOT NULL,
                created_at TEXT NOT NULL,
                reference_id TEXT,
                date_key TEXT NOT NULL
            )
        """)
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_aura_transactions_user_id_type_date_key ON aura_transactions (user_id, type, date_key)")
    }
}

// Migration 6 -> 7: add openTo column
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_profile ADD COLUMN openTo TEXT")
    }
}

// Migration 7 -> 8: Rename openTo to matchPreferences and remove summary
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE user_profile RENAME COLUMN openTo TO matchPreferences")
    }
}

// Migration 8 -> 9: Aligns schema, adds tagline, and removes summary
val MIGRATION_8_9 = object : Migration(8, 9) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 1. Create new table without 'summary' and WITH 'tagline'
        database.execSQL("""
            CREATE TABLE user_profile_new (
                uid TEXT NOT NULL PRIMARY KEY,
                baseProfile TEXT NOT NULL,
                contact TEXT NOT NULL,
                education TEXT,
                skills TEXT,
                matchPreferences TEXT,
                isCurrentUser INTEGER NOT NULL,
                tagline TEXT NOT NULL DEFAULT '',
                aura_points INTEGER NOT NULL DEFAULT 0,
                aura_level TEXT NOT NULL DEFAULT 'NEWCOMER'
            )
        """)
        
        // 2. Copy data. We try to get 'tagline' if it existed, otherwise use default.
        // We also check if 'summary' existed to drop it implicitly by not selecting it.
        database.execSQL("""
            INSERT INTO user_profile_new (
                uid, baseProfile, contact, education, skills, 
                matchPreferences, isCurrentUser, aura_points, aura_level
            )
            SELECT 
                uid, baseProfile, contact, education, skills, 
                matchPreferences, isCurrentUser, aura_points, aura_level
            FROM user_profile
        """)
        
        // 3. Drop old table and rename new one
        database.execSQL("DROP TABLE user_profile")
        database.execSQL("ALTER TABLE user_profile_new RENAME TO user_profile")
    }
}

