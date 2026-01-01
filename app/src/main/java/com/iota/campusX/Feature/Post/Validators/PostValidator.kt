package com.iota.campusX.Feature.Post.Validators

import com.iota.campusX.Feature.Post.Validators.ValidationResult.*
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.PostPayload

//interface PostValidator<T : PostType> {
//    fun validate(post: T): ValidationResult
//}
//
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val errors: String) : ValidationResult()
}
//
//class MediaPostValidator : PostValidator<PostType.MediaPost> {
//    override fun validate(post: PostType.MediaPost): ValidationResult {
//        val errors = mutableListOf<String>()
//
//        if (post.imageUri == null && post.postText.isBlank()) {
//            errors.add("Post must have either an image or text.")
//        }
//        return if (errors.isEmpty()) ValidationResult.Success
//        else ValidationResult.Error(errors)
//    }
//}
//
//class PollPostValidator : PostValidator<PostType.Poll> {
//    override fun validate(post: PostType.Poll): ValidationResult {
//        val errors = mutableListOf<String>()
//
//        if (post.question.isBlank()) errors.add("Question cannot be empty.")
//        if (post.options.size < 2) errors.add("Poll must have at least 2 options.")
//
//        return if (errors.isEmpty()) ValidationResult.Success
//        else ValidationResult.Error(errors)
//    }
//}


class PostValidator {
    fun validate(post: PostPayload): ValidationResult {
        // Common rules
        if (post.feedMode == FeedMode.CAMPUS && post.campusId.isBlank()) {
            return ValidationResult.Error("Campus ID required for Campus feed")
        }

        return when (post) {

            is PostPayload.MediaPost -> {
                if (post.image == null && post.postText.isBlank()) {
                    Error("Media post must contain text or an image")
                }
                else if (post.postText.length > 1000) {
                    Error("Post text cannot exceed 1000 characters")
                }
                else if (post.creatorId.isEmpty()){
                    Error("Creator is missing.")
                }
                else ValidationResult.Success
            }
            is PostPayload.PollPost -> {
                if (post.poll.question.isEmpty()) {
                    Error("Poll question cannot be empty")
                } else if (post.poll.options.count() < 2) {
                    Error("Poll must have at least 2 options")
                }else if (post.poll.options.any { it.text.isEmpty() }) {
                    Error("Poll options cannot be empty")
                }
                else if (post.creatorId.isEmpty()){
                    Error("Creator is missing.")
                }else {
                    ValidationResult.Success
                }
            }

            is PostPayload.TextPost -> {

                 if (post.postText.length > 1000) {
                    Error("Post text cannot exceed 1000 characters")
                }
                else if (post.creatorId.isEmpty()){
                    Error("Creator is missing.")
                }
                else ValidationResult.Success
            }
        }
    }
}
