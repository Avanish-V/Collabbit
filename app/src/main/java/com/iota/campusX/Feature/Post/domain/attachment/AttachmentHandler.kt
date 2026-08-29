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
    ): AttachmentDto
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
    ): AttachmentDto {

        attachment as ImageAttachment

        val images = imageUploader.uploadImages(attachment.images.map { Uri.parse(it) })

        return ImageAttachmentDto(
            images = images
        )
    }
}

class AttachmentProcessor(
    private val handlers: List<AttachmentHandler>
) {

    suspend fun process(
        attachment: Attachment?
    ): AttachmentDto? {
        if (attachment == null) return null

        val handler = handlers.firstOrNull {
            it.supports(attachment)
        } ?: throw IllegalArgumentException(
            "No AttachmentHandler found for ${attachment::class.simpleName}"
        )

        return handler.process(attachment)
    }
}