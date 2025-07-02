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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.ReplyDTO
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.presentation.UploadState
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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class PostRepoImpl(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) :PostRepository {

    override suspend fun createPost(dto: CreatePostDTO, imageUri: Uri?): Flow<UploadState> = callbackFlow {

        trySend(UploadState.Loading)

        val path = if (dto.feedMode == FeedMode.CAMPUS) "CampusPosts" else "GlobalPosts"

         fun uploadPost(post: CreatePostDTO, uploadId: String? = null) {
            firestore.collection(path).document(dto.postId).set(post)
                .addOnSuccessListener {
                    trySend(UploadState.Success(uploadId ?: ""))
                    close()
                }
                .addOnFailureListener {
                    trySend(UploadState.Error(it.localizedMessage ?: "Failed to save post"))
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

                firestore.collection(path)
                    .document(postId)
                    .delete()
            } else {
                firestore.collection(path)
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
        return try {

            if (auth.currentUser == null) return Result.failure(Exception("User not logged in"))

            val baseCollection = getBaseCollection(feedMode = FeedMode.GLOBAL, campusId = null, firestore = firestore)
            val postsSnapshot = baseCollection.get().await()

            val postList = coroutineScope {
                postsSnapshot.documents.map { doc ->
                    async {
                        val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(post.creatorId)
                                .get()
                                .await()
                                .toObject(UserDetail::class.java)
                        }

                        val likesDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Likes")
                                .document(post.postId)
                                .get()
                                .await()
                                .get("likes") as? List<String> ?: emptyList()
                        }

                        val repliesCountDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Replies")
                                .get()
                                .await()
                                .size()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val repliesCount = repliesCountDeferred.await()

                        val isLiked = auth.currentUser?.uid in likes
                        val isCurrentUser = auth.currentUser?.uid == post.creatorId
                        val profile = visibilityMode(post.visibilityMode, user)

                        GetPostDTO(
                            postId = post.postId,
                            createdAt = post.createdAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = false,
                                isPremium = false,
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

    override suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Result<List<GetPostDTO>> {
        return try {


            if (auth.currentUser == null) return Result.failure(Exception("User not logged in"))

            if (campusId.isNullOrEmpty()) return Result.failure(Exception(Error.CAMPUS_NOT_FOUND.name))

            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val postsSnapshot = baseCollection
                .whereEqualTo("campusId", campusId)
                .whereEqualTo("feedMode", feedMode)
                .get()
                .await()

            val postList = coroutineScope {
                postsSnapshot.documents.map { doc ->
                    async {
                        val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(post.creatorId)
                                .get()
                                .await()
                                .toObject(UserDetail::class.java)
                        }

                        val likesDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Likes")
                                .document(post.postId)
                                .get()
                                .await()
                                .get("likes") as? List<String> ?: emptyList()
                        }

                        val repliesCountDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Replies")
                                .get()
                                .await()
                                .size()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val repliesCount = repliesCountDeferred.await()

                        val isLiked = auth.currentUser?.uid in likes
                        val isCurrentUser = auth.currentUser?.uid == post.creatorId
                        val profile = visibilityMode(post.visibilityMode, user)

                        GetPostDTO(
                            postId = post.postId,
                            createdAt = post.createdAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = false,
                                isPremium = false,
                                profile = UserDetail(
                                    id = post.creatorId,
                                    userName = profile.first,
                                    userImage = profile.second,
                                    userBio = user?.userBio ?: ""
                                )
                            ),
                            feedMode = post.feedMode,
                            campusId = post.campusId,
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

    override suspend fun editPost(postId: String, editedText: String, campusId: String?, feedMode: FeedMode): Result<Unit> {
        return try {

            if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))
            if (editedText.isBlank()) return Result.failure(IllegalArgumentException("Edited text cannot be empty"))

            val postRef = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

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

    override suspend fun getPostsById(userId: String, campusId: String?,feedMode: FeedMode): Result<List<GetPostDTO>> {
        return try {

            if (auth.currentUser == null) return Result.failure(Exception("User not logged in"))

            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val postsSnapshot = baseCollection
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("campusId", campusId)
                .get()
                .await()

            val postList = coroutineScope {
                postsSnapshot.documents.map { doc ->
                    async {
                        val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(post.creatorId)
                                .get()
                                .await()
                                .toObject(UserDetail::class.java)
                        }

                        val likesDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Likes")
                                .document(post.postId)
                                .get()
                                .await()
                                .get("likes") as? List<String> ?: emptyList()
                        }

                        val repliesCountDeferred = async {
                            baseCollection.document(post.postId)
                                .collection("Replies")
                                .get()
                                .await()
                                .size()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val repliesCount = repliesCountDeferred.await()

                        val isLiked = auth.currentUser?.uid in likes
                        val isCurrentUser = auth.currentUser?.uid == post.creatorId
                        val profile = visibilityMode(post.visibilityMode, user)

                        GetPostDTO(
                            postId = post.postId,
                            createdAt = post.createdAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = false,
                                isPremium = false,
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

    override suspend fun createReply(replyId: String, postId: String, content: String, creatorId: String, visibilityMode: PostVisibilityMode, mode: FeedMode,campusId: String?): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(IllegalStateException("User not logged in"))

            val replyPayload = mapOf(
                "replyId" to replyId,
                "postId" to postId,
                "creatorId" to creatorId,
                "repliedBy" to currentUserId,
                "content" to content,
                "repliedAt" to System.currentTimeMillis(),
                "visibilityMode" to visibilityMode.name,
                "mode" to mode,

            )

            val baseCollection = getBaseCollection(
                feedMode = mode,
                campusId = campusId,
                firestore = firestore
            )

            baseCollection
                .document(postId)
                .collection("Replies")
                .document(replyId)
                .set(replyPayload)
                .await()

            if (creatorId != currentUserId) {
                val notification = CreateNotificationDTO(
                    notificationId = replyId,
                    type = NotificationType.COMMENTED,
                    visibilityMode = visibilityMode,
                    replyId = replyId,
                    postId = postId,
                    creatorId = creatorId,
                    actionBy = currentUserId,
                    createdAt = System.currentTimeMillis()
                )

                firestore.collection("Users")
                    .document(creatorId)
                    .collection("Notifications")
                    .add(notification)
                    .await()

                sendPushNotification.messageNotification(
                    notificationReceiverId = creatorId,
                    notificationType = "COMMENTED"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReplies(postId: String,capurId: String?,feedMode: FeedMode): Result<List<GetRepliesDTO>> {
        return try {

            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = capurId, firestore = firestore)

            val repliesSnapshot = baseCollection
                .document(postId)
                .collection("Replies")
                .get()
                .await()

            val replies = coroutineScope {
                repliesSnapshot.documents.map { document ->
                    async {
                        val reply = document.toObject(ReplyDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(reply.repliedBy)
                                .get()
                                .await()
                                .toObject(UserDetail::class.java)
                        }

                        val likesDeferred = async {
                            firestore.collection("GlobalPosts")
                                .document(reply.postId)
                                .collection("Replies")
                                .document(reply.replyId)
                                .collection("Likes")
                                .document(reply.replyId)
                                .get()
                                .await()
                                .get("likes") as? List<String> ?: emptyList()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()

                        val currentUserId = auth.currentUser?.uid
                        val isLiked = currentUserId != null && likes.contains(currentUserId)
                        val isCurrentUser = reply.creatorId == currentUserId

                        val (userName, userImage) = when (reply.visibilityMode) {
                            PostVisibilityMode.USER -> user?.userName to user?.userImage
                            else -> "Anonymous" to anonymousImage
                        }

                        GetRepliesDTO(
                            postId = reply.postId,
                            replyId = reply.replyId,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = false,
                                isPremium = false,
                                profile = UserDetail(
                                    id = reply.creatorId,
                                    userName = userName ?: "",
                                    userImage = userImage ?: "",
                                    userBio = user?.userBio ?: "",
                                )
                            ),
                            isEdited = reply.isEdited,
                            content = reply.content,
                            repliedAt = reply.repliedAt,
                            visibilityMode = reply.visibilityMode,
                            actions = PostActions(
                                isLiked = isLiked,
                                likesCount = likes.size,
                                replyCount = 0,
                                replies = emptyList()
                            )
                        )
                    }
                }.mapNotNull { it.await() }
            }

            Log.d("PostRepoImpl", "getReplies: ${replies.size}")
            Result.success(replies.sortedByDescending { it.repliedAt })


        } catch (e: Exception) {
            Log.e("getReplies", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean, campusId: String?, feedMode: FeedMode): Result<Unit> = suspendCancellableCoroutine { cont ->

        val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

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
                        type = NotificationType.LIKE_POST,
                        postId = postId,
                        isRead = false,
                        replyId = null,
                        creatorId = userId,
                        actionBy = auth.currentUser?.uid ?: "",
                        createdAt = System.currentTimeMillis()
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

    override suspend fun likeReply(creatorId: String, postId: String, replyId: String, isLiked: Boolean): Result<Unit> = suspendCoroutine { cont ->
        val likeDocRef = firestore.collection("GlobalPosts")
            .document(postId)
            .collection("Replies")
            .document(replyId)
            .collection("Likes")
            .document(replyId)

        val updateData = mapOf(
            "likes" to if (isLiked)
                FieldValue.arrayRemove(auth.currentUser?.uid)
            else
                FieldValue.arrayUnion(auth.currentUser?.uid)
        )

        likeDocRef.set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                // Skip notification if the user liked their own reply
                if (creatorId == auth.currentUser?.uid) {
                    cont.resume(Result.success(Unit))
                    return@addOnSuccessListener
                }

                if (!isLiked) {
                    val notification = CreateNotificationDTO(
                        type = NotificationType.LIKE_REPLY,
                        replyId = replyId,
                        postId = postId,
                        creatorId = creatorId,
                        actionBy = auth.currentUser?.uid ?: "",
                        createdAt = System.currentTimeMillis(),
                        notificationId = replyId + (auth.currentUser?.uid ?: "")
                    )

                    firestore.collection("Users").document(creatorId)
                        .collection("Notifications")
                        .document(notification.notificationId)
                        .set(notification)
                        .addOnSuccessListener {
                            sendPushNotification.messageNotification(
                                notificationReceiverId = creatorId,
                                notificationType = "LIKE_REPLY"
                            )
                            cont.resume(Result.success(Unit))
                        }
                        .addOnFailureListener { notifError ->
                            cont.resume(Result.failure(notifError))
                        }
                } else {
                    cont.resume(Result.success(Unit))
                }
            }
            .addOnFailureListener { e ->
                cont.resume(Result.failure(e))
            }
    }

    override suspend fun deleteReply(postId: String, replyId: String, campusId: String?,feedMode: FeedMode): Result<Unit> {
        return try {
            if (replyId.isBlank()) {
                return Result.failure(Exception("Invalid post ID"))
            }

            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val task = baseCollection.document(postId)
                .collection("Replies")
                .document(replyId)
                .delete()

            // Suspend until task completes
            task.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editReply(postId: String, replyId: String, content: String, campusId: String?,feedMode: FeedMode): Result<Unit> {
        return try {

            if (replyId.isBlank()) {
                return Result.failure(IllegalArgumentException("Invalid reply ID"))
            }

            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)


            val update = mapOf(
                "content" to content,
                "isEdited" to true
            )

            baseCollection.document(postId)
                .collection("Replies").document(replyId)
                .update(update)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPoll(post: CreatePostDTO): Result<Unit> {
        return runCatching {

            val baseCollection = getBaseCollection(feedMode = post.feedMode, campusId = post.campusId, firestore = firestore)

            baseCollection
                .document(post.postId)
                .set(post)
                .await() // If this throws, it's caught by runCatching
        }
    }

    override suspend fun voteOnPoll(postId: String, optionId: String,campusId: String?,feedMode: FeedMode): Result<Unit> {
        return runCatching {

            Log.d("VOTE_ON_POL", "voteOnPoll: $postId $feedMode $campusId")
            val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

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

fun visibilityMode(visibilityMode: PostVisibilityMode, user: UserDetail?): Pair<String, String> {
    return when (visibilityMode) {
        PostVisibilityMode.ANONYMOUS -> Pair("Anonymous", anonymousImage)
        PostVisibilityMode.USER -> Pair(user?.userName.toString(), user?.userImage.toString())
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


