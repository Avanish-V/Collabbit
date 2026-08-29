package com.iota.campusX.Feature.Notificattion.data.local.converters

import androidx.room.TypeConverter
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType

class NotificationConverters {
    @TypeConverter
    fun fromEntityType(value: EntityType): String = value.name

    @TypeConverter
    fun toEntityType(value: String): EntityType = EntityType.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)
}
