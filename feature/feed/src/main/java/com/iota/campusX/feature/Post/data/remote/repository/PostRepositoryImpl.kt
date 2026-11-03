package com.iota.campusX.Feature.Post.data.remote.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.iota.campusX.Feature.Post.data.local.dao.PostDao
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.Post.data.local.entity.PostEntity
import com.iota.campusX.Feature.Post.data.local.mapper.toDomain
import com.iota.campusX.Feature.Post.data.local.mapper.toEntity
import com.iota.campusX.Feature.Post.data.mediator.FeedRemoteMediator
import com.iota.campusX.Feature.Post.data.mediator.PostsPagingSource
import com.iota.campusX.Feature.Post.data.remote.api.PostApi
import com.iota.campusX.Feature.Post.data.remote.mapper.PostRes
import com.iota.campusX.Feature.Post.data.remote.request.CreatePostRequest
import com.iota.campusX.Feature.Post.data.remote.request.toDomain
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable


class PostRemoteDataSource(
    private val httpClient: HttpClient,
    private val postDao: PostDao,
    private val database: CampusDatabase,
    private val api: PostApi
):PostRepositoryInterface {


     override suspend fun createPost(request: CreatePostRequest): Result<Post> {
        return try {
            val response = httpClient.post("feed") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()) {
                val postResponse: PostRes = response.body()
                postDao.insert(postResponse.toEntity())
                Result.success(postResponse.toDomain())
            } else {
                Result.failure(Exception("Failed to create post: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(postId: String): Result<Unit> {

        if (postId.isBlank()) return Result.failure(IllegalArgumentException("Invalid post ID"))

        return try {
            val response: HttpResponse = httpClient.delete("feed/$postId")

            if (response.status.isSuccess()) {
                postDao.deleteById(postId)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete post: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

     @OptIn(ExperimentalPagingApi::class)
     override fun getPosts(): Flow<PagingData<Post>> {

         return Pager(

             config = PagingConfig(
                 pageSize = 10,
                 prefetchDistance = 3,
                 initialLoadSize = 20
             ),
             remoteMediator = FeedRemoteMediator(
                 api,
                 database
             ),
             pagingSourceFactory = {
                 database.postDao().pagingSource()
             }
         ).flow.map { pagingData ->

             pagingData.map {

                 it.toDomain()

             }

         }

     }

    override suspend fun fetchSinglePost(postId: String): Result<Post> {
        return try {

            val cachedPost = postDao.getPostById(postId)

            if (cachedPost != null) {

                return Result.success(cachedPost.toDomain())
            }

            val response: HttpResponse = httpClient.get("feed/postById/$postId")

            if (response.status.isSuccess()) {
                val post: PostRes = response.body()
                Result.success(post.toDomain())
            } else {
                Result.failure(Exception("Failed to get post"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getPostsById(userId: String): Flow<PagingData<Post>> {

        return Pager(
            config = PagingConfig(
                pageSize = 10,
                prefetchDistance = 3,
                initialLoadSize = 20
            ),
            pagingSourceFactory = {
                PostsPagingSource(
                    api = PostApi(client = httpClient),
                    userId = userId,
                )
            }
        ).flow

    }
     
    override suspend fun editPost(postId: String, editedText: String): Result<Unit> {
        return try {

            val response = httpClient.patch("feed/$postId") {
                setBody(EditPostRequest(editedText))
                contentType(ContentType.Application.Json)
            }

            if (response.status.isSuccess()){
                postDao.updatePostText(postId, editedText)
                Result.success(Unit)
            } else {
                Result.failure(Exception("${response.status.value} Something went wrong!"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun toggleLike(
        postId: String,
        isLiked: Boolean
    ): Result<Unit> {

        val cachedPost = postDao.getPostById(postId)

        val originalIsLiked = cachedPost?.isLiked ?: false
        val originalLikeCount = cachedPost?.likeCount ?: 0

        // ─────────────────────────────────────
        // 1. Optimistic update
        // ─────────────────────────────────────

        if (cachedPost != null) {

            val newLikeCount = when {
                isLiked && !originalIsLiked ->
                    originalLikeCount + 1

                !isLiked && originalIsLiked ->
                    maxOf(0, originalLikeCount - 1)

                else ->
                    originalLikeCount
            }

            postDao.updateLikeStatus(
                postId = postId,
                isLiked = isLiked,
                likeCount = newLikeCount
            )
        }

        // ─────────────────────────────────────
        // 2. API request
        // ─────────────────────────────────────

        return try {

            val response = if (isLiked) {

                httpClient.post(
                    "feed/$postId/like"
                )

            } else {

                httpClient.delete(
                    "feed/$postId/like"
                )
            }

            // ─────────────────────────────────
            // 3. Success
            // ─────────────────────────────────

            if (response.status.isSuccess()) {

                Result.success(Unit)

            } else {

                // API failed → rollback
                rollbackLike(
                    postId = postId,
                    cachedPost = cachedPost,
                    originalIsLiked = originalIsLiked,
                    originalLikeCount = originalLikeCount
                )

                Result.failure(
                    Exception(
                        "${response.status.value} Something went wrong!"
                    )
                )
            }

        } catch (e: Exception) {

            // Network/unknown error → rollback
            rollbackLike(
                postId = postId,
                cachedPost = cachedPost,
                originalIsLiked = originalIsLiked,
                originalLikeCount = originalLikeCount
            )

            Result.failure(
                Exception(
                    e.localizedMessage
                        ?: "Something went wrong while updating like."
                )
            )
        }
    }

    override suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit> {
        return try {
            val response = httpClient.post("feed/poll/vote/$postId/$optionId")

            if (response.status.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to vote: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error voting on poll: ${e.message}"))
        }
    }

    private suspend fun rollbackLike(
        postId: String,
        cachedPost: PostEntity?,
        originalIsLiked: Boolean,
        originalLikeCount: Int
    ) {
        if (cachedPost != null) {
            postDao.updateLikeStatus(
                postId = postId,
                isLiked = originalIsLiked,
                likeCount = originalLikeCount
            )
        }
    }


}

@Serializable
data class EditPostRequest(
    val text: String
)









