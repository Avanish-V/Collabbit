package com.iota.campusX.Feature.Reply.data.local.dao

import androidx.room.*
import com.iota.campusX.Feature.Reply.data.local.entity.ReplyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReplyDao {
    @Query("SELECT * FROM replies WHERE postId = :postId AND parentReplyId IS NULL ORDER BY createdAt DESC")
    fun getRepliesForPost(postId: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE parentReplyId = :parentId ORDER BY createdAt ASC")
    fun getChildReplies(parentId: String): Flow<List<ReplyEntity>>

    @Query("SELECT * FROM replies WHERE id = :replyId")
    suspend fun getReplyById(replyId: String): ReplyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReplies(replies: List<ReplyEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReply(reply: ReplyEntity)

    @Query("UPDATE replies SET isLiked = :isLiked, likesCount = :likesCount WHERE id = :replyId")
    suspend fun updateLikeState(replyId: String, isLiked: Boolean, likesCount: Int)

    @Query("DELETE FROM replies WHERE id = :replyId")
    suspend fun deleteReply(replyId: String)

    @Query("DELETE FROM replies WHERE postId = :postId")
    suspend fun deleteRepliesForPost(postId: String)
}
