package com.iota.campusX.Feature.Post.data

import SendPushNotification
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.domain.CommentPayload
import com.iota.campusX.Feature.Notification.domain.ContentType
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.LikePayload
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.domain.toTypedObject
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.CreateReplyDTO
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.Models.UserReplyDTO
import com.iota.campusX.Feature.Post.domain.ReplyRepository
import com.iota.campusX.Utils.anonymousImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ReplyRepoImpl(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : ReplyRepository{

    override suspend fun createReply(replyId: String, postId: String, content: String, postCreatorId: String, visibilityMode: PostVisibilityMode, mode: FeedMode,campusId: String?): Result<Unit> {
        return try {
            val currentUserId = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("User not logged in"))

            val replyPayload = mapOf(
                "replyId" to replyId,
                "postId" to postId,
                "repliedBy" to currentUserId,
                "content" to content,
                "repliedAt" to System.currentTimeMillis(),
                "visibility" to visibilityMode.name,
                "mode" to mode,

                )

//            val baseCollection = getBaseCollection(
//                feedMode = mode,
//                campusId = campusId,
//                firestore = firestore
//            )

            val baseCollection = firestore.collection("Posts")

            baseCollection
                .document(postId)
                .collection("Replies")
                .document(replyId)
                .set(replyPayload)
                .await()

            firestore.collection("Users")
                .document(currentUserId)
                .collection("Replies")
                .document(replyId)
                .set(
                    mapOf(
                        "replyId" to replyId,
                        "postId" to postId,
                        "visibility" to visibilityMode,
                        "mode" to mode
                    )
                ).await()

            if (postCreatorId != currentUserId) {
                val notification = CreateNotificationDTO(
                    notificationId = postId + currentUserId,
                    createdAt = FieldValue.serverTimestamp(),
                    isRead = false,
                    type = NotificationType.COMMENT,
                    feedMode = mode,
                    payload = CommentPayload(
                        postId = postId,
                        commentId = replyId,
                        actionBy = currentUserId,
                        visibilityMode = visibilityMode,
                        contentType = ContentType.REPLY_POST,
                    ).toTypedObject()
                )

                firestore.collection("Users")
                    .document(postCreatorId)
                    .collection("Notifications")
                    .add(notification)
                    .await()

                sendPushNotification.messageNotification(
                    notificationReceiverId = postCreatorId,
                    notificationType = "COMMENTED"
                )
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReplies(postId: String, campusId: String?, feedMode: FeedMode): Result<List<GetRepliesDTO>> {
        return try {

           // val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
            val baseCollection = firestore.collection("Posts")

            val repliesSnapshot = baseCollection
                .document(postId)
                .collection("Replies")
                .get()
                .await()

            val replies = coroutineScope {
                repliesSnapshot.documents.map { document ->
                    async {

                        val reply = document.toObject(CreateReplyDTO::class.java) ?: return@async null

                        val userDeferred = async {
                            firestore.collection("Users")
                                .document(reply.repliedBy)
                                .get()
                                .await()
                                .toObject(UserDetail::class.java)
                        }

                        val likesDeferred = async {
                            baseCollection

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
                        val isCurrentUser = user?.id == currentUserId

                        val (userName, userImage) = when (reply.visibility) {
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
                }.mapNotNull { it.await() }
            }

            Log.d("PostRepoImpl", "getReplies: ${replies.size}")
            Result.success(replies.sortedByDescending { it.repliedAt })


        } catch (e: Exception) {
            Log.e("getReplies", "Error: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun likeReply(repliedById: String, postId: String, replyId: String, isLiked: Boolean, campusId: String?, feedMode: FeedMode): Result<Unit> = suspendCoroutine { cont ->

        //val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
        val baseCollection = firestore.collection("Posts")

        val likeDocRef = baseCollection
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
                if (repliedById == auth.currentUser?.uid) {
                    cont.resume(Result.success(Unit))
                    return@addOnSuccessListener
                }

                if (!isLiked) {
                    val notification = CreateNotificationDTO(
                        notificationId = replyId + (auth.currentUser?.uid ?: ""),
                        createdAt = FieldValue.serverTimestamp(),
                        isRead = false,
                        type = NotificationType.LIKE,
                        payload = LikePayload(
                            postId = postId,
                            actionBy = repliedById,
                            contentType = ContentType.LIKE_REPLY
                        ).toTypedObject()

                    )

                    firestore.collection("Users").document(repliedById)
                        .collection("Notifications")
                        .document(notification.notificationId)
                        .set(notification)
                        .addOnSuccessListener {
                            sendPushNotification.messageNotification(
                                notificationReceiverId = repliedById,
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

            //val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)
            val baseCollection = firestore.collection("Posts")

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

           // val baseCollection = getBaseCollection(feedMode = feedMode, campusId = campusId, firestore = firestore)

            val baseCollection = firestore.collection("Posts")


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

    override suspend fun fetchUserReplies(userId: String): Result<List<UserReplyDTO>> {
        return try {
            val replies = firestore.collection("Users")
                .document(userId)
                .collection("Replies")
                .get()
                .await()


            val data = coroutineScope {
                replies.map { snapshot ->
                    async {
                        try {
                            val postId = snapshot.get("postId") as? String
                            val replyId = snapshot.get("replyId") as? String

                            if (postId == null || replyId == null) {
                                Log.w("FetchUserReplies", "Missing postId or replyId in snapshot: ${snapshot.id}")
                                return@async null
                            }

                            // Fetch Post
                            val postDoc = firestore.collection("Posts")
                                .document(postId)
                                .get()
                                .await()

                            if (!postDoc.exists()) {
                                Log.w("FetchUserReplies", "Post not found for postId: $postId")
                                return@async null
                            }

                            val postData = postDoc.toObject(CreatePostDTO::class.java)
                            if (postData == null) {
                                Log.w("FetchUserReplies", "Failed to parse post data for postId: $postId")
                                return@async null
                            }

                            // Fetch Reply
                            val replyDoc = firestore.collection("Posts")
                                .document(postId)
                                .collection("Replies")
                                .document(replyId)
                                .get()
                                .await()

                            if (!replyDoc.exists()) {
                                Log.w("FetchUserReplies", "Reply not found: $replyId for postId: $postId")
                                return@async null
                            }

                            val replyData = replyDoc.toObject(CreateReplyDTO::class.java)
                            if (replyData == null) {
                                Log.w("FetchUserReplies", "Failed to parse reply data for replyId: $replyId")
                                return@async null
                            }

                            // Fetch Post Creator
                            val postUserDoc = firestore.collection("Users")
                                .document(postData.creatorId)
                                .get()
                                .await()

                            val postUserName = postUserDoc.getString("userName")
                            val postUserImage = postUserDoc.getString("userImage")

                            if (postUserName == null || postUserImage == null) {
                                Log.w("FetchUserReplies", "Missing post user data for userId: ${postData.creatorId}")
                                return@async null
                            }

                            // Fetch Reply Creator
                            val replyUserId = replyData.repliedBy.toString()
                            if (replyUserId.isBlank()) {
                                Log.w("FetchUserReplies", "Missing reply user ID in reply: $replyId")
                                return@async null
                            }

                            val replyUserDoc = firestore.collection("Users")
                                .document(replyUserId)
                                .get()
                                .await()

                            val replyUserName = replyUserDoc.getString("userName")
                            val replyUserImage = replyUserDoc.getString("userImage")

                            if (replyUserName == null || replyUserImage == null) {
                                Log.w("FetchUserReplies", "Missing reply user data for userId: $replyUserId")
                                return@async null
                            }

                            // Construct DTO
                            Log.d("FetchUserReplies", "Successfully loaded reply $replyId for post $postId")

                            UserReplyDTO(
                                post = GetPostDTO(
                                    postId = postData.postId,
                                    createdAt = postData.createdAt.toDate().time,
                                    creatorDetail = CreatorDetail(
                                        isCurrentUser = false,
                                        isVerified = false,
                                        isPremium = false,
                                        profile = UserDetail(
                                            userName = postUserName,
                                            userImage = postUserImage
                                        )
                                    ),
                                    feedMode = postData.feedMode,
                                    reference = null,
                                    visibilityMode = postData.visibilityMode,
                                    campusId = postData.campusId,
                                    postContent = postData.postContent,
                                ),
                                reply = GetRepliesDTO(
                                    postId = replyData.postId,
                                    replyId = replyData.replyId,
                                    edited = replyData.isEdited,
                                    creatorDetail = CreatorDetail(
                                        isCurrentUser = false,
                                        isVerified = false,
                                        isPremium = false,
                                        profile = UserDetail(
                                            userName = replyUserName,
                                            userImage = replyUserImage
                                        )
                                    ),
                                    content = replyData.content,
                                    visibility = replyData.visibility,
                                    feedMode = replyData.feedMode,
                                    repliedAt = replyData.repliedAt
                                )
                            )
                        } catch (e: Exception) {
                            Log.e("FetchUserReplies", "Exception in async task: ${e.message}", e)
                            null
                        }
                    }
                }.mapNotNull { it.await() }
            }

            Log.d("FetchUserReplies", "Total valid replies: ${data.size}")

            Result.success(data)
        } catch (e: Exception) {
            Log.e("FetchUserReplies", "Failed to fetch user replies: ${e.message}", e)
            Result.failure(e)
        }
    }

}