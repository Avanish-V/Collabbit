package com.iota.campusX.Feature.Notification.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.ID
import com.iota.campusX.Feature.Notification.domain.CommentPayload
import com.iota.campusX.Feature.Notification.domain.ConnectionRequestPayload
import com.iota.campusX.Feature.Notification.domain.Content
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.LikePayload
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.CreateReplyDTO
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostData
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Screens.Post.PostOptions
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val database: FirebaseDatabase
) :
    NotificationRepository {

    override fun fetchNotification(): Flow<ResultState<List<NotificationDTO>>> = flow {
        emit(ResultState.Loading)

        val userId = auth.currentUser?.uid
        if (userId == null) {
            emit(ResultState.Error("User not logged in"))
            return@flow
        }

        try {
            val notificationSnapshot = firestore.collection("Users")
                .document(userId)
                .collection("Notifications")
                .get()
                .await()

            val notificationList = coroutineScope {
                notificationSnapshot.map { data ->
                    async {
                        val notificationData = data.toObject(CreateNotificationDTO::class.java)
                        val type = notificationData.type
                        val path = if (notificationData.feedMode == FeedMode.GLOBAL) "GlobalPosts" else "CampusPosts"

                        // 1. Parse the payload dynamically
                        val likePayload = notificationData.getTypedPayload<LikePayload>()
                        val commentPayload = notificationData.getTypedPayload<CommentPayload>()
                        val connPayload = notificationData.getTypedPayload<ConnectionRequestPayload>()

                        // 2. Get actionBy (user who triggered the notification)
                        val actionById = likePayload?.actionBy ?: commentPayload?.actionBy ?: connPayload?.actionBy

                        val actionByDeferred = async {
                            actionById?.let {
                                try {
                                    firestore.collection("Users")
                                        .document(it)
                                        .get()
                                        .await()
                                        .toObject(UserDetail::class.java)
                                } catch (e: Exception) {
                                    Log.e("NotificationImpl", "Failed to fetch actionBy: ${e.message}")
                                    null
                                }
                            }
                        }

                        // 3. Get comment reply (if it's a COMMENT notification)
                        val replyDeferred = async {
                            if (type == NotificationType.COMMENT && commentPayload != null) {
                                try {
                                    firestore.collection("Posts")
                                        .document(commentPayload.postId)
                                        .collection("Replies")
                                        .document(commentPayload.commentId)
                                        .get()
                                        .await()
                                        .toObject(CreateReplyDTO::class.java)
                                } catch (e: Exception) {
                                    Log.e("NotificationImpl", "Failed to fetch reply: ${e.message}")
                                    null
                                }
                            } else null
                        }

                        // 4. Get liked post content (if it's a LIKE notification)
                        val postDeferred = async {
                            if (type == NotificationType.LIKE && likePayload != null) {
                                try {
                                    val snapshot = firestore.collection("Posts")
                                        .document(likePayload.postId)
                                        .get()
                                        .await()

                                    val postContentMap = snapshot.get("postContent") as? Map<*, *> ?: return@async null
                                    val postTypeStr = postContentMap["postType"] as? String ?: "TEXT"
                                    val postType = runCatching { PostOptions.valueOf(postTypeStr) }.getOrDefault(PostOptions.TEXT)

                                    val postDataMap = postContentMap["postData"] as? Map<*, *> ?: emptyMap<Any, Any>()
                                    val postText = postDataMap["postText"] as? String ?: ""
                                    val postImage = postDataMap["postImage"] as? String

                                    PostContent(
                                        postType = postType,
                                        postData = PostData(
                                            postText = postText,
                                            postImage = postImage,
                                            poll = null
                                        )
                                    )
                                } catch (e: Exception) {
                                    Log.e("NotificationImpl", "Failed to fetch liked post: ${e.message}")
                                    null
                                }
                            } else null
                        }

                        val actionedBy = actionByDeferred.await()
                        val reply = replyDeferred.await()
                        val post = postDeferred.await()

                        // Determine username + image (anonymous or not)
                        val userType = if (commentPayload?.visibilityMode == PostVisibilityMode.USER) {
                            Pair(actionedBy?.userName ?: "", actionedBy?.userImage ?: "")
                        } else {
                            Pair("Anonymous", "https://res.cloudinary.com/dni4h8jjy/image/upload/v1746629954/wyuwxwa8qwx0hu0i6flk.png")
                        }

                        val content: Content = when (type) {
                            NotificationType.COMMENT -> {
                                Content(
                                    text = reply?.content,
                                    image = null // or use post/reply image if applicable
                                )
                            }

                            NotificationType.LIKE -> {
                                Content(
                                    text = post?.postData?.postText,
                                    image = post?.postData?.postImage
                                )
                            }

                            NotificationType.CONNECTION_REQUEST -> {
                                Content(
                                    text = "sent you a connection request",
                                    image = null
                                )
                            }

                            else -> {
                                Content(
                                    text = "You have a new notification",
                                    image = null
                                )
                            }
                        }


                        NotificationDTO(
                            notificationId = notificationData.notificationId,
                            createdAt = notificationData.createdAt as? Timestamp,
                            userDetail = UserDetail(
                                userName = userType.first,
                                id = actionedBy?.id.orEmpty(),
                                userImage = userType.second
                            ),
                            type = notificationData.type,
                            feedMode = notificationData.feedMode,
                            content = content,
                            payload = notificationData.payload
                            // Optionally include: post, reply, payload if needed
                        )
                    }
                }.map { it.await() }
            }

            emit(ResultState.Success(notificationList))

        } catch (e: Exception) {
            emit(ResultState.Error("Failed to fetch notifications: ${e.message}"))
            Log.e("NotificationImpl", "Error in fetchNotification: ${e.message}", e)
        }
    }



    override fun markNotificationAsRead() {

        val currentUser = auth.currentUser ?: return

        val userNotificationsRef = firestore.collection("Users")
            .document(currentUser.uid)
            .collection("Notifications")

        userNotificationsRef
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.update(document.reference, "read", true)
                    }
                    batch.commit()
                        .addOnSuccessListener {
                            Log.d("Notification", "All unread notifications marked as read.")
                        }
                        .addOnFailureListener { e ->
                            Log.e("Notification", "Error committing batch update", e)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Notification", "Error fetching notifications", e)
            }
    }

    override fun getNotificationCount(): Flow<ResultState<Int>> {
        return callbackFlow {
            val currentUser = auth.currentUser ?: return@callbackFlow
            val userNotificationsRef = firestore.collection("Users")
                .document(currentUser.uid)
                .collection("Notifications")
            userNotificationsRef.whereEqualTo("read", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message.toString()))
                    } else {
                        val unreadCount = snapshot?.size() ?: 0
                        Log.d("NOTIFICATION_BADGE", "Unread count: $unreadCount")
                        trySend(ResultState.Success(unreadCount))

                    }
                }
            awaitClose()

        }
    }

    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            firestore.collection("Users")
                .document(auth.currentUser!!.uid)
                .collection("Notifications")
                .document(notificationId)
                .delete()
                .await()
            Result.success(Unit)
        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override fun observeTotalUnreadCount(): Flow<Int> = callbackFlow {

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            trySend(0)
            close()
            return@callbackFlow
        }

        val roomListeners = mutableMapOf<String, ValueEventListener>()

        // Use coroutine to fetch user chat rooms once
        firestore.collection("Chats").document(currentUserId).collection("Messages")
            .get()
            .addOnSuccessListener { messageDocs ->
                for (doc in messageDocs) {
                    val idData = doc.toObject(ID::class.java)
                    val roomId = idData.roomId

                    if (roomId.isNotEmpty()) {
                        val messagesRef = database.getReference("ChatRoom").child(roomId).child("messages")

                        val listener = object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                var roomUnread = 0
                                for (child in snapshot.children) {
                                    val message = child.getValue(ChatMessage::class.java)
                                    if (message != null && message.senderId != currentUserId && !message.read) {
                                        roomUnread++
                                    }
                                }

                                // Update total unread count by recomputing across all rooms
                                roomListeners[roomId] = this // save listener for cleanup
                                val allCounts = mutableListOf<Int>()
                                roomListeners.keys.forEach { rid ->
                                    val ref = database.getReference("ChatRoom").child(rid).child("messages")
                                    ref.get().addOnSuccessListener { data ->
                                        var count = 0
                                        for (m in data.children) {
                                            val msg = m.getValue(ChatMessage::class.java)
                                            if (msg != null && msg.senderId != currentUserId && !msg.read) {
                                                count++
                                            }
                                        }
                                        allCounts.add(count)
                                        if (allCounts.size == roomListeners.size) {
                                            trySend(allCounts.sum()) // emit the total
                                        }
                                    }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        }

                        messagesRef.addValueEventListener(listener)
                        roomListeners[roomId] = listener
                    }
                }
            }
            .addOnFailureListener {
                trySend(0)
                close()
            }

        awaitClose {
            roomListeners.forEach { (roomId, listener) ->
                database.getReference("ChatRoom").child(roomId).child("messages")
                    .removeEventListener(listener)
            }
        }
    }




}