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
    version = 6,
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
                    .addMigrations(MIGRATION_3_4, MIGRATION_4_5)
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
                rule_code TEXT NOT NULL,
                points INTEGER NOT NULL,
                created_at TEXT NOT NULL,
                source_id TEXT,
                date_key TEXT NOT NULL
            )
        """)
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_aura_transactions_user_id_rule_code_date_key ON aura_transactions (user_id, rule_code, date_key)")
    }
}
