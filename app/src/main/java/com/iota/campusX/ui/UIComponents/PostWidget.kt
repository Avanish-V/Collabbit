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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.Reference
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.PollOption
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
                Box(
                    modifier = Modifier
                        .size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircleImage(
                        image = post.creatorDetail.profile?.userImage ?: "",
                        modifier = Modifier.size(48.dp),
                        onClick = {
                            goToProfile.invoke()
                        }
                    )
                }

//                TODO:: For Column
            }

            Spacer(modifier = Modifier.width(12.dp))

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
        modifier = Modifier.height(48.dp),
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
fun Modifier.imageRatio(imageUrl: String): Modifier {
    val context = LocalContext.current
    var imageSize by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val imageSizeCache = remember { mutableStateMapOf<String, Pair<Int, Int>>() }

    LaunchedEffect(Unit) {
        if (imageSize != null) return@LaunchedEffect
        if (imageSizeCache.contains(imageUrl)) {
            imageSize = imageSizeCache[imageUrl]
        } else {
            getImageSize(context, imageUrl)?.let {
                imageSizeCache[imageUrl] = it
                imageSize = it
            }
        }
    }


    imageSize?.let { (width, height) ->
        return when {
            width > height -> this.then(Modifier.fillMaxWidth().height(180.dp))
            else -> this.then(Modifier.fillMaxWidth().height(350.dp))
        }
    }

    return this.then(Modifier.fillMaxWidth().height(200.dp)) // fallback
}

@Composable
fun PostBody(
    postContent: PostContent,
    navHostController: NavHostController
) {

    Column(modifier = Modifier.padding(vertical = 12.dp)) {

        when(postContent.postType){
            "TEXT"->{

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

                    ImageWithDynamicRatio(
                        imageUrl = postContent.postData.postImage,
                        modifier = Modifier.fillMaxWidth(),
                        navHostController = navHostController
                    )
                }

            }
            "POLL"->{

                PollOptionsUI(
                    question = postContent.postData.poll?.question ?: "",
                    options = postContent.postData.poll?.options,
                    showResults = true,
                    totalVotes = postContent.postData.poll?.options?.sumOf { it.votes } ?: 0,
                    onOptionSelected = {}
                )

            }

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
    totalVotes: Int
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = question,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        options?.forEach { option ->
            val isSelected = selectedOptionId == option.id
            val percentage = if (totalVotes > 0) (option.votes * 100 / totalVotes) else 0

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "$percentage%",
                    modifier = Modifier.width(40.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .drawBehind {
                            if (showResults) {
                                val fillWidth = size.width * (percentage / 100f)
                                drawRoundRect(
                                    color = if (isSelected) secondary else Color.Red,
                                    size = androidx.compose.ui.geometry.Size(width = fillWidth, height = size.height),
                                    cornerRadius = CornerRadius(12f, 12f)
                                )
                            }
                        }
                        .border(
                            width = 1.dp,
                            color = Black300,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(enabled = !showResults) {
                            onOptionSelected(option.id)
                        }
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = option.text,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = option.votes.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Total Votes: $totalVotes • Poll ended")
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


