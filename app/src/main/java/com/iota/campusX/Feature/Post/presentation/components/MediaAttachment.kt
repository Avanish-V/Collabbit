package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.presentation.components.DocumentAttachmentCard
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachmentDto
import com.iota.campusX.R

@Composable
fun MediaAttachment(
    attachment: com.iota.campusX.Feature.Post.domain.attachment.AttachmentDto,
    onMediaClick: (Int) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        when (attachment) {
            is ImageAttachmentDto -> {
                ImageGrid(
                    images = attachment.images,
                    ratios = attachment.aspectRatios ?: emptyList(),
                    onClick = onMediaClick
                )
            }
            is VideoAttachmentDto -> {
                VideoPreview(
                    thumbnailUrl = attachment.thumbnailUrl,
                    videoUrl = attachment.videoUrl,
                    ratio = attachment.aspectRatio ?: 1f,
                    onClick = { onMediaClick(0) }
                )
            }
            is DocumentAttachmentDto -> {
                DocumentPreview(
                    name = attachment.name,
                    thumbnailUrl = attachment.thumbnailUrl,
                    pageCount = attachment.pageCount,
                    onClick = { onMediaClick(0) }
                )
            }
            else -> {}
        }
    }
}

@Composable
private fun DocumentPreview(
    name: String,
    thumbnailUrl: String?,
    pageCount: Int,
    onClick: () -> Unit
) {
    DocumentAttachmentCard(
        name = name,
        onCardClick = onClick,
        headerActions = {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(onClick = onClick, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.OpenInFull,
                        contentDescription = "Expand",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = { /* Handle download if needed */ }, modifier = Modifier.size(24.dp)) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
        ) {
            if (thumbnailUrl != null) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            } else {
                Column(
                    modifier = Modifier
                        .padding(60.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(R.drawable.file_text),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        modifier = Modifier.size(100.dp)
                    )
                }
            }

            if (pageCount > 0) {
                Surface(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.6f)
                ) {
                    Text(
                        text = "1 / $pageCount",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageGrid(
    images: List<String>,
    ratios: List<Float>,
    onClick: (Int) -> Unit
) {
    if (images.isEmpty()) return

    if (images.size == 1) {
        MediaCard(
            imageUrl = images[0],
            ratio = ratios.getOrNull(0) ?: 1f,
            onClick = { onClick(0) }
        )
    } else {
        // Grid for multiple images
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = images[0],
                    contentDescription = null,
                    modifier = Modifier.weight(1f).fillMaxHeight().clickable { onClick(0) },
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder)
                )

                if (images.size > 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        AsyncImage(
                            model = images[1],
                            contentDescription = null,
                            modifier = Modifier.weight(1f).fillMaxWidth().clickable { onClick(1) },
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(R.drawable.placeholder)
                        )
                        
                        if (images.size > 2) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                AsyncImage(
                                    model = images[2],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize().clickable { onClick(2) },
                                    contentScale = ContentScale.Crop,
                                    placeholder = painterResource(R.drawable.placeholder)
                                )
                                
                                if (images.size > 3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.4f))
                                            .clickable { onClick(3) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "+${images.size - 3}",
                                            color = Color.White,
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

@Composable
private fun VideoPreview(
    thumbnailUrl: String?,
    videoUrl: String,
    ratio: Float,
    onClick: () -> Unit
) {
    MediaCard(
        imageUrl = thumbnailUrl ?: videoUrl,
        ratio = ratio,
        onClick = onClick,
        overlay = {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(48.dp),
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.4f)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    )
}

@Composable
private fun MediaCard(
    imageUrl: String,
    ratio: Float,
    onClick: () -> Unit,
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    // Threads-like behavior: Constrain media so tall content doesn't dominate the feed.
    val isPortrait = ratio < 1f
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 420.dp), // Strict height cap for the container
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (isPortrait) {
                        Modifier
                            .heightIn(max = 420.dp)
                            .aspectRatio(ratio.coerceIn(0.6f, 1f))
                    } else {
                        Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratio.coerceIn(1f, 1.91f))
                    }
                )
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { onClick() }
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder)
            )
            overlay()
        }
    }
}
