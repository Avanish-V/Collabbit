package com.iota.campusX.Screens.Profile

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.LinkUpRequestDTO
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheetSharedViewModel
import com.iota.campusX.Screens.Home.postsLazyColumn
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ResultState
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.timeMillsToString
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.PostDotOptionBottomSheet
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    profileViewModel: UserProfileViewModel,
    googleSignInViewModel: AuthViewModel,
    navigationViewModel: NavigationViewModel
) {


    val currentUser = googleSignInViewModel.userId()
    val context = LocalContext.current
    val bottomSheetViewModel: BottomSheetSharedViewModel = viewModel()
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value
    val hasMessage = profileViewModel.hasMessage.collectAsState().value
    val isLoading = remember { mutableStateOf(false) }
    val isAlertDialogVisible = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val creatorId = remember { navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID") }

    val userBaseProfile by profileViewModel.userBaseProfile.collectAsState()
    val profileByIdState by profileViewModel.profileById.collectAsState()

    LaunchedEffect(creatorId) {
        if (currentUser != creatorId) {
            profileViewModel.getUserById(creatorId.toString())
        }
    }

    LaunchedEffect(Unit) {
        profileViewModel.hasMessage(creatorId.toString())
    }

    val profileState = if (currentUser == creatorId || creatorId == null) {
        userBaseProfile.baseProfileData
    } else {
        profileByIdState.baseProfileData
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White900
                ),
                actions = {
                    if (creatorId.isNullOrEmpty()) {
                        Row(horizontalArrangement = Arrangement.End) {

                            IconButton(onClick = {
                                navHostController.navigate("SETTING")
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.setting),
                                    contentDescription = null
                                )
                            }
                        }
                    }

                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState) {
                Snackbar(snackbarData = it)
            }
        },
        containerColor = secondary
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {

            ProfileHeader(
                navHostController = navHostController,
                user = User(
                    userName = profileState?.userName.toString(),
                    userImage = profileState?.userImage.toString(),
                    _id = profileState?._id.toString()
                ),
                currentUser = currentUser,
                onLinkUpRequestClick = {
                    scope.launch {
                        profileViewModel.sendLinkUpRequest(
                            requestUserId = creatorId.toString(),
                            currentState = profileState?.isRequestSent
                        ).collect {
                            when (it) {
                                is ResultState.Success -> {
                                    profileViewModel.getUserById(creatorId.toString())
                                    snackBarHostState.showSnackbar("Done")
                                }

                                is ResultState.Error -> {
                                    snackBarHostState.showSnackbar("Something went wrong")
                                }

                                is ResultState.Loading -> {
                                }
                            }
                        }
                    }
                },
                onMessageClick = {

                    navHostController.navigate(Routes.Main.SendMessage.routes).apply {
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "USER_ID",
                            profileState?._id
                        )
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "USER_NAME",
                            profileState?.userName
                        )
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "USER_IMAGE",
                            profileState?.userImage
                        )
                        navHostController.currentBackStackEntry?.savedStateHandle?.set(
                            "ROOM_ID",
                            hasMessage
                        )
                    }

                },
                isLinkUpRequestSent = profileState?.isRequestSent
            )

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalTabComponent { page ->

                when (page) {
                    0 -> {
                        UserAbout(
                            userBasicProfileDTO = profileState
                        )
                    }

                    1 -> {
                        PostScreenComponent(
                            navHostController,
                            postViewModel,
                            navigationViewModel,
                            bottomSheetSharedViewModel = bottomSheetViewModel,
                            currentUser = profileState?._id ?: "",
                            campusId = profileState?.campus?.campusCode,
                            context = context
                        )
                    }
                }
            }
        }

        PostDotOptionBottomSheet(
            isBottomSheet = bottomSheetData.isBottomSheet,
            bottomSheetSharedViewModel = bottomSheetViewModel,
            postViewModel = postViewModel,
            onDismiss = { bottomSheetViewModel.hideBottomSheet(false) },
            isCurrentUser = bottomSheetData.isCurrentUser,
            onDeleteClick = {
                isAlertDialogVisible.value = !isAlertDialogVisible.value
            },
            onEditClick = {

            },
            onHideBottomSheet = {
                bottomSheetViewModel.hideBottomSheet(false)
            }
        )

        AnimatedVisibility(visible = isAlertDialogVisible.value) {

            Box(contentAlignment = Alignment.Center) {

                BasicAlertDialog(
                    onDismissRequest = { isAlertDialogVisible.value = false },
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
                                Text("Delete Post", fontWeight = FontWeight.Bold)
                                Text(
                                    "Are you sure you want to delete this post?",
                                    textAlign = TextAlign.Center
                                )
                            }

                            Column {
                                HorizontalDivider()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth(),

                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .clickable(
                                                onClick = { isAlertDialogVisible.value = false },
                                                indication = null,
                                                interactionSource = remember { MutableInteractionSource() }),
                                        contentAlignment = Alignment.Center
                                    ) {
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
                                                                            false  // <-- Add this line
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
                                    ) {
                                        if (isLoading.value)
                                            CircularProgressIndicator(
                                                color = primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        else
                                            Text(
                                                "Delete",
                                                modifier = Modifier.padding(16.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        LoadingUI(profileByIdState.isLoading && currentUser != creatorId)
    }
}


@Composable
fun ProfileHeader(
    navHostController: NavHostController,
    user: User,
    currentUser: String,
    onLinkUpRequestClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    isLinkUpRequestSent: Boolean? = null
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Row(modifier = Modifier.fillMaxWidth()) {

            AsyncImage(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = secondary,
                        shape = CircleShape
                    ),
                model = user.userImage,
                contentDescription = null,
                contentScale = ContentScale.Crop
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = user.userName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            if (currentUser == user._id) {
                IconButton(
                    onClick = {
                        navHostController.navigate("EDIT_PROFILE")
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = primary
                    )
                }
            }

        }


        if (user._id != currentUser) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min) // ✅ Makes dividers take height of tallest Column
                    .border(
                        width = 1.dp,
                        color = White400,
                        shape = RoundedCornerShape(5.dp)
                    )
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {

                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            onLinkUpRequestClick?.invoke()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.user_add),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = if (isLinkUpRequestSent == null) Black900 else Black500
                        )
                    )
                    Text(
                        text = if (isLinkUpRequestSent == true) "Remove Connection"
                        else if (isLinkUpRequestSent == false) "Withdraw request"
                        else if (isLinkUpRequestSent == null) "Connect" else "",
                        style = typography.labelMedium,
                        color = if (isLinkUpRequestSent == null) Black900 else Black500
                    )
                }

                VerticalDivider(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp),
                    color = White400
                )

                Column(
                    modifier = Modifier.clickable(
                        onClick = {
                            onMessageClick?.invoke()
                        },
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.chatbubble_outline),
                        contentDescription = null
                    )
                    Text(
                        text = "Message",
                        style = typography.labelMedium
                    )
                }

            }
        }

        Spacer(modifier = Modifier.height(12.dp))

    }


}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorizontalTabComponent(pageIndex: @Composable (Int) -> Unit) {

    val tabs = listOf("About", "Posts")
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { tabs.size })
    val scope = rememberCoroutineScope()

    PrimaryTabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = White900,
        divider = {
            HorizontalDivider(
                color = White400
            )
        },
        indicator = {
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(
                    selectedTabIndex = pagerState.currentPage,
                    matchContentSize = false
                ),
                color = primary,
                height = 1.dp
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

        pageIndex(page)

    }


}


