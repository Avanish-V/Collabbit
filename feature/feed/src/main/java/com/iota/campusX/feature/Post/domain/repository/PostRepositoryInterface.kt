package com.iota.campusX.Feature.Post.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.remote.request.CreatePostRequest
import com.iota.campusX.Feature.Post.data.remote.response.Post
import kotlinx.coroutines.flow.Flow

interface PostRepositoryInterface {

    suspend fun createPost(request: CreatePostRequest): Result<Post>

    suspend fun deletePost(postId: String): Result<Unit>

      fun getPosts(): Flow<PagingData<Post>>

    suspend fun fetchSinglePost(postId: String): Result<Post>

    suspend fun editPost(postId: String, editedText: String): Result<Unit>

    suspend fun getPostsById(userId: String): Flow<PagingData<Post>>

    suspend fun toggleLike( postId: String, isLiked: Boolean): Result<Unit>

    suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit>


}