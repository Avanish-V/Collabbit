package com.iota.campusX.Feature.Society.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CommunityEntity::class, SocietyMessageEntity::class, UserCacheEntity::class], version = 12, exportSchema = false)
@TypeConverters(Converters::class)
abstract class SocietyDatabase : RoomDatabase() {
    abstract fun communityDao(): CommunityDao
    abstract fun userCacheDao(): UserCacheDao
}
