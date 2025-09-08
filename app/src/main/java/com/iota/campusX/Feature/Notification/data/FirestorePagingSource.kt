package com.iota.campusX.Feature.Notification.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.iota.campusX.Feature.Notification.domain.GetNotification
import com.iota.campusX.Feature.Notification.domain.GetNotification.CommentNotification
import com.iota.campusX.Feature.Notification.domain.GetNotification.ConnectionRequestNotification
import com.iota.campusX.Feature.Notification.domain.GetNotification.LikeNotification
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.domain.UserPayload
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.remote.visibilityMode
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await


class NotificationPagingSource(
    private val newsQuery: Query,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : PagingSource<QuerySnapshot, GetNotification>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, GetNotification>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, GetNotification> {
        return try {
            val currentPage = params.key ?: newsQuery.get().await()

            // Build the list from current snapshot
            val data = fetchNotification(currentPage, firestore, auth)

            // If this page is empty OR less than requested load size → no more data
            val lastVisibleDoc = currentPage.documents.lastOrNull()
            val nextPage =
                if (lastVisibleDoc != null && currentPage.size() > 0) {
                    val nextSnapshot = newsQuery.startAfter(lastVisibleDoc).get().await()
                    if (nextSnapshot.isEmpty) null else nextSnapshot
                } else {
                    null
                }

            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }


}

suspend fun fetchNotification(
    query: QuerySnapshot,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
): List<GetNotification> {

    return coroutineScope {
        query.documents.mapNotNull { doc ->

            val typeStr = doc.getString("type") ?: return@mapNotNull null
            val type = NotificationType.valueOf(typeStr)

            when (type) {
                NotificationType.LIKE -> {

                    val raw = doc.toObject(CreateNotification.LikeNotification::class.java) ?: return@mapNotNull null


                    async {
                        // 1. Fetch post content
                        val postDeferred = async {

                            val postSnap = firestore.collection("Posts")
                                .document(raw.postId)
                                .get()
                                .await()

                            val text = postSnap.getString("postText") ?: ""
                            val image = postSnap.getString("image") ?: ""

                            if (!postSnap.exists()) return@async null

                            PostContent(
                                text = postSnap.getString("postText") ?: "",
                                image = postSnap.getString("image") ?: ""
                            )

                        }

                        // 2. Fetch each user who liked
                        val creatorDeferred = async {
                            raw.likes.take(3).mapNotNull { userId ->
                                val userSnap = firestore.collection("Users")
                                    .document(userId)
                                    .get()
                                    .await()

                                if (!userSnap.exists()) return@mapNotNull null

                                val userImage = userSnap.getString("userImage") ?: ""
                                val userName = userSnap.getString("userName") ?: ""

                                UserPayload(
                                    userName = userName,
                                    userImage = userImage
                                )
                            }
                        }

                        val likesCount = creatorDeferred.await().count()

                        // 3. Build notification with hydrated data
                        LikeNotification(
                            notificationId = raw.notificationId,
                            type = raw.type,
                            createdAt = raw.createdAt as? Timestamp,
                            isRead = raw.read,
                            postId = raw.postId,
                            postContent = postDeferred.await(),
                            likes = creatorDeferred.await(),
                            likesCount = if (likesCount>3) likesCount-3 else likesCount
                        )
                    }
                }


                NotificationType.COMMENT -> {

                    val raw = doc.toObject(CreateNotification.CommentNotification::class.java) ?: return@mapNotNull null

                    async {
                        // hydrate post content

                        val postDeferred = async {
                            val postSnap = firestore.collection("Posts")
                                .document(raw.postId)
                                .get()
                                .await()

                            if (!postSnap.exists()) return@async null

                            PostContent(
                                text = postSnap.getString("postText") ?: "",
                                image = postSnap.getString("image") ?: ""
                            )
                        }

                        val creatorDeferred = async {
                            raw.commentContent.distinctBy { it.repliedBy }.take(3).mapNotNull { userId ->
                                val userSnap = firestore.collection("Users")
                                    .document(userId.repliedBy)
                                    .get()
                                    .await()

                                if (!userSnap.exists()) return@mapNotNull null

                                val userImage = userSnap.getString("userImage") ?: ""
                                val userName = userSnap.getString("userName") ?: ""

                                val visibility = visibilityMode(
                                    visibilityMode = userId.visibilityMode,
                                    userName = userName,
                                    userImage = userImage
                                )
                                UserPayload(
                                    visibilityMode = userId.visibilityMode,
                                    userName = visibility.first,
                                    userImage = visibility.second
                                )

                            }
                        }


                        CommentNotification(
                            notificationId = raw.notificationId,
                            type = raw.type,
                            createdAt = raw.createdAt as? Timestamp,
                            isRead = raw.read,
                            postId = raw.postId,
                            visibilityMode = raw.visibilityMode,
                            postContent = postDeferred.await(),
                            replyUsers = creatorDeferred.await()
                        )

                    }

                }

                NotificationType.CONNECTION_REQUEST -> {
                    val raw = doc.toObject(CreateNotification.ConnectionRequestNotification::class.java) ?: return@mapNotNull null
                    async {

                        val userSnap = firestore.collection("Users")
                            .document(raw.actionBy)
                            .get()
                            .await()

                        val userImage = userSnap.getString("userImage")?:""
                        val userName = userSnap.getString("userName") ?:""
                        val isVerified = userSnap.getBoolean("metaData.verified") ?: false

                        val creator = CreatorDetail(
                            profile = UserBasicDetail(
                                userName = userName,
                                id = raw.actionBy,
                                userImage = userImage,
                            ),
                            isVerified = isVerified
                        )


                        ConnectionRequestNotification(
                            notificationId = raw.notificationId,
                            type = raw.type,
                            createdAt = raw.createdAt as Timestamp?,
                            isRead = raw.read,
                            actionBy = creator
                        )
                    }
                }

                NotificationType.SYSTEM -> null
            }
        }.map { it.await() }
    }
}



