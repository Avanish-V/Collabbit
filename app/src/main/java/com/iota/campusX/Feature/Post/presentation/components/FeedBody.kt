package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto
import com.iota.campusX.ui.UIComponents.FeedUI.ExpandableText

@Composable
fun FeedBody(
    caption: String,
    attachment: AttachmentDto?,
    goToFeedViewer: (Int) -> Unit,
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

        if (attachment != null) {
            MediaAttachment(
                attachment = attachment,
                onMediaClick = { index -> goToFeedViewer(index) }
            )
        }
    }
}
