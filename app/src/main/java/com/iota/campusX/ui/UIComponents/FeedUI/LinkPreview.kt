package com.iota.campusX.ui.UIComponents.FeedUI

import android.util.Patterns
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.Savers.LinkPreviewViewModel
import com.iota.campusX.R
import org.koin.androidx.compose.koinViewModel


@Composable
fun LinkPreviewCard(
    url: String,
    linkPreviewViewModel: LinkPreviewViewModel = koinViewModel(),
    onClick:()-> Unit
) {

    val previews = linkPreviewViewModel.previews
    val meta = previews[url]

    LaunchedEffect(url) {
        linkPreviewViewModel.loadPreview(url)
    }

    meta?.let { data ->

        Column (
            modifier = Modifier.border(
                width = 0.1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            ).clickable(
                onClick = {
                    onClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ).clip(RoundedCornerShape(16.dp))

        ){

            AsyncImage(
                model = data.imageUrl?:R.drawable.landscape_placeholder_svgrepo_com,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(180.dp),
                fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(data.title ?: "", style = MaterialTheme.typography.titleMedium)
                data.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
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