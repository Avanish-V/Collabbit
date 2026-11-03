package com.iota.campusX.Feature.Society.domain.model

import com.google.firebase.firestore.PropertyName

data class Community(
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",
    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",
    @get:PropertyName("logoUrl")
    @set:PropertyName("logoUrl")
    var logoUrl: String? = null,
    @get:PropertyName("memberCount")
    @set:PropertyName("memberCount")
    var memberCount: Int = 0,
    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "",
    @get:PropertyName("creatorId")
    @set:PropertyName("creatorId")
    var creatorId: String = "",
    @get:PropertyName("createdAt")
    @set:PropertyName("createdAt")
    var createdAt: Long = System.currentTimeMillis(),
    @get:PropertyName("isDeleted")
    @set:PropertyName("isDeleted")
    var isDeleted: Boolean = false,
    @get:PropertyName("pinnedMessageId")
    @set:PropertyName("pinnedMessageId")
    var pinnedMessageId: String? = null,
    @get:PropertyName("pinnedMessageText")
    @set:PropertyName("pinnedMessageText")
    var pinnedMessageText: String? = null,
    var unreadCount: Int = 0,
    var lastMessageText: String? = null,
    var lastMessageTime: Long? = null
)
