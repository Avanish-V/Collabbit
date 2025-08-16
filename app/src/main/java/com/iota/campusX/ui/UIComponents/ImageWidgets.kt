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

