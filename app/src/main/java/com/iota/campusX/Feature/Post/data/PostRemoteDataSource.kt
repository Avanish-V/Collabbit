package com.iota.campusX.Feature.Post.data

import SendPushNotification
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.domain.ContentType
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.LikePayload
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.domain.toTypedObject
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.data.model.UserDetail
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Utils.anonymousImage
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.Date
import kotlin.collections.mapOf
import kotlin.coroutines.resume

class PostRemoteDataSource(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
):PostRepository {

    val PAGE_SIZE = 8
    var lastVisibleDoc: DocumentSnapshot? = null
    var latestPostTimestamp: com.google.firebase.Timestamp? = null

    override suspend fun createPost(dto: CreatePostDTO, imageUri: Uri?): Flow<UploadState> = callbackFlow {

        if (dto.feedMode == FeedMode.CAMPUS && dto.campusId == null){
            trySend(UploadState.Error("Campus id is not updated"))
            awaitClose {
                close()
            }
            return@callbackFlow
        }

        trySend(UploadState.Loading)

        val path = if (dto.feedMode == FeedMode.CAMPUS) "CampusPosts" else "GlobalPosts"

        fun uploadPost(post: CreatePostDTO, uploadId: String? = null) {
            firestore.collection("Posts").document(dto.postId).set(post)
                .addOnSuccessListener {
                    trySend(UploadState.Success(uploadId ?: ""))
                    close()
                }
                .addOnFailureListener {
                    Log.e("PostRepoImpl", "createPost: ${it.message}")
                    trySend(UploadState.Error(it.message ?: "Failed to save post"))
                    close()
                }
        }

        if (imageUri != null) {
            MediaManager.get().upload(imageUri)
                .callback(object : UploadCallback {
                    override fun onStart(requestId: String?) {
                        trySend(UploadState.Started(requestId ?: ""))
                    }

                    override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                        val progress = ((bytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
                        trySend(UploadState.Progress(progress, requestId ?: ""))
                    }

                    override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                        val imageUrl = resultData?.get("secure_url")?.toString()
                        val updatedPost = dto.copy(
                            postContent = dto.postContent.copy(
                                postData = dto.postContent.postData.copy(
                                    postImage = imageUrl
                                )
                            )
                        )
                        launch { uploadPost(updatedPost, requestId) }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        trySend(UploadState.Error("Upload error"))
                        close()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
                }).dispatch()
        } else {
            uploadPost(dto)
        }

        awaitClose { /* clean-up if needed */ }

    }

    override suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit> {

        if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))

        val path = if (feedMode == FeedMode.CAMPUS) "CampusPosts" else "GlobalPosts"

        return try {
            val task = if (!campusId.isNullOrBlank()) {

                firestore.collection("Posts")
                    .document(postId)
                    .delete()
            } else {
                firestore.collection("Posts")
                    .document(postId)
                    .delete()
            }

            task.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Something went wrong!"))
        }
    }

    override suspend fun getPosts(): Result<List<GetPostDTO>> {

        val query = firestore.collection("Posts")
            .whereEqualTo("feedMode", FeedMode.GLOBAL)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(PAGE_SIZE.toLong())


        return fetchPosts(query).onSuccess { posts ->
            lastVisibleDoc = query.get().await().documents.lastOrNull()
            latestPostTimestamp = posts.lastOrNull()?.createdAt
        }

    }

    suspend fun getMorePosts(): Result<List<GetPostDTO>> {
        val lastDoc = lastVisibleDoc ?: return Result.success(emptyList())

        val query = firestore.collection("Posts")
            .whereEqualTo("feedMode", FeedMode.GLOBAL)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .startAfter(lastDoc)
            .limit(PAGE_SIZE.toLong())

        return fetchPosts(query).onSuccess { posts ->
            lastVisibleDoc = query.get().await().documents.lastOrNull()
        }
    }

    suspend fun refreshPosts(): Result<List<GetPostDTO>> {
        val latestTs = latestPostTimestamp ?: return Result.success(emptyList())

        val query = firestore.collection("Posts")
            .whereEqualTo("feedMode", FeedMode.GLOBAL)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .whereGreaterThan("createdAt", latestTs)
            .limit(PAGE_SIZE.toLong())

        return fetchPosts(query).onSuccess { posts ->
            latestPostTimestamp = posts.firstOrNull()?.createdAt ?: latestTs
        }
    }

    override suspend fun fetchSinglePost(postId: String): Result<GetPostDTO> {
        val query = firestore.collection("Posts")
            .whereEqualTo("postId", postId)

        return fetchPosts(query).mapCatching { posts ->
            posts.firstOrNull() ?: throw NoSuchElementException("Post not found for id=$postId")
        }
    }

    override suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Result<List<GetPostDTO>> {
        if (campusId.isNullOrEmpty()) return Result.failure(Exception(Error.CAMPUS_NOT_FOUND.name))
        val query = firestore.collection("Posts")
            .whereEqualTo("campusId", campusId)
            .whereEqualTo("feedMode", feedMode)
        return fetchPosts(query)
    }

    private suspend fun fetchPosts(query: Query): Result<List<GetPostDTO>> {
        return try {
            if (auth.currentUser == null) return Result.failure(Exception("User not logged in"))

            val postsSnapshot = query.get().await()

            val postList = coroutineScope {
                postsSnapshot.documents.map { doc ->
                    async {
                        val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(post.creatorId)
                                .get()
                                .await()
                                .toObject(BaseProfileDTO::class.java)
                        }

                        val likesDeferred = async {
                            firestore.collection("Posts")
                                .document(post.postId)
                                .collection("Likes")
                                .document(post.postId)
                                .get()
                                .await()
                                .get("likes") as? List<String> ?: emptyList()
                        }

                        val repliesCountDeferred = async {
                            firestore.collection("Posts")
                                .document(post.postId)
                                .collection("Replies")
                                .get()
                                .await()
                                .size()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val repliesCount = repliesCountDeferred.await()
                        val date: Date = post.createdAt.toDate()

                        val isLiked = auth.currentUser?.uid in likes
                        val isCurrentUser = auth.currentUser?.uid == post.creatorId
                        val profile = visibilityMode(
                            post.visibilityMode,
                            userName = user?.userName ?: "",
                            userImage = user?.userImage ?: ""
                        )

                        GetPostDTO(
                            postId = post.postId,
                            createdAt = post.createdAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = user?.metaData?.verified ?: false,
                                isPremium = user?.metaData?.premium ?: false,
                                profile = UserDetail(
                                    id = post.creatorId,
                                    userName = profile.first,
                                    userImage = profile.second,
                                    userBio = user?.userBio ?: ""
                                )
                            ),
                            campusId = post.campusId,
                            feedMode = post.feedMode,
                            reference = post.reference,
                            visibilityMode = post.visibilityMode,
                            postContent = post.postContent,
                            postActions = PostActions(
                                isLiked = isLiked,
                                likesCount = likes.size,
                                replies = emptyList(),
                                replyCount = repliesCount
                            )
                        )
                    }
                }.mapNotNull { it.await() }
            }

            Result.success(postList)

        } catch (e: FirebaseNetworkException) {
            Result.failure(Exception("No internet connection"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostsById(userId: String): Result<List<GetPostDTO>> {

        val query = firestore.collection("Posts")
            .whereEqualTo("creatorId", userId)
        return fetchPosts(query)
    }

    override suspend fun editPost(postId: String, editedText: String, campusId: String?, feedMode: FeedMode): Result<Unit> {
        return try {

            Log.d("PostRepoImpl", "editPost: $postId $editedText $campusId $feedMode")

            if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))
            if (editedText.isBlank()) return Result.failure(IllegalArgumentException("Edited text cannot be empty"))

            //val postRef = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val postRef = firestore.collection("Posts")

            postRef.document(postId).update("postContent.postData.postText", editedText).await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.localizedMessage ?: "Something went wrong while editing the post."
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit> = suspendCancellableCoroutine { cont ->

       // val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
        val baseCollection = firestore.collection("Posts")

        val path =  baseCollection.document(postId).collection("Likes").document(postId)

        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(auth.currentUser?.uid) else FieldValue.arrayUnion(
                auth.currentUser?.uid
            )
        )

        path.set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                if (userId != auth.currentUser?.uid && !isLiked) {
                    val notification = CreateNotificationDTO(
                        notificationId = postId + userId,
                        createdAt = FieldValue.serverTimestamp(),
                        type = NotificationType.LIKE,
                        isRead = false,
                        payload = LikePayload(
                            postId = postId,
                            actionBy = auth.currentUser?.uid.toString(),
                            contentType = ContentType.LIKE_POST
                        ).toTypedObject()
                    )
                    firestore.collection("Users").document(userId)
                        .collection("Notifications")
                        .document(postId + userId)
                        .set(notification)
                        .addOnSuccessListener {
                            sendPushNotification.messageNotification(
                                notificationReceiverId = userId,
                                notificationType = "LIKE_POST"
                            )
                        }
                }
                cont.resume(Result.success(Unit))
            }
            .addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }
    }

    override suspend fun createPoll(post: CreatePostDTO): Result<Unit> {
        return runCatching {

            //val baseCollection = getBaseCollection(feedMode = post.feedMode, campusId = post.campusId, firestore = firestore)

            val baseCollection = firestore.collection("Posts")

            baseCollection
                .document(post.postId)
                .set(post)
                .await() // If this throws, it's caught by runCatching
        }
    }

    override suspend fun voteOnPoll(postId: String, optionId: String,campusId: String?,feedMode: FeedMode): Result<Unit> {
        return runCatching {

           // val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val baseCollection = firestore.collection("Posts")

            val postRef = baseCollection.document(postId)
            val snapshot = postRef.get().await()
            val post = snapshot.toObject(CreatePostDTO::class.java)
            val poll = post?.postContent?.postData?.poll ?: throw Exception("Poll not found")

            val updatedOptions = poll.options?.map {
                if (it.optionId == optionId) {
                    it.copy(votes = (it.votes + auth.currentUser?.uid).distinct() as List<String>)
                } else it
            }

            val updateMap = mapOf(
                "postContent.postData.poll.options" to updatedOptions,
                "postContent.postData.poll.hasVoted" to true
            )

            postRef.update(updateMap).await()
        }
    }

}

enum class Error {
    NO_INTERNET,
    CAMPUS_NOT_FOUND
}

fun visibilityMode(visibilityMode: VisibilityMode, userName: String, userImage: String): Pair<String, String> {
    return when (visibilityMode) {
        VisibilityMode.ANONYMOUS -> Pair("Anonymous", anonymousImage)
        VisibilityMode.USER -> Pair(userName.toString(), userImage.toString())
    }
}

fun getBaseCollection(feedMode: FeedMode, campusId: String?, firestore: FirebaseFirestore): CollectionReference {
    return when (feedMode) {
        FeedMode.CAMPUS -> {
            requireNotNull(campusId) { "Campus ID is required for campus feed." }
            firestore.collection("CampusPosts")
        }
        FeedMode.GLOBAL -> firestore.collection("GlobalPosts")
    }
}


