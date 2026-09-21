package com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextAlign
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
import com.iota.campusX.Feature.UserProfile.ui.Components.AuraStatsDialog
import com.iota.campusX.Feature.UserProfile.ui.Components.AuraTransactionsDialog
import com.iota.campusX.Feature.UserProfile.ui.Components.ProfileCompletionSection
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileViewModel
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.EditProfile
import com.iota.campusX.Navigation.HideBottomBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Feature.Post.domain.attachment.VideoAttachmentDto
import com.iota.campusX.Feature.Post.domain.attachment.DocumentAttachmentDto
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Navigation.VideoView
import com.iota.campusX.Navigation.PdfView
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.Setting
import com.iota.campusX.R
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentDto
import com.iota.campusX.Utils.sharePost
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.UIComponents.AppTabRow
import com.iota.campusX.ui.UIComponents.BasicDetailRow
import com.iota.campusX.ui.UIComponents.CampusWidget
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
    val context = androidx.compose.ui.platform.LocalContext.current
    val editProfileViewModel: EditProfileViewModel = koinViewModel()

    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val effectiveUserId = userId ?: profileState.profile?.uid

    val pullToRefreshState = rememberPullToRefreshState()
    val isRefreshing by profileViewModel.isRefreshing.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val snackBarHostState = remember { SnackbarHostState() }
    val postLazyColumnState = rememberLazyListState()
    HideBottomBar(navigationViewModel, postLazyColumnState)

    val tabList = remember { listOf("View & Edit", "Activity", "Collabs") }

    LaunchedEffect(userId) {
        profileViewModel.load(userId)
    }

    LaunchedEffect(Unit) {
        auraViewModel.fetchAuraInfo()
    }

    val postState = postFeedViewModel.userPosts.collectAsLazyPagingItems()

    val collabViewModel: CollabViewModel = koinViewModel()
    val userCollabs by collabViewModel.userCollabsState.collectAsStateWithLifecycle()
    val deleteCollabState by collabViewModel.deleteCollabState.collectAsStateWithLifecycle()

    val auraTransactions by auraViewModel.transactions.collectAsStateWithLifecycle()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showAuraStats by remember { mutableStateOf(false) }
    var showAuraHistory by remember { mutableStateOf(false) }
    var collabToDelete by remember { mutableStateOf<CollabResponse?>(null) }

    var showReplyBottomSheet by remember { mutableStateOf(false) }
    var replyPostId by remember { mutableStateOf<String?>(null) }
    val replyBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    LaunchedEffect(deleteCollabState) {
        if (deleteCollabState is UiState.Success) {
            snackBarHostState.showSnackbar("Collaboration deleted successfully")
            effectiveUserId?.let { collabViewModel.getCollabsByUserId(userId = it) }
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

    LaunchedEffect(selectedTabIndex, effectiveUserId) {
        when(selectedTabIndex){
            1 -> {
                effectiveUserId?.let {
                    postFeedViewModel.getUserPost(userId = it)
                }
            }
            2 -> {
                effectiveUserId?.let {
                    collabViewModel.getCollabsByUserId(userId = it)
                }
            }
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Profile", style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    profileState.profile?.isCurrentUser?.let {
                        if(!it){
                            IconButton(onClick = {navHostController.popBackStack()}) {
                                Icon(
                                    imageVector = Icons.Default.ArrowBack,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                },
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { profileViewModel.refreshProfile(userId) },
            state = pullToRefreshState,
            modifier = Modifier.padding(innerPadding),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = postLazyColumnState
            ) {

                item {
                    RedesignedProfileHeader(
                        modifier = Modifier.fillMaxWidth(),
                        user = profileState.profile?.baseProfile,
                        onEditNameClick = {
                            navHostController.navigate(EditProfile())
                            editProfileViewModel.onEditProfileEvent(EditProfileActions.EditBasicDetails(it))
                        },
                        onAddPhotoClick = {
                            navHostController.navigate(EditProfile(editType = ProfileEdit.PROFILE_SCREEN.name))
                        },
                        onAuraClick = {
                            showAuraStats = true
                        },
                        isCurrentUser = profileState.profile?.isCurrentUser ?: false,
                        auraPoints = profileState.profile?.aura
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

                        profileState.profile?.let {
                            viewAndEditTabContent(
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
                        } else if (postState.loadState.refresh is LoadState.Error) {
                            item {
                                ActivityErrorState(
                                    message = (postState.loadState.refresh as LoadState.Error).error.localizedMessage ?: "Failed to load activity",
                                    onRetry = { postState.retry() }
                                )
                            }
                        } else if (postState.loadState.refresh is LoadState.NotLoading && postState.itemCount == 0) {
                            item {
                                ActivityEmptyState(isCurrentUser = profileState.profile?.isCurrentUser ?: false)
                            }
                        }

                        items(postState.itemCount) { index ->
                            val post = postState[index]
                            if (post != null) {
                                FeedItem(
                                    feedItem = post,
                                    handlers = { action ->
                                        when (action) {
                                            is PostAction.ViewPostVisualContent -> {
                                                val attachment = post.attachment
                                                if (attachment is VideoAttachmentDto) {
                                                    navHostController.navigate(VideoView(videoUrl = attachment.videoUrl, thumbnailUrl = attachment.thumbnailUrl))
                                                } else if (attachment is DocumentAttachmentDto) {
                                                    navHostController.navigate(PdfView(pdfUrl = attachment.url, fileName = attachment.name))
                                                } else {
                                                    val initialImage = (attachment as? ImageAttachmentDto)?.images?.getOrNull(action.initialIndex)
                                                    navHostController.navigate(PostView(postId = post.postId, postImage = initialImage, initialIndex = action.initialIndex))
                                                }
                                            }
                                            is PostAction.Share -> {
                                                val imageUrl = when (val attachment = post.attachment) {
                                                    is ImageAttachmentDto -> attachment.images.firstOrNull()
                                                    is VideoAttachmentDto -> attachment.thumbnailUrl
                                                    is DocumentAttachmentDto -> attachment.thumbnailUrl
                                                    else -> null
                                                }
                                                scope.launch {
                                                    sharePost(context, post.postId, post.caption, imageUrl)
                                                }
                                            }
                                            else -> {
                                                postFeedViewModel.onPostEvent(action)
                                            }
                                        }
                                    },
                                    onReplyClick = { p ->
                                        replyPostId = p.postId
                                        showReplyBottomSheet = true
                                        scope.launch { replyBottomSheetState.show() }
                                    },
                                    onMoreClick = {
                                        menuActionViewModel.showMenu(
                                            menuController = menuController,
                                            context = MenuContext(
                                                id = it.postId,
                                                type = ContentType.POST,
                                                isOwner = it.author.isCurrentUser
                                            )
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
                                val collabs = (userCollabs as UiState.Success<List<CollabResponse>>).data
                                if (collabs.isEmpty()) {
                                    item {
                                        CollabEmptyState(isCurrentUser = profileState.profile?.isCurrentUser ?: false)
                                    }
                                } else {
                                    collabsSection(
                                        posts = collabs,
                                        onCardClick = {
                                            navHostController.navigate(CollabDetail(it))
                                        },
                                        onDeleteClick = { collab ->
                                            collabToDelete = collab
                                            showDeleteDialog = true
                                        }
                                    )
                                }
                            }

                            is UiState.Error -> {
                                item {
                                    ActivityErrorState(
                                        message = (userCollabs as UiState.Error).message,
                                        onRetry = { 
                                            effectiveUserId?.let { collabViewModel.getCollabsByUserId(userId = it) }
                                        }
                                    )
                                }
                            }

                            else -> {}
                        }
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
                        menuActionViewModel.showMenu(
                            menuController = menuController,
                            context = MenuContext(
                                id = it.id,
                                type = ContentType.REPLY,
                                isOwner = it.author.isCurrentUser
                            )
                        )
                    },
                    onAction = {

                    }
                )
            }
        }

        if (showAuraStats && profileState.profile?.aura != null) {
            AuraStatsDialog(
                aura = profileState.profile!!.aura,
                transactions = auraTransactions,
                onDismiss = { showAuraStats = false },
                onViewHistory = {
                    showAuraStats = false
                    showAuraHistory = true
                }
            )
        }

        if (showAuraHistory) {
            AuraTransactionsDialog(
                transactions = auraTransactions,
                onDismiss = { showAuraHistory = false }
            )
        }
    }
}


@Composable
fun ActivityEmptyState(isCurrentUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.file_text),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isCurrentUser) "No posts yet" else "No posts yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = if (isCurrentUser) "Share your thoughts or questions with the community!" else "This user hasn't shared any activity yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CollabEmptyState(isCurrentUser: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.hands_together),
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No collabs yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (isCurrentUser) "Start collaborating on projects and hackathons!" else "This user hasn't joined any collaborations yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ActivityErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
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


fun LazyListScope.viewAndEditTabContent(
    user: ProfileResponse,
    auraViewModel: AuraViewModel,
    onEditClick:(EditProfileActions)-> Unit,
) {
    item { Spacer(modifier = Modifier.padding(4.dp)) }
    
    if (user.isCurrentUser) {
        item {
            ProfileCompletionSection(
                userProfile = user,
                onActionClick = onEditClick
            )
        }
    }
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
                isVerified = false
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
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    item {
        ProfileSectionCard(
            title = "Profile summary",
            isCurrentUser = user.isCurrentUser,
            onActionClick = {
                onEditClick(EditProfileActions.EditSummary(user.baseProfile.summary))
            }
        ) {
        val summary = if (user.baseProfile.summary.isEmpty() && user.isCurrentUser){
                "Tell your story — what you’re passionate about, what you enjoy building, what you’re looking to learn, and where you want to go next.\n"
            }else if (user.baseProfile.summary.isNotEmpty() && user.isCurrentUser) {
                user.baseProfile.summary
            } else {
                "No summary added yet"
        }
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium
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

    item {
        val hasOpenTo = !user.matchPreferences.isNullOrEmpty()
        ProfileSectionCard(
            title = "Open to",
            isCurrentUser = user.isCurrentUser,
            onActionClick = {
                onEditClick(EditProfileActions.EditOpenTo(user.matchPreferences))
            }
        ) {
            if (hasOpenTo) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    user.matchPreferences.forEach { item ->
                        SkillChipRedesigned(text = item.title)
                    }
                }
            } else {
                Text(
                    text = if (user.isCurrentUser) "What are you looking for? (e.g. Hackathons, Projects, Study)" else "No preferences added yet",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }

//    item {
//        ProfileSectionCard(
//            title = "Internships",
//            isCurrentUser = user.isCurrentUser,
//            onActionClick = {
//                // Future: add internship action
//            }
//        ) {
//            // Since internship field is not yet in ProfileResponse, we show Coming Soon beautifully
//            com.iota.campusX.ui.UIComponents.ComingSoonWidget(
//                title = "Professional Experience",
//                description = "Soon you will be able to add and showcase your internships and work history.",
//                iconRes = R.drawable.briefcase__1_
//            )
//        }
//    }
}
