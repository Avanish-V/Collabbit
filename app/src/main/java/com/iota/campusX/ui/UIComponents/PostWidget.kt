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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.PostActions
import com.iota.campusX.Feature.Post.domain.Models.PostContent
import com.iota.campusX.Feature.Post.domain.Models.PostVisibilityMode
import com.iota.campusX.Feature.Post.domain.Models.Reference
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.BottomSheetSharedViewModel
import com.iota.campusX.Screens.Home.BottomSheet.Content
import com.iota.campusX.Screens.Home.BottomSheet.ContentType
import com.iota.campusX.Screens.Home.BottomSheet.SheetType
import com.iota.campusX.Screens.Post.PollOption
import com.iota.campusX.Screens.Post.PostOptions
import com.iota.campusX.Screens.Profile.ProfileTypeViewModel
import com.iota.campusX.Utils.buildAnnotatedAutoLinkText
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun PostCard(
    feedViewModel: PostFeedViewModel,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    profileViewModel: UserProfileViewModel,
    post: GetPostDTO,
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit = {
        feedViewModel.toggleLike(
            userId = post.creatorDetail.profile?.id ?: "",
            postId = post.postId,
            isLiked = post.postActions.isLiked,
            campusId = post.campusId,
            feedMode = post.feedMode)
        },
    onReplyClick: () -> Unit,
    onDotMenuClick: () -> Unit = {
        bottomSheetSharedViewModel.setBottomSheetState(
            state = true,
            isCurrentUser = post.creatorDetail.isCurrentUser,
            campusId = post.campusId,
            content = Content(
                postId = post.postId,
                text = post.postContent.postData.postText
            ),
            feedMode = post.feedMode,
            contentType = ContentType.POST,
            sheetType = SheetType.MENU_LIST,
        )
    },
    onPollSelect: (String) -> Unit,
    navHostController: NavHostController
) {

    var rightColumnHeight by remember { mutableIntStateOf(0) }

    LocalDensity.current

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp, vertical = 12.dp)
            .background(White900)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onPostClick
            )
            .fillMaxWidth()

    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left column with explicit height synced to rightColumnHeight
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                //modifier = Modifier.height(with(density) { rightColumnHeight.toDp() })
            ) {

                CircleImage(
                    image = post.creatorDetail.profile?.userImage.orEmpty(),
                    modifier = Modifier.size(42.dp),
                    onClick = {

                        if (post.visibilityMode != PostVisibilityMode.USER) return@CircleImage

                        navHostController.navigate(Routes.Main.ProfileByID.routes)
                            .apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.apply {
                                    set("USER_ID", post.creatorDetail.profile?.id)
                                }
                            }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

//                VerticalDivider(
//                    modifier = Modifier.fillMaxHeight()
//                )

            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right column - capture height on layout
            Column(
                modifier = Modifier
                    .weight(1f)
                    .onGloballyPositioned { coordinates ->
                        rightColumnHeight = coordinates.size.height
                    }
            ) {

                PostHeader(
                    about = post.creatorDetail.profile?.userBio.orEmpty(),
                    user = post.creatorDetail.profile,
                    pod = post.reference,
                    postedAt = getTimeAgo(post.createdAt),
                )

                PostBody(
                    postContent = post.postContent,
                    navHostController = navHostController,
                    onPollSelect = {
                        onPollSelect(it)
                    }
                )

                PostActionsComponent(
                    postAction = post.postActions,
                    user = post.creatorDetail.profile,
                    onLikeClick = onLikeClick,
                    onReplyClick = onReplyClick,
                    onDotMenuClick = onDotMenuClick
                )

            }
        }

    }
}


@Composable
fun PostHeader(
    about: String = "",
    user: UserDetail?,
    pod: Reference? = null,
    postedAt: String? = null,
    visibilityMode: PostVisibilityMode? = null
) {

    Row(
        modifier = Modifier.height(48.dp),
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
                    text = user?.userName ?: "",
                    fontSize = 14.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    softWrap = false,
                    style = typography.headingMedium,
                    overflow = TextOverflow.Ellipsis
                )

                Text(" ● ",color = White400)

                Text(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {

                            }
                        ),
                    text = postedAt.toString(),
                    fontSize = 14.sp,
                    lineHeight = 0.1.sp,
                    maxLines = 1,
                    color = Black300
                )


            }



            if (visibilityMode == PostVisibilityMode.USER){
                if (about.isEmpty()) return@Column
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = about,
                    style = typography.labelRegular,
                    color = Black800,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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

    var interactionSource = remember { MutableInteractionSource() }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                AnimatedContent(
                    targetState = postAction.likesCount,
                    transitionSpec = {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    },
                    label = "LikeCountAnimation"
                ) { likeCount ->
                    Text(
                        text = likeCount.toString(),
                        color = Black500,
                        style = typography.labelMedium
                    )
                }


                Icon(
                    modifier = Modifier
                        .size(22.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = { onLikeClick?.invoke() }
                        ),
                    painter = painterResource(if (postAction.isLiked) R.drawable.heart_sharp else R.drawable.heart_outline),
                    contentDescription = "Like",
                    tint = if (postAction.isLiked) Color.Red else Black500
                )
            }


            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = postAction.replyCount.toString(),
                    color = Black500,
                    style = typography.labelMedium
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
                    tint = Black500
                )
            }

        }

        Icon(
            modifier = Modifier
                .rotate(90f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { onDotMenuClick?.invoke() }
                ),
            painter = painterResource(R.drawable.dots_menu),
            contentDescription = "Dots",
            tint = Black500
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
            style = MaterialTheme.typography.bodyMedium,
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
                color = MaterialTheme.colorScheme.primary,
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
                        .border(2.dp, White900, CircleShape),
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
    navHostController: NavHostController,
    onPollSelect: (String) -> Unit
) {

    Column(modifier = Modifier.padding(vertical = 12.dp)) {

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
                        navHostController = navHostController
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
            style = typography.bodyMedium
        )

        options?.forEach { option ->
            val isSelected = selectedOptionId == option.optionId
            val percentage = if (totalVotes > 0) (option.votes.count() * 100 / totalVotes) else 0

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
                            width = 1.dp,
                            color = White400,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .drawBehind {
                            if (showResults) {
                                val fillWidth = size.width * (percentage / 100f)
                                drawRoundRect(
                                    color = if (isSelected) Color.Black else White400,
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
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(text = "$percentage%")
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Total Votes $totalVotes • Poll ended", color = Black300)
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
    navHostController: NavHostController
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
                if (width > height) 180.dp else 350.dp
            }
        }
    }

    AsyncImage(
        modifier = Modifier
            .height(dynamicHeight)
            .border(
                width = 2.dp,
                color = secondary,
                shape = RoundedCornerShape(5.dp)
            )
            .clickable(
                onClick = {
                    navHostController.navigate(Routes.Main.PostViewScreen.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "POST_IMAGE",
                            imageUrl
                        )
                    }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .clip(RoundedCornerShape(5.dp)),
        model = imageUrl,
        placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
        contentDescription = "Post Image",
        contentScale = ContentScale.Crop,
    )
}


