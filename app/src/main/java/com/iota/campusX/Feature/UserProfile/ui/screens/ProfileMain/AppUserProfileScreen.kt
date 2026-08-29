package com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.iota.campusX.Feature.Collab.data.model.CollabResponse
import com.iota.campusX.Feature.Collab.presentation.CollabCard
import com.iota.campusX.Feature.Collab.presentation.CollabViewModel
import com.iota.campusX.Feature.Collab.presentation.components.CollabShimmerItem
import com.iota.campusX.Feature.Post.presentation.components.FeedItem
import com.iota.campusX.Feature.Post.presentation.components.FeedShimmerItem
import com.iota.campusX.Feature.Post.presentation.components.PostAction
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.ContentType
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuContext
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Reply.presentation.ReplyBottomSheet
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.presentation.AuraViewModel
import com.iota.campusX.Feature.UserProfile.ui.Components.AuraStreakCard
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileViewModel
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.EditProfile
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.Setting
import com.iota.campusX.Navigation.rememberScrollContext
import com.iota.campusX.R
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.BasicDetailRow
import com.iota.campusX.ui.UIComponents.CampusWidget
import com.iota.campusX.ui.UIComponents.CompleteProfileSection
import com.iota.campusX.ui.UIComponents.ProfileSectionCard
import com.iota.campusX.ui.UIComponents.RedesignedProfileHeader
import com.iota.campusX.ui.UIComponents.SkillChipRedesigned
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

