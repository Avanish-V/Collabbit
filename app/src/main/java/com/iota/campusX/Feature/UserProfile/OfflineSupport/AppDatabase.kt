package com.iota.campusX.Feature.UserProfile.OfflineSupport

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [UserProfileEntity::class], // add all your entities here
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class) // optional, if you use custom converters
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
}
