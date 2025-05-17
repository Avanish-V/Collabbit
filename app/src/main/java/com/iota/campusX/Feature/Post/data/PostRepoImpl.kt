package com.iota.campusX.Feature.Post.data

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
import com.iota.campusX.Feature.Notification.domain.Reply
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
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.getTimeAgo
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.tasks.await


class PostRepoImpl(private val auth: FirebaseAuth, private val firestore: FirebaseFirestore) :PostRepository {

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
                                Log.e("FirestoreError", "Invalid creatorId: $creatorId for postId: ${post.postId}")
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
                                "https://res.cloudinary.com/dni4h8jjy/image/upload/v1746629954/wyuwxwa8qwx0hu0i6flk.png"
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
                                    _id = post.creatorId,
                                    userName = postMode.first,
                                    userImage = postMode.second
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
                                replyCount = replyCount
                            )
                        )
                    }
                }.mapNotNull { it.await() }
            }

            emit(ResultState.Success(postList.sortedByDescending { it.postedAt }))
        } catch (e: FirebaseNetworkException) {
            emit(ResultState.Error("No internet connection"))
        } catch (e: Exception) {
            emit(ResultState.Error(e.localizedMessage ?: "Something went wrong"))
        }
    }

    override fun getPostsById(userId: String, campusId: String): Flow<ResultState<List<PostDTO>>> =
        flow {
            emit(ResultState.Loading)

            try {
                val globalPostsTask = firestore.collection("GlobalPosts")
                    .whereEqualTo("creatorId", userId)
                    .get()

                val campusPostsTask = campusId.let {
                    firestore.collection("CampusPosts")
                        .document(it)
                        .collection("Posts")
                        .whereEqualTo("creatorId", userId)
                        .get()
                }

                val (globalPosts, campusPosts) = coroutineScope {
                    if (true) {
                        val tasks = listOf(
                            async { globalPostsTask.await() },
                            async { campusPostsTask.await() })
                        tasks[0].await() to tasks[1].await()
                    } else {
                        val global = async { globalPostsTask.await() }
                        global.await() to null
                    }
                }

                val allDocuments = buildList {
                    addAll(globalPosts.documents)
                    campusPosts?.documents?.let { addAll(it) }
                }.distinctBy { it.id }

                val postList = coroutineScope {
                    allDocuments.map { doc ->
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
                                    firestore.collection("Posts")
                                        .document(post.postId)
                                        .collection("Likes")
                                        .document(post.postId)
                                        .get()
                                        .await()
                                        .get("likes") as? List<String> ?: emptyList()
                                }

                                val replyCountDeferred = async {
                                    firestore.collection("Posts")
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

                                val isLiked = (likes as List<*>).contains(post.creatorId)
                                val reply_Count = replyCount as Int

                                val userInfo = if (post.type == "USER") {
                                    Pair(
                                        (user as? User)?.userName ?: "",
                                        (user as? User)?.userImage ?: ""
                                    )
                                } else {
                                    "Anonymous" to "https://cdn-icons-png.flaticon.com/128/11029/11029675.png"
                                }

                                val isCurrentUser = post.creatorId == auth.currentUser?.uid

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
                                            _id = post.creatorId,
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

        if (userId == auth.currentUser!!.uid) return

        likeDocRef.set(updateData, SetOptions.merge()) // This creates the doc if it doesn't exist
            .addOnSuccessListener {
                if (!isLiked) {
                    val notification = mapOf(
                        "type" to "LIKE",
                        "postId" to postId,
                        "createrId" to userId,
                        "actionBy" to (auth.currentUser?.uid ?: ""),
                        "createdAt" to System.currentTimeMillis()
                    )
                    firestore.collection("Users").document(userId)
                        .collection("Notifications")
                        .document(postId + userId)
                        .set(notification)
                        .addOnSuccessListener {

                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("TAG", "toggleLike: Failed: ${e.message}")
            }
    }

    override fun likeReply(userId: String, postId: String, replyId: String, isLiked: Boolean) {


        val likeDocRef = firestore.collection("Posts")
            .document(postId)
            .collection("Replies")
            .document(replyId)
            .collection("Likes")
            .document(replyId) // This could also be userId if you're storing one doc per user

        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(userId) else FieldValue.arrayUnion(userId)
        )

        likeDocRef.set(updateData, SetOptions.merge()) // This creates the doc if it doesn't exist
            .addOnSuccessListener {
                val notification = CreateNotificationDTO(
                    type = "LIKE_REPLY",
                    postId = postId,
                    createrId = userId,
                    actionBy = (auth.currentUser?.uid ?: ""),
                    createdAt = System.currentTimeMillis(),
                    reply = Reply(
                        replyId = replyId
                    )
                )
                firestore.collection("Users").document(userId)
                    .collection("Notifications")
                    .add(notification)
                    .addOnSuccessListener {

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

                val repliesSnapshot = firestore.collection("Posts")
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
                                val snap = firestore.collection("Posts")
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

                            GetRepliesDTO(
                                postId = reply.postId,
                                replyId = reply.replyId,
                                user = User(
                                    userName = user?.userName ?: "",
                                    _id = reply.userId,
                                    userImage = user?.userImage ?: "",
                                    designation = user?.designation ?: ""
                                ),
                                content = reply.content,
                                actions = PostActions(
                                    isLiked = isLiked,
                                    likesCount = likes.size,
                                    replies = emptyList(),
                                    replyCount = 0
                                )
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

    override fun createReply(
        replyId: String,
        postId: String,
        content: String,
        repliedAt: Long,
        creatorId: String
    ): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {
                firestore
                    .collection("Posts")
                    .document(postId)
                    .collection("Replies")
                    .document(replyId)
                    .set(
                        ReplyDTO(
                            replyId = replyId,
                            postId = postId,
                            userId = auth.currentUser?.uid ?: "",
                            content = content,
                            repliedAt = repliedAt
                        )
                    )
                    .addOnSuccessListener {

                        trySend(ResultState.Success(true))

                        val notification = CreateNotificationDTO(
                            type = "POST_REPLY",
                            postId = postId,
                            createrId = creatorId,
                            actionBy = (auth.currentUser?.uid ?: ""),
                            createdAt = System.currentTimeMillis(),
                            reply = Reply(
                                replyId = replyId
                            )
                        )
                        firestore.collection("Users").document(creatorId)
                            .collection("Notifications")
                            .add(notification)
                            .addOnSuccessListener {

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

    override fun createPost(
        createPostDTO: CreatePostDTO,
        postMode: Boolean,
        imageUri: Uri?
    ): Flow<ResultState<UploadResponse>> = callbackFlow {
        trySend(ResultState.Loading)

        try {
            // Early exit if campusId is required but not provided
            if (postMode && createPostDTO.campusId == null) {
                trySend(ResultState.Error("Campus ID is required for campus posts"))
                close()
                return@callbackFlow
            }

            // Choose the correct Firestore document reference
            val collectionRef = if (postMode) {
                firestore.collection("CampusPosts")
                    .document(createPostDTO.campusId!!)
                    .collection("Posts")
                    .document(createPostDTO.postId)
            } else {
                firestore.collection("GlobalPosts")
                    .document(createPostDTO.postId)
            }

            // Function to build post data with optional image URL
            fun buildPostData(imageUrl: String? = null): CreatePostDTO {
                if (imageUrl == null) {
                    return createPostDTO
                }
                return createPostDTO.copy(
                    postContent = createPostDTO.postContent.copy(
                        postData = createPostDTO.postContent.postData.copy(
                            postImage = imageUrl.toString()
                        )
                    )
                )
            }

            // Image Upload flow
            if (imageUri != null) {
                MediaManager.get().upload(imageUri)
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String?) {
                            trySend(
                                ResultState.Success(
                                    UploadResponse("STARTED", uploadId = requestId ?: "")
                                )
                            )
                        }

                        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                            val progress = ((bytes.toFloat() / totalBytes.toFloat()) * 100).toInt()
                            trySend(
                                ResultState.Success(
                                    UploadResponse("PROGRESS", progress = progress, uploadId = requestId ?: "")
                                )
                            )
                        }

                        override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                            val imageUrl = resultData?.get("secure_url")?.toString()
                            val postData = buildPostData(imageUrl)

                            collectionRef.set(postData)
                                .addOnSuccessListener {
                                    trySend(ResultState.Success(UploadResponse("COMPLETED", uploadId = requestId ?: "")))
                                    close()
                                }
                                .addOnFailureListener { e ->
                                    trySend(ResultState.Error(e.localizedMessage ?: "Firestore upload failed"))
                                    close()
                                }
                        }

                        override fun onError(requestId: String?, error: ErrorInfo?) {
                            trySend(
                                ResultState.Error(
                                    error?.description ?: "Image upload failed"
                                )
                            )
                            close()
                        }

                        override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                            // Optional: handle retry if needed
                        }
                    }).dispatch()
            } else {
                val postData = buildPostData()

                collectionRef.set(postData)
                    .addOnSuccessListener {
                        trySend(ResultState.Success(UploadResponse("COMPLETED")))
                        close()
                    }
                    .addOnFailureListener { e ->
                        trySend(ResultState.Error(e.localizedMessage ?: "Firestore post failed"))
                        close()
                    }
            }

            awaitClose {
                // Clean up if needed
            }
        } catch (e: FirebaseNetworkException) {
            trySend(ResultState.Error("Network error, please try again."))
            close()
        } catch (e: Exception) {
            trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            close()
        }
    }

    override fun deletePost(postId: String): Flow<ResultState<Boolean>> {
        return callbackFlow {
            try {
                firestore.collection("GlobalPosts").document(postId).delete()
                trySend(ResultState.Success(true))
            } catch (e: Exception) {
                trySend(ResultState.Error(e.localizedMessage ?: "Something went wrong!"))
            }
        }
    }


}

