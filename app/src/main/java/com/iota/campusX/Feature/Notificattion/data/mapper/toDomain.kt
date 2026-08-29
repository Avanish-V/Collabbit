package com.iota.campusX.Feature.Notificattion.data.mapper

import com.iota.campusX.Feature.Notificattion.data.dto.NotificationResponse
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun NotificationResponse.toDomain() =
    Notification(
        id = id ?: 0L,
        senderUid = senderUid ?: "",
        senderName = senderName ?: "User",
        senderProfileUrl = senderImage,
        title = "", // No title provided in new DTO
        body = generateBody(type),
        type = type ?: NotificationType.SYSTEM,
        entityType = entityType ?: EntityType.POST,
        entityId = entityId ?: "",
        deepLink = deepLink,
        isRead = isRead ?: false,
        createdAt = parseDate(createdAt)
    )

private fun generateBody(type: NotificationType?): String {
    return when (type) {
        NotificationType.LIKE -> "upvoted your post"
        NotificationType.COMMENT -> "commented on your post"
        NotificationType.REPLY -> "replied to your post"
        NotificationType.FOLLOW -> "started following you"
        NotificationType.MESSAGE -> "sent you a message"
        NotificationType.MENTION -> "mentioned you in a post"
        else -> "sent you a notification"
    }
}

private fun parseDate(dateStr: String?): Long {
    if (dateStr == null) return System.currentTimeMillis()
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        format.parse(dateStr)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}
