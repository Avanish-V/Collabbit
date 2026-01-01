 package com.iota.campusX.Feature.Post.data.remote

import SendPushNotification
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Post.Validators.PostValidator
import com.iota.campusX.Feature.Post.Validators.ValidationResult
import com.iota.campusX.Feature.Post.data.mapper.PostsPagingSource
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.PostPayload
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.data.model.Vote
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Koin.END_POINT
import com.iota.campusX.Utils.anonymousImage
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


 @Serializable
 data class CreatePostRequest(
     val postType: Type,
     val campusId: String? = null,
     val visibility: VisibilityMode = VisibilityMode.USER,
     val feedMode: FeedMode = FeedMode.OPEN,

     // text
     val text: String? = null,

     // media
     val mediaUrl: List<String> = emptyList(),
     val mediaType: MediaType? = null,

     // poll
     val question: String? = null,
     val options: List<String>? = null
 )

 fun uriToFile(context: Context, uri: Uri): File {
     val inputStream = context.contentResolver.openInputStream(uri)!!
     val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
     file.outputStream().use { output ->
         inputStream.copyTo(output)
     }
     return file
 }

 class PostRemoteDataSource(
    private val sendPushNotification: SendPushNotification,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val validator: PostValidator,
    private val httpClient: HttpClient,
    private val s3Uploader: S3Uploader


):PostRepositoryInterface {


    override suspend fun createPost(postType: PostPayload): Flow<UploadState> = flow {

        // 1. Validate
        when (val validation = validator.validate(postType)) {
            is ValidationResult.Error -> {
                emit(UploadState.Error(validation.errors))
                return@flow
            }
            else -> {}
        }

        // 2. Handle Media Upload if needed
        val mediaUrl = if (postType is PostPayload.MediaPost && postType.image != null) {
            val images =  s3Uploader.uploadImages(postType.image)
            images
        } else emptyList()

        // 3. Save to Firestore
        emit(UploadState.Loading)

        val tokenResult = auth.currentUser?.getIdToken(true)?.await()

        val token = tokenResult?.token ?: run {
            emit(UploadState.Error("Failed to upload."))
        }

        val data = when(val value = postType){

            is PostPayload.TextPost -> {
                CreatePostRequest(
                    postType = Type.TEXT,
                    campusId = value.campusId,
                    visibility = value.visibilityMode,
                    text = value.postText,
                    feedMode = value.feedMode

                )
            }

            is PostPayload.MediaPost -> {
                CreatePostRequest(
                    postType = value.type,
                    campusId = value.campusId,
                    visibility = value.visibilityMode,
                    text = value.postText,
                    mediaUrl = mediaUrl,
                    mediaType = value.mediaType,
                    feedMode = value.feedMode
                )

            }
            is PostPayload.PollPost -> {
                CreatePostRequest(
                    postType = value.type,
                    campusId = value.campusId,
                    visibility = value.visibilityMode,
                    text = value.poll.question,
                    options = value.poll.options.map { it.text },
                    question = value.poll.question,
                    feedMode = value.feedMode

                )
            }
        }
        val postData = Json.encodeToString(data)

        val response = httpClient.post("$END_POINT/posts") {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer $token")
            setBody(postData)
        }

        val postResponse = Json{ ignoreUnknownKeys = true }.decodeFromString<PostResponse>(response.bodyAsText())

        Log.d("POST_CREATED", "createPost: ${response.bodyAsText()}")

        if (response.status.value in 200..299){
            Log.d("POST_CREATED", "createPost: ${response.bodyAsText()}")
            emit(UploadState.Success(postResponse))
        }
        else{
            Log.d("POST_CREATED", "createPost: ${response}")
            emit(UploadState.Error("${response.status.value} Something went wrong!"))
        }

    }

    override suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit> {

        if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))


        return try {

            val token = try {
                auth.currentUser?.getIdToken(true)?.await()?.token
                    ?: throw IllegalStateException("Failed to obtain auth token")
            } catch (e: Exception) {
                Log.e("PostApi", "Token error: ${e.message}")
            }

            return try {

                val response: HttpResponse = httpClient.delete("$END_POINT/posts/$postId") {
                    contentType(ContentType.Application.Json)
                    header("Authorization", "Bearer $token")
                }

                if (response.status != HttpStatusCode.OK) {
                    return Result.failure(Exception("Failed to get posts"))
                }


                if (response.status.value == 200){
                    Result.success(Unit)
                }
                else{
                    Result.failure(Exception("Failed to get posts"))
                }

            } catch (e: Exception) {
                Log.e("PostApi", "Exception fetching posts: ${e.message}")
                Result.failure(e)
            }


        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "Something went wrong!"))
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override suspend fun getPosts(feedMode: FeedMode,campusId: String?): Flow<PagingData<GetPostDTO>> {

      return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                PostsPagingSource(
                    api = PostApi(client = httpClient, auth = auth),
                    feedMode = feedMode,
                    campusId = campusId
                )
            }
        ).flow
    }

    override suspend fun fetchSinglePost(postId: String): Result<GetPostDTO> {


        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("PostApi", "Token error: ${e.message}")
        }

        return try {

            val response: HttpResponse = httpClient.get("$END_POINT/posts/postById/$postId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }

            if (response.status != HttpStatusCode.OK) {
                Log.e("PostApi", "Failed to get posts: ${response.status}")
               return Result.failure(Exception("Failed to get posts"))
            }

            // ✅ Either do this (Option 1)
            // val postResponse: List<PostResponse> = response.body()

            // ✅ Or this (Option 2)
            val post: PostResponse = Json{
                ignoreUnknownKeys = true
            }.decodeFromString(response.bodyAsText())

            Log.d("PostApi", "Posts fetched successfully: $postId")
            val data =   GetPostDTO(
                postId = post.postId.toString(),
                creatorDetail = CreatorDetail(
                    profile = UserBasicDetail(
                        id = post.authorDetails?.authorId.orEmpty(),
                        name = post.authorDetails?.authorName.orEmpty(),
                        tagline = post.authorDetails?.authorTagline.orEmpty(),
                        image = post.authorDetails?.authorImage.orEmpty()
                    ),
                    isCurrentUser = post.authorDetails?.isCurrentUser ?:false

                ),
                createdAt = post.createdAt,
                visibilityMode = post.visibility,
                type = post.postType,
                feedMode = post.feedMode,
                postContent = PostContent(
                    postText = post.text,
                    postImage = post.mediaPost
                ),
                postActions = PostActions(
                    likesCount = post.likes,
                    isLiked = post.isLiked,
                    replyCount = post.comments
                ),
            )

            if (response.status.value == 200){
                Result.success(data)
            }
            else{
                Result.failure(Exception("Failed to get posts"))
            }

        } catch (e: Exception) {
            Log.e("PostApi", "Exception fetching posts: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getPostsById(userId: String): Flow<PagingData<GetPostDTO>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 1
            ),
            pagingSourceFactory = {
                PostsPagingSource(
                    api = PostApi(client = httpClient, auth = auth),
                    userId = userId,
                )
            }
        ).flow

    }


     @Serializable
     data class EditPostRequest(
         val text: String
     )

    override suspend fun editPost(postId: String, editedText: String): Result<Unit> {
        return try {

            Log.d("POST_EDITED", "editPost: $postId $editedText")
            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
               return Result.failure(Exception("Failed to upload."))
            }

            val postData = Json.encodeToString(EditPostRequest(editedText))

            val response = httpClient.patch("$END_POINT/posts/$postId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(postData)
            }


            if (response.status.value in 200..299){
                Log.d("POST_EDITED", "editPost: ${response.bodyAsText()}")
                Result.success(Unit)
            }
            else{
                Log.d("POST_EDITED", "editPost: ${response}")
                Result.failure(Exception("${response.status.value} Something went wrong!"))
            }

        } catch (e: Exception) {
            Log.d("POST_EDITED", "editPost: ${e.message}")
            Result.failure(
                Exception(
                    e.localizedMessage ?: "Something went wrong while editing the post."
                )
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit>{

        return try {

            val tokenResult = auth.currentUser?.getIdToken(true)?.await()

            val token = tokenResult?.token ?: run {
                return Result.failure(Exception("Failed to upload."))
            }

            val response = httpClient.post("$END_POINT/posts/like/$postId") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
            }

            if (response.status.value in 200..299){
                Result.success(Unit)
            }
            else{
                Result.failure(Exception("${response.status.value} Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(
                Exception(
                    e.localizedMessage ?: "Something went wrong while editing the post."
                )
            )
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

fun visibilityMode(visibilityMode: VisibilityMode, userName: String, userImage: String?): Pair<String, String> {
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

 suspend fun createPostWithImagesAsync(
     text: String,
     files: List<File>,
     backendBaseUrl: String
 ) {
     val client = HttpClient(CIO)

     // 1️⃣ Request presigned URLs for all files
     val fileNames = files.map { it.name }
     val presignResponse = client.post("$backendBaseUrl/api/posts/presign") {
         contentType(ContentType.Application.Json)
         setBody(fileNames)
     }

     val presignedMap: Map<String, String> = kotlinx.serialization.json.Json.decodeFromString(
         presignResponse.bodyAsText()
     )

     // 2️⃣ Upload all files asynchronously in parallel
     val uploadedUrls = uploadMultipleFilesAsync(files, presignedMap)

     // 3️⃣ Create post with uploaded image URLs
     val createPostRequest = """{"text":"$text","imageUrls":$uploadedUrls}"""
     client.post("$backendBaseUrl/api/posts/create") {
         contentType(ContentType.Application.Json)
         setBody(createPostRequest)
     }

     client.close()
 }


 suspend fun uploadMultipleFilesAsync(
     files: List<File>,
     presignedMap: Map<String, String>
 ): List<String> = coroutineScope {
     files.map { file ->
         async {
             val presignedUrl = presignedMap[file.name] ?: return@async null
             val success = uploadFileToS3(file, presignedUrl)
             if (success) presignedUrl.split("?")[0] else null
         }
     }.awaitAll().filterNotNull()
 }


 suspend fun uploadFileToS3(file: File, presignedUrl: String): Boolean {
     val client = HttpClient(CIO)
     val response: HttpResponse = client.put(presignedUrl) {
         setBody(file.readBytes())
         header(HttpHeaders.ContentType, ContentType.Image.Any)
     }
     client.close()
     return response.status.isSuccess()
 }
