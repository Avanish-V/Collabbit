package com.iota.campusX.Feature.Notification.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.Content
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.ReplyDTO
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.UserProfile.data.LinkUpRequestDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationImpl(private val firestore: FirebaseFirestore, private val auth: FirebaseAuth) :
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


                            val likedByDeferred = async {
                                firestore.collection("Users")
                                    .document(notificationData.actionBy)
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                            }

                            val getRepliesDeferred = async {
                                if (notificationData.contentId.isNotBlank()) {
                                    firestore.collection("GlobalPosts")
                                        .document(notificationData.postId)
                                        .collection("Replies")
                                        .document(notificationData.contentId)
                                        .get()
                                        .await()
                                        .toObject(ReplyDTO::class.java)
                                } else {
                                    null
                                }
                            }

                            val getLikedReplyDeferred = async {
                                if (notificationData.contentId.isNotBlank()) {
                                    firestore.collection("GlobalPosts")
                                        .document(notificationData.postId)
                                        .collection("Replies")
                                        .document(notificationData.contentId)
                                        .get()
                                        .await()
                                        .toObject(ReplyDTO::class.java)
                                } else {
                                    null
                                }
                            }

                            val getLikedPostDeferred = async {
                                if (notificationData.contentId.isNotBlank()) {
                                    val snapshot = firestore.collection("GlobalPosts")
                                        .document(notificationData.postId)
                                        .get()
                                        .await()
                                    snapshot.toObject(PostDTO::class.java)
                                } else {
                                    null
                                }
                            }

                            val getReplies = getRepliesDeferred.await()
                            val getLikedReply = getLikedReplyDeferred.await()
                            val likedBy = likedByDeferred.await()
                            val post = getLikedPostDeferred.await()

                            val content: Content = when (notificationData.type) {
                                "LIKE" -> Content(
                                    text = post?.postContent?.postData?.postText
                                        ?: "Deleted by user",
                                    image = post?.postContent?.postData?.postImage ?: ""
                                )

                                "POST_REPLY" -> Content(
                                    text = getReplies?.content ?: "Deleted by user"
                                )

                                "LIKE_REPLY" -> Content(
                                    text = getLikedReply?.content ?: "Deleted by user"
                                )

                                "LINK_REQUEST" -> Content(
                                    text = "Sent you a link request"
                                )

                                else -> Content(
                                    text = ""
                                )
                            }


                            val createdAt = notificationData.createdAt


                            NotificationDTO(
                                notificationId = notificationData.notificationId,
                                createrId = notificationData.creatorId,
                                createdAt = createdAt,
                                postId = notificationData.postId,
                                actionBy = User(
                                    userName = likedBy?.userName ?: "",
                                    _id = likedBy?._id ?: "",
                                    userImage = likedBy?.userImage ?: ""
                                ),
                                content = content,
                                type = notificationData.type
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

    override fun fetchLinkUpRequest(): Flow<ResultState<List<NotificationDTO>>> {
        return flow {

            emit(ResultState.Loading)

            try {

                val notificationSnapshot =
                    firestore.collection("Users").document(auth.currentUser!!.uid)
                        .collection("LinkUpRequests")
                        .whereEqualTo("status", false)
                        .get()
                        .await()

                val notificationList = coroutineScope {

                    notificationSnapshot.map { data ->

                        async {

                            val notificationData = data.toObject(LinkUpRequestDTO::class.java)

                            val sendByDeferred = async {
                                firestore.collection("Users")
                                    .document(notificationData.senderId)
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                            }


                            val likedBy = sendByDeferred.await()


                            val createdAt = notificationData.createdAt


                            NotificationDTO(
                                notificationId = notificationData.senderId,
                                createrId = notificationData.senderId,
                                createdAt = createdAt,
                                actionBy = User(
                                    userName = likedBy?.userName ?: "",
                                    _id = likedBy?._id ?: "",
                                    userImage = likedBy?.userImage ?: ""
                                ),
                                type = "LINK_REQUEST"
                            )

                        }

                    }.map { it.await() }

                }

                emit(ResultState.Success(notificationList))


            } catch (e: Exception) {
                emit(ResultState.Error(e.message.toString()))
            }

        }
    }

    override fun markNotificationAsRead() {

        val currentUser = auth.currentUser ?: return

        val userNotificationsRef = firestore.collection("Users")
            .document(currentUser.uid)
            .collection("Notifications")

        userNotificationsRef
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val batch = firestore.batch()
                    for (document in querySnapshot.documents) {
                        batch.update(document.reference, "isRead", true)
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
            userNotificationsRef.whereEqualTo("isRead", false)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        trySend(ResultState.Error(error.message.toString()))
                    } else {
                        val unreadCount = snapshot?.size() ?: 0
                        trySend(ResultState.Success(unreadCount))
                    }
                }

        }
    }


}