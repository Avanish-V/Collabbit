package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import com.iota.campusX.core.common.extractUrlFromText
import com.iota.campusX.core.common.normalizeUrl
import com.iota.campusX.core.designsystem.components.LinkPreviewCard

@Composable
fun LinkAttachment(
    attachment: String
){
    val uriHandler = LocalUriHandler.current
    val url = extractUrlFromText(attachment)
    url?.let {
        val url = normalizeUrl(it)
        LinkPreviewCard(attachment) {
            uriHandler.openUri(url)
        }
    }
}
