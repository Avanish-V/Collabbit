package com.iota.campusX.Feature.Notificattion.data.local.converters

import androidx.room.TypeConverter
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class NotificationConverters {
    @TypeConverter
    fun fromEntityType(value: EntityType): String = value.name

    @TypeConverter
    fun toEntityType(value: String): EntityType = EntityType.valueOf(value)

    @TypeConverter
    fun fromNotificationType(value: NotificationType): String = value.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = NotificationType.valueOf(value)

    @TypeConverter
    fun fromUpvoterList(value: List<Upvoter>): String {
        return Json.encodeToString(value)
    }

    @TypeConverter
    fun toUpvoterList(value: String): List<Upvoter> {
        return try {
            Json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
