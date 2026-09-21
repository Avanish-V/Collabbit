package com.iota.campusX.Feature.Post.domain.attachment

import android.net.Uri
import com.iota.campusX.Feature.Post.data.remote.request.AttachmentType
import com.iota.campusX.Feature.Post.data.remote.S3Uploader

interface AttachmentHandler {
    fun supports(
        attachment: Attachment
    ): Boolean

    suspend fun process(
        attachment: Attachment
    ): Result<AttachmentDto>
}

class ImageAttachmentHandler(
    private val imageUploader: S3Uploader
) : AttachmentHandler {

    override fun supports(
        attachment: Attachment
    ): Boolean {
        return attachment is ImageAttachment
    }

    override suspend fun process(
        attachment: Attachment
    ): Result<AttachmentDto> {

        attachment as ImageAttachment

        val localUris = attachment.images
        if (localUris.isEmpty()) {
             return Result.success(ImageAttachmentDto(emptyList()))
        }

        val uploadedUrls = imageUploader.uploadImages(localUris.map { Uri.parse(it) })

        // If user provided images but NONE were uploaded successfully, treat as failure
        if (uploadedUrls.isEmpty() && localUris.isNotEmpty()) {
            return Result.failure(Exception("Failed to upload images. Please check your connection."))
        }
        
        // Optional: If SOME failed, you might want to fail the whole post or continue.
        // The user said "if attachment uploading failed then cancel post".
        // Let's be strict: if number of uploaded urls != number of local uris, it's a partial failure.
        if (uploadedUrls.size < localUris.size) {
             return Result.failure(Exception("Some images failed to upload (${uploadedUrls.size}/${localUris.size} succeeded)"))
        }

        return Result.success(
            ImageAttachmentDto(
                images = uploadedUrls,
                widths = attachment.widths,
                heights = attachment.heights,
                aspectRatios = attachment.aspectRatios
            )
        )
    }
}

class VideoAttachmentHandler(
    private val s3Uploader: S3Uploader
) : AttachmentHandler {
    override fun supports(attachment: Attachment): Boolean = attachment is VideoAttachment

    override suspend fun process(attachment: Attachment): Result<AttachmentDto> {
        attachment as VideoAttachment
        val (videoUrl, thumbnailUrl) = s3Uploader.uploadVideoWithThumbnail(
            videoUri = Uri.parse(attachment.videoUri),
            folder = "post_videos"
        )

        if (videoUrl == null) {
            return Result.failure(Exception("Failed to upload video"))
        }

        return Result.success(
            VideoAttachmentDto(
                videoUrl = videoUrl,
                thumbnailUrl = thumbnailUrl,
                width = attachment.width,
                height = attachment.height,
                aspectRatio = attachment.aspectRatio
            )
        )
    }
}

class PollAttachmentHandler : AttachmentHandler {
    override fun supports(attachment: Attachment): Boolean = attachment is PollAttachment
    
    override suspend fun process(attachment: Attachment): Result<AttachmentDto> {
        attachment as PollAttachment
        return Result.success(PollAttachmentDto(options = attachment.options))
    }
}

class TeamFormationAttachmentHandler : AttachmentHandler {
    override fun supports(attachment: Attachment): Boolean = attachment is TeamFormationAttachment
    
    override suspend fun process(attachment: Attachment): Result<AttachmentDto> {
        attachment as TeamFormationAttachment
        return Result.success(
            TeamFormationAttachmentDto(
                teamType = attachment.teamType,
                requiredSkills = attachment.requiredSkills
            )
        )
    }
}

class DocumentAttachmentHandler(
    private val s3Uploader: S3Uploader
) : AttachmentHandler {
    override fun supports(attachment: Attachment): Boolean = attachment is DocumentAttachment

    override suspend fun process(attachment: Attachment): Result<AttachmentDto> {
        attachment as DocumentAttachment
        val (url, thumbnailUrl) = s3Uploader.uploadDocumentWithThumbnail(
            documentUri = Uri.parse(attachment.uri),
            folder = "post_documents"
        )

        if (url == null) {
            return Result.failure(Exception("Failed to upload document"))
        }

        return Result.success(
            DocumentAttachmentDto(
                url = url,
                name = attachment.name,
                size = attachment.size,
                thumbnailUrl = thumbnailUrl,
                pageCount = attachment.pageCount
            )
        )
    }
}

class AttachmentProcessor(
    private val handlers: List<AttachmentHandler>
) {

    suspend fun process(
        attachment: Attachment?
    ): Result<AttachmentDto?> {
        if (attachment == null) return Result.success(null)

        val handler = handlers.firstOrNull {
            it.supports(attachment)
        } ?: throw IllegalArgumentException(
            "No AttachmentHandler found for ${attachment::class.simpleName}"
        )

        return handler.process(attachment)
    }
}