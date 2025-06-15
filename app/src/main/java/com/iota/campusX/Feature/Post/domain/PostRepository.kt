package com.iota.campusX.Feature.Post.domain

import android.net.Uri
import com.iota.campusX.Feature.Post.domain.Models.CreatePostDTO
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.GetRepliesDTO
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.presentation.UploadState
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    suspend fun createPost(
        dto: CreatePostDTO,
        feedMode: FeedMode,
        imageUri: Uri?
    ): Flow<UploadState>

    suspend fun deletePost(postId: String, campusId: String?): Result<Unit>

    suspend fun getPosts(): Result<List<GetPostDTO>>


    suspend fun fetchCampusPosts(feedMode: FeedMode, campusId: String?): Result<List<GetPostDTO>>


    suspend fun editPost(postId: String, editedText: String, campusId: String?): Result<Unit>

    suspend fun getPostsById(userId: String, campusId: String?): Result<List<GetPostDTO>>


    suspend fun createReply(
        replyId: String,
        postId: String,
        content: String,
        creatorId: String,
        visibilityMode: PostVisibilityMode
    ): Result<Unit>

    suspend fun getReplies(postId: String): Result<List<GetRepliesDTO>>


    suspend fun toggleLike(userId: String, postId: String, isLiked: Boolean): Result<Unit>

    suspend fun likeReply(
        creatorId: String,
        postId: String,
        replyId: String,
        isLiked: Boolean
    ): Result<Unit>


    suspend fun deleteReply(postId: String, replyId: String, campusId: String?): Result<Unit>

    suspend fun editReply(
        postId: String,
        replyId: String,
        content: String,
        campusId: String?
    ): Result<Unit>


    suspend fun createPoll(post: CreatePostDTO): Result<Unit>

    suspend fun voteOnPoll(postId: String, optionId: String): Result<Unit>

}