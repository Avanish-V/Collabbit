package com.iota.campusX.Feature.Notification.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.Reply
import com.iota.campusX.Feature.Post.domain.ReplyDTO
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.UserProfile.data.LinkUpRequestDTO
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.getTimeAgo
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class NotificationImpl(private val firestore: FirebaseFirestore,private val auth: FirebaseAuth): NotificationRepository {

    override fun fetchNotification(): Flow<ResultState<List<NotificationDTO>>> {

        return flow {

            emit(ResultState.Loading)

            try {

                val notificationSnapshot = firestore.collection("Users").document(auth.currentUser!!.uid)
                    .collection("Notifications")
                    .get()
                    .await()

                val notificationList = coroutineScope {

                    notificationSnapshot.map { data->

                        async {

                            val notificationData = data.toObject(CreateNotificationDTO::class.java)

                            val likedByDeferred = async {
                                firestore.collection("Users")
                                    .document(notificationData.actionBy)
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                            }

                            val repliesDeferred = async {
                                if (notificationData.postId != null && notificationData.reply?.replyId != null) {
                                    firestore.collection("Posts")
                                        .document(notificationData.postId!!)
                                        .collection("Replies")
                                        .document(notificationData.reply!!.replyId!!)
                                        .get()
                                        .await()
                                        .toObject(ReplyDTO::class.java)
                                } else {
                                    null
                                }
                            }

                            val linkUpRequestDeferred = async {
                                if (notificationData.postId != null && notificationData.reply?.replyId != null) {
                                    firestore.collection("Users")
                                        .document(notificationData.actionBy)
                                        .collection("LinkUpRequests")
                                        .document(notificationData.postId!!)
                                        .get()
                                        .await()
                                        .toObject(ReplyDTO::class.java)
                                } else {
                                    null
                                }
                            }


                            val likedBy = likedByDeferred.await()

                            val replyData = repliesDeferred.await()


                            val createdAt = getTimeAgo(notificationData.createdAt)


                            NotificationDTO(
                                notificationId = notificationData.notificationId,
                                createrId = notificationData.createrId,
                                createdAt = createdAt,
                                postId = notificationData.postId,
                                actionBy= User(
                                    userName = likedBy?.userName ?: "",
                                    _id = likedBy?._id ?: "",
                                    userImage = likedBy?.userImage ?: ""
                                ),
                                reply = Reply(
                                    replyContent = replyData?.content
                                ),
                                type = notificationData.type
                            )

                        }

                    }.map {it.await()}

                }

                emit(ResultState.Success(notificationList))


            }catch (e: Exception){
                emit(ResultState.Error(e.message.toString()))
                Log.d("NotificationImpl", "fetchNotification: ${e.message}")
            }

        }

    }

    override fun fetchLinkUpRequest(): Flow<ResultState<List<NotificationDTO>>> {
        return flow {

            emit(ResultState.Loading)

            try {

                val notificationSnapshot = firestore.collection("Users").document(auth.currentUser!!.uid)
                    .collection("LinkUpRequests")
                    .whereEqualTo("status", false)
                    .get()
                    .await()

                val notificationList = coroutineScope {

                    notificationSnapshot.map { data->

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


                            val createdAt = getTimeAgo(notificationData.createdAt)


                            NotificationDTO(
                                notificationId = notificationData.senderId,
                                createrId = notificationData.senderId,
                                createdAt = createdAt,
                                actionBy= User(
                                    userName = likedBy?.userName ?: "",
                                    _id = likedBy?._id ?: "",
                                    userImage = likedBy?.userImage ?: ""
                                ),
                                type = "LINK_REQUEST"
                            )

                        }

                    }.map {it.await()}

                }

                emit(ResultState.Success(notificationList))


            }catch (e: Exception){
                emit(ResultState.Error(e.message.toString()))
            }

        }
    }


}