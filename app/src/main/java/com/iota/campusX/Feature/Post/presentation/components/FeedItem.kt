package com.iota.campusX.Feature.Post.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.data.remote.response.Post
import com.iota.campusX.Feature.Reply.presentation.components.ReplyButtonComponent
import com.iota.campusX.ui.UIComponents.FeedUI.Avatar

@Composable
fun FeedItem(
    feedItem: Post,
    handlers: (PostAction) -> Unit,
    onReplyClick: (Post) -> Unit,
    onMoreClick: (Post) -> Unit,
    onProfileClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
        onClick = {
            handlers(PostAction.OpenPostDetail(feedItem))
        }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Avatar(
                    imageUrl = feedItem.author.authorImage,
                    onAvatarClick = {
                        if (feedItem.author.isCurrentUser) return@Avatar
                        onProfileClick(feedItem.author.authorId)
                    }
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    FeedHeader(
                        authorName = feedItem.author.authorName,
                        authorSubTitle = feedItem.author.authorTagline,
                        postedAt = feedItem.createdAt
                    )

                    FeedBody(
                        caption = feedItem.caption,
                        attachment = feedItem.attachment,
                        goToFeedViewer = {
                            handlers(PostAction.ViewPostVisualContent(feedItem))
                        },
                        onPollSelect = { optionId ->
                            handlers(PostAction.VotePoll(feedItem.postId, optionId))
                        }
                    )

                    FeedAction(
                        likesCount = feedItem.likesCount,
                        isLiked = feedItem.isLiked,
                        onLikeClick = { isLiked ->
                            handlers(
                                PostAction.Like(
                                    isLiked = isLiked,
                                    contentId = feedItem.postId
                                )
                            )
                        },
                        onMoreVertClick = {
                            onMoreClick(feedItem)
                        },
                        otherActionContent = {
                            ReplyButtonComponent(
                                replyCount = feedItem.commentCount.toString(),
                                onReplyClick = {
                                    onReplyClick(feedItem)
                                },
                                enableText = false
                            )
                        }
                    )
                }
            }
        }
    }
}
