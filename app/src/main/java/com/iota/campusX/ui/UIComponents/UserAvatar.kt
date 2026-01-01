package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.R // <-- replace with your package name

@Composable
fun UserAvatar(
    modifier: Modifier,
    imageUrl: String?,
    bgColor: String,
    visibilityMode: VisibilityMode = VisibilityMode.USER
) {
    val colors = listOf(
        Color(0xFFFFB74D), // Orange
        Color(0xFF4FC3F7), // Blue
        Color(0xFF81C784), // Green
        Color(0xFFBA68C8), // Purple
        Color(0xFFE57373), // Red
        Color(0xFFFF8A65), // Coral
        Color(0xFFA1887F), // Brownish gray
    )

    // Pick a color deterministically based on userId
//    val index = (userId.hashCode() and 0x7FFFFFFF) % colors.size
//    val bgColor = colors[index]

    Box(
        modifier = modifier.background(colors[2]),
        contentAlignment = Alignment.Center
    ) {
        when(visibilityMode){

            VisibilityMode.USER -> {

                if (!imageUrl.isNullOrEmpty()) {
                    AsyncImage(
                        modifier = Modifier.clip(CircleShape).fillMaxSize(),
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        fallback = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                    )
                } else {
                    // When image is not available
                    Icon(
                        painter = painterResource(R.drawable.app_logo),
                        contentDescription = "User logo",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

            }
            VisibilityMode.ANONYMOUS -> {
                AnonymousImage()
            }
        }

    }
}
