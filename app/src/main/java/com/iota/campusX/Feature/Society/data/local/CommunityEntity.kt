package com.iota.campusX.Feature.Society.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.iota.campusX.Feature.Society.domain.model.Community

@Entity(tableName = "communities")
data class CommunityEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val logoUrl: String?,
    val memberCount: Int,
    val category: String,
    val creatorId: String,
    val createdAt: Long,
    val isSynced: Boolean = true,
    val isJoined: Boolean = false,
    val isDeleted: Boolean = false,
    val pinnedMessageId: String? = null,
    val pinnedMessageText: String? = null,
    val unreadCount: Int = 0,
    val lastMessageText: String? = null,
    val lastMessageTime: Long? = null
)

fun CommunityEntity.toDomain(): Community {
    return Community(
        id = id,
        name = name,
        description = description,
        logoUrl = logoUrl,
        memberCount = memberCount,
        category = category,
        creatorId = creatorId,
        createdAt = createdAt,
        isDeleted = isDeleted,
        pinnedMessageId = pinnedMessageId,
        pinnedMessageText = pinnedMessageText,
        unreadCount = unreadCount,
        lastMessageText = lastMessageText,
        lastMessageTime = lastMessageTime
    )
}

fun Community.toLocal(isSynced: Boolean = true, isJoined: Boolean = false): CommunityEntity {
    return CommunityEntity(
        id = id,
        name = name,
        description = description,
        logoUrl = logoUrl,
        memberCount = memberCount,
        category = category,
        creatorId = creatorId,
        createdAt = createdAt,
        isSynced = isSynced,
        isJoined = isJoined,
        isDeleted = isDeleted,
        pinnedMessageId = pinnedMessageId,
        pinnedMessageText = pinnedMessageText,
        unreadCount = unreadCount,
        lastMessageText = lastMessageText,
        lastMessageTime = lastMessageTime
    )
}
