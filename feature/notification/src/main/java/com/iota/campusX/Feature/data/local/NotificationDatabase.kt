package com.iota.campusX.Feature.Notificattion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.iota.campusX.Feature.Notificattion.data.local.converters.NotificationConverters
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationDao
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationSyncDao
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationSyncEntity

@Database(
    entities = [
        NotificationEntity::class,
        NotificationSyncEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(NotificationConverters::class)
abstract class NotificationDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
    abstract fun notificationSyncDao(): NotificationSyncDao
}
