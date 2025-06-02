package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.iota.campusX.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter

@Composable
fun CircleImage(image: String, modifier: Modifier = Modifier,onClick: () -> Unit) {
    AsyncImage(
        modifier = modifier.clip(CircleShape).
        clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onClick.invoke() }
        ),
        model = image,
        contentDescription = "Profile Picture",
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
    )
}



@Composable
fun PostImageFromUrl(imageUrl: String) {
    val painter = rememberAsyncImagePainter(model = imageUrl)
    val state = painter.state

    var aspectRatio by remember { mutableStateOf(1f) }
    var isVertical by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is AsyncImagePainter.State.Success) {
            val drawable = state.result.drawable
            val width = drawable.intrinsicWidth
            val height = drawable.intrinsicHeight

            if (width > 0 && height > 0) {
                aspectRatio = width.toFloat() / height
                isVertical = height > width
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspectRatio)
            .background(Color.LightGray)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Text(
            text = if (isVertical) "Vertical" else "Horizontal",
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
