package com.iota.campusX.ui.UIComponents

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.Reference
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography

@Composable
fun PostCard(
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onDotMenuClick: () -> Unit,
    goToProfile: () -> Unit,
    post: PostDTO,
    navHostController: NavHostController
) {

    Column(
        modifier = Modifier
            .background(
                color = White900
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onPostClick.invoke() }
            )
            .fillMaxWidth()
            .padding(12.dp)
    ) {

        Row {

            Column {

                CircleImage(
                    image = post.creatorDetail.profile?.userImage ?: "",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    onClick = {
                        goToProfile.invoke()
                    }
                )

            }

            Column {

                PostHeader(
                    about = post.creatorDetail.profile?.about ?: "",
                    user = post.creatorDetail.profile,
                    pod = post.reference,
                    postedAt = getTimeAgo(post.postedAt),
                    onNameClick = {
                        goToProfile.invoke()

                    }
                )

                PostBody(
                    postContent = post.postContent,
                    navHostController = navHostController
                )

                PostActionsComponent(
                    postAction = post.postActions,
                    user = post.creatorDetail.profile,
                    onLikeClick = { onLikeClick.invoke() },
                    onReplyClick = { onReplyClick.invoke() },
                    onDotMenuClick = { onDotMenuClick.invoke() }
                )

            }


        }


    }
}

@Composable
fun PostHeader(
    about: String = "",
    user: User?,
    pod: Reference? = null,
    postedAt: String? = null,
    onNameClick: () -> Unit
) {

    val text = buildAnnotatedString {

        withStyle(style = SpanStyle(color = White400)) {
            append(" ● ")
        }

        withStyle(style = SpanStyle(color = Black300, fontWeight = FontWeight.Normal)) {
            append("Alumni")
        }

    }


    Row(
        modifier = Modifier.height(48.dp).padding(start = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Column(modifier = Modifier.weight(1f)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Row (){
                    Text(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onNameClick.invoke()
                                }
                            ),
                        text = user?.userName ?: "",
                        fontSize = 14.sp,
                        lineHeight = 0.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                }

                Row (Modifier.weight(1f)){

                    Text(
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onNameClick.invoke()
                                }
                            ),
                        text = text,
                        fontSize = 14.sp,
                        lineHeight = 0.1.sp,
                        maxLines = 1,
                    )
                }


            }

            Row (verticalAlignment = Alignment.CenterVertically){

                if (!about.isEmpty()){
                    Text(
                        modifier = Modifier.weight(1f),
                        text = about,
                        style = typography.labelRegular,
                        color = Black800,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    modifier = Modifier,
                    text = postedAt.toString(),
                    fontSize = 14.sp,
                    lineHeight = 0.1.sp,
                    maxLines = 1,
                    color = Black300
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
fun PostBody(
    postContent: PostContent,
    navHostController: NavHostController
) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {

        if (postContent.postData.postText.isNotEmpty()){
            ExpandableText(
                text = postContent.postData.postText,
                minimizedMaxLines = 4
            )
        }

        if (postContent.postData.postText.isNotEmpty()&&!postContent.postData.postImage.isNullOrBlank() && postContent.postData.postImage != "null"){
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (!postContent.postData.postImage.isNullOrBlank() && postContent.postData.postImage != "null") {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
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
                                    postContent.postData.postImage
                                )
                            }
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    )
                    .height(180.dp)
                    .clip(RoundedCornerShape(5.dp)),
                model = postContent.postData.postImage,
                placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                contentDescription = "Post Image",
                contentScale = ContentScale.Crop,
            )
        }

    }
}

@Composable
fun PostActionsComponent(
    postAction: PostActions,
    user: User?,
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
                        .size(24.dp)
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
                        .size(20.dp)
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

    // Use this to measure text layout result
    val textLayoutResultState = remember { mutableStateOf<TextLayoutResult?>(null) }

    Column(
        modifier = Modifier.animateContentSize( // Animate layout height changes
            animationSpec = tween(
                durationMillis = 500,
                easing = FastOutSlowInEasing
            )),
        horizontalAlignment = Alignment.End
    ) {



        Text(
            text = text,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { textLayoutResult ->
                textLayoutResultState.value = textLayoutResult
                if (!isExpanded) {
                    isTextOverflowing = textLayoutResult.hasVisualOverflow
                }
            },
            style = MaterialTheme.typography.bodyMedium
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
                        onClick = {isExpanded = !isExpanded},
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