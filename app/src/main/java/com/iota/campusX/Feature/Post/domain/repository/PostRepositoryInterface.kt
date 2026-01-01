package com.iota.campusX.Feature.Post.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostPayload
import com.iota.campusX.Feature.Post.presentation.UploadState
import kotlinx.coroutines.flow.Flow

interface PostRepositoryInterface {

    suspend fun createPost(postType: PostPayload): Flow<UploadState>

    suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit>

     suspend fun getPosts(feedMode: FeedMode,campusId: String?): Flow<PagingData<GetPostDTO>>

    suspend fun fetchSinglePost(postId: String): Result<GetPostDTO>

    suspend fun editPost(postId: String, editedText: String): Result<Unit>

    suspend fun getPostsById(userId: String): Flow<PagingData<GetPostDTO>>

    suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit>

    suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit>


}