// AppUserProfileScreen.kt
@SuppressLint("ConfigurationScreenWidthHeight")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppUserProfile(
    userId: String?,
    navHostController: NavHostController,
    postFeedViewModel: PostFeedViewModel = koinViewModel(),
    profileViewModel: UserProfileViewModel,
    auraViewModel: AuraViewModel = koinViewModel(),
    navigationViewModel: NavigationViewModel,
    menuController: MenuController,
    menuActionViewModel: MenuActionViewModel
) {

   val editProfileViewModel: EditProfileViewModel = koinViewModel()

   val profile by profileViewModel.uiState.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackBarHostState = remember { SnackbarHostState() }
    val postLazyColumnState = rememberLazyListState()
    val scrollContext = rememberScrollContext(navigationViewModel)
    HideBottomBar(navigationViewModel, postLazyColumnState)

    val tabList = remember { listOf("View & Edit", "Activity", "Collabs") }

    LaunchedEffect(Unit) {
        auraViewModel.fetchAuraInfo()
    }

    val postState = postFeedViewModel.userPosts.collectAsLazyPagingItems()

    val collabViewModel: CollabViewModel = koinViewModel()
    val userCollabs by collabViewModel.userCollabsState.collectAsStateWithLifecycle()
    val deleteCollabState by collabViewModel.deleteCollabState.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var collabToDelete by remember { mutableStateOf<CollabResponse?>(null) }

    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyPostId by remember { mutableStateOf<String?>(null) }
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(deleteCollabState) {
        if (deleteCollabState is UiState.Success) {
            snackBarHostState.showSnackbar("Collaboration deleted successfully")
            collabViewModel.getCollabsByUserId(userId = userId ?: "")
            collabViewModel.resetDeleteState()
        } else if (deleteCollabState is UiState.Error) {
            snackBarHostState.showSnackbar((deleteCollabState as UiState.Error).message)
            collabViewModel.resetDeleteState()
        }
    }

    if (showDeleteDialog && collabToDelete != null) {

        AlertDialogWidget(
            title = "Delete Collaboration",
            description = "Are you sure you want to delete this collaboration?",
            onDismiss = { showDeleteDialog = false },
            onPositiveClick = {
                collabToDelete?.let { collabViewModel.deleteCollab(it.id) }
            },
            positiveButtonText = "Delete",
            negativeButtonText = "Cancel",
            showLoading = deleteCollabState is UiState.Loading,
        )
    }

    LaunchedEffect(selectedTabIndex, userId, profile.profile) {
        when(selectedTabIndex){
            0 -> {
                profileViewModel.load(userId)
            }
            1 -> {
                val targetUserId = userId ?: profile.profile?.uid
                targetUserId?.let {
                    postFeedViewModel.getUserPost(userId = it)
                }
            }
            2 -> {
                val targetUserId = userId ?: profile.profile?.uid
                targetUserId?.let {
                    collabViewModel.getCollabsByUserId(userId = it)
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile", style = MaterialTheme.typography.headlineMedium

                ) },
                actions = {
                    IconButton(
                        onClick = { navHostController.navigate(Setting) }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.settings), 
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(modifier = Modifier.padding(bottom = 80.dp), hostState = snackBarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection).nestedScroll(scrollContext)
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            state = postLazyColumnState
        ) {

            item {
                RedesignedProfileHeader(
                    modifier = Modifier.fillMaxWidth(),
                    user = profile.profile?.baseProfile,
                    onEditNameClick = {
                        navHostController.navigate(EditProfile())
                        editProfileViewModel.onEditProfileEvent(EditProfileActions.EditBasicDetails(it))
                    },
                    onAddPhotoClick = {
                        navHostController.navigate(EditProfile(editType = ProfileEdit.PROFILE_SCREEN.name))
                    },
                    isCurrentUser = profile.profile?.isCurrentUser ?: false,
                    auraPoints = profile.profile?.aura
                )
            }

            stickyHeader(key = "tab_header") {
                AppTabRow(
                    selectedIndex = selectedTabIndex,
                    tabList = tabList,
                    isScrollable = true,
                    onTabSelected = { selectedTabIndex = it }
                )
            }

            when (selectedTabIndex) {
                0 -> {

                    profile.profile?.let {
                        ViewAndEditTabContent(
                            user = it,
                            auraViewModel = auraViewModel,
                            onEditClick = {
                                editProfileViewModel.onEditProfileEvent(it)
                                navHostController.navigate(EditProfile())
                            }
                        )
                    }

                }

                1 -> {
                    if (postState.loadState.refresh is LoadState.Loading) {
                        items(5) {
                            FeedShimmerItem()
                        }
                    }
                    items(postState.itemCount) { index ->
                        val post = postState[index]
                        if (post != null) {
                            FeedItem(
                                feedItem = post,
                                handlers = { action ->
                                    if (action is PostAction.ViewPostVisualContent) {
                                        navHostController.navigate(PostView(postId = post.postId))
                                    } else {
                                        postFeedViewModel.onPostEvent(action)
                                    }
                                },
                                onReplyClick = { p ->
                                    replyPostId = p.postId
                                    showReplyBottomSheet = true
                                    scope.launch { replyBottomSheetState.show() }
                                },
                                onMoreClick = {
                                    menuController.show(
                                        context = MenuContext(
                                            id = it.postId,
                                            type = ContentType.POST,
                                            isOwner = it.author.isCurrentUser
                                        )
                                    )
                                    menuActionViewModel.loadMenu(
                                        menuContext = MenuContext(
                                            id = it.postId,
                                            type = ContentType.POST,
                                            isOwner = it.author.isCurrentUser
                                        ),
                                    )
                                },
                                onProfileClick = {
                                    if (it != userId) {
                                        navHostController.navigate(Profile(it))
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }

                2 -> {

                    when (userCollabs) {
                        is UiState.Loading -> {
                            items(5) {
                                CollabShimmerItem()
                            }
                        }

                        is UiState.Success -> {

                            collabsSection(
                                posts = (userCollabs as UiState.Success<List<CollabResponse>>).data,
                                onCardClick = {
                                    navHostController.navigate(CollabDetail(it))
                                },
                                onDeleteClick = { collab ->
                                    collabToDelete = collab
                                    showDeleteDialog = true
                                }
                            )
                        }

                        is UiState.Error -> {
                            item {
                                Text(
                                    text = (userCollabs as UiState.Error).message,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }

                        else -> {}
                    }
                }
            }

        }

        if (showReplyBottomSheet) {
            replyPostId?.let { postId ->
                ReplyBottomSheet(
                    postId = postId,
                    onDismiss = {
                        showReplyBottomSheet = false
                        replyPostId = null
                    },
                    sheetState = replyBottomSheetState,
                    onMoreClick = {
                        menuController.show(
                            MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            )
                        )
                        menuActionViewModel.loadMenu(
                            MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            ),
                        )
                    },
                    onAction = {

                    }
                )
            }
        }
    }
}

fun LazyListScope.collabsSection(
    posts: List<CollabResponse>,
    onCardClick:(String)-> Unit,
    onDeleteClick: (CollabResponse) -> Unit
) {
    items(posts) { collab ->
        CollabCard(
            collab = collab,
            onActionClick = { },
            onClick = { onCardClick.invoke(collab.id) },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            onAvatarClick = {},
            onDeleteClick = { onDeleteClick(collab) }
        )
    }
}


fun LazyListScope.ViewAndEditTabContent(
    user: ProfileResponse,
    auraViewModel: AuraViewModel,
    onEditClick:(EditProfileActions)-> Unit,
) {
    item { Spacer(modifier = Modifier.padding(6.dp)) }
    
//    if (user.isCurrentUser) {
//
//        item {
//            CompleteProfileSection(
//                userProfile = user,
//                onAddScoreClick = {
//
//                },
//                onAddInternshipClick = {
//
//                }
//            )
//        }
//    }
    // 2. Basic details card
    item {
        ProfileSectionCard(
            title = "Basic details",
            isCurrentUser = false,
            onActionClick = {

            }
        ) {
            BasicDetailRow(
                icon = Icons.Default.Email,
                text = user.contact.email,
                isVerified = user.contact.email.isNotEmpty()
            )

        }
    }

    item {
        val hasCampus = user.education != null
        ProfileSectionCard(
            title = "Education",
            isCurrentUser = user.isCurrentUser,
            onActionClick = {
                onEditClick(EditProfileActions.EditEducation(user.education))
            }
        ) {
            if (hasCampus) {
                CampusWidget(campus = user.education)
            } else {
                Text(
                    text = if (user.isCurrentUser) "Add your educational details" else "No educational details added yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }

    item {
        ProfileSectionCard(
            title = "Profile summary",
            isCurrentUser = user.isCurrentUser,
            onActionClick = {
                onEditClick(EditProfileActions.EditSummary(user.summary))
            }
        ) {
        val summary = if (user.summary.isNullOrEmpty() && user.isCurrentUser){
                "Put forward your educational and career journey in a few lines"
            }else if (!user.summary.isNullOrEmpty() && user.isCurrentUser) {
                user.summary
            } else {
                "No summary added yet"
        }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }


    item {
        val hasInterests = !user.skills.isNullOrEmpty()
        ProfileSectionCard(
            title = "Key skills",
            isCurrentUser = user.isCurrentUser,
            onActionClick = {
                onEditClick(EditProfileActions.EditSkills(user.skills))
            }
        ) {
            if (hasInterests) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    user.skills.forEach { skill ->
                        SkillChipRedesigned(text = skill.name)
                    }
                }
            } else {
                Text(
                    text = if (user.isCurrentUser)"Add your key skills (e.g. Kotlin, UI Design, Java)" else "No skills added yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
