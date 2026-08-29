package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.iota.campusX.R

@Composable
fun UserAvatar(
    modifier: Modifier,
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
            contentScale = ContentScale.Crop,
            fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
            placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
        )

    }
}
