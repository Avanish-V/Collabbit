package com.iota.campusX.ui.UIComponents

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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.Timestamp
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.Post.data.model.Reference
import com.iota.campusX.Feature.Post.data.model.UserDetail
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.MediaType
import com.iota.campusX.Screens.Post.Poll
import com.iota.campusX.Screens.Post.PollOption
import com.iota.campusX.Screens.Post.PostActions.PostAction
import com.iota.campusX.Screens.Post.PostOptions
import com.iota.campusX.Screens.Post.Type
import com.iota.campusX.Utils.buildAnnotatedAutoLinkText
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.theme.White
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Timestamp?.toMillis(): Long {
    return this?.toDate()?.time ?: 0L
}

@Composable
fun PostCard(
    post: GetPostDTO?,
    handlers: (PostAction) -> Unit,
    onDotMenuClick: ((GetPostDTO) -> Unit)?
) {
    if (post == null) return
    Column(
        modifier = Modifier
            .clickable(
                onClick = { handlers.invoke(PostAction.OpenPostDetail(postId = post.postId)) },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (post.visibilityMode == VisibilityMode.ANONYMOUS){
                    AnonymousImage(
                        modifier = Modifier.size(42.dp)
                    )
                }else{
                    CircleImage(
                        image = post.creatorDetail.profile?.userImage.orEmpty(),
                        modifier = Modifier.size(42.dp),
                        visibility = post.visibilityMode,
                        onClick = {
                            if (post.visibilityMode == VisibilityMode.USER) {
                                handlers.invoke(PostAction.OpenUserProfile(
                                    userId = post.creatorDetail.profile?.id ?: "",
                                    isCurrentUser = post.creatorDetail.isCurrentUser
                                ))
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                PostHeader(
                    user = post.creatorDetail,
                    pod = post.reference,
                    postedAt = getTimeAgo(post.createdAt.toMillis()),
                    isCurrentUser = post.creatorDetail.isCurrentUser,
                    feedMode = post.feedMode,
                    visibilityMode = post.visibilityMode
                )

                Spacer(modifier = Modifier.height(6.dp))

                PostBody(
                    postContent = post.postContent,
                    onPollSelect = {
                        post.postContent.poll?.let { poll ->
                            handlers.invoke(PostAction.VotePoll(
                                postId = post.postId,
                                optionId = it,
                                feedMode = post.feedMode
                            ))
                        }
                    },
                    onPostImageClick = {
                        handlers.invoke(PostAction.ViewPostVisualContent(post = post))
                    },
                    onBodyClick = {
                        handlers.invoke(PostAction.OpenPostDetail(postId = post.postId))
                    },
                    type = post.type,
                    mediaType = post.mediaType
                )

                Spacer(modifier = Modifier.height(6.dp))

                PostActionsComponent(
                    postAction = post.postActions,
                    user = post.creatorDetail.profile,
                    onLikeClick = {
                        handlers.invoke(
                            PostAction.Like(
                                contentId = ContentId.Post(postId = post.postId),
                                isLiked = post.postActions.isLiked,
                                userId = post.creatorDetail.profile?.id ?: ""
                            )
                        )
                    },
                    onReplyClick = {
                        handlers.invoke(PostAction.OpenPostDetail(postId = post.postId))
                    },
                    onDotMenuClick = {
                        onDotMenuClick?.invoke(post)
                    }
                )
            }
        }
    }
}


@Composable
fun PostHeader(
    user: CreatorDetail,
    pod: Reference? = null,
    postedAt: String? = null,
    visibilityMode: VisibilityMode? = null,
    isCurrentUser: Boolean? = null,
    feedMode: FeedMode? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
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
                        onClick = { /* TODO: navigate to profile */ }
                    ),
                    text = user.profile?.userName ?: "",
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.titleSmall, // ✅ username: section title weight
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis
                )

                // Verified badge
                if (user.isVerified && visibilityMode == VisibilityMode.USER) {
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
                        modifier = Modifier.alpha(0.7f),
                        text = "• $postedAt",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (visibilityMode == VisibilityMode.USER) {
                user.profile?.userBio?.takeIf { it.isNotEmpty() }?.let { bio ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        modifier = Modifier.alpha(0.7f),
                        text = bio,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        // Feed label (Global / Campus)
        isCurrentUser?.let {

            if (isCurrentUser) {
                Text(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    text = if (feedMode == FeedMode.GLOBAL) "Global" else "Campus",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium
                )
            }

        }
    }

        // Pod icon
        if (pod != null) {
            AsyncImage(
                modifier = Modifier.size(32.dp),
                model = pod.icon,
                contentDescription = "Pod icon"
            )
        }
    }



@Composable
fun PostActionsComponent(
    postAction: PostActions,
    user: UserDetail?,
    onLikeClick: (() -> Unit)? = null,
    onReplyClick: (() -> Unit)? = null,
    onDotMenuClick: (() -> Unit)? = null,
) {

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
                onLike = { onLikeClick?.invoke() },
                likesCount = postAction.likesCount,
                isLiked = postAction.isLiked
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = postAction.replyCount.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Icon(
                    modifier = Modifier
                        .size(18.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onReplyClick?.invoke() }
                        ),
                    painter = painterResource(R.drawable.chatbubble_outline),
                    contentDescription = "Reply",
                )
            }

        }

        Icon(
            modifier = Modifier
                .size(22.dp)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onDotMenuClick?.invoke() }
                ),
            painter = painterResource(R.drawable.baseline_more_vert_24),
            contentDescription = "Dots",
            tint =  MaterialTheme.colorScheme.onSurfaceVariant
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
            tint = if (isSystemInDarkTheme()) Color.White else Color.Black
        )
    }
}

