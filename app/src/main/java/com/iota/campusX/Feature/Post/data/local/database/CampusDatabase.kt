package com.iota.campusX.Feature.Post.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iota.campusX.Feature.Post.data.local.converters.RoomConverters
import com.iota.campusX.Feature.Post.data.local.dao.PostDao
import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.local.entity.RemoteKeys
import com.iota.campusX.Feature.Post.data.local.entity.RemoteKeysDao

@Database(
    entities = [
        PostEntity::class,
        RemoteKeys::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(
    RoomConverters::class
)
abstract class CampusDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    abstract fun remoteKeysDao(): RemoteKeysDao
}