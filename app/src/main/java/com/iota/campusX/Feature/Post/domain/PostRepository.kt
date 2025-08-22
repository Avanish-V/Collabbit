package com.iota.campusX.Feature.Post.domain

import android.net.Uri
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.presentation.UploadState
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    suspend fun createPost(dto: CreatePostDTO, imageUri: Uri?): Flow<UploadState>

    suspend fun deletePost(postId: String, campusId: String?,feedMode: FeedMode): Result<Unit>

    suspend fun getPosts(): Result<List<GetPostDTO>>

    suspend fun fetchSinglePost(postId: String): Result<GetPostDTO>

    suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Result<List<GetPostDTO>>

    suspend fun editPost(postId: String, editedText: String, campusId: String?,feedMode: FeedMode): Result<Unit>

    suspend fun getPostsById(userId: String): Result<List<GetPostDTO>>

    suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit>

    suspend fun createPoll(post: CreatePostDTO): Result<Unit>

    suspend fun voteOnPoll(postId: String, optionId: String,campusId: String?,feedMode: FeedMode): Result<Unit>





}