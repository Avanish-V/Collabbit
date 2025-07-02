package com.iota.campusX.Screens.Profile

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
import com.iota.campusX.Feature.Post.domain.Models.UserDetail
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.BasicProfileDTO
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.R
import com.iota.campusX.Screens.Home.BottomSheet.BottomSheetSharedViewModel
import com.iota.campusX.Screens.Home.BottomSheet.PostDotOptionBottomSheet
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.StatusScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.vibrate
import com.iota.campusX.ui.UIComponents.CourseDuration
import com.iota.campusX.ui.UIComponents.ErrorScreen
import com.iota.campusX.ui.UIComponents.PostCard
import com.iota.campusX.ui.theme.Black300
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.Black900
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import kotlinx.coroutines.launch
import java.util.Calendar

// ProfileScreen.kt
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    profileViewModel: UserProfileViewModel,
    googleSignInViewModel: AuthViewModel,
    navigationViewModel: NavigationViewModel,
    replyViewModel: ReplyViewModel,
    profileTypeViewModel: ProfileTypeViewModel
) {

//

    val useridByFeed =
        navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID")
    val currentDestination = navHostController.currentDestination?.route

    LaunchedEffect(Unit) {
        profileViewModel.getProfileIdByPost(
            userIdByFeed = useridByFeed ?: "",
            loggedInUserId = googleSignInViewModel.userId(),
            currentDestination = currentDestination ?: ""
        )
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { 2 })
    val currentUser = googleSignInViewModel.userId()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val postLazyColumnState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }

    // ✅ Use shared ViewModel properly
    val bottomSheetViewModel: BottomSheetSharedViewModel = viewModel()
    val bottomSheetData by bottomSheetViewModel.bottomSheetState.collectAsState()

    val userType by profileViewModel.userType.collectAsState()

    // ✅ Avoid unnecessary fetching
    LaunchedEffect(Unit) {
        profileViewModel.getConnectionCount(currentUser)

    }

    // ✅ Collect profile states
    val userBaseProfile by profileViewModel.userBaseProfile.collectAsState()
    val profileByIdState by profileViewModel.profileById.collectAsState()
    val connectionsCountState by profileViewModel.connectionCount.collectAsState()
    val hasConnection by profileViewModel.hasConnection.collectAsState()
    val modifyState by profileViewModel.modifyState.collectAsState()

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
    HideBottomBar(navigationViewModel, postLazyColumnState)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White,
                    scrolledContainerColor = Color.White
                ),
                actions = {
                    if (userType == UserType.Owner) {
                        IconButton(onClick = { navHostController.navigate("SETTING") }) {
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
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) },
        containerColor = secondary
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            state = postLazyColumnState
        ) {

            item {
                ProfileHeader(
                    snackbarHostState = snackBarHostState,
                    modifier = Modifier.fillMaxSize(),
                    headerHeight = { headerHeightDp = it },
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
                    hasConnection = hasConnection
                )
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }

            stickyHeader {
                PrimaryTabRow(
                    modifier = Modifier.onGloballyPositioned {
                        tabRowHeightDp = with(density) { it.size.height.toDp() }
                    },
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = Color.White,
                    divider = { HorizontalDivider(color = White400) },
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
                    listOf("About", "Posts").forEachIndexed { index, title ->
                        Tab(
                            text = {
                                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            },
                            selected = pagerState.currentPage == index,
                            onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                            selectedContentColor = Black800,
                            unselectedContentColor = Black400
                        )
                    }
                }
            }

            item {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(horizontalPagerHeight)
                ) { page ->
                    when (page) {
                        0 -> profileData?.let {
                            UserAbout(
                                userBasicProfileDTO = it,
                                navHostController = navHostController,
                                isCurrentUser = userType == UserType.Owner
                            )
                        }
                        1 -> PostScreenComponent(
                            navHostController = navHostController,
                            postViewModel = postViewModel,
                            userProfileViewModel = profileViewModel,
                            navigationViewModel = navigationViewModel,
                            bottomSheetSharedViewModel = bottomSheetViewModel,
                            currentUser = profileData?.id ?: "",
                            campusId = profileData?.campus?.campusCode,
                            context = context
                        )
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
                        Text("Delete Post", fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
                        Text("Are you sure you want to delete this post?", textAlign = TextAlign.Center, modifier = Modifier.padding(12.dp))
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
                                    CircularProgressIndicator(color = primary, modifier = Modifier.size(24.dp))
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



@Composable
fun ProfileHeader(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier,
    headerHeight:(Dp)-> Unit,
    navHostController: NavHostController,
    user: UserDetail,
    userType: UserType,
    onLinkUpRequestClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    connectionsCount: Int = 0,
    hasConnection:UiState<Boolean?>
) {

    val density = LocalDensity.current
    var headerHeightDp by remember { mutableStateOf(0.dp) }


    Column(
        modifier = modifier
            .onGloballyPositioned {
                val heightPx = it.size.height
                headerHeightDp = with(density) { heightPx.toDp() }
                headerHeight(headerHeightDp)
            }
            .background(White900)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {

        Box(modifier = Modifier.fillMaxWidth(),contentAlignment = Alignment.TopEnd){
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                Box(
                    modifier = Modifier.size(120.dp),
                    contentAlignment = Alignment.BottomEnd

                ) {

                    AsyncImage(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .border(width = 6.dp, color = Color.White, shape = CircleShape)
                            .shadow(
                                elevation = 12.dp,
                                shape = CircleShape,
                                ambientColor = primary,
                                spotColor = primary
                            ),
                        model = user.userImage,
                        contentDescription = null,
                        contentScale = ContentScale.Crop
                    )

                    if (userType == UserType.Owner) {

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(color = Color.White)
                                .clickable(
                                    onClick = {
                                        navHostController.navigate(Routes.Main.EditProfile.routes)
                                            .apply {
                                                navHostController.currentBackStackEntry?.savedStateHandle?.set(
                                                    "PROFILE_EDIT",
                                                    ProfileEdit.PROFILE_SCREEN
                                                )
                                            }
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )

                        ) {
                            Icon(
                                modifier = Modifier.padding(3.dp),
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Back",
                                tint = primary
                            )
                        }
                    }
                }


                Text(
                    text = user.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

        }



//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            Row (modifier = Modifier.weight(1f)){
//
//                Column (
//                    Modifier.weight(1f),
//                    verticalArrangement = Arrangement.spacedBy(12.dp)
//                ){
//
//                    Row (verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(12.dp)){
//                        Image(
//                            painter = painterResource(R.drawable.user_add__1_),
//                            modifier = Modifier.size(24.dp),
//                            contentDescription = null
//                        )
//                        Text(text = connectionsCount.toString(), fontWeight = FontWeight.Bold)
//                    }
//                    Text("Connections")
//
//                }
//
//                Column (Modifier.weight(1f),verticalArrangement = Arrangement.spacedBy(12.dp)){
//
//                    Row (verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(12.dp)){
//                        Image(
//                            painter = painterResource(R.drawable.fire_flame_curved),
//                            modifier = Modifier.size(24.dp),
//                            contentDescription = null
//                        )
//                        Text(text = connectionsCount.toString(), fontWeight = FontWeight.Bold)
//                    }
//                    Text("Aura")
//
//                }
//
//                Column (Modifier.weight(1f)){
//
//                    Row (verticalAlignment = Alignment.CenterVertically,horizontalArrangement = Arrangement.spacedBy(12.dp)){
//                        Image(
//                            painter = painterResource(R.drawable.fire_flame_curved),
//                            modifier = Modifier.size(24.dp),
//                            contentDescription = null
//                        )
//                        Text(text = connectionsCount.toString(), fontWeight = FontWeight.Bold)
//                    }
//                    Text("Posts")
//
//                }
//
//            }
//
//        }

        TextButton( onClick = {
            navHostController.navigate(Routes.Main.Connections.routes).apply {
                navHostController.currentBackStackEntry?.savedStateHandle?.set("USER_ID", user.id)
            }
        }
        ) {
            Text(text = "$connectionsCount Connections",fontWeight = FontWeight.Bold)
        }

        Log.d("ProfileHeader", "ProfileHeader: $userType")

        if (userType == UserType.User) {

            Row {
                Button(
                    onClick = { onLinkUpRequestClick?.invoke() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .shadow(
                            elevation = 20.dp,
                            shape = RoundedCornerShape(6.dp),
                            ambientColor = primary,
                            spotColor = primary
                        ),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    when(hasConnection){

                        is UiState.Success -> {

                            val connectionText = when (hasConnection.data) {
                                null -> "Connect"
                                true -> "Remove"
                                false -> "Requested"
                            }

                            Image(
                                modifier = Modifier.size(24.dp),
                                painter = painterResource(R.drawable.user_add),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    color = if (hasConnection.data == null || hasConnection.data == true) White900 else Black500
                                )
                            )
                            Spacer(
                                modifier = Modifier.width(12.dp)
                            )
                            Text(
                                text = connectionText,
                                style = typography.labelMedium,
                                color = if (hasConnection.data == null || hasConnection.data == true) White900 else Black500
                            )
                        }
                        is UiState.Loading->{
                            CircularProgressIndicator(
                                color = Black900,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        is UiState.Error->{
                            LaunchedEffect(Unit) {
                                snackbarHostState.showSnackbar(hasConnection.message)
                            }
                        }
                        else -> {}
                    }
                }
                Spacer(
                    modifier = Modifier.width(12.dp)
                )
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .align(Alignment.CenterVertically),
                    onClick = { onMessageClick?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Black300),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = White900,
                        contentColor = Black900
                    )
                ) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.send_2),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(
                            color = Black500
                        )
                    )
                    Spacer(
                        modifier = Modifier.width(12.dp)
                    )
                    Text("Message", color = Black500)
                }
            }

        }

        Spacer(modifier = Modifier.height(12.dp))

    }


}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserAbout(userBasicProfileDTO: BasicProfileDTO, navHostController: NavHostController, isCurrentUser: Boolean) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = White900)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
//        space b
    ) {

        ProfileComponent(
            title = "Bio",
            onEditClick = {
                navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set("PROFILE_EDIT", ProfileEdit.EDIT_ABOUT_SCREEN)
                }
            },
            body = {
                if (userBasicProfileDTO.userBio.isEmpty()) return@ProfileComponent
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = userBasicProfileDTO.userBio.toString())

            },
            contentDescription = "BIO",
            isCurrentUser = isCurrentUser,
            isContentExist = userBasicProfileDTO.userBio.isEmpty()
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        ProfileComponent(
            title = "Interests",
            onEditClick = {
                navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set("PROFILE_EDIT", ProfileEdit.EDIT_INTERESTS)
                }
                          },
            body = {
                if (userBasicProfileDTO.interests.isEmpty()) return@ProfileComponent
                Spacer(modifier = Modifier.height(12.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    userBasicProfileDTO.interests.forEach {
                        AssistChip(
                            onClick = {
                                null
                            },
                            label = {
                                Text(
                                    it,
                                    modifier = Modifier.padding(10.dp),
                                    color = Color.Black
                                )
                            },
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color.LightGray
                            ),

                            )
                    }
                }

            },
            contentDescription = "INTERESTS",
            isCurrentUser = isCurrentUser,
            isContentExist = userBasicProfileDTO.interests.isEmpty()
        )

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = White400
        )

        ProfileComponent(
            title = "Campus Detail",
            onEditClick = {
                navHostController.navigate(Routes.Main.EditProfile.routes).apply {
                    navHostController.currentBackStackEntry?.savedStateHandle?.set("PROFILE_EDIT", ProfileEdit.EDIT_CAMPUS)
                }
            },
            body = {

               if (userBasicProfileDTO.campus == null) return@ProfileComponent
               Spacer(modifier = Modifier.height(12.dp))
               CampusWidget(
                   campus = userBasicProfileDTO.campus
               )
            },
            contentDescription = "CAMPUS",
            isCurrentUser = isCurrentUser,
            isContentExist = userBasicProfileDTO.campus == null
        )

    }


}

