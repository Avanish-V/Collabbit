package com.iota.campusX.ui.UIComponents.FeedUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.MediaType
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.Type
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.PostActions.PostAction
import com.iota.campusX.Utils.getTimeAgo


@Composable
fun FeedItem(
    feedItem: GetPostDTO,
    handlers: (PostAction) -> Unit,
    onDotMenuClick: ((GetPostDTO) -> Unit),
    enableFeedMode: Boolean = false
) {

    Row(
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    handlers.invoke(PostAction.OpenPostDetail(postId = feedItem.postId))
                }
            )
            .fillMaxWidth()
            .padding(12.dp)

    ) {

        Avatar(
            imageUrl = feedItem.creatorDetail.profile?.userImage ?: "",
            visibilityMode = feedItem.visibilityMode,
            onAvatarClick = {
                if (feedItem.creatorDetail.isCurrentUser) return@Avatar
                feedItem.creatorDetail.profile?.let {
                    handlers.invoke(
                        PostAction.OpenUserProfile(
                            userId = it.id,
                            isCurrentUser = feedItem.creatorDetail.isCurrentUser
                        )
                    )
                }
            }
        )

        Spacer(modifier = Modifier.size(12.dp))

        Column (modifier = Modifier.weight(1f)){

            FeedHeader(
                creator = feedItem.creatorDetail,
                feedMode = feedItem.feedMode,
                visibilityMode = feedItem.visibilityMode,
                postedAt = getTimeAgo(feedItem.createdAt.toMillis()),
                trailingComponent = {

                    feedItem.creatorDetail.let {
                        if (!it.isCurrentUser && feedItem.visibilityMode == VisibilityMode.USER){

                            Text(
                                modifier = Modifier.clickable(
                                    onClick = {
                                        if (it.isFollow){
                                            handlers.invoke(PostAction.UnFollowUser(it.profile?.id ?: ""))
                                        }else{
                                            handlers.invoke(PostAction.FollowUser(it.profile?.id ?: ""))
                                        }
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }

                                ),
                                text = if (it.isFollow) "Unfollow" else "Follow",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (it.isFollow) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (enableFeedMode){

                        if (feedItem.creatorDetail.isCurrentUser){

                            Text(
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = MaterialTheme.shapes.small
                                    )
                                    .padding(6.dp),
                                text = if (feedItem.feedMode == FeedMode.CAMPUS) "Campus" else "Global",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }

                    }
                }
            )

            FeedSpace()

            FeedBody(
                type = feedItem.type,
                mediaType = feedItem.mediaType,
                postContent = feedItem.postContent,
                goToFeedViewer = {
                    handlers.invoke(PostAction.ViewPostVisualContent(post = feedItem))
                },
                onPollSelect = {optionID->
                    handlers.invoke(
                        PostAction.VotePoll(
                            postId = feedItem.postId,
                            optionId = optionID,
                            feedMode = feedItem.feedMode
                        )
                    )
                }
            )

            FeedSpace()

            FeedAction(
                likesCount = feedItem.postActions.likesCount,
                isLiked = feedItem.postActions.isLiked,
                onLikeClick = {
                    handlers.invoke(
                        PostAction.Like(
                            contentId = ContentId.Post(postId = feedItem.postId),
                            isLiked = feedItem.postActions.isLiked,
                            userId = feedItem.creatorDetail.profile?.id ?: ""
                        )
                    )
                },
                onMoreVertClick = {
                    onDotMenuClick.invoke(feedItem)
                },
                otherActionContent = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = feedItem.postActions.replyCount.toString(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Icon(
                            modifier = Modifier
                                .size(18.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        handlers.invoke(
                                            PostAction.OpenPostDetail(postId = feedItem.postId)
                                        )
                                    }
                                ),
                            painter = painterResource(R.drawable.chatbubble_outline),
                            contentDescription = "Reply",
                            tint =  MaterialTheme.colorScheme.onBackground
                        )
                    }

                },
            )

        }

    }

}

@Composable
fun FeedHeader(
    creator: CreatorDetail,
    feedMode: FeedMode,
    visibilityMode: VisibilityMode,
    postedAt: String? = null,
    trailingComponent: @Composable () -> Unit = {}
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Username
                Text(
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {

                        }
                    ),
                    text = creator.profile?.userName ?: "",
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )

                if (creator.isAlumni && feedMode == FeedMode.CAMPUS){
                    Text(
                        text = "Alumni",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                // Verified badge
                if (creator.isVerified && visibilityMode == VisibilityMode.USER) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.baseline_verified_24),
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Posted time
                if (!postedAt.isNullOrEmpty()) {
                    Text(
                        text = "• $postedAt",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (visibilityMode == VisibilityMode.USER) {
                creator.profile?.userBio?.takeIf { it.isNotEmpty() }?.let { bio ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        trailingComponent.invoke()

    }

}

@Composable
fun FeedBody(
    type: Type,
    mediaType: MediaType,
    postContent: PostContent,
    goToFeedViewer: (String) -> Unit,
    onPollSelect: (String) -> Unit,
) {

    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    when(type){

        Type.Media -> {

            if (mediaType == MediaType.Image){

                if (postContent.postText.isNotEmpty()) {
                    ExpandableText(
                        text = postContent.postText,
                        context = context,
                    )
                }

                postContent.postImage?.let {
                    FeedSpace()
                    ImageWithDynamicRatio(
                        imageUrl = it,
                        modifier = Modifier.fillMaxWidth(),
                        onImageClick = {goToFeedViewer.invoke(postContent.postImage)}
                    )
                }

                if (postContent.postText.isNotEmpty() && postContent.postImage == null){
                    val url = extractUrlFromText(postContent.postText)
                    val normalUrl = url?.let { normalizeUrl(it) }
                    normalUrl?.let {
                        LinkPreviewCard(it){
                            uriHandler.openUri(normalUrl)
                        }
                    }
                }

            }
        }

        Type.Poll -> {

            postContent.poll?.let {
                PollOptionsUI(
                    poll = it,
                    totalVotes = postContent.poll.votes.count(),
                    onOptionSelected = {
                        onPollSelect(it)
                    },
                    hasVoted = postContent.poll.hasVoted
                )
            }

        }
    }

}


@Composable
fun FeedAction(
    likesCount: Int,
    isLiked: Boolean,
    onLikeClick:()-> Unit,
    onMoreVertClick:()-> Unit,
    otherActionContent:@Composable ()-> Unit
){

    val interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {

            AnimatedLikeButton(
                onLike = { onLikeClick.invoke() },
                likesCount = likesCount,
                isLiked = isLiked
            )

            otherActionContent.invoke()

        }

        Icon(
            modifier = Modifier
                .size(22.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onMoreVertClick.invoke() }
                ),
            painter = painterResource(R.drawable.baseline_more_vert_24),
            contentDescription = "Dots",
            tint =  MaterialTheme.colorScheme.onSurfaceVariant
        )


    }


}