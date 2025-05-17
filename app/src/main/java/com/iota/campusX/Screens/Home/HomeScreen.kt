package com.iota.campusX.Screens.Home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val tabs = listOf("Trending", "Latest")
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

                    }

                }
            }
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
    val context = LocalContext.current
    val postResultState = postViewModel.postState.collectAsState().value
    val scope = rememberCoroutineScope()
    val pullToRefreshState = rememberPullToRefreshState()
    val lazyState = rememberLazyListState(initialFirstVisibleItemIndex = 0)
    var isRefreshing by remember { mutableStateOf(false) }
    val isBottomSheetVisible = remember { mutableStateOf(false) }
    val isCurrentUser = remember { mutableStateOf(false) }

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


            if (postResultState.error.isNotEmpty()) {

                ErrorScreen(
                    error = postResultState.error
                )
            }


            isRefreshing = false

            LazyColumn(
                state = lazyState,
                verticalArrangement = Arrangement.spacedBy(1.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
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

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = White400
                            )
                        }

                    }

                }

                if (postResultState.postData.isNotEmpty()) {


                    val sortedPost = postResultState.postData.sortedByDescending { it.postedAt }

                    items(sortedPost, key = {it.postId}) {
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
                                isBottomSheetVisible.value = !isBottomSheetVisible.value
                                isCurrentUser.value = it.creatorDetail.isCurrentUser
                            }
                        )
                    }

                }


            }


        }

        if (isBottomSheetVisible.value) {
            PostDotOptionBottomSheet(
                isBottomSheet = isBottomSheetVisible.value,
                onDismiss = { isBottomSheetVisible.value = false },
                isCurrentUser = isCurrentUser.value
            )
        }

    }
}

@Composable
fun PostCard(
    onPostClick: () -> Unit,
    onLikeClick: () -> Unit,
    onReplyClick: () -> Unit,
    onDotMenuClick: () -> Unit,
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
                    .size(48.dp)
                    .clip(CircleShape)
            )

            Column {

                PostHeader(
                    user = post.creatorDetail.profile,
                    pod = post.reference,
                    postedAt = getTimeAgo(post.postedAt),
                    onNameClick = {

                        if (post.postMode != "USER")return@PostHeader

                        navHostController.navigate(Routes.Main.ProfileByID.routes).toString()
                            .apply {
                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                    "USER_ID",
                                    post.creatorDetail.profile?._id
                                )
                            }
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

        withStyle(
            style = SpanStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp
            )
        ) {
            append(user?.userName ?: "")
        }

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
                lineHeight = 0.1.sp
            )
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

        if (postContent.postData.postImage.isNotEmpty()) {
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
    isCurrentUser: Boolean
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
                    .padding(vertical = 20.dp, horizontal = 16.dp)
            ) {

                if (isCurrentUser){

                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .background(secondary, shape = RoundedCornerShape(10.dp))
                ) {

                    Icon(
                        modifier = Modifier.size(22.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = primary
                    )

                    Text("Delete")

                }

            }


        }

    }


}