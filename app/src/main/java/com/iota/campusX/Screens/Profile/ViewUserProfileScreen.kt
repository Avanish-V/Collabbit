package com.iota.campusX.Screens.Profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Post.presentation.ViewUserPostViewModel
import com.iota.campusX.Feature.Post.presentation.ViewUserReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.ViewProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.CampusWidget
import com.iota.campusX.ui.UIComponents.ConnectionComponent
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.EmptyState
import com.iota.campusX.ui.UIComponents.ProfileAction
import com.iota.campusX.ui.UIComponents.ProfileContents
import com.iota.campusX.ui.UIComponents.ProfileHeader
import com.iota.campusX.ui.theme.LightTheme_Blue
import kotlinx.coroutines.launch

// AppUserProfileScreen.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewProfile(
    navHostController: NavHostController,
    viewUserPostViewModel: ViewUserPostViewModel,
    viewProfileViewModel: ViewProfileViewModel,
    navigationViewModel: NavigationViewModel,
    viewUserReplyViewModel: ViewUserReplyViewModel,
) {


    val useridByFeed = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")

    LaunchedEffect(useridByFeed) {
        useridByFeed?.let { viewProfileViewModel.getUserById(it) }
    }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    val profileByIdState by viewProfileViewModel.profileById.collectAsState()
    val connectionsCountState by viewProfileViewModel.connectionCount.collectAsState()
    val hasConnectionState by viewProfileViewModel.hasConnection.collectAsState()
    val linkupRequestState by viewProfileViewModel.sendLinkUpRequestState.collectAsState()

    val replyState by viewUserReplyViewModel.viewUserReplies.collectAsState()
    val postState = viewUserPostViewModel.viewUserPost.collectAsLazyPagingItems()

    var viewProfileData by remember { mutableStateOf<BaseProfileDTO?>(null) }

    when(profileByIdState){
        is UiState.Loading -> {
            LoadingUI(isLoading = true)
        }
        is UiState.Success -> {
            viewProfileData = (profileByIdState as UiState.Success).data
        }
        is UiState.Error -> {
            LaunchedEffect(Unit) {
                snackBarHostState.showSnackbar((profileByIdState as UiState.Error).message)
            }
        }
        else -> {}
    }

    LaunchedEffect(linkupRequestState) {
        when (linkupRequestState) {
            is UiState.Loading -> {}
            is UiState.Success -> {
                useridByFeed?.let { viewProfileViewModel.hasConnection(it) }
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar((linkupRequestState as UiState.Error).message)
            }

            else -> {}
        }
    }

    LaunchedEffect(useridByFeed) {
        useridByFeed?.let { viewProfileViewModel.getConnectionCount(it) }
    }
    LaunchedEffect(useridByFeed) {
        useridByFeed?.let { viewProfileViewModel.hasConnection(it) }
    }


    // ✅ Other UI states
    val isConnected = (hasConnectionState as? UiState.Success)?.data
    val isLoading = remember { mutableStateOf(false) }
    val isAlertDialogVisible = remember { mutableStateOf(false) }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    var headerHeightDp by remember { mutableStateOf(0.dp) }
    var tabRowHeightDp by remember { mutableStateOf(0.dp) }

    val horizontalPagerHeight by remember {
        derivedStateOf {
            screenHeight - (headerHeightDp + tabRowHeightDp + 12.dp + 52.dp)
        }
    }


    val postLazyColumnState = rememberLazyListState()
    HideBottomBar(navigationViewModel, postLazyColumnState)

    val headerPinned by remember {
        derivedStateOf {
            postLazyColumnState.firstVisibleItemIndex >= 2 // item index where stickyHeader is placed
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            state = postLazyColumnState
        ) {

            item {
                ProfileHeader(
                    modifier = Modifier.fillMaxSize(),
                    headerHeight = { },
                    user = viewProfileData,
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                useridByFeed?.let {
                    ConnectionComponent(
                        navHostController = navHostController,
                        pagerState = pagerState,
                        followersCount = viewProfileData?.count?.followers?:0,
                        connectionCount = viewProfileData?.count?.connections?:0,
                        postsCountCount = viewProfileData?.count?.posts?:0,
                        userId = it
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ProfileAction(
                    onLinkUpRequestClick = {
                        scope.launch {
                            viewProfileData?.let {
                                viewProfileViewModel.sendLinkUpRequest(
                                    requestUserId = it.id,
                                    currentState = isConnected
                                )
                            }
                        }
                    },
                    onMessageClick ={
                        navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("USER_ID", viewProfileData?.id)
                                set("USER_NAME", viewProfileData?.userName)
                                set("USER_IMAGE", viewProfileData?.userImage)
                            }
                        }
                    },
                    hasConnectionState = hasConnectionState,
                    snackBarHostState = snackBarHostState
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    thickness = 12.dp
                )
            }

            stickyHeader {
                AppTabRow(pagerState = pagerState,tabList = listOf("About","Posts","Replies"))
            }

            item {
                ProfileContents(
                    pagerState = pagerState,
                    screenHeight = screenHeight,
                    headerPinned = headerPinned,
                    content = {
                        when (it) {

                            0 -> {
                                viewProfileData?.let { userBasicProfileDTO ->
                                    ViewUserAbout(
                                        screenHeight = screenHeight,
                                        pinned = headerPinned,
                                        userBasicProfileDTO = userBasicProfileDTO,
                                        navHostController = navHostController
                                    )
                                }
                            }

                            1 ->{

                                useridByFeed?.let { userId ->
                                    PostScreenComponent(
                                        screenHeight = screenHeight,
                                        pinned = headerPinned,
                                        navHostController = navHostController,
                                        lazyPagingItems = postState,
                                        onPageActive = {
                                            viewUserPostViewModel.fetchViewUserPosts(userId = userId,)
                                        },
                                        onRetryClick = {},
                                    )
                                }
                            }

                            2 -> {
                                useridByFeed?.let { userId ->
                                    RepliesComponent(
                                        screenHeight = screenHeight,
                                        pinned = headerPinned,
                                        userId = userId,
                                        repliesState = replyState,
                                        navHostController = navHostController,
                                        onRetryClick = {
                                            viewUserReplyViewModel.getUserReplies(userId)
                                        },
                                        onPageActive = {
                                            viewUserReplyViewModel.getUserReplies(userId)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

//        PostDotOptionBottomSheet(
//            isBottomSheet = bottomSheetData.isBottomSheet,
//            bottomSheetViewModel = bottomSheetViewModel,
//            replyViewModel = replyViewModel,
//            postFeedViewModel = postViewModel,
//            onDismiss = {},
//            isCurrentUser = bottomSheetData.isCurrentUser,
//            onDeleteClick = { isAlertDialogVisible.value = true },
//            onEditClick = {},
//            onHideBottomSheet = {}
//        )

        if (isAlertDialogVisible.value) {
            BasicAlertDialog(
                onDismissRequest = { isAlertDialogVisible.value = false },
            ) {
                Surface(shape = RoundedCornerShape(6.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Delete Post",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                        Text(
                            "Are you sure you want to delete this post?",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                        HorizontalDivider()
                        Row(Modifier.fillMaxWidth()) {
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = { isAlertDialogVisible.value = false },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() })
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("Cancel")
                            }
                            VerticalDivider(modifier = Modifier.height(48.dp))
                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = {
                                            context.vibrate()
                                            // Delete post logic here
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() })
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading.value)
                                    CircularProgressIndicator(
                                        color = LightTheme_Blue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                else
                                    Text("Delete", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ViewUserAbout(
    screenHeight: Dp,
    pinned: Boolean,
    userBasicProfileDTO: BaseProfileDTO,
    navHostController: NavHostController,
) {

    LazyColumn (
        modifier = Modifier.height(height = screenHeight),
        userScrollEnabled = pinned
    ){
        item {
            Column() {
                ProfileComponent(
                    title = "About",
                    body = {
                        Spacer(modifier = Modifier.height(4.dp))
                        if (userBasicProfileDTO.userBio.isNotEmpty()) {
                            // Show Bio Text
                            Text(
                                text = userBasicProfileDTO.userBio,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            // Show dotted placeholder box
                            EmptyState(
                                title = "Not Yet Updated",
                                isAppUser = false
                            )
                        }
                    },
                    contentDescription = "Bio",
                    editIconVisible = false
                )

                Divider()

                ProfileComponent(
                    title = "Interests",
                    body = {

                        if (userBasicProfileDTO.interests.isEmpty()) {

                            EmptyState(
                                title = "Not Yet Updated",
                                isAppUser = false
                            )

                        } else {
                            // Normal case: show chips
                            Spacer(modifier = Modifier.height(12.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                userBasicProfileDTO.interests.forEach {
                                    AssistChip(
                                        onClick = { /* could trigger something later */ },
                                        label = {
                                            Text(
                                                text = it,
                                                modifier = Modifier.padding(10.dp),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                        },
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant
                                        ),
                                    )
                                }
                            }
                        }
                    },
                    contentDescription = "INTERESTS",
                    editIconVisible = false
                )

                Divider()
                ProfileComponent(
                    title = "Campus Detail",
                    body = {
                        if (userBasicProfileDTO.campus == null) {

                            EmptyState(
                                title = "Not Yet Updated",
                                isAppUser = false
                            )

                        } else {
                            // Normal campus widget
                            Spacer(modifier = Modifier.height(12.dp))
                            CampusWidget(
                                campus = userBasicProfileDTO.campus
                            )
                        }
                    },
                    contentDescription = "CAMPUS",
                    editIconVisible = false
                )
            }
        }
    }

}


