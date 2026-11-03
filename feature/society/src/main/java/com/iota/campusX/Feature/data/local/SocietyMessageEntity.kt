package com.iota.campusX.Feature.Society.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iota.campusX.Feature.Society.domain.model.SocietyMessage
import com.iota.campusX.Feature.Society.domain.model.MessageType

@Entity(tableName = "society_messages")
data class SocietyMessageEntity(
    @PrimaryKey val id: String,
    val societyId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatarUrl: String?,
    val text: String,
    val type: String, // Store enum name
    val mediaUrl: String?,
    val thumbnailUrl: String?,
    val width: Int,
    val height: Int,
    val aspectRatio: Float,
    val fileName: String?,
    val timestamp: Long,
    val replyToId: String?,
    val replyToText: String?,
    val replyToName: String?,
    val reactions: Map<String, List<String>>,
    val isRead: Boolean = false,
    val isSending: Boolean = false,
    val isFailed: Boolean = false,
    val uploadProgress: Float = 0f
)

fun SocietyMessageEntity.toDomain(): SocietyMessage {
    return SocietyMessage(
        id = id,
        societyId = societyId,
        senderId = senderId,
        senderName = senderName,
        senderAvatarUrl = senderAvatarUrl,
        text = text,
        type = try { MessageType.valueOf(type) } catch (e: Exception) { MessageType.TEXT },
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
        fileName = fileName,
        timestamp = timestamp,
        replyToId = replyToId,
        replyToText = replyToText,
        replyToName = replyToName,
        reactions = reactions,
        isSending = isSending,
        isFailed = isFailed,
        uploadProgress = uploadProgress
    )
}

fun SocietyMessage.toLocal(isRead: Boolean = false): SocietyMessageEntity {
    return SocietyMessageEntity(
        id = id,
        societyId = societyId,
        senderId = senderId,
        senderName = senderName,
        senderAvatarUrl = senderAvatarUrl,
        text = text,
        type = type.name,
        mediaUrl = mediaUrl,
        thumbnailUrl = thumbnailUrl,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
        fileName = fileName,
        timestamp = timestamp,
        replyToId = replyToId,
        replyToText = replyToText,
        replyToName = replyToName,
        reactions = reactions,
        isRead = isRead,
        isSending = isSending,
        isFailed = isFailed,
        uploadProgress = uploadProgress
    )
}
