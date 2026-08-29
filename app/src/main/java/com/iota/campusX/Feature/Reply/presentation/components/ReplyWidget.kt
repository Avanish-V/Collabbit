package com.iota.campusX.Feature.Reply.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Reply.data.remote.response.ReplyResponse
import com.iota.campusX.Feature.Reply.domain.model.MentionBuilder
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.UIComponents.CircleImage
import com.iota.campusX.ui.UIComponents.FeedUI.AnimatedLikeButton
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Composable
fun ReplyWidget(
    reply: ReplyResponse,
    onDotsClick: () -> Unit,
    childReplies: Map<String, List<ReplyResponse>> = emptyMap(),
    childLoadingStates: Map<String, Boolean> = emptyMap(),
    onReplyClick: (MentionBuilder?) -> Unit = { },
    onLikeClick: (replyId: String, isLiked: Boolean, authorId: String) -> Unit = { _, _, _ -> },
    onLoadChildren: (String) -> Unit = {}
) {
    var columnHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    
    val children = childReplies[reply.id] ?: emptyList()
    val isLoadingChildren = childLoadingStates[reply.id] ?: false

    val timeAgo by remember {
        derivedStateOf {
            getTimeAgo(
                Instant.parse(reply.createdAt).toEpochMilliseconds()
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left column with Avatar and dynamic divider
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircleImage(
                    image = reply.author.authorImage ?: "",
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape),
                    onClick = {

                    },
                )

                if (children.isNotEmpty() || columnHeight > 40.dp) {
                    VerticalDivider(
                        modifier = Modifier
                            .height(columnHeight - 34.dp)
                            .padding(top = 4.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }

            // Right column with content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        columnHeight = with(density) { coordinates.size.height.toDp() }
                    }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = reply.author.authorName ?: "User",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.clickable {

                            }
                        )
                        
                        Text(
                            text = " • $timeAgo", // Ideally use a time formatter with reply.createdAt
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }

                    IconButton(
                        onClick = onDotsClick,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.menu_dots_vertical),
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                ReplyText(
                    text = reply.content,
                    mentionedUser = reply.mentionedUser?.let { 
                        MentionBuilder(
                            mentionUserName = it.name,
                            mentionedUserId = it.uid,
                            postId = "", 
                            parentId = reply.parentReplyId ?: ""
                        )
                    },
                    onMentionClick = {

                    }
                )

                if (!reply.imageUrl.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(reply.imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Reply attachment",
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                            .clickable { /* Expand image */ },
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {

                    AnimatedLikeButton(
                        isLiked = reply.isLiked,
                        likesCount = reply.likesCount,
                        onLike = {
                            onLikeClick(reply.id, reply.isLiked, reply.author.authorId)
                        }
                    )


                    Text(
                        text = "Reply",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                        modifier = Modifier
                            .clickable(
                                indication = null,
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                onClick = {
                                    onReplyClick.invoke(
                                        MentionBuilder(
                                            mentionUserName = reply.author.authorName ?: "",
                                            parentId = reply.id,
                                            postId = "",
                                            mentionedUserId = reply.author.authorId
                                        )
                                    )
                                }
                            )
                    )
                }

                // Show more replies button
                if (reply.childCount > 0 && children.isEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onLoadChildren(reply.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (isLoadingChildren) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                strokeWidth = 1.dp
                            )
                        } else {
                            Text(
                                text = "View ${reply.childCount} more replies",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // Nested replies
                if (children.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    children.forEach { child ->
                        ReplyWidget(
                            reply = child,
                            onDotsClick = onDotsClick,
                            onReplyClick = onReplyClick,
                            onLikeClick = onLikeClick,
                            childReplies = childReplies,
                            childLoadingStates = childLoadingStates,
                            onLoadChildren = onLoadChildren
                        )
                    }
                }
            }
        }
    }
}
