package com.iota.campusX.Feature.Post.domain

import android.net.Uri
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.flow.Flow

interface PostRepository {

    fun getPosts(postMode: Boolean): Flow<ResultState<List<PostDTO>>>

    fun getPostsById(userId: String,campusId: String): Flow<ResultState<List<PostDTO>>>

    fun toggleLike(userId: String, postId: String,isLiked: Boolean)

    fun likeReply(userId: String, postId: String,replyId:String,isLiked: Boolean)

    fun getReplies(postId: String): Flow<ResultState<List<GetRepliesDTO>>>

    fun createReply(
        replyId : String,
        postId : String,
        content : String,
        repliedAt : Long,
        creatorId: String,

        ): Flow<ResultState<Boolean>>

    fun createPost(createPostDTO: CreatePostDTO,postMode: Boolean, imageUri: Uri?): Flow<ResultState<UploadResponse>>

    fun deletePost(postId: String): Flow<ResultState<Boolean>>

}