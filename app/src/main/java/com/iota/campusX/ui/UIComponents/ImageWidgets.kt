package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.iota.campusX.R

@Composable
fun CircleImage(image: String, modifier: Modifier = Modifier,onClick: () -> Unit) {
    AsyncImage(
        modifier = modifier.
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