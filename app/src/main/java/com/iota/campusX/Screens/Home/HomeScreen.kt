package com.iota.campusX.Screens.Home

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Feature.Post.domain.Reference
import com.iota.campusX.Feature.Post.domain.PostActions
import com.iota.campusX.Feature.Post.domain.PostContent
import com.iota.campusX.Feature.Post.domain.PostDTO
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.getTimeAgo
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    navigationViewModel: NavigationViewModel,
    profileViewModel: UserProfileViewModel,
    homeViewModel: HomeViewModel
) {


    val userProfile = profileViewModel.userBaseProfile.collectAsState().value
    val context = LocalContext.current
    val tabs = listOf("Latest", "Trending")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    var tabIndex by remember { mutableIntStateOf(0) }

    val scope = rememberCoroutineScope()

    val switchState = homeViewModel.switchState.collectAsState().value

    LaunchedEffect(UInt) {
        profileViewModel.getUserProfile()
    }

    LaunchedEffect(switchState) {
        if (switchState.isLoad == false) {
            postViewModel.fetchPosts(postMode = switchState.isActive)
        }

    }

    val snackBarState by remember { mutableStateOf(SnackbarHostState()) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Image(
                        modifier = Modifier
                            .height(60.dp)
                            .width(140.dp),
                        painter = painterResource(R.drawable.campusx),
                        contentDescription = "Logo",
                    )
                },
                actions = {

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Switch(
                            checked = switchState.isActive,
                            enabled = false,
                            onCheckedChange = { newValue ->

                                homeViewModel.saveSwitchState(newValue)

                                if (userProfile.baseProfileData?.campus?.campusCode.isNullOrEmpty()){
                                    scope.launch(Dispatchers.IO) {
                                        homeViewModel.saveSwitchState(false)
                                        snackBarState.showSnackbar("Complete Campus Details First")
                                    }
                                }

                                context.vibrate()
                            },
                            colors = SwitchDefaults.colors(
                                uncheckedThumbColor = Black500,
                                uncheckedIconColor = White400,
                                uncheckedTrackColor = White900,
                                uncheckedBorderColor = Black500
                            ),
                        )
                        IconButton(
                            onClick = { navHostController.navigate(Routes.Main.ChatList.routes) },
                            Modifier.border(
                                width = 1.dp,
                                color = background,
                                shape = CircleShape

                            )
                        ) {
                            Icon(
                                painter = painterResource((R.drawable.messages_normal)),
                                contentDescription = "Message"
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier.padding(bottom = 100.dp),
                hostState = snackBarState
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(
                selectedTabIndex = tabIndex,
                containerColor = White900,
                divider = {
                    HorizontalDivider(
                        color = White400
                    )
                },
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            selectedTabIndex = pagerState.currentPage,
                            matchContentSize = false
                        ),
                        width = 48.dp,
                        color = primary,
                        shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                    )
                }
            ) {

                tabs.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            Text(
                                text = title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        selectedContentColor = Black800,
                        unselectedContentColor = Black400
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->

                when (page) {
                    0 -> {
                        TrendingScreen(
                            navHostController = navHostController,
                            postViewModel = postViewModel,
                            navigationViewModel = navigationViewModel,
                            profileImage = userProfile.baseProfileData?.userImage ?: "",
                            postMode = switchState.isActive,
                            scrollBehavior = scrollBehavior

                        )
                    }

                    1 -> {

                        Box(modifier = Modifier.fillMaxSize(),contentAlignment = Alignment.Center){
                            Text("Not have implemented yet")
                        }

                    }

                }
            }
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.writePost(
    navHostController: NavHostController,
    context: Context,
    profileImage: String
) {
    item {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = White900
                )
                .padding(12.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        navHostController.navigate(Routes.Main.CreatePost.routes)
                        context.vibrate()
                    }
                ),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            AsyncImage(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                model = profileImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )

            Column() {
                Box(
                    modifier = Modifier.height(40.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "What's on your mind?",
                        color = Black500
                    )
                }

                Row {
                    Icon(
                        painter = painterResource(R.drawable.write),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun LazyListScope.postsLazyColumn(
    postData: List<PostDTO>,
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    bottomSharedViewModel: BottomSheetSharedViewModel,
    context: Context,
    onDotMenuClick: () -> Unit
) {

    if (postData.isNotEmpty()) {

        val sortedPost = postData.sortedByDescending { it.postedAt }

        items(sortedPost, key = {it.postId}) {

            Log.d("POST_IMAGE", "postsLazyColumn: ${it.postContent.postData.postImage} ${it.postContent.postData.postText}")

            PostCard(
                onPostClick = {
                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("POST_ID", it.postId)
                    }
                },
                onLikeClick = {
                    postViewModel.toggleLike(
                        userId = it.creatorDetail.profile?._id ?: "",
                        postId = it.postId,
                        isLiked = it.postActions.isLiked
                    )
                    context.vibrate()
                },
                onReplyClick = {
                    navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("POST_ID", it.postId)
                    }
                },
                post = it,
                navHostController = navHostController,
                onDotMenuClick = {
                   bottomSharedViewModel.setBottomSheetState(state = true,isCurrentUser = it.creatorDetail.isCurrentUser, postId = it.postId, campusId = it.campusId.toString())
                },
                goToProfile = {

                    if (it.postMode != "USER") return@PostCard

                    navHostController.navigate(Routes.Main.Profile.routes)
                        .apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                "USER_ID",
                                it.creatorDetail.profile?._id
                            )
                        }
                }
            )
        }
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    navigationViewModel: NavigationViewModel,
    profileImage: String,
    postMode: Boolean,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val bottomSheetViewModel: BottomSheetSharedViewModel = viewModel()
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value
    val context = LocalContext.current
    val postResultState = postViewModel.postState.collectAsState().value
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }
    val isAlertDialogVisible = remember { mutableStateOf(false) }

    if (isRefreshing) {
        LaunchedEffect(Unit) {
            postViewModel.refreshPosts(postMode)
        }
    }

    HideBottomBar(
        navigationViewModel = navigationViewModel,
        lazyState = lazyState
    )

    PullToRefreshBox(
        modifier = Modifier.fillMaxSize(),
        isRefreshing = isRefreshing,
        onRefresh = {
            context.vibrate()
            scope.launch {
                isRefreshing = true
                lazyState.animateScrollToItem(0)
            }

        },
        state = pullToRefreshState,
        contentAlignment = Alignment.TopCenter,
        indicator = {
            Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                color = primary,
            )
        },
    ) {

        Column {


            if (postResultState.isLoading) {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = primary
                    )
                }
            }


            ErrorScreen(
                isActive = postResultState.error.isNotEmpty(),
                text = postResultState.error.toString(),
                onReTry = {
                    postViewModel.refreshPosts(postMode)
                }
            )


            isRefreshing = false

            LazyColumn(
                state = lazyState,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {

                writePost(navHostController = navHostController, context = context, profileImage = profileImage)

                postsLazyColumn(
                    postData = postResultState.postData,
                    navHostController = navHostController,
                    postViewModel = postViewModel,
                    bottomSharedViewModel = bottomSheetViewModel,
                    context = context,
                    onDotMenuClick = {
                    },

                )
            }
        }

        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            onDismiss = { bottomSheetViewModel.hideBottomSheet(false) },
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = {
                isAlertDialogVisible.value = !isAlertDialogVisible.value
            }
        )

        AnimatedVisibility(visible = isAlertDialogVisible.value) {

            Box(contentAlignment = Alignment.Center){

                BasicAlertDialog(
                    onDismissRequest = {isAlertDialogVisible.value = false},
                ) {

                    Surface(
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Column {

                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                Text("Delete Post",fontWeight = FontWeight.Bold)
                                Text("Are you sure you want to delete this post?", textAlign = TextAlign.Center)
                            }

                            Column {
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),

                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(Modifier
                                        .weight(1f)
                                        .clickable(
                                            onClick = { isAlertDialogVisible.value = false },
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }),contentAlignment = Alignment.Center){
                                        Text("Cancel", modifier = Modifier.padding(16.dp))
                                    }

                                    VerticalDivider(
                                        modifier = Modifier.height(48.dp)

                                    )

                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .clickable(
                                                onClick = {

                                                    scope.launch {
                                                        postViewModel.deletePost(
                                                            bottomSheetData.postId,
                                                            bottomSheetData.campusId
                                                        )
                                                            .collect {
                                                                when (it) {
                                                                    is ResultState.Success -> {
                                                                        delay(1000)
                                                                        isLoading.value = false
                                                                        isAlertDialogVisible.value =
                                                                            false
                                                                        bottomSheetData.isBottomSheet =
                                                                            false
                                                                        postViewModel.updateDeletePost(
                                                                            bottomSheetData.postId
                                                                        )
                                                                    }

                                                                    is ResultState.Error -> {
                                                                        bottomSheetData.isBottomSheet =
                                                                            false
                                                                        isLoading.value = false

                                                                        Toast.makeText(
                                                                            context,
                                                                            it.message,
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()


                                                                    }

                                                                    is ResultState.Loading -> {
                                                                        isLoading.value = true
                                                                    }
                                                                }
                                                            }
                                                    }

                                                    context.vibrate()

                                                },
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ){
                                        if (isLoading.value)
                                            CircularProgressIndicator(color = primary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                        else
                                            Text("Delete", modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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


        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {

            CircleImage(
                image = post.creatorDetail.profile?.userImage ?: "",
                modifier = Modifier
                    .clickable(
                        onClick = { goToProfile.invoke() },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() })
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Column {

                PostHeader(
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

                PostActions(
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
            append("3rd")
        }
        withStyle(style = SpanStyle(color = White400)) {
            append(" ● ")
        }

        withStyle(style = SpanStyle(color = Black300, fontWeight = FontWeight.Normal)) {
            append(postedAt)
        }
    }


    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {

            Row {


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

            Text(
                text = "IET Vivekanand, Campus, Agra",
                style = typography.labelRegular,
                color = Black800
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
fun PostBody(
    postContent: PostContent,
    navHostController: NavHostController
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Text(
            text = postContent.postData.postText,
            maxLines = 4,
            color = Black900,
            style = typography.bodyRegular,
            overflow = TextOverflow.Ellipsis
        )

        if (!postContent.postData.postImage.isNullOrBlank() && postContent.postData.postImage != "null") {
            AsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
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
fun PostActions(
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
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {

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
fun ReplyRail(modifier: Modifier = Modifier) {

//    Row {
//        Box(){
//            postAction.replies?.forEachIndexed { index, item->
//                AsyncImage(
//                    modifier = Modifier
//                        .padding(start = (index * 20).dp)
//                        .size(28.dp)
//                        .clip(CircleShape)
//                        .border(2.dp, White900, CircleShape),
//                    model = item,
//                    contentDescription = "Reply User",
//                    contentScale = ContentScale.Crop
//                )
//            }
//        }
//    }
}

@Composable
fun CircleImage(image: String, modifier: Modifier = Modifier) {
    AsyncImage(
        modifier = modifier,
        model = image,
        contentDescription = "Profile Picture",
        contentScale = ContentScale.Crop,
        placeholder = painterResource(R.drawable.anonymous)
    )
}


@Composable
fun HideBottomBar(
    navigationViewModel: NavigationViewModel,
    lazyState: LazyListState,
) {

    val isScrollingDown = remember {
        derivedStateOf {
            val firstVisibleItem = lazyState.firstVisibleItemIndex
            val scrollOffset = lazyState.firstVisibleItemScrollOffset
            firstVisibleItem to scrollOffset
        }
    }

    var previousIndex by remember { mutableStateOf(0) }
    var previousScrollOffset by remember { mutableStateOf(0) }
    var bottomBarVisible by remember { mutableStateOf(true) }

    LaunchedEffect(isScrollingDown.value) {
        val (currentIndex, currentOffset) = isScrollingDown.value
        bottomBarVisible = if (currentIndex > previousIndex ||
            (currentIndex == previousIndex && currentOffset > previousScrollOffset)
        ) {
            false // scrolling down
        } else {
            true // scrolling up
        }

        previousIndex = currentIndex
        previousScrollOffset = currentOffset
    }

    LaunchedEffect(bottomBarVisible) {
        navigationViewModel.isBottomBarVisible(bottomBarVisible)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDotOptionBottomSheet(
    isBottomSheet: Boolean,
    onDismiss: () -> Unit,
    isCurrentUser: Boolean,
    onDeleteClick:()-> Unit
) {


    if (isBottomSheet) {

        ModalBottomSheet(
            onDismissRequest = { onDismiss.invoke() },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            containerColor = Color.White,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (isCurrentUser){
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .padding(start = 10.dp)
                            .background(
                                color = secondary,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                onClick = {
                                    onDeleteClick.invoke()
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {

                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.trash),
                            contentDescription = null,
                            tint = primary
                        )

                        Text("Delete")

                    }
                }

                HorizontalDivider(
                    color = White400
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .background(
                            color = secondary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            onClick = {

                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )

                ) {

                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.warning_2),
                        contentDescription = null,
                        tint = Color.Red
                    )

                    Text("Report (Work in progress)", color = Color.Red)

                }

            }


        }

    }


}