@Composable
fun UserAbout(userBasicProfileDTO: UserBasicProfileDTO?) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(30.dp)
    ) {

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Bio", fontWeight = FontWeight.Bold)
            Text(text = userBasicProfileDTO?.userBio ?: "")
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "Interest", fontWeight = FontWeight.Bold)
            InterestComponent(
                interestList = interestList
            )
        }

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        CampusWidget(
            campus = userBasicProfileDTO?.campus
        )


    }


}

@Composable
fun CampusWidget(campus: Campus?) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        Text(text = "Campus", fontWeight = FontWeight.Bold)

        if (campus == null){
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text(text = "Update Campus", modifier = Modifier.align(Alignment.Center), color = Black400)
            }
        }else{
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AsyncImage(
                    model = campus.university?.logo ?: "",
                    contentDescription = null,
                    placeholder = painterResource(R.drawable.landscape_placeholder_svgrepo_com),
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(5.dp)),
                )
                Column {
                    Text(
                        text = campus.university?.university ?: "",
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = campus.collegeName,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                    Text(text = campus.fieldOfStudy)


                    if (campus.courseStart != null && campus.courseEnd != null) {
                        Text(
                            text = "${timeMillsToString(campus.courseStart)} to ${
                                timeMillsToString(
                                    campus.courseEnd
                                )
                            }"
                        )
                    }


                    Text(text = campus.campusCode)
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostScreenComponent(
    navHostController: NavHostController,
    postViewModel: PostViewModel,
    navigationViewModel: NavigationViewModel,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    currentUser: String,
    campusId: String?,
    context: Context
) {

    LaunchedEffect(Unit) {
        postViewModel.fetchPostById(currentUser, campusId)
    }

    val postState = postViewModel.postState.collectAsState().value

    val lazyColumnState = rememberLazyListState()

    HideBottomBar(
        navigationViewModel,
        lazyColumnState
    )

    when {

        postState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    color = primary
                )
            }
        }

        postState.postData.isNotEmpty() -> {

            LazyColumn(
                state = lazyColumnState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                postsLazyColumn(
                    postData = postState.postData,
                    navHostController = navHostController,
                    postViewModel = postViewModel,
                    bottomSharedViewModel = bottomSheetSharedViewModel,
                    context = context,
                    onDotMenuClick = {

                    }
                )
            }
        }

        postState.error.isNotEmpty() -> {

        }

    }
    StatusScreen(
        isActive = postState.postData.isEmpty(),
        text = "No Posts Yet"
    )

}

