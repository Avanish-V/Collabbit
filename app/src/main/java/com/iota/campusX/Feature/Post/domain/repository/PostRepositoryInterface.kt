package com.iota.campusX.Feature.Post.domain.repository

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Screens.Post.PostType
import kotlinx.coroutines.flow.Flow

interface PostRepositoryInterface {

    suspend fun createPost(postType: PostType): Flow<UploadState>

    suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit>

     fun getPosts(): Flow<PagingData<GetPostDTO>>

    suspend fun fetchSinglePost(postId: String): Result<GetPostDTO>

    suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Flow<PagingData<GetPostDTO>>

    suspend fun editPost(postId: String, editedText: String, campusId: String?,feedMode: FeedMode): Result<Unit>

    suspend fun getPostsById(userId: String): Flow<PagingData<GetPostDTO>>

    suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit>

    suspend fun createPoll(postType: PostType): Result<Unit>

    suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit>


}