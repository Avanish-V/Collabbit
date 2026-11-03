package com.iota.campusX.Feature.Notificattion.data.mapper

import com.iota.campusX.Feature.Notificattion.data.dto.NotificationResponse
import com.iota.campusX.Feature.Notificattion.data.dto.UpvoterResponse
import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.domain.model.Upvoter
import com.iotabuild.campuscircle.Notification.entity.EntityType
import com.iotabuild.campuscircle.Notification.entity.NotificationType
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

fun NotificationResponse.toDomain() =
    Notification(
        id = id,
        senderUid = senderUid ?: "",
        senderName = senderName ?: "User",
        senderProfileUrl = senderImage,
        title = title ?: "",
        body = generateBody(type, note, postTitle, upvoters),
        note = note,
        type = type ?: NotificationType.SYSTEM,
        entityType = entityType ?: EntityType.POST,
        entityId = entityId ?: "",
        deepLink = deepLink,
        isRead = isRead ?: false,
        createdAt = parseDate(createdAt),
        postTitle = postTitle,
        postThumbnail = postThumbnail,
        upvoters = upvoters.map { it.toDomain() }
    )

fun UpvoterResponse.toDomain() = Upvoter(
    uid = uid,
    name = name,
    image = image,
    about = about,
    tagline = tagline
)

private fun generateBody(
    type: NotificationType?,
    note: String?,
    postTitle: String?,
    upvoters: List<UpvoterResponse>
): String {
    val postRef = if (!postTitle.isNullOrBlank()) " on '$postTitle'" else " on your post"
    val count = upvoters.size
    val upvoterText = when {
        count > 1 -> " and ${count - 1} other${if (count > 2) "s" else ""}"
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