@Composable
fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    context: Context,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
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



@Composable
fun ReplyRail(reliesList: List<PostActions>) {

    Row {
        Box(){
            reliesList.forEachIndexed { index, item->
                AsyncImage(
                    modifier = Modifier
                        .padding(start = (index * 20).dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .border(2.dp, White, CircleShape),
                    model = item,
                    contentDescription = "Reply User",
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}


suspend fun getImageSize(context: Context, imageUrl: String): Pair<Int, Int>? {
    return withContext(Dispatchers.IO) {
        try {
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false)
                .build()

            val result = (loader.execute(request) as? SuccessResult)?.drawable
            result?.intrinsicWidth?.let { w ->
                result.intrinsicHeight.let { h -> w to h }
            }
        } catch (e: Exception) {
            null
        }
    }
}


@Composable
fun PostBody(
    type: Type,
    mediaType: MediaType,
    postContent: PostContent,
    onPollSelect: (String) -> Unit,
    onPostImageClick:(String)-> Unit,
    onBodyClick:()-> Unit
) {
     val context = LocalContext.current

    Column(modifier = Modifier.clickable(
        onClick = {onBodyClick.invoke()},
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )) {

        if (postContent.postText.isNotEmpty()) {
            ExpandableText(
                text = postContent.postText,
                context = context,
            )
        }

        when(type){
            Type.Media -> {
               if (mediaType == MediaType.Image){
                   postContent.postImage?.let {
                       Spacer(modifier = Modifier.height(12.dp))
                       ImageWithDynamicRatio(
                           imageUrl = it,
                           modifier = Modifier.fillMaxWidth(),
                           onImageClick = {onPostImageClick.invoke(postContent.postImage)}
                       )
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
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) // <-- COMPOSABLE SAFE
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
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
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



class ImageSizeViewModel : ViewModel() {
    val imageSizeCache = mutableStateMapOf<String, Pair<Int, Int>>()
}

@Composable
fun ImageWithDynamicRatio(
    imageUrl: String,
    modifier: Modifier = Modifier,
    viewModel: ImageSizeViewModel = viewModel(),
    onImageClick:()-> Unit
) {
    val context = LocalContext.current
    val imageSizeCache = viewModel.imageSizeCache
    var imageSize by remember(imageUrl) { mutableStateOf<Pair<Int, Int>?>(imageSizeCache[imageUrl]) }

    LaunchedEffect(imageUrl) {
        if (imageSize == null && !imageSizeCache.containsKey(imageUrl)) {
            val size = getImageSize(context, imageUrl)
            if (size != null) {
                imageSizeCache[imageUrl] = size
                imageSize = size
            }
        }
    }

    val dynamicHeight = remember(imageSize) {
        when (val size = imageSize) {
            null -> 200.dp // fallback
            else -> {
                val (width, height) = size
                if (width > height) 180.dp else 400.dp
            }
        }
    }

    AsyncImage(
        modifier = Modifier
            .height(dynamicHeight)
            .border(
                width = 0.1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp)
            )

            .padding(3.dp)

            .clickable(
                onClick = {
                    onImageClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .clip(RoundedCornerShape(13.dp)),
        model = imageUrl,
        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
        contentDescription = "Post Image",
        contentScale = ContentScale.Crop,
    )

}


