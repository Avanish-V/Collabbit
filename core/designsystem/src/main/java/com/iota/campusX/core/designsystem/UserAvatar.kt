package com.iota.campusX.core.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage

@Composable
fun UserAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String?,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            modifier = Modifier.clip(CircleShape).fillMaxSize(),
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
    }
}
