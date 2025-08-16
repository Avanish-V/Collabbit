package com.iota.campusX.Feature.Notification.domain

import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import kotlinx.serialization.Serializable

data class NotificationDTO(
    var notificationId: String = "",
    var creatorId: String? = null,
    var createdAt: Timestamp? = null,
    var userDetail: UserDetail = UserDetail(), // Ensure User also has a no-arg constructor
    var type: NotificationType = NotificationType.SYSTEM,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    var payload: Map<String, Any>? = null,  // Raw payload data
    val content: Content = Content(),
)

data class Content(
    val text: String? = null,
    val image: String? = null,
)

data class CreateNotificationDTO(
    var notificationId: String = "",
    var createdAt: Any? = null,
    val isRead: Boolean = false,
    var type: NotificationType = NotificationType.SYSTEM,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    var payload: Map<String, Any>? = null,  // Raw payload data
){
    inline fun <reified T> getTypedPayload(): T? {
        return payload?.toTypedObject<T>()
    }
}

inline fun <reified T> Any.toTypedObject(): T? {
    return try {
        val json = Gson().toJson(this)
        Gson().fromJson(json, T::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
enum class NotificationType {
    LIKE,
    COMMENT,
    CONNECTION_REQUEST,
    SYSTEM
}

enum class ContentType{
    LIKE_POST,
    LIKE_REPLY,
    REPLY_POST,
    CONNECTION_REQUEST
}

data class LikePayload(
    val postId: String = "",
    val actionBy: String = "",
    val contentType: ContentType = ContentType.LIKE_POST
)

data class CommentPayload(
    val postId: String = "",
    val commentId: String = "",
    var actionBy: String = "",
    var visibilityMode: PostVisibilityMode = PostVisibilityMode.USER,
    val contentType: ContentType = ContentType.REPLY_POST
)

data class ConnectionRequestPayload(
    val actionBy: String = "",
    val contentType: ContentType = ContentType.CONNECTION_REQUEST
)


