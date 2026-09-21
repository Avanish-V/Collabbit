package com.iota.campusX.Feature.Notificattion.data.local.mapper

import com.iota.campusX.Feature.Notificattion.data.dto.NotificationResponse
import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun NotificationResponse.toEntity() = NotificationEntity(
    id = id,
    senderUid = senderUid ?: "",
    senderName = senderName ?: "User",
    senderProfileUrl = senderImage,
    type = type ?: NotificationType.SYSTEM,
    entityType = entityType ?: EntityType.POST,
    entityId = entityId ?: "",
    title = title,
    body = body ?: generateBody(type, note, postTitle, upvoters.size),
    note = note,
    deepLink = deepLink,
    isRead = isRead ?: false,
    createdAt = parseDate(createdAt),
    postTitle = postTitle,
    postThumbnail = postThumbnail,
    upvoters = upvoters.map { 
        Upvoter(
            uid = it.uid,
            name = it.name,
            image = it.image,
            about = it.about,
            tagline = it.tagline
        )
    }
)

fun Notification.toEntity() = NotificationEntity(
    id = id,
    senderUid = senderUid,
    senderName = senderName,
    senderProfileUrl = senderProfileUrl,
    type = type,
    entityType = entityType,
    entityId = entityId,
    title = title,
    body = body,
    note = note,
    deepLink = deepLink,
    isRead = isRead,
    createdAt = createdAt,
    isActionDone = isActionDone,
    postTitle = postTitle,
    postThumbnail = postThumbnail,
    upvoters = upvoters
)

private fun generateBody(
    type: NotificationType?, 
    note: String? = null, 
    postTitle: String? = null,
    upvoterCount: Int = 0
): String {
    val postRef = if (!postTitle.isNullOrBlank()) " on '$postTitle'" else " on your post"
    val upvoterText = when {
        upvoterCount > 1 -> " and ${upvoterCount - 1} other${if (upvoterCount > 2) "s" else ""}"
        else -> ""
    }
    return when (type) {
        NotificationType.LIKE -> "${upvoterText} upvoted your post$postRef"
        NotificationType.COMMENT -> "commented$postRef"
        NotificationType.REPLY -> "replied to your post$postRef"
        NotificationType.FOLLOW -> "started following you"
        NotificationType.MESSAGE -> "sent you a message"
        NotificationType.MENTION -> "mentioned you in a post"
        NotificationType.COLLABORATION_REQUEST -> "sent you a collab request"
        NotificationType.CONNECT_REQUEST -> note ?: "wants to connect with you"
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
