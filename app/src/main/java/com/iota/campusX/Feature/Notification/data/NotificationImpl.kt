package com.iota.campusX.Feature.Notification.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Chats.data.ChatMessage
import com.iota.campusX.Feature.Chats.data.ID
import com.iota.campusX.Feature.Notification.domain.Content
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.CreateReplyDTO
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
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

    override fun fetchNotification(): Flow<ResultState<List<NotificationDTO>>> {

        return flow {

            emit(ResultState.Loading)

            try {

                val notificationSnapshot =
                    firestore.collection("Users").document(auth.currentUser!!.uid)
                        .collection("Notifications")
                        .get()
                        .await()

                val notificationList = coroutineScope {

                    notificationSnapshot.map { data ->

                        async {

                            val notificationData = data.toObject(CreateNotificationDTO::class.java)
                            val path = if (notificationData.feedMode == FeedMode.GLOBAL) "GlobalPosts" else "CampusPosts"


                            val actionByDeferred = async {
                                firestore.collection("Users")
                                    .document(notificationData.actionBy)
                                    .get()
                                    .await()
                                    .toObject(UserDetail::class.java)
                            }

                            val getReplyDeferred = async {
                                if (!notificationData.replyId.isNullOrEmpty()) {
                                    firestore.collection(path)
                                        .document(notificationData.postId.toString())
                                        .collection("Replies")
                                        .document(notificationData.replyId.toString())
                                        .get()
                                        .await()
                                        .toObject(CreateReplyDTO::class.java)
                                } else {
                                    null
                                }
                            }


                            val getLikedPostDeferred = async {
                                if (notificationData.postId != null) {
                                    val snapshot = firestore.collection(path)
                                        .document(notificationData.postId)
                                        .get()
                                        .await()
                                    snapshot.toObject(GetPostDTO::class.java)
                                } else {
                                    null
                                }
                            }

                            val getReply = getReplyDeferred.await()
                            val actionedBy = actionByDeferred.await()
                            val post = getLikedPostDeferred.await()



                            val content: Content = when (notificationData.type) {
                                NotificationType.LIKE_POST  -> Content(
                                    text = post?.postContent?.postData?.postText ?: "",
                                    image = post?.postContent?.postData?.postImage ?: ""
                                )

                                NotificationType.LIKE_REPLY  -> Content(
                                    text = getReply?.content ?: ""
                                )

                                NotificationType.COMMENTED  -> Content(
                                    text = getReply?.content ?: ""
                                )

                                NotificationType.REQUEST -> Content(text = "")
                            }

                            val userType: Pair<String, String> =

                                if (notificationData.visibilityMode == PostVisibilityMode.USER) Pair(
                                    actionedBy?.userName ?: "",
                                    actionedBy?.userImage ?: ""
                                )
                                else Pair(
                                    "Anonymous",
                                    "https://res.cloudinary.com/dni4h8jjy/image/upload/v1746629954/wyuwxwa8qwx0hu0i6flk.png"
                                )


                            val createdAt = notificationData.createdAt


                            NotificationDTO(
                                notificationId = notificationData.notificationId,
                                creatorId = notificationData.creatorId,
                                createdAt = createdAt,
                                postId = notificationData.postId.toString(),
                                actionBy = UserDetail(
                                    userName = userType.first,
                                    id = actionedBy?.id ?: "",
                                    userImage = userType.second
                                ),
                                content = content,
                                type = notificationData.type,
                                visibilityMode = notificationData.visibilityMode,
                                feedMode = notificationData.feedMode
                            )

                        }

                    }.map { it.await() }

                }

                emit(ResultState.Success(notificationList))


            } catch (e: Exception) {
                emit(ResultState.Error(e.message.toString()))
                Log.d("NotificationImpl", "fetchNotification: ${e.message}")
            }

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