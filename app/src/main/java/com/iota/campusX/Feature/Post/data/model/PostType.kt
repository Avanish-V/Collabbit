package com.iota.campusX.Feature.Post.data.model

import android.net.Uri
import com.google.firebase.firestore.FieldValue
import kotlinx.serialization.Contextual

sealed class PostType {

    abstract val postId: String
    abstract val creatorId: String
    @Contextual
    abstract val createdAt: FieldValue
    abstract val feedMode: FeedMode?
    abstract val campusId: String
    abstract val visibilityMode: VisibilityMode

    abstract val type: Type

    data class MediaPost(
        val image: Uri?,
        val postText: String,
        val mediaType: MediaType,
        override val postId: String,
        override val creatorId: String,
        @Contextual
        override val createdAt: FieldValue,
        override val feedMode: FeedMode,
        override val campusId: String,
        override val visibilityMode: VisibilityMode,
        override val type: Type
    ) : PostType()

    data class PollPost(
        val poll: Poll,
        override val postId: String,
        override val creatorId: String,
        @Contextual
        override val createdAt: FieldValue,
        override val feedMode: FeedMode,
        override val campusId: String,
        override val visibilityMode: VisibilityMode,
        override val type: Type
    ) : PostType()

}

enum class Type{Poll,Media}
enum class MediaType {Image, Video}