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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.iota.campusX.Feature.Post.domain.Models.CreatorDetail
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.Reference
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.PollOption
import com.iota.campusX.Screens.Post.PostActionHandlers
import com.iota.campusX.Screens.Post.PostOptions
import com.iota.campusX.Utils.buildAnnotatedAutoLinkText
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.LightTheme_LightGray
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White
//import com.iota.campusX.ui.theme.secondary
//import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PostCard(
    post: GetPostDTO,
    handlers: PostActionHandlers,
    isCurrentUser: Boolean? = null
) {
    Column(
        modifier = Modifier
            .clickable(
                onClick = {handlers.onPostClick.invoke()},
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .fillMaxWidth()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircleImage(
                    image = post.creatorDetail.profile?.userImage.orEmpty(),
                    modifier = Modifier.size(42.dp),
                    onClick = {
                        if (post.visibilityMode == PostVisibilityMode.USER) {
                            post.creatorDetail.profile?.id?.let(handlers.onProfileClick)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                PostHeader(
                    user = post.creatorDetail,
                    pod = post.reference,
                    postedAt = getTimeAgo(post.createdAt),
                    isCurrentUser = isCurrentUser,
                    feedMode = post.feedMode,
                    visibilityMode = post.visibilityMode
                )

                Spacer(modifier = Modifier.height(6.dp))

                PostBody(
                    postContent = post.postContent,
                    onPollSelect = handlers.onPollSelect,
                    onPostImageClick = {
                        handlers.onPostImageClick.invoke(post.postContent.postData.postImage.toString())
                    },
                    onBodyClick = {
                        handlers.onPostClick.invoke()
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                PostActionsComponent(
                    postAction = post.postActions,
                    user = post.creatorDetail.profile,
                    onLikeClick = handlers.onLikeClick,
                    onReplyClick = handlers.onReplyClick,
                    onDotMenuClick = handlers.onDotMenuClick
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
    visibilityMode: PostVisibilityMode? = null,
    isCurrentUser: Boolean? = null,
    feedMode: FeedMode? = null
) {

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Text(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {

                            }
                        ),
                    text = user.profile?.userName ?: "",
                    maxLines = 1,
                    softWrap = false,
                    style = MaterialTheme.typography.headlineMedium,
                    overflow = TextOverflow.Ellipsis
                )

                if (user.isVerified){
                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(R.drawable.check_circle),
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(text = " ● ",color = MaterialTheme.colorScheme.surface)

                Text(
                    modifier = Modifier,
                    text = postedAt.toString(),
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )


            }

            if (visibilityMode == PostVisibilityMode.USER){

                user.profile?.userBio?.let {

                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }
            }
        }

        if (isCurrentUser != null){
            Text(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(5.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                text = if (feedMode == FeedMode.GLOBAL) "Global" else "Campus",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium
            )
        }

        if (pod != null) {
            AsyncImage(
                modifier = Modifier.size(32.dp),
                model = pod.icon,
                contentDescription = ""
            )
        }

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
            tint = if (isLiked) if (isSystemInDarkTheme()) Color.White else Color.Black else MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun ExpandableText(
    text: String,
    minimizedMaxLines: Int = 4
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isTextOverflowing by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val annotatedText = remember(text) { buildAnnotatedAutoLinkText(text) }

    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.End

    ) {
        ClickableText(
            text = annotatedText,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { result ->
                textLayoutResultState.value = result
                if (!isExpanded) {
                    isTextOverflowing = result.hasVisualOverflow
                }
            },
            style = TextStyle(
                color = MaterialTheme.colorScheme.onSurface
            ),
            onClick = { offset ->
                annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(annotation.item))
                        context.startActivity(intent)
                    } ?: run {

                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        if (isTextOverflowing || isExpanded) {
            val label = if (isExpanded) "Read Less" else "Read More"
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
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
    postContent: PostContent,
    onPollSelect: (String) -> Unit,
    onPostImageClick:(String)-> Unit,
    onBodyClick:()-> Unit
) {

    Column(modifier = Modifier.clickable(
        onClick = {onBodyClick.invoke()},
        indication = null,
        interactionSource = remember { MutableInteractionSource() }
    )) {

        when(postContent.postType){

            PostOptions.TEXT -> {

                if (postContent.postData.postText.isNotEmpty()) {
                    ExpandableText(
                        text = postContent.postData.postText,
                        minimizedMaxLines = 4
                    )
                }

            }

            PostOptions.IMAGE -> {

                if (postContent.postData.postText.isNotEmpty()){
                    ExpandableText(
                        text = postContent.postData.postText,
                        minimizedMaxLines = 4
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                if (!postContent.postData.postImage.isNullOrBlank() && postContent.postData.postImage != "null") {

                    ImageWithDynamicRatio(
                        imageUrl = postContent.postData.postImage,
                        modifier = Modifier.fillMaxWidth(),
                        onImageClick = {onPostImageClick.invoke(postContent.postData.postImage)}
                    )
                }

            }

            PostOptions.POLL -> {
                PollOptionsUI(
                    question = postContent.postData.poll?.question ?: "",
                    options = postContent.postData.poll?.options,
                    showResults = true,
                    totalVotes = postContent.postData.poll?.options?.sumOf { it.votes.count() }
                        ?: 0,
                    onOptionSelected = {
                        onPollSelect(it)
                    },
                    hasVoted = postContent.postData.poll?.hasVoted == true

                )
            }

            PostOptions.VIDEO -> {}
            PostOptions.FILE -> {}
        }
    }
}

@Composable
fun PollOptionsUI(
    question: String,
    options: List<PollOption>?,
    selectedOptionId: String? = null,
    onOptionSelected: (String) -> Unit,
    showResults: Boolean = false,
    totalVotes: Int,
    hasVoted: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(
            text = question,
            style = MaterialTheme.typography.bodyMedium
        )


        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

            options?.forEach { option ->

                val isSelected = selectedOptionId == option.optionId

                val percentage = if (totalVotes > 0) (option.votes.count() * 100 / totalVotes) else 0

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
                                color=MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clip(RoundedCornerShape(6.dp))
                            .drawBehind {
                                if (showResults) {
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

                        Text(text = "$percentage%",style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

        }


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
                width = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                onClick = {
                    onImageClick()
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .clip(RoundedCornerShape(16.dp)),
        model = imageUrl,
        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
        contentDescription = "Post Image",
        contentScale = ContentScale.Crop,
    )
}


