package com.iota.campusX.Feature.Notificattion.presentation.mapper

import com.iota.campusX.Feature.Notificattion.domain.model.Notification
import com.iota.campusX.Feature.Notificattion.presentation.model.NotificationUi

fun Notification.toUi() =
    NotificationUi(
        id = id,
        senderUid = senderUid,
        title = title,
        body = body,
        note = note,
        senderName = senderName,
        senderProfileUrl = senderProfileUrl,
        type = type,
        isRead = isRead,
        createdAt = createdAt,
        entityId = entityId,
        entityType = entityType,
        deepLink = deepLink,
        isActionDone = isActionDone,
        postTitle = postTitle,
        postThumbnail = postThumbnail,
        upvoters = upvoters
    )

fun NotificationUi.toDomain() =
    Notification(
        id = id,
        senderUid = senderUid,
        title = title,
        body = body,
        note = note,
        senderName = senderName,
        senderProfileUrl = senderProfileUrl,
        type = type,
        isRead = isRead,
        createdAt = createdAt,
        entityId = entityId,
        entityType = entityType,
        deepLink = deepLink,
        isActionDone = isActionDone,
        postTitle = postTitle,
        postThumbnail = postThumbnail,
        upvoters = upvoters
    )
