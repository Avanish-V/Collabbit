package com.iota.campusX.Feature.Post.data

import SendPushNotification
import android.net.Uri
import android.util.Log
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Post.domain.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.CreatorDetail
import com.iota.campusX.Feature.Post.domain.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.PostData
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.ReplyDTO
import com.iota.campusX.Feature.Post.domain.UploadResponse
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Screens.Post.Poll
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.anonymousImage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await


class PostRepoImpl(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) :PostRepository {

    override fun getPosts(postMode: Boolean): Flow<ResultState<List<PostDTO>>> = flow {
        emit(ResultState.Loading)

        try {
            val baseCollection = if (postMode) {
                firestore.collection("CampusPosts").document("0900").collection("Posts")
            } else {
                firestore.collection("GlobalPosts")
            }

            val postsSnapshot = baseCollection.get().await()

            val postList = supervisorScope {
                postsSnapshot.documents.map { doc ->
                    async {
                        val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            val creatorId = post.creatorId
                            if (creatorId.isNotBlank()) {
                                firestore.collection("Users")
                                    .document(creatorId)
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                            } else {
                                null
                            }
                        }


                        val likesDeferred = async {
                            val path = if (postMode) {
                                firestore.collection("CampusPosts").document("0900")
                                    .collection("Posts")
                            } else {
                                firestore.collection("GlobalPosts")
                            }

                            val snap = path
                                .document(post.postId)
                                .collection("Likes")
                                .document(post.postId)
                                .get()
                                .await()

                            snap.get("likes") as? List<String> ?: emptyList()
                        }

                        val repliesDeferred = async {
                            val path = if (postMode) {
                                firestore.collection("CampusPosts").document("0900")
                                    .collection("Posts")
                            } else {
                                firestore.collection("GlobalPosts")
                            }

                            path.document(post.postId)
                                .collection("Replies")
                                .get()
                                .await()
                                .size()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val replyCount = repliesDeferred.await()
                        val currentUserId = auth.currentUser?.uid
                        val isLiked = currentUserId?.let { likes.contains(it) } ?: false
                        val isCurrentUser = post.creatorId == currentUserId


                        val postMode: Pair<String, String> =
                            if (post.type == "USER") Pair(
                                user?.userName ?: "",
                                user?.userImage ?: ""
                            )
                            else Pair(
                                "Anonymous",
                                anonymousImage
                            )

                        PostDTO(
                            postId = post.postId,
                            postedAt = post.postedAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = false,
                                isPremium = false,
                                type = post.type,
                                profile = User(
                                    id = post.creatorId,
                                    userName = postMode.first,
                                    userImage = postMode.second,
                                    about = user?.about ?: ""
                                )
                            ),
                            reference = post.reference,
                            postMode = post.type,
                            postContent = PostContent(
                                postType = post.postContent.postType,
                                postData = PostData(
                                    postText = post.postContent.postData.postText,
                                    postImage = post.postContent.postData.postImage,
                                    poll = post.postContent.postData.poll
                                )
                            ),
                            postActions = PostActions(
                                isLiked = isLiked,
                                likesCount = likes.size,
                                replies = emptyList(),
                                replyCount = replyCount
                            )
                        )
                    }
                }.mapNotNull { it.await() }
            }

            emit(ResultState.Success(postList))
        } catch (e: FirebaseNetworkException) {
            emit(ResultState.Error("No internet connection"))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Something went wrong"))
        }
    }

    override fun getPostsById(userId: String, campusId: String?): Flow<ResultState<List<PostDTO>>> =
        flow {
            emit(ResultState.Loading)

            try {
                val currentUserId = auth.currentUser?.uid

                val globalPostsTask = firestore.collection("GlobalPosts")
                    .whereEqualTo("creatorId", userId)
                    .get()

                val campusPostsTask = if (!campusId.isNullOrBlank()) {
                    firestore.collection("CampusPosts")
                        .document(campusId)
                        .collection("Posts")
                        .whereEqualTo("creatorId", userId)
                        .get()
                } else null

                val (globalPosts, campusPosts) = coroutineScope {
                    val global = async { globalPostsTask.await() }
                    val campus = campusPostsTask?.let { async { it.await() } }

                    global.await() to campus?.await()
                }

                val allDocuments = buildList {
                    addAll(globalPosts.documents)
                    campusPosts?.documents?.let { addAll(it) }
                }.distinctBy { it.id }

                // ⚠️ Filter based on userId vs current user
                val filteredDocuments = allDocuments.filter { doc ->
                    val type = doc.getString("type")
                    if (userId == currentUserId) {
                        // Owner is viewing: show all posts
                        type == "USER" || type == "ANONYMOUS"
                    } else {
                        // Viewer is not the owner: show only USER posts
                        type == "USER"
                    }
                }

                val postList = coroutineScope {
                    filteredDocuments.map { doc ->
                        async {
                            doc.toObject(CreatePostDTO::class.java)?.let { post ->

                                val userDeferred = async {
                                    firestore.collection("Users")
                                        .document(post.creatorId)
                                        .get()
                                        .await()
                                        .toObject(User::class.java)
                                }

                                val likesDeferred = async {
                                    firestore.collection("GlobalPosts")
                                        .document(post.postId)
                                        .collection("Likes")
                                        .document(post.postId)
                                        .get()
                                        .await()
                                        .get("likes") as? List<String> ?: emptyList()
                                }

                                val replyCountDeferred = async {
                                    firestore.collection("GlobalPosts")
                                        .document(post.postId)
                                        .collection("Replies")
                                        .get()
                                        .await()
                                        .size()
                                }

                                val (user, likes, replyCount) = awaitAll(
                                    userDeferred,
                                    likesDeferred,
                                    replyCountDeferred
                                )

                                val isLiked = (likes as List<*>).contains(auth.currentUser?.uid)
                                val reply_Count = replyCount as Int

                                val userInfo = if (post.type == "USER") {
                                    Pair(
                                        (user as? User)?.userName ?: "",
                                        (user as? User)?.userImage ?: ""
                                    )
                                } else {
                                    "Anonymous" to anonymousImage
                                }

                                val isCurrentUser = post.creatorId == currentUserId

                                PostDTO(
                                    postId = post.postId,
                                    postedAt = post.postedAt,
                                    creatorDetail = CreatorDetail(
                                        isCurrentUser = isCurrentUser,
                                        isVerified = false,
                                        isPremium = false,
                                        type = post.type,
                                        profile = User(
                                            userName = userInfo.first,
                                            id = post.creatorId,
                                            userImage = userInfo.second
                                        )
                                    ),
                                    reference = post.reference,
                                    postMode = post.type,
                                    postContent = PostContent(
                                        postType = post.postContent.postType,
                                        postData = PostData(
                                            postText = post.postContent.postData.postText,
                                            postImage = post.postContent.postData.postImage
                                        )
                                    ),
                                    postActions = PostActions(
                                        isLiked = isLiked,
                                        likesCount = likes.size,
                                        replies = emptyList(),
                                        replyCount = reply_Count
                                    )
                                )
                            }
                        }
                    }.mapNotNull { it.await() }
                }

                emit(ResultState.Success(postList))

            } catch (e: FirebaseNetworkException) {
                emit(ResultState.Error(e.localizedMessage ?: "No internet connection"))
            } catch (e: Exception) {
                emit(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }
        }

    override fun toggleLike(userId: String, postId: String, isLiked: Boolean) {


        val likeDocRef = firestore.collection("GlobalPosts")
            .document(postId)
            .collection("Likes")
            .document(postId) // This could also be userId if you're storing one doc per user

        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(auth.currentUser?.uid) else FieldValue.arrayUnion(auth.currentUser?.uid)
        )

        likeDocRef.set(updateData, SetOptions.merge()) // This creates the doc if it doesn't exist
            .addOnSuccessListener {

                if (userId == auth.currentUser!!.uid) return@addOnSuccessListener


                if (!isLiked) {
                    val notification = CreateNotificationDTO(
                        notificationId = postId+userId,
                        type = "LIKE",
                        postId = postId,
                        isRead = false,
                        contentId = postId,
                        creatorId = userId,
                        actionBy = (auth.currentUser?.uid ?: ""),
                        createdAt = System.currentTimeMillis(),
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
                        .addOnFailureListener {

                        }
                }
            }
            .addOnFailureListener { e ->

            }
    }

    override fun likeReply(creatorId: String, postId: String, replyId: String, isLiked: Boolean) {


        val likeDocRef = firestore.collection("GlobalPosts")
            .document(postId)
            .collection("Replies")
            .document(replyId)
            .collection("Likes")
            .document(replyId) // This could also be userId if you're storing one doc per user

        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(creatorId) else FieldValue.arrayUnion(creatorId)
        )

        likeDocRef.set(updateData, SetOptions.merge()) // This creates the doc if it doesn't exist
            .addOnSuccessListener {

                if (creatorId == auth.currentUser!!.uid) return@addOnSuccessListener

                if (!isLiked) {

                    val notification = CreateNotificationDTO(
                        type = "LIKE_REPLY",
                        contentId = replyId,
                        postId = postId,
                        creatorId = creatorId,
                        actionBy = (auth.currentUser?.uid ?: ""),
                        createdAt = System.currentTimeMillis(),
                        notificationId = replyId,
                    )
                    firestore.collection("Users").document(creatorId)
                        .collection("Notifications")
                        .document(replyId+auth.currentUser!!.uid)
                        .set(notification)
                        .addOnSuccessListener {

                            sendPushNotification.messageNotification(
                                notificationReceiverId = creatorId,
                                notificationType = "LIKE_REPLY"
                            )

                        }

                }

            }
            .addOnFailureListener { e ->
                Log.e("TAG", "toggleLike: Failed: ${e.message}")
            }

    }

    override fun getReplies(postId: String): Flow<ResultState<List<GetRepliesDTO>>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                val repliesSnapshot = firestore.collection("GlobalPosts")
                    .document(postId)
                    .collection("Replies")
                    .get()
                    .await()

                val repliesList = coroutineScope {

                    repliesSnapshot.documents.map { doc ->

                        async {
                            val reply = doc.toObject(ReplyDTO::class.java) ?: return@async null

                            val userDeferred = async {
                                firestore.collection("Users")
                                    .document(reply.userId)
                                    .get()
                                    .await()
                                    .toObject(User::class.java)
                            }

                            val likesDeferred = async {
                                val snap = firestore.collection("GlobalPosts")
                                    .document(reply.postId)
                                    .collection("Replies")
                                    .document(reply.replyId)
                                    .collection("Likes")
                                    .document(reply.replyId)
                                    .get()
                                    .await()
                                (snap.get("likes") as? List<String>) ?: emptyList()
                            }


                            val user = userDeferred.await()
                            val likes = likesDeferred.await()
                            val isLiked = likes.contains(reply.userId)
                            val isCurrentUser = reply.userId == auth.currentUser?.uid

                            val userType: Pair<String, String> =
                                if (reply.userType == "USER") Pair(
                                    user?.userName ?: "",
                                    user?.userImage ?: ""
                                )
                                else Pair(
                                    "Anonymous",
                                    anonymousImage
                                )

                            GetRepliesDTO(
                                postId = reply.postId,
                                replyId = reply.replyId,
                                user = User(
                                    userName = userType.first,
                                    id = reply.userId,
                                    userImage = userType.second,
                                    designation = user?.designation ?: "",
                                    isCurrentUser = isCurrentUser

                                ),
                                content = reply.content,
                                actions = PostActions(
                                    isLiked = isLiked,
                                    likesCount = likes.size,
                                    replies = emptyList(),
                                    replyCount = 0
                                ),
                                repliedAt = reply.repliedAt,
                                userType = userType.toString()
                            )


                        }

                    }

                }.mapNotNull { it.await() }

                trySend(ResultState.Success(repliesList))

            } catch (e: Exception) {
                trySend(ResultState.Error(e.message ?: "Unknown error"))
                Log.e("getReplies", "Error: ${e.message}", e)
            }

            awaitClose()
        }
    }

    override fun createReply(replyId: String, postId: String, content: String, repliedAt: Long, creatorId: String, userType: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {
                firestore
                    .collection("GlobalPosts")
                    .document(postId)
                    .collection("Replies")
                    .document(replyId)
                    .set(
                        ReplyDTO(
                            replyId = replyId,
                            postId = postId,
                            userId = auth.currentUser?.uid ?: "",
                            content = content,
                            repliedAt = repliedAt,
                            userType = userType
                        )
                    )
                    .addOnSuccessListener {

                        trySend(ResultState.Success(true))

                        if (creatorId == auth.currentUser?.uid) return@addOnSuccessListener


                        val notification = CreateNotificationDTO(
                            notificationId = replyId,
                            type = "POST_REPLY",
                            userType = userType,
                            contentId = replyId,
                            postId = postId,
                            creatorId = creatorId,
                            actionBy = (auth.currentUser?.uid ?: ""),
                            createdAt = System.currentTimeMillis(),
                        )
                        firestore.collection("Users").document(creatorId)
                            .collection("Notifications")
                            .add(notification)
                            .addOnSuccessListener {

                                sendPushNotification.messageNotification(
                                    notificationReceiverId = creatorId,
                                    notificationType = "COMMENTED"
                                )
                            }


                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))
                    }
            } catch (e: Exception) {
                trySend(ResultState.Error(e.message.toString()))
            }

            awaitClose {
                close()
            }
        }
    }

    override fun createPost(createPostDTO: CreatePostDTO, postMode: Boolean, imageUri: Uri?): Flow<ResultState<UploadResponse>> = callbackFlow {

        trySend(ResultState.Success(UploadResponse("LOADING")))

        if (postMode && createPostDTO.campusId == null) {
            trySend(ResultState.Error("Campus ID is required for campus posts"))
            close()
            return@callbackFlow
        }

        val collectionRef = if (postMode) {
            firestore.collection("CampusPosts")
                .document(createPostDTO.campusId!!)
                .collection("Posts")
                .document(createPostDTO.postId)
        } else {
            firestore.collection("GlobalPosts")
                .document(createPostDTO.postId)
        }

        fun buildPostData(imageUrl: String? = null): CreatePostDTO {
            return if (imageUrl != null) {
                createPostDTO.copy(
                    postContent = createPostDTO.postContent.copy(
                        postData = createPostDTO.postContent.postData.copy(
                            postImage = imageUrl
                        )
                    )
                )
            } else {
                createPostDTO
            }
        }

        val sendCompletedAndFinish: (String?) -> Unit = { uploadId ->
            trySend(ResultState.Success(UploadResponse("COMPLETED", uploadId = uploadId ?: "")))
            trySend(ResultState.Success(UploadResponse("FINISHED", uploadId = uploadId ?: "")))
            close()
        }

        try {
            if (imageUri != null) {
                MediaManager.get().upload(imageUri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            trySend(ResultState.Success(UploadResponse("STARTED", uploadId = requestId ?: "")))
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            val progress = ((bytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
                            trySend(ResultState.Success(UploadResponse("PROGRESS", progress, uploadId = requestId ?: "")))
                        }

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val imageUrl = resultData?.get("secure_url")?.toString()
                            val postData = buildPostData(imageUrl)

                            collectionRef.set(postData)
                                .addOnSuccessListener {
                                    sendCompletedAndFinish(requestId)
                                }
                                .addOnFailureListener { e ->
                                    trySend(ResultState.Error(e.localizedMessage ?: "Firestore upload failed"))
                                    close()
                                }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            trySend(ResultState.Error("Image upload failed"))
                            close()
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            // Optional: implement retry logic
                        }
                    }).dispatch()
            } else {
                val postData = buildPostData()
                collectionRef.set(postData)
                    .addOnSuccessListener {
                        sendCompletedAndFinish(null)
                    }
                    .addOnFailureListener { e ->
                        trySend(ResultState.Error(e.localizedMessage ?: "Firestore post failed"))
                        close()
                    }
            }
        } catch (e: FirebaseNetworkException) {
            trySend(ResultState.Error("Network error, please try again."))
            close()
        } catch (e: Exception) {
            trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            close()
        }

        awaitClose { /* no-op cleanup */ }
    }

    override fun deletePost(postId: String,campusId: String?): Flow<ResultState<Boolean>> {
        return callbackFlow {
            if (postId.isBlank()) trySend(ResultState.Error("Invalid post ID"))
            trySend(ResultState.Loading)
            try {
                if (campusId.isNullOrBlank()) {
                    firestore.collection("CampusPosts").document(campusId.toString())
                        .collection("Posts").document(postId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "CampusPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            close()
                        }
                } else {
                    firestore.collection("GlobalPosts").document(postId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "GlobalPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            Log.e("DELETE_POST", "deletePost: FAILED")
                            close()
                        }

                }
            } catch (e: Exception) {
                trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }

            awaitClose()
        }
    }

    override fun deleteReply(postId:String,replyId: String,campusId: String?): Flow<ResultState<Boolean>> {
        return callbackFlow {

            if (replyId.isBlank()) trySend(ResultState.Error("Invalid post ID"))

            trySend(ResultState.Loading)

            try {
                if (!campusId.isNullOrEmpty()) {
                    firestore.collection("CampusPosts").document(campusId.toString())
                        .collection("Posts").document(replyId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "CampusPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            close()
                        }
                } else {
                    firestore.collection("GlobalPosts").document(postId).
                            collection("Replies").document(replyId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "GlobalPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            Log.e("DELETE_POST", "deletePost: FAILED")
                            close()
                        }

                }
            } catch (e: Exception) {
                trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }

            awaitClose()
        }
    }

    override fun editReply(postId:String,replyId: String,content: String,campusId: String?): Flow<ResultState<Boolean>> {
        return callbackFlow {

            if (replyId.isBlank()) trySend(ResultState.Error("Invalid post ID"))

            trySend(ResultState.Loading)

            Log.e("EDIT_REPLY", "editReply: $content", )
            Log.e("EDIT_REPLY", "editReply: $postId", )
            Log.e("EDIT_REPLY", "editReply: $replyId", )

            val update = mapOf(
                "content" to content,
                "isEdited" to true
            )

            try {
                if (!campusId.isNullOrEmpty()) {
                    firestore.collection("CampusPosts").document(campusId.toString())
                        .collection("Posts").document(replyId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "CampusPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            close()
                        }
                } else {
                    firestore.collection("GlobalPosts").document(postId).
                    collection("Replies").document(replyId).update(update)
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "GlobalPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            Log.e("DELETE_POST", "deletePost: FAILED")
                            close()
                        }

                }
            } catch (e: Exception) {
                trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }

            awaitClose()
        }
    }

    override fun editPost(postId: String,editedText:String,campusId: String?): Flow<ResultState<Boolean>> {
        return callbackFlow {
            if (postId.isBlank()) trySend(ResultState.Error("Invalid post ID"))
            trySend(ResultState.Loading)
            try {
                if (campusId.isNullOrBlank()) {
                    firestore.collection("CampusPosts").document(campusId.toString())
                        .collection("Posts").document(postId).delete()
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "CampusPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            close()
                        }
                } else {
                    firestore.collection("GlobalPosts").document(postId).update("postContent.postData.postText",editedText)
                        .addOnSuccessListener {
                            trySend(ResultState.Success(true))
                            Log.e("DELETE_POST", "GlobalPosts deletePost: SUCCESS")
                            close()
                        }.addOnFailureListener {
                            trySend(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                            Log.e("DELETE_POST", "deletePost: FAILED")
                            close()
                        }

                }
            } catch (e: Exception) {
                trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }

            awaitClose()
        }
    }

    override suspend fun createPoll(post: CreatePostDTO, callback: (ResultState<Boolean>) -> Unit) {
        try {
            firestore.collection("GlobalPosts").document(post.postId).set(post)
                .addOnSuccessListener {
                    callback(ResultState.Success(true))
                }
                .addOnFailureListener {
                    callback(ResultState.Error(it.localizedMessage ?: "Something went wrong!"))
                }
        } catch (e: Exception) {
            callback(ResultState.Error(e.localizedMessage ?: "Unexpected error"))
        }
    }



}

