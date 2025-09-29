package com.iota.campusX.Feature.Post.data.remote

import SendPushNotification
import android.net.Uri
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.Validators.PostValidator
import com.iota.campusX.Feature.Post.Validators.ValidationResult
import com.iota.campusX.Feature.Post.data.mapper.FirestorePagingSource
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.data.model.PostType
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Navigation.isPollExpired
import com.iota.campusX.Feature.Post.data.model.Vote
import com.iota.campusX.Utils.anonymousImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

class PostRemoteDataSource(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val validator: PostValidator,
    private val notificationRepository: NotificationRepository

):PostRepositoryInterface {


    override suspend fun createPost(postType: PostType): Flow<UploadState> = flow {

        // 1. Validate
        when (val validation = validator.validate(postType)) {
            is ValidationResult.Error -> {
                emit(UploadState.Error(validation.errors))
                return@flow
            }
            else -> {}
        }

        // 2. Handle Media Upload if needed
        val finalPost = if (postType is PostType.MediaPost && postType.image != null) {
            var uploadedUrl: String? = null
            uploadImage(postType.image).collect { state ->
                when (state) {
                    is UploadState.Progress -> emit(state)
                    is UploadState.MediaUploaded -> uploadedUrl = state.url
                    is UploadState.Error -> {
                        emit(state)
                        return@collect
                    }
                    else -> {}
                }
            }
            postType.copy(image = Uri.parse(uploadedUrl)) // replace local Uri with uploaded URL
        } else postType

        // 3. Save to Firestore
        emit(UploadState.Loading)
        firestore.collection("Posts")
            .document(finalPost.postId)
            .set(finalPost)
            .await()
        emit(UploadState.Success(finalPost.postId))
    }

    override suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit> {

        if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))


        return try {

            firestore.collection("Posts")
                .document(postId)
                .delete()
                .await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Something went wrong!"))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun getPosts(): Flow<PagingData<GetPostDTO>> {

        val query = firestore.collection("Posts")
            .whereEqualTo("feedMode", FeedMode.OPEN)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)

      return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                FirestorePagingSource(
                    newsQuery = query,
                    firestore = firestore,
                    auth = auth
                )
            }
        ).flow
    }

    override suspend fun fetchSinglePost(postId: String): Result<GetPostDTO> {
        val query = firestore.collection("Posts")
            .whereEqualTo("postId", postId)

        return fetchPosts(query).mapCatching { posts ->
            posts.firstOrNull() ?: throw NoSuchElementException("Post not found for id=$postId")
        }
    }

    override suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Flow<PagingData<GetPostDTO>> {

        val query = firestore.collection("Posts")
            .whereEqualTo("campusId", campusId)
            .whereEqualTo("feedMode", feedMode)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(10)

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                FirestorePagingSource(
                    newsQuery = query, // !! is for Non Null Query
                    firestore = firestore,
                    auth = auth

                )
            }
        ).flow
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
                        val isFollowDeferred = async {
                            firestore.collection("Users")
                                .document(post.creatorId)
                                .collection("Followers")
                                .document(auth.currentUser?.uid ?: "")
                                .get()
                                .await()
                                .exists()
                        }

                        val user = userDeferred.await()
                        val likes = likesDeferred.await()
                        val repliesCount = repliesCountDeferred.await()


                        val isLiked = auth.currentUser?.uid in likes
                        val isCurrentUser = auth.currentUser?.uid == post.creatorId

                        val profile = visibilityMode(
                            post.visibilityMode,
                            userName = user?.userName ?: "",
                            userImage = user?.userImage ?: ""
                        )

                        val postContent = when(post.type){
                            Type.Poll -> PostContent(
                                poll = post.poll?.copy(
                                    hasVoted = post.poll.votes.any { it.userId == auth.currentUser?.uid },
                                    isActive = isPollExpired(
                                        createdAt = post.createdAt.toDate().time,
                                    ),
                                    selectedOptionId = post.poll.votes.firstOrNull { it.userId == auth.currentUser?.uid }?.optionId
                                )
                            )
                            Type.Media -> {
                                PostContent(
                                    postImage = post.image,
                                    postText = post.postText
                                )

                            }

                        }

                        GetPostDTO(
                            postId = post.postId,
                            createdAt = post.createdAt,
                            creatorDetail = CreatorDetail(
                                isCurrentUser = isCurrentUser,
                                isVerified = user?.metaData?.verified ?: false,
                                isPremium = user?.metaData?.premium ?: false,
                                isFollow = isFollowDeferred.await(),
                                profile = UserBasicDetail(
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
                            postContent = postContent,
                            type = post.type,
                            mediaType = post.mediaType,
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

    override suspend fun getPostsById(userId: String): Flow<PagingData<GetPostDTO>> {


        val query = if (userId == auth.currentUser?.uid){

            firestore.collection("Posts")
                .whereEqualTo("creatorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
        }else{
            firestore.collection("Posts")
                .whereEqualTo("creatorId", userId)
                .whereEqualTo("visibilityMode", VisibilityMode.USER)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(10)
        }

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                FirestorePagingSource(
                    newsQuery = query, // !! is for Non Null Query
                    firestore = firestore,
                    auth = auth

                )
            }
        ).flow

    }

    override suspend fun editPost(postId: String, editedText: String, campusId: String?, feedMode: FeedMode): Result<Unit> {
        return try {


            if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))
            if (editedText.isBlank()) return Result.failure(IllegalArgumentException("Edited text cannot be empty"))

            //val postRef = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val postRef = firestore.collection("Posts")

            postRef.document(postId).update("postText", editedText).await()

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
    override suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit> = runCatching {

        val currentUid = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

        val postRef = firestore
            .collection("Posts")
            .document(postId)
            .collection("Likes")
            .document(postId)


        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(currentUid) else FieldValue.arrayUnion(currentUid)
        )
        postRef.set(updateData, SetOptions.merge()).await() // ✅ suspending


        // 4️⃣ Send notification only if someone else’s post is liked
        if (userId != currentUid && !isLiked) {

            notificationRepository.createNotification(
                CreateNotification.LikeNotification(
                    notificationId = postId,
                    type = NotificationType.LIKE,
                    createdAt = FieldValue.serverTimestamp(),
                    read = false,
                    likes = emptyList(),
                    postId = postId
                ),
                creatorId = userId
            )

            sendPushNotification.messageNotification(
                notificationReceiverId = userId,
                notificationType = "LIKE_POST"
            )
        }
    }

    override suspend fun createPoll(postType: PostType): Result<Unit> {
        return runCatching {

            when (val validation = validator.validate(postType)) {
                is ValidationResult.Error -> {
                    return Result.failure(Exception(validation.errors))
                }
                else -> {}
            }

            val baseCollection = firestore.collection("Posts")

            if (postType is PostType.PollPost){
                baseCollection
                    .document(postType.postId)
                    .set(postType)
                    .await() // If this throws, it's caught by runCatching

                Result.success(Unit)
            }
        }
    }

    override suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit> {
        return runCatching {
            val baseCollection = firestore.collection("Posts")
            val postRef = baseCollection.document(postId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(postRef)
                val post = snapshot.toObject(CreatePostDTO::class.java)
                    ?: throw Exception("Post not found")
                val poll = post.poll ?: throw Exception("Poll not found")

                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

                // ✅ Check if user already voted
                if (poll.votes.any { it.userId == userId }) {
                    throw Exception("User already voted")
                }

                // ✅ Add new vote
                val updatedVotes = poll.votes.toMutableList()
                updatedVotes.add(Vote(userId = userId, optionId = optionId))


                // ✅ Build update map
                val updateMap = mapOf(
                    "poll.votes" to updatedVotes,
                )

                transaction.update(postRef, updateMap)

            }.await()
        }
    }

}

fun visibilityMode(visibilityMode: VisibilityMode, userName: String, userImage: String): Pair<String, String> {
    return when (visibilityMode) {
        VisibilityMode.ANONYMOUS -> Pair("Anonymous", anonymousImage)
        VisibilityMode.USER -> Pair(userName.toString(), userImage.toString())
    }
}


fun uploadImage(uri: Uri): Flow<UploadState> = callbackFlow {
    MediaManager.get().upload(uri)
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
                trySend(UploadState.MediaUploaded(imageUrl ?: ""))
                close()
            }

            override fun onError(requestId: String?, error: ErrorInfo?) {
                trySend(UploadState.MediaUploadError("Upload error: ${error?.description}"))
                close()
            }

            override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
        }).dispatch()

    awaitClose { /* cleanup if needed */ }
}
