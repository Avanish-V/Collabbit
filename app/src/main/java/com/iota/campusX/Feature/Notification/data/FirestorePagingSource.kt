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
import com.iota.campusX.Feature.Notification.domain.GetNotification.LikeNotification
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.domain.UserPayload
import com.iota.campusX.Feature.Post.data.remote.visibilityMode
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await


class NotificationPagingSource(
    private val newsQuery: Query,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val getSinglePostByIdUseCase: GetSinglePostByIdUseCase,
    private val userProfileRepository: UserProfileRepository
) : PagingSource<QuerySnapshot, GetNotification>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, GetNotification>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, GetNotification> {
        return try {
            val currentPage = params.key ?: newsQuery.get().await()

            // Build the list from current snapshot
            val data = fetchNotification(
                query = currentPage,
                firestore = firestore,
                getSinglePostByIdUseCase = getSinglePostByIdUseCase,
                userProfileRepository = userProfileRepository
            )

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
    getSinglePostByIdUseCase: GetSinglePostByIdUseCase,
    userProfileRepository: UserProfileRepository
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

                           val postData =   getSinglePostByIdUseCase(postId = raw.postId)

                            postData.fold(
                                onSuccess = {
                                    PostContent(text = it.postContent.postText, image = it.postContent.postImage.map { it.mediaUrl })
                                },
                                onFailure = {return@async}
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

                        LikeNotification(
                            notificationId = raw.notificationId,
                            type = raw.type,
                            createdAt = raw.createdAt as? Timestamp,
                            isRead = raw.read,
                            postId = raw.postId,
                            postContent = postDeferred.await() as PostContent?,
                            likes = creatorDeferred.await(),
                            likesCount = if (raw.likes.size>2) raw.likes.size-2 else raw.likes.size
                        )
                    }
                }


                NotificationType.COMMENT -> {

                    val raw = doc.toObject(CreateNotification.CommentNotification::class.java) ?: return@mapNotNull null

                    async {

                        val postDeferred = async {

                            val postData =   getSinglePostByIdUseCase(postId = raw.postId)
                            var postText: String = ""
                            var mediaList : List<String> = emptyList()
                            postData.fold(
                                onSuccess = {
                                     mediaList = it.postContent.postImage.map { it.mediaUrl }
                                     postText = it.postContent.postText.toString()
                                },
                                onFailure = {}
                            )
                            PostContent(
                                text = postText,
                                image = mediaList
                            )

                        }

                        val creatorDeferred = async {

                            raw.commentContent.distinctBy { it.repliedBy }.take(3).mapNotNull { userId ->

                                val result = userProfileRepository.getUserProfileById(userId = userId.repliedBy)

                                val data = result.fold(
                                    onSuccess = {it},
                                    onFailure = {return@mapNotNull null}
                                )

                                val visibility = visibilityMode(
                                    visibilityMode = userId.visibilityMode,
                                    userName = data.name,
                                    userImage = data.image?:""
                                )
                                UserPayload(
                                    visibilityMode = userId.visibilityMode,
                                    userName = visibility.first,
                                    userImage = visibility.second,
                                    repliedAt = userId.repliedAt
                                )

                            }
                        }


                        CommentNotification(
                            notificationId = raw.notificationId,
                            type = raw.type,
                            createdAt = raw.createdAt as? Timestamp,
                            isRead = raw.read,
                            postId = raw.postId,
                            postContent = postDeferred.await(),
                            replyUsers = creatorDeferred.await()
                        )

                    }

                }


                NotificationType.SYSTEM -> null
                NotificationType.CONNECTION_REQUEST ->null
            }
        }.map { it.await() }
    }
}



