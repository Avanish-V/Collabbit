package com.iota.campusX.Feature.Post.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iota.campusX.Feature.Post.data.local.converters.RoomConverters
import com.iota.campusX.Feature.Post.data.local.dao.PostDao
import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.local.entity.RemoteKeys
import com.iota.campusX.Feature.Post.data.local.entity.RemoteKeysDao
import com.iota.campusX.Feature.Reply.data.local.dao.ReplyDao
import com.iota.campusX.Feature.Reply.data.local.entity.ReplyEntity

@Database(
    entities = [
        PostEntity::class,
        RemoteKeys::class,
        ReplyEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(
    RoomConverters::class
)
abstract class CampusDatabase : RoomDatabase() {

    abstract fun postDao(): PostDao

    abstract fun remoteKeysDao(): RemoteKeysDao

    abstract fun replyDao(): ReplyDao
}