@Composable
fun CampusWidget(campus: Campus?) {

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

        if (campus == null){
            Box(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp), contentAlignment = Alignment.Center) {
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
                            text = "${campus.courseStart.month + campus.courseStart.year} - ${campus.courseEnd.month + campus.courseEnd.year}"

                        )
                    }

                    campus.campusCode?.let { Text(text = it) }
                }
            }
        }

    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PostScreenComponent(
    navHostController: NavHostController,
    postViewModel: PostFeedViewModel,
    userProfileViewModel: UserProfileViewModel,
    navigationViewModel: NavigationViewModel,
    bottomSheetSharedViewModel: BottomSheetSharedViewModel,
    currentUser: String,
    campusId: String?,
    context: Context
) {

    LaunchedEffect(Unit) {
        postViewModel.fetchPostById(
            currentUser,
            campusId,
            feedMode = FeedMode.GLOBAL
        )
    }

    val postById = postViewModel.postById.collectAsState().value

    Box(Modifier.fillMaxSize()){

        Column (modifier = Modifier.fillMaxSize()){

            when (postById) {
                is UiState.Loading -> {
                    LoadingUI(true)
                }

                is UiState.Success -> {

                    val sortedPost = postById.data.sortedByDescending { it.createdAt }

                    if (sortedPost.isEmpty())
                        StatusScreen(
                            isActive = true,
                            text = "No Posts"
                        )
                    return

                    sortedPost.forEach {

                        PostCard(
                            feedViewModel = postViewModel,
                            bottomSheetSharedViewModel = bottomSheetSharedViewModel,
                            profileViewModel = userProfileViewModel,
                            onPostClick = {
                                navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("POST_ID", it.postId)
                                }
                            },
                            onReplyClick = {
                                navHostController.navigate(Routes.Main.ReplyPost.routes).apply {
                                    navHostController.currentBackStackEntry?.savedStateHandle?.set<String>("POST_ID", it.postId)
                                }
                            },
                            post = it,
                            navHostController = navHostController,
                            onPollSelect = {

                            }
                        )

                    }

                }

                is UiState.Error -> {
                    ErrorScreen(
                        text = "Something went wrong",
                        image = R.drawable.landscape_placeholder_svgrepo_com,
                        buttonText = "Try again",
                        onReTry = {
                            postViewModel.fetchPostById(
                                currentUser,
                                campusId,
                                feedMode = FeedMode.GLOBAL
                            )
                        }
                    )
                }

                is UiState.Idle -> {}
            }

        }
    }
}

fun currentYear(courseStart: CourseDuration, courseEnd: CourseDuration) {

    val currentMonth = Calendar.MONTH
    val currentYear = Calendar.YEAR


}