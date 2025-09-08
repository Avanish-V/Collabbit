package com.iota.campusX.ui.UIComponents.FeedUI

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Notification.domain.UserPayload
import com.iota.campusX.Feature.Post.data.model.Poll
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.R
import com.iota.campusX.Utils.buildAnnotatedAutoLinkText
import com.iota.campusX.ui.UIComponents.AnonymousImage
import com.iota.campusX.ui.theme.LightBlack

@Composable
fun Avatar(
    imageUrl: String,
    visibilityMode: VisibilityMode,
    onAvatarClick:()-> Unit
) {

    if (visibilityMode == VisibilityMode.ANONYMOUS){
        AnonymousImage(
            modifier = Modifier.size(42.dp)
        )
    }else{

        AsyncImage(
            modifier = Modifier
                .size(42.dp)
                .border(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (visibilityMode == VisibilityMode.USER) {
                            onAvatarClick.invoke()
                        }
                    }
                ),
            model = imageUrl,
            contentDescription = "Profile Picture",
            contentScale = ContentScale.Crop,
            error = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
        )

    }

}

@Composable
fun AnimatedLikeButton(onLike:()-> Unit,likesCount: Int,isLiked: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        AnimatedContent(
            targetState = likesCount,
            transitionSpec = {
                slideInVertically { height -> height } + fadeIn() togetherWith
                        slideOutVertically { height -> -height } + fadeOut()
            },
            label = "LikeCountAnimation"
        ) { likeCount ->
            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.bodyMedium
            )
        }


        Icon(
            modifier = Modifier
                .size(16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onLike.invoke() }
                ),
            painter = painterResource(if (isLiked) R.drawable.up_solid else R.drawable.up_regular),
            contentDescription = "Like",
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun FeedSpace() {
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun PollOptionsUI(
    poll: Poll,
    selectedOptionId: String? = null,
    onOptionSelected: (String) -> Unit,
    totalVotes: Int,
    hasVoted: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = poll.question,
            style = MaterialTheme.typography.bodyLarge
        )


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            poll.options.forEach { option ->

                val isSelected = selectedOptionId == option.optionId

                val filter = poll.votes.filter { it.optionId == option.optionId }

                val percentage = if (totalVotes > 0) (filter.count() * 100 / totalVotes) else 0

                val backgroundColor = if (isSelected) {
                    Color.Transparent
                } else {
                    MaterialTheme.colorScheme.surface // <-- COMPOSABLE SAFE
                }


                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .border(
                                width = 0.5.dp,
                                color = if (option.optionId == poll.selectedOptionId) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                shape = MaterialTheme.shapes.small
                            )
                            .clip(MaterialTheme.shapes.small)
                            .drawBehind {
                                if (poll.hasVoted) {
                                    val fillWidth = size.width * (percentage / 100f)
                                    drawRoundRect(
                                        color = backgroundColor,
                                        size = Size(
                                            width = fillWidth,
                                            height = size.height
                                        ),
                                        cornerRadius = CornerRadius(12f, 12f)
                                    )
                                }
                            }
                            .clickable {
                                if (hasVoted) return@clickable
                                onOptionSelected(option.optionId)
                            }
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        if (poll.hasVoted){
                            Text(text = "$percentage%",style = MaterialTheme.typography.bodyMedium)
                        }

                    }
                }
            }
        }
        if (poll.isActive){
            Spacer(modifier = Modifier.height(0.dp))
        }

        if (!poll.isActive){
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Total Votes $totalVotes • Poll ended",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun LikeRail(reliesList: List<UserPayload>) {

    Row {
        Box(){
            reliesList.forEachIndexed { index, item->

                if (item.visibilityMode == VisibilityMode.ANONYMOUS){
                    AnonymousImage(
                        modifier = Modifier.padding(start = (index * 20).dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(2.dp, LightBlack, CircleShape)
                    )

                }else{

                    AsyncImage(
                        modifier = Modifier
                            .padding(start = (index * 20).dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .border(2.dp, LightBlack, CircleShape),
                        model = item.userImage,
                        contentDescription = "Reply User",
                        contentScale = ContentScale.Crop
                    )
                }

            }
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
    labelStyle: TextStyle = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.primary
    ),
    onLinkClick: (String) -> Unit = { url ->
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }


    val annotatedText = remember(text) { buildAnnotatedAutoLinkText(text) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = modifier
            .animateContentSize(
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        BasicText(
            text = annotatedText,
            style = textStyle,
            maxLines = if (isExpanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                textLayoutResult = result
                if (!isExpanded) {
                    isTextOverflowing = result.hasVisualOverflow
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectTapGestures { offsetPos ->
                        textLayoutResult?.let { layoutResult ->
                            val offset = layoutResult.getOffsetForPosition(offsetPos)
                            annotatedText.getStringAnnotations(
                                tag = "URL",
                                start = offset,
                                end = offset
                            ).firstOrNull()?.let { annotation ->
                                onLinkClick(annotation.item)
                            }
                        }
                    }
                }
        )

        if (isTextOverflowing || isExpanded) {
            val label = if (isExpanded) "Reed Less" else "Reed More"
            Text(
                text = label,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .clickable(
                        onClick = { isExpanded = !isExpanded },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
            )
        }
    }
}

fun Timestamp?.toMillis(): Long {
    return this?.toDate()?.time ?: 0L
}