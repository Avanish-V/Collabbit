package com.iota.campusX.Feature.Post.data

import SendPushNotification
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.iota.campusX.Feature.Notification.domain.CommentPayload
import com.iota.campusX.Feature.Notification.domain.ContentType
import com.iota.campusX.Feature.Notification.domain.CreateNotificationDTO
import com.iota.campusX.Feature.Notification.domain.LikePayload
import com.iota.campusX.Feature.Notification.domain.NotificationType
import com.iota.campusX.Feature.Notification.domain.toTypedObject
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreateReplyDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.data.model.UserDetail
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.domain.ReplyRepository
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
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
) : ReplyRepository {

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

                val notification = CreateNotificationDTO(
                    notificationId = postId + currentUserId,
                    createdAt = FieldValue.serverTimestamp(),
                    isRead = false,
                    type = NotificationType.COMMENT,
                    payload = CommentPayload(
                        postId = postId,
                        commentId = replyId,
                        actionBy = currentUserId,
                        visibilityMode = visibilityMode,
                        contentType = ContentType.REPLY_POST,
                    ).toTypedObject()
                )

                firestore.collection("Users").document(postCreatorId).collection("Notifications")
                    .add(notification).await()

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

                firestore.collection("Users").document(repliedById).collection("Notifications")
                    .document(notification.notificationId).set(notification)
                    .addOnSuccessListener {
                        sendPushNotification.messageNotification(
                            notificationReceiverId = repliedById,
                            notificationType = "LIKE_REPLY"
                        )
                        cont.resume(Result.success(Unit))
                    }.addOnFailureListener { notifError ->
                        cont.resume(Result.failure(notifError))
                    }
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

                            val postDTO = fetchPostDTO(postId, firestore) ?: return@runCatching null

                            // ✅ fetch the actual reply doc
                            val replyDoc = baseCollection.document(postId)
                                .collection("Replies")
                                .document(replyId)
                                .get()
                                .await()

                            if (!replyDoc.exists()) {
                                Log.w("FetchUserReplies", "Reply not found: $replyId for postId: $postId")
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
                            Log.e("FetchUserReplies", "Error mapping reply: ${e.message}", e)
                            null
                        }
                    }
                }.mapNotNull { it.await() }
            }

            Log.d("FetchUserReplies", "Total valid replies: ${data.size}")
            Result.success(data)
        } catch (e: Exception) {
            Log.e("FetchUserReplies", "Failed: ${e.message}", e)
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


private suspend fun fetchPostDTO(postId: String, firestore: FirebaseFirestore): GetPostDTO? {

    val postDoc = firestore.collection("Posts").document(postId).get().await()
    val postData = postDoc.toObject(CreatePostDTO::class.java) ?: return null

    val postUserDoc = firestore.collection("Users").document(postData.creatorId).get().await()
    val postUserName = postUserDoc.getString("userName") ?: return null
    val postUserImage = postUserDoc.getString("userImage") ?: return null

    return GetPostDTO(
        postId = postData.postId,
        createdAt = postData.createdAt,
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
    )
}