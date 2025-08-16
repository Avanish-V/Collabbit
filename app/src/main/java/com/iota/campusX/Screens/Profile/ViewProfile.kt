package com.iota.campusX.Screens.Profile

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.GetPostDTO
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.domain.Models.UserReplyDTO
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.SharedBottomSheetViewModel
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Screens.Post.defaultPostHandlers
import com.iota.campusX.Screens.ReplyWidget
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.UIComponents.ProfileHeader
import com.iota.campusX.ui.theme.LightBlack
import com.iota.campusX.ui.theme.LightTheme_Blue
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.White
import kotlinx.coroutines.launch

// AppUserProfile.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ViewProfile(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    profileViewModel: UserProfileViewModel,
    googleSignInViewModel: AuthViewModel,
    navigationViewModel: NavigationViewModel,
    replyViewModel: ReplyViewModel,
) {


    val useridByFeed = navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")
    val currentDestination = navHostController.currentDestination?.route


    LaunchedEffect(Unit) {
        profileViewModel.getProfileIdByPost(
            userIdByFeed = useridByFeed ?: "",
            loggedInUserId = googleSignInViewModel.userId(),
            currentDestination = currentDestination ?: ""
        )
    }


    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 3 })
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }


    // ✅ Use shared ViewModel properly
    val bottomSheetViewModel: SharedBottomSheetViewModel = viewModel()
    val bottomSheetData by bottomSheetViewModel.bottomSheetState.collectAsState()
    val userType by profileViewModel.userType.collectAsState()


    // ✅ Collect profile states
    val userBaseProfile by profileViewModel.userBaseProfile.collectAsState()
    val profileByIdState by profileViewModel.profileById.collectAsState()
    val connectionsCountState by profileViewModel.connectionCount.collectAsState()
    val hasConnection by profileViewModel.hasConnection.collectAsState()
    val linkupRequestState by profileViewModel.sendLinkUpRequestState.collectAsState()

    val replyState by replyViewModel.userRepliesState.collectAsState()
    val postState by postViewModel.postById.collectAsState()



    LaunchedEffect(linkupRequestState) {
        when (linkupRequestState) {
            is UiState.Loading -> {}
            is UiState.Success -> {
                useridByFeed?.let { profileViewModel.hasConnection(it) }
            }

            is UiState.Error -> {
                snackBarHostState.showSnackbar((linkupRequestState as UiState.Error).message)
                profileViewModel.resetModifyState()
            }

            else -> {}
        }
    }


    // ✅ Derive profile data and loading state
    val profileData =
        when (userType) {
            UserType.Owner -> (userBaseProfile as? UiState.Success)?.data
            UserType.User -> (profileByIdState as? UiState.Success)?.data
            else -> null
        }


    val isProfileLoading = remember(userType, userBaseProfile, profileByIdState) {
        when (userType) {
            UserType.User -> profileByIdState is UiState.Loading
            else -> false

        }
    }


    // ✅ Other UI states
    val isConnected = (hasConnection as? UiState.Success)?.data
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
                actions = {
                    if (userType == UserType.Owner) {
                        IconButton(
                            onClick = { navHostController.navigate("SETTING") },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )

                        ) {
                            Icon(painterResource(R.drawable.setting), contentDescription = null)
                        }
                    }
                },
                navigationIcon = {
                    if (userType == UserType.User) {
                        IconButton(onClick = { navHostController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
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
                    snackbarHostState = snackBarHostState,
                    modifier = Modifier.fillMaxSize(),
                    headerHeight = { },
                    navHostController = navHostController,
                    user = UserDetail(
                        userName = profileData?.userName.orEmpty(),
                        userImage = profileData?.userImage.orEmpty(),
                        id = profileData?.id.orEmpty()
                    ),
                    userType = userType,
                    onLinkUpRequestClick = {
                        scope.launch {
                            profileData?.let {
                                profileViewModel.sendLinkUpRequest(
                                    requestUserId = it.id,
                                    currentState = isConnected
                                )
                            }
                        }
                    },
                    onMessageClick = {
                        navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                            navHostController.currentBackStackEntry?.savedStateHandle?.apply {
                                set("USER_ID", profileData?.id)
                                set("USER_NAME", profileData?.userName)
                                set("USER_IMAGE", profileData?.userImage)
                            }
                        }
                    },
                    connectionsCount = (connectionsCountState as? UiState.Success)?.data ?: 0,
                    hasConnection = hasConnection,
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            stickyHeader {
                PrimaryTabRow(
                    modifier = Modifier,
                    selectedTabIndex = pagerState.currentPage,
                    divider = { Divider() },
                    indicator = {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(
                                selectedTabIndex = pagerState.currentPage,
                                matchContentSize = false
                            ),
//                            width = 48.dp,
//                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp)
                        )
                    },
//                    containerColor = MaterialTheme.colorScheme.background
                ) {
                    listOf("Profile", "Posts", "Replies").forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Text(text = title)
                            },
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
//                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
//                            selectedContentColor = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            item {
                HorizontalPager(state = pagerState) { page ->

                    LazyColumn(
                        modifier = Modifier.height(screenHeight),
                        userScrollEnabled = headerPinned,
                    ) {

                        when (page) {

                            0 -> profileData?.let {

                                userAbout(
                                    userBasicProfileDTO = it,
                                    navHostController = navHostController,
                                    isCurrentUser = userType == UserType.Owner
                                )
                            }

                            1 -> postScreenComponent(
                                navHostController = navHostController,
                                postViewModel = postViewModel,
                                postState = postState,
                                bottomSheetSharedViewModel = bottomSheetViewModel,
                                currentUser = profileData?.id ?: "",
                                campusId = profileData?.campus?.campusCode,
                                userType = userType,
                            )

                            2 -> {
                                repliesComponent(
                                    userId = profileData?.id ?: "",
                                    replyViewModel = replyViewModel,
                                    repliesState = replyState,
                                    onRetryClick = { },
                                    navHostController = navHostController,
                                    postViewModel = postViewModel,
                                    sharedBottomSheetViewModel = bottomSheetViewModel,
                                )
                            }

                        }
                    }
                }
            }
        }

        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            bottomSheetViewModel = bottomSheetViewModel,
            replyViewModel = replyViewModel,
            postFeedViewModel = postViewModel,
            onDismiss = {},
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = { isAlertDialogVisible.value = true },
            onEditClick = {},
            onHideBottomSheet = {}
        )

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

        LoadingUI(isLoading = isProfileLoading)
    }
}


