package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.ui.UIComponents.FeedUI.ExpandableText

@Composable
fun FeedBody(
    caption: String,
    attachment: AttachmentDto?,
    goToFeedViewer: () -> Unit,
    onPollSelect: (String) -> Unit,
) {
    val context = LocalContext.current

    Column {
        if (caption.isNotBlank()) {
            ExpandableText(
                text = caption,
                context = context,
            )
        }

        when (attachment) {
            is ImageAttachmentDto -> {
                ImageAttachment(
                    images = attachment.images,
                    onImageClick = goToFeedViewer
                )
            }
            else -> {}
        }
    }
}
