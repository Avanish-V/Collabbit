package com.iota.campusX.Feature.Post.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.iota.campusX.Feature.Post.data.local.entity.PostEntity

@Dao
interface PostDao {

    @Query("""
        SELECT *
        FROM posts
        ORDER BY createdAt DESC
    """)
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE postId = :postId")
    suspend fun getPostById(postId: String): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Update
    suspend fun update(post: PostEntity)

    @Delete
    suspend fun delete(post: PostEntity)

    @Query("DELETE FROM posts WHERE postId = :postId")
    suspend fun deleteById(postId: String)

    @Query("UPDATE posts SET caption = :text WHERE postId = :postId")
    suspend fun updatePostText(postId: String, text: String)

    @Query("UPDATE posts SET isLiked = :isLiked, likeCount = :likeCount WHERE postId = :postId")
    suspend fun updateLikeStatus(postId: String, isLiked: Boolean, likeCount: Int)

    @Query("DELETE FROM posts")
    suspend fun clear()

}