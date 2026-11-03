package com.iota.campusX.Feature.Notificattion.data.local.mapper

import com.iota.campusX.Feature.Notificattion.data.local.entity.NotificationEntity
import com.iota.campusX.Feature.Notificattion.domain.model.Notification

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        senderUid = senderUid,
        senderName = senderName,
        senderProfileUrl = senderProfileUrl,
        title = title.orEmpty(),
        body = body.orEmpty(),
        note = note,
        type = type,
        entityType = entityType,
        entityId = entityId,
        deepLink = deepLink,
        isRead = isRead,
        createdAt = createdAt,
        isActionDone = isActionDone,
        postTitle = postTitle,
        postThumbnail = postThumbnail,
        upvoters = upvoters
    )
}
