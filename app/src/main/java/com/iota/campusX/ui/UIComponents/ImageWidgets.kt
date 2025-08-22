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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.data.model.VisibilityMode

@Composable
fun CircleImage(image: String, modifier: Modifier = Modifier,onClick: () -> Unit,visibility: VisibilityMode) {

    Box(modifier = modifier.clip(CircleShape).background(color = MaterialTheme.colorScheme.primary),contentAlignment = Alignment.Center){

        when(visibility){

            VisibilityMode.ANONYMOUS -> {
                AsyncImage(
                    modifier = Modifier.size(24.dp),
                    colorFilter =  ColorFilter.tint(color = Color.White),
                    model = image,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                )
            }
            VisibilityMode.USER -> {

                AsyncImage(
                    modifier = modifier.clip(CircleShape).
                    background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ).
                    clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick.invoke() }
                    ),
                    model = image,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                )

            }

            null -> {
                AsyncImage(
                    modifier = modifier.clip(CircleShape).
                    background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ).
                    clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onClick.invoke() }
                    ),
                    model = image,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    error = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                )
            }
        }

    }
}

