package com.iota.campusX.ui.UIComponents.FeedUI

import android.util.Patterns
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.Savers.LinkPreviewViewModel
import com.iota.campusX.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun LinkPreviewCard(
    url: String,
    linkPreviewViewModel: LinkPreviewViewModel = koinViewModel(),
    onClick: () -> Unit
) {
    val normalizedUrl = remember(url) { normalizeUrl(url) }
    val previews = linkPreviewViewModel.previews
    val meta = previews[normalizedUrl]

    LaunchedEffect(normalizedUrl) {
        if (normalizedUrl.isNotBlank()) {
            linkPreviewViewModel.loadPreview(normalizedUrl)
        }
    }

    meta?.let { data ->
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(
                    onClick = { onClick() },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            AsyncImage(
                model = data.imageUrl ?: R.drawable.landscape_placeholder_svgrepo_com,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = data.title ?: "",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                data.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


fun extractUrlFromText(text: String): String? {
    val matcher = Patterns.WEB_URL.matcher(text)
    return if (matcher.find()) matcher.group() else null
}
fun normalizeUrl(url: String): String {
    return if (url.startsWith("http://") || url.startsWith("https://")) {
        url
    } else {
        "https://$url"
    }
}