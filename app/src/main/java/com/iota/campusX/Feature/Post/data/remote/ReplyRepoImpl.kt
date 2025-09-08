package com.iota.campusX.Feature.Post.data.remote

import SendPushNotification
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.data.CommentContent
import com.iota.campusX.Feature.Notification.data.CreateNotification
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreateReplyDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Utils.anonymousImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReplyRepoImpl(
    private val sendPushNotification: SendPushNotification,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ReplyRepositoryInterface {

    override suspend fun createReply(
        replyId: String,
        postId: String,
        content: String,
        postCreatorId: String,
        visibilityMode: VisibilityMode
    ): Result<Unit> {
        return try {

            val currentUserId = auth.currentUser?.uid ?: return Result.failure(
                exception = IllegalStateException("User not logged in")
            )

            val replyPayload = mapOf(
                "replyId" to replyId,
                "postId" to postId,
                "repliedBy" to currentUserId,
                "content" to content,
                "repliedAt" to System.currentTimeMillis(),
                "visibility" to visibilityMode.name,
            )

            firestore.collection("Posts")
                .document(postId)
                .collection("Replies")
                .document(replyId)
                .set(replyPayload)
                .await()



            firestore.collection("Users").document(currentUserId).collection("Replies")
                .document(replyId).set(
                    mapOf(
                        "replyId" to replyId,
                        "postId" to postId,
                        "visibility" to visibilityMode,
                    )
                ).await()



            if (postCreatorId != currentUserId) {

                notificationRepository.createNotification(
                    createNotification = CreateNotification.CommentNotification(
                        notificationId = postId,
                        type = NotificationType.COMMENT,
                        createdAt = FieldValue.serverTimestamp(),
                        read = false,
                        postId = postId,
                        visibilityMode = visibilityMode,
                        commentContent = listOf(
                            CommentContent(
                                visibilityMode = visibilityMode,
                                repliedBy = auth.currentUser?.uid ?: "",
                                replyId = replyId
                            )
                        )
                    ),
                    creatorId = postCreatorId
                )


                sendPushNotification.messageNotification(
                    notificationReceiverId = postCreatorId, notificationType = "COMMENTED"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReplies(
        postId: String, campusId: String?, feedMode: FeedMode
    ): Result<List<GetRepliesDTO>> {
        return try {
            val baseCollection = firestore.collection("Posts")
            val repliesSnapshot = baseCollection.document(postId).collection("Replies").get().await()
            val currentUserId = auth.currentUser?.uid

            val replies = coroutineScope {
                repliesSnapshot.documents.map { document ->
                    async {
                        mapReplyDocumentToDTO(
                            replyDoc = document,
                            baseCollection = baseCollection,
                            currentUserId = currentUserId,
                            firestore = firestore
                        )
                    }
                }.mapNotNull { it.await() }
            }

            Result.success(replies.sortedByDescending { it.repliedAt })
        } catch (e: Exception) {
            Log.e("getReplies", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }


    override suspend fun likeReply(
        repliedById: String,
        postId: String,
        replyId: String,
        isLiked: Boolean,
    ): Result<Unit> = suspendCoroutine { cont ->

        //val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
        val baseCollection = firestore.collection("Posts")

        val likeDocRef = baseCollection.document(postId).collection("Replies").document(replyId)
            .collection("Likes").document(replyId)

        val updateData = mapOf(
            "likes" to if (isLiked) FieldValue.arrayRemove(auth.currentUser?.uid)
            else FieldValue.arrayUnion(auth.currentUser?.uid)
        )

        likeDocRef.set(updateData, SetOptions.merge()).addOnSuccessListener {
            // Skip notification if the user liked their own reply
            if (repliedById == auth.currentUser?.uid) {
                cont.resume(Result.success(Unit))
                return@addOnSuccessListener
            }

            if (repliedById != auth.currentUser?.uid) {

                // Notification

            } else {
                cont.resume(Result.success(Unit))
            }
        }.addOnFailureListener { e ->
            cont.resume(Result.failure(e))
        }
    }

    override suspend fun deleteReply(postId: String, replyId: String?): Result<Unit> {
        return try {

            Log.d("DeleteReply", "postId: $postId, replyId: $replyId")

            if (replyId.isNullOrEmpty()) {
                return Result.failure(Exception("Invalid post ID"))
            }

            //val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
            val baseCollection = firestore.collection("Posts")

            val task = baseCollection.document(postId).collection("Replies").document(replyId).delete()

            // Suspend until task completes
            task.await()

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun editReply(postId: String, replyId: String?, content: String,): Result<Unit> {
        return try {

            if (replyId.isNullOrEmpty()) {
                return Result.failure(IllegalArgumentException("Invalid reply ID"))
            }

            // val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val baseCollection = firestore.collection("Posts")


            val update = mapOf(
                "content" to content, "isEdited" to true
            )

            baseCollection.document(postId).collection("Replies").document(replyId).update(update)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchUserReplies(userId: String): Result<List<UserReplyDTO>> {
        return try {

            val repliesSnapshot = firestore.collection("Users")
                .document(userId)
                .collection("Replies")
                .get()
                .await()

            val baseCollection = firestore.collection("Posts")
            val currentUserId = auth.currentUser?.uid

            val data = coroutineScope {

                repliesSnapshot.map { snapshot ->
                    async {
                        runCatching {

                            val postId = snapshot.getString("postId") ?: return@runCatching null
                            val replyId = snapshot.getString("replyId") ?: return@runCatching null

                            val postDTO = fetchPostDTO(postId, firestore,auth) ?: return@runCatching null

                            // ✅ fetch the actual reply doc
                            val replyDoc = baseCollection.document(postId)
                                .collection("Replies")
                                .document(replyId)
                                .get()
                                .await()

                            if (!replyDoc.exists()) {
                                return@runCatching null
                            }

                            val replyDTO = mapReplyDocumentToDTO(
                                replyDoc = replyDoc,
                                baseCollection = baseCollection,
                                currentUserId = currentUserId,   // ✅ correct user
                                firestore = firestore
                            ) ?: return@runCatching null

                            UserReplyDTO(post = postDTO, reply = replyDTO)
                        }.getOrElse { e ->
                            null
                        }
                    }
                }.mapNotNull { it.await() }
            }

            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private suspend fun mapReplyDocumentToDTO(
    replyDoc: DocumentSnapshot,
    baseCollection: CollectionReference,
    currentUserId: String?,
    firestore: FirebaseFirestore
): GetRepliesDTO? {

    val reply = replyDoc.toObject(CreateReplyDTO::class.java) ?: return null

    val userDeferred = coroutineScope {
        async {
            firestore.collection("Users").document(reply.repliedBy).get().await()
                .toObject(BaseProfileDTO::class.java)
        }
    }

    val likesDeferred = coroutineScope {
        async {
            baseCollection.document(reply.postId).collection("Replies")
                .document(reply.replyId).collection("Likes")
                .document(reply.replyId).get().await().get("likes") as? List<String> ?: emptyList()
        }
    }

    val user = userDeferred.await()
    val likes = likesDeferred.await()

    val isLiked = currentUserId != null && likes.contains(currentUserId)
    val isCurrentUser = user?.id == currentUserId

    val (userName, userImage) = when (reply.visibility) {
        VisibilityMode.USER -> user?.userName to user?.userImage
        else -> "Anonymous" to anonymousImage
    }

    return GetRepliesDTO(
        postId = reply.postId,
        replyId = reply.replyId,
        creatorDetail = CreatorDetail(
            isCurrentUser = isCurrentUser,
            isVerified = user?.metaData?.verified ?: false,
            isPremium = user?.metaData?.premium ?: false,
            profile = UserBasicDetail(
                id = user?.id ?: "",
                userName = userName ?: "",
                userImage = userImage ?: "",
                userBio = user?.userBio ?: "",
            )
        ),
        edited = reply.isEdited,
        content = reply.content,
        repliedAt = reply.repliedAt,
        visibility = reply.visibility,
        actions = PostActions(
            isLiked = isLiked,
            likesCount = likes.size,
            replyCount = 0,
            replies = emptyList()
        )
    )
}


suspend fun fetchPostDTO(
    postId: String,
    firestore: FirebaseFirestore,
    firebaseAuth: FirebaseAuth
): GetPostDTO? = coroutineScope {
    try {
        // Launch post fetch
        val postDeferred = async {
            firestore.collection("Posts").document(postId).get().await()
        }

        // Wait for post before fetching user (since we need creatorId)
        val postDoc = postDeferred.await()
        if (!postDoc.exists()) return@coroutineScope null

        val postData = postDoc.toObject(CreatePostDTO::class.java) ?: return@coroutineScope null

        // Now fetch user in parallel (as soon as we know creatorId)
        val userDeferred = async {
            firestore.collection("Users").document(postData.creatorId).get().await()
        }

        val isFollowDeferred = async {
            firestore.collection("Users")
                .document(postDeferred.await().getString("creatorId").orEmpty())
                .collection("Followers")
                .document(firebaseAuth.currentUser?.uid ?: "")
                .get()
                .await()
                .exists()
        }

        val postUserDoc = userDeferred.await()
        if (!postUserDoc.exists()) return@coroutineScope null

        val postUserName = postUserDoc.getString("userName").orEmpty()
        val postUserImage = postUserDoc.getString("userImage").orEmpty()
        val isVerified = postUserDoc.getBoolean("verified") ?: false

        GetPostDTO(
            postId = postData.postId,
            createdAt = postData.createdAt,
            creatorDetail = CreatorDetail(
                isCurrentUser = firebaseAuth.currentUser?.uid == postData.creatorId,
                isVerified = isVerified,
                isFollow = isFollowDeferred.await(),
                isPremium = false,
                profile = UserBasicDetail(
                    id = postData.creatorId,
                    userName = postUserName,
                    userImage = postUserImage
                )
            ),
            feedMode = postData.feedMode,
            reference = null,
            visibilityMode = postData.visibilityMode,
            campusId = postData.campusId,
            postContent = PostContent(
                postText = postData.postText,
                postImage = postData.image,
                poll = postData.poll
            ),
            type = postData.type,
            mediaType = postData.mediaType
        )
    } catch (e: Exception) {
        null
    }
}