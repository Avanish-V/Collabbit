package com.iota.campusX.Feature.Post.data.remote

import SendPushNotification
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreateReplyDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.GetRepliesDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.ReplyRequest
import com.iota.campusX.Feature.Post.data.model.ReplyResponse
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.UserReplyDTO
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.BaseProfileDTO
import com.iota.campusX.Koin.END_POINT
import com.iota.campusX.Utils.anonymousImage
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ReplyRepoImpl(
    private val sendPushNotification: SendPushNotification,
    private val notificationRepository: NotificationRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val httpClient: HttpClient,
    private val s3Uploader: S3Uploader
) : ReplyRepositoryInterface {

    override suspend fun createReply(
        replyRequest: ReplyRequest,
        uploadImage: Uri?
    ): Result<ReplyResponse> {
        return try {

            val currentUser = auth.currentUser ?: return Result.failure(Exception("User not signed in"))

            val tokenResult = currentUser.getIdToken(true).await()

            val token = tokenResult?.token ?: return Result.failure(Exception("Failed to get authentication token"))

            var updatedRequest = replyRequest



            if (uploadImage != null) {

                Log.e("IMAGE_UPLOAD", "Uploading image...")

                val imageUrl = s3Uploader.UploadImageToS3(uploadImage)
                    ?: return Result.failure(Exception("Failed to upload image"))

                updatedRequest = replyRequest.copy(
                    mediaUrl = imageUrl
                )
                Log.e("IMAGE_UPLOAD", "Image uploaded successfully: $imageUrl")


//                firebaseStorage.reference.putFile(uploadImage)
//                    .addOnCompleteListener { task ->
//                        if (task.isComplete){
//                            val imageUrl = firebaseStorage.reference.downloadUrl.toString()
//                            postData.replace("imageUrl", imageUrl.toString())
//                        }
//                    }
//                    .addOnFailureListener {
//
//                    }
            }

            val postData = Json.encodeToString(updatedRequest)


            Log.e("IMAGE_UPLOAD", "Uploading reply... $postData")


            val response = httpClient.post("$END_POINT/reply") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(postData)
            }

            val responseBody = response.bodyAsText()
            val statusCode = response.status.value

            return when (statusCode) {
                in 200..299 -> {
                    if (responseBody.isBlank()) {
                        Result.failure(Exception("Server returned empty response"))
                    } else {
                        try {
                            val postResponse = Json { ignoreUnknownKeys = true }.decodeFromString<ReplyResponse>(responseBody)
                            Result.success(postResponse)
                        } catch (e: Exception) {

                            Result.failure(Exception("Failed to parse server response: ${e.message}"))
                        }
                    }
                }

                401 -> {
                    Result.failure(Exception("Unauthorized: Your session may have expired. Please sign in again."))
                }

                403 -> {
                    Result.failure(Exception("Forbidden: You don't have permission to create replies on this post."))
                }

                404 -> {
                    Result.failure(Exception("Post not found or endpoint doesn't exist."))
                }

                500 -> {
                    Result.failure(Exception("Server error. Please try again later."))
                }

                else -> {
                    val errorMsg = responseBody.ifBlank { "Unknown error" }
                    Result.failure(Exception("Error $statusCode: $errorMsg"))
                }
            }

        } catch (e: Exception) {
            Result.failure(Exception("Network error: ${e.message ?: "Unknown error"}"))
        }
    }

    override suspend fun getReplies(
        postId: String,
        campusId: String?,
        feedMode: FeedMode
    ): Result<List<ReplyResponse>> {
        return try {

            val token = try {
                auth.currentUser?.getIdToken(true)?.await()?.token
                    ?: throw IllegalStateException("Failed to obtain auth token")
            } catch (e: Exception) {
                return Result.failure(e)
            }

            return try {

                val response: HttpResponse =
                    httpClient.get("$END_POINT/reply/$postId") {
                        contentType(ContentType.Application.Json)
                        header("Authorization", "Bearer $token")
                    }

                if (response.status != HttpStatusCode.OK) {
                    return Result.failure(Exception("Failed to get posts"))
                }

                // ✅ Either do this (Option 1)
                // val postResponse: List<PostResponse> = response.body()

                // ✅ Or this (Option 2)
                val replyResponse: List<ReplyResponse> =
                    Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())

                if (response.status.value == 200) {
                    Result.success(replyResponse)
                } else {
                    Result.failure(Exception("Failed to get posts: ${response.status}"))
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
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
    ): Result<Unit> {

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return try {

            val response: HttpResponse =
                httpClient.post("http://192.168.29.180:8080/reply/$replyId/$isLiked") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                }

            if (response.status != HttpStatusCode.OK) {
                return Result.failure(Exception("Failed to get posts"))
            }

            if (response.status.value == 200) {
                Result.success(Unit)
            } else {
                Log.e("LIKE_REPLY", "Error: ${response.status.value}", )
                Result.failure(Exception("Failed to get posts: ${response.status}"))
            }

        } catch (e: Exception) {
            Log.e("LIKE_REPLY", "Error: ${e.message}", e)
            return Result.failure(e)
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

override suspend fun editReply(postId: String, replyId: String?, content: String?): Result<Unit> {
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

        val repliesSnapshot = if (userId == auth.currentUser?.uid) {
            firestore.collection("Users")
                .document(userId)
                .collection("Replies")
                .get()
                .await()
        } else {
            firestore.collection("Users")
                .document(userId)
                .collection("Replies")
                .whereEqualTo("visibility", VisibilityMode.USER)
                .get()
                .await()
        }


        val baseCollection = firestore.collection("Posts")

        val currentUserId = auth.currentUser?.uid

        val data = coroutineScope {

            repliesSnapshot.map { snapshot ->
                async {
                    runCatching {

                        val postId = snapshot.getString("postId") ?: return@runCatching null
                        val replyId = snapshot.getString("replyId") ?: return@runCatching null

                        val postDTO =
                            fetchPostDTO(postId, firestore, auth) ?: return@runCatching null

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
    val isCurrentUser = user?.uid == currentUserId

    val (userName, userImage) = when (reply.visibility) {
        VisibilityMode.USER -> user?.name to user?.image
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
                id = user?.uid ?: "",
                name = userName ?: "",
                image = userImage ?: "",
                tagline = user?.about ?: "",
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

        val visibility = visibilityMode(
            visibilityMode = postData.visibilityMode,
            userImage = postUserImage,
            userName = postUserName
        )

        GetPostDTO(
            postId = postData.postId,
            //createdAt = postData.createdAt,
            creatorDetail = CreatorDetail(
                isCurrentUser = firebaseAuth.currentUser?.uid == postData.creatorId,
                isVerified = isVerified,
                isFollow = isFollowDeferred.await(),
                isPremium = false,
                profile = UserBasicDetail(
                    id = postData.creatorId,
                    name = visibility.first,
                    image = visibility.second
                )
            ),
            feedMode = postData.feedMode,
            reference = null,
            visibilityMode = postData.visibilityMode,
            campusId = postData.campusId,
            postContent = PostContent(
                postText = postData.postText,
                //postImage = postData.image,
                poll = postData.poll
            ),
            type = postData.type,

        )
    } catch (e: Exception) {
        null
    }
}