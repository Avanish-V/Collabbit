package com.iota.campusX.ui.UIComponents.FeedUI

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iota.campusX.R
import com.iota.campusX.Utils.buildAnnotatedAutoLinkText
import com.iota.campusX.ui.UIComponents.UserAvatar

@Composable
fun Avatar(
    imageUrl: String?,
    onAvatarClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAvatarClick
            )
    ) {
        UserAvatar(
            imageUrl = imageUrl,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun AnimatedLikeButton(
    onLike: (Boolean) -> Unit,
    likesCount: Int,
    isLiked: Boolean,
    tint: Color? = null
) {
    val iconTint = tint ?: if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val textColor = tint ?: if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onLike(!isLiked) }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painterResource(if (isLiked) R.drawable.up_solid else R.drawable.up_regular),
            contentDescription = "Like",
            tint = iconTint
        )
        
        AnimatedContent(
            targetState = likesCount,
            transitionSpec = {
                if (targetState > initialState) {
                    slideInVertically { height -> height } + fadeIn() togetherWith
                            slideOutVertically { height -> -height } + fadeOut()
                } else {
                    slideInVertically { height -> -height } + fadeIn() togetherWith
                            slideOutVertically { height -> height } + fadeOut()
                }.using(SizeTransform(clip = false))
            },
            label = "LikeCountAnimation"
        ) { count ->
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = textColor
            )
        }
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    context: Context,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface
    ),
    onLinkClick: (String) -> Unit = { url ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Log or handle error
        }
    }
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }
    val annotatedText = remember(text) { buildAnnotatedAutoLinkText(text) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var url by remember(text) { mutableStateOf(extractUrlFromText(text) ?: "") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(tween(300, easing = FastOutSlowInEasing))
    ) {
        BasicText(
            text = annotatedText,
            style = textStyle,
            maxLines = if (isExpanded) Int.MAX_VALUE else 4,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                textLayoutResult = result
                if (!isExpanded) {
                    isTextOverflowing = result.hasVisualOverflow
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(annotatedText) {
                    detectTapGestures { offsetPos ->
                        textLayoutResult?.let { layoutResult ->
                            val offset = layoutResult.getOffsetForPosition(offsetPos)
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                url = annotation.item
                                onLinkClick(annotation.item)
                            }
                        }
                    }
                }
        )

        if (isTextOverflowing || isExpanded) {
            Text(
                text = if (isExpanded) "Show less" else "Read more",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { isExpanded = !isExpanded }
                    )
            )
        }


        if (url.isNotBlank()){
            Spacer(modifier = Modifier.padding(6.dp))
            LinkPreviewCard(
                url = url,
                onClick = {

                }
            )
        }
    }
}
