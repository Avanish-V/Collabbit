package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iota.campusX.R
import com.iota.campusX.ui.UIComponents.FeedUI.ImageWithDynamicRatio

@Composable
fun ImageAttachment(
    images: List<String>,
    onImageClick: () -> Unit = {}
) {
    if (images.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        if (images.size == 1) {
            ImageWithDynamicRatio(
                imageUrl = images.first(),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onImageClick() },
                onImageClick = onImageClick
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onImageClick() }
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left image (main)
                    AsyncImage(
                        model = images[0],
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                    )

                    if (images.size > 1) {
                        Spacer(modifier = Modifier.width(2.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            // Top right
                            AsyncImage(
                                model = images[1],
                                contentDescription = null,
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                            )
                            
                            if (images.size > 2) {
                                Spacer(modifier = Modifier.height(2.dp))
                                // Bottom right
                                Box(modifier = Modifier.weight(1f)) {
                                    AsyncImage(
                                        model = images[2],
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com)
                                    )
                                    
                                    if (images.size > 3) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.4f)),
                                            contentAlignment = androidx.compose.ui.Alignment.Center
                                        ) {
                                            androidx.compose.material3.Text(
                                                text = "+${images.size - 3}",
                                                color = androidx.compose.ui.graphics.Color.White,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
