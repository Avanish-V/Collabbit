package com.iota.campusX


import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.messaging.FirebaseMessaging
import androidx.navigation.navDeepLink
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Chats.presentation.ui.ChatScreen
import com.iota.campusX.Feature.Chats.presentation.ui.SendMessageScreen
import com.iota.campusX.Feature.Collab.presentation.CollabDetailScreen
import com.iota.campusX.Feature.Collab.presentation.CollabExploreScreen
import com.iota.campusX.Feature.Collab.presentation.CreateCollabScreen
import com.iota.campusX.Feature.Notificattion.presentation.NotificationScreen
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.Opportunities.presentation.CourseDetailScreen
import com.iota.campusX.Feature.Opportunities.presentation.OpportunitiesScreen
import com.iota.campusX.Feature.Opportunities.presentation.OpportunityDetailScreen
import com.iota.campusX.Feature.Post.presentation.create.CreatePostScreen
import com.iota.campusX.Feature.Post.presentation.create.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.edit.EditPostScreen
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuAction
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuBottomSheet
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuItem
import com.iota.campusX.Feature.UserProfile.domain.useCases.UpdateFcmTokenUseCase
import com.iota.campusX.Feature.UserProfile.presentation.AuraViewModel
import com.iota.campusX.Feature.UserProfile.presentation.CheckInEvent
import com.iota.campusX.Feature.UserProfile.ui.Components.AlreadyClaimedDialog
import com.iota.campusX.Feature.UserProfile.ui.Components.DailyAuraCheckInDialog
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileScreen
import com.iota.campusX.Feature.Society.presentation.SocietyScreen
import com.iota.campusX.Feature.Society.presentation.SocietyChatScreen
import com.iota.campusX.Feature.Society.presentation.SocietyInfoScreen
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.AppUserProfile
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
import com.iota.campusX.Navigation.AuthGraph
import com.iota.campusX.Navigation.BottomAppBar
import com.iota.campusX.Navigation.ChatList
import com.iota.campusX.Navigation.Collab
import com.iota.campusX.Navigation.CollabDetail
import com.iota.campusX.Navigation.Collaborations
import com.iota.campusX.Navigation.CommunityChat
import com.iota.campusX.Navigation.Connection
import com.iota.campusX.Navigation.CourseDetail
import com.iota.campusX.Navigation.Courses
import com.iota.campusX.Navigation.CreateCollab
import com.iota.campusX.Navigation.CreatePost
import com.iota.campusX.Navigation.EditPost
import com.iota.campusX.Navigation.EditProfile
import com.iota.campusX.Navigation.Home
import com.iota.campusX.Navigation.MainGraph
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Notification
import com.iota.campusX.Navigation.Opportunities
import com.iota.campusX.Navigation.OpportunityDetail
import com.iota.campusX.Navigation.PostView
import com.iota.campusX.Navigation.VideoView
import com.iota.campusX.Navigation.PdfView
import com.iota.campusX.Navigation.Profile
import com.iota.campusX.Navigation.ReplyPost
import com.iota.campusX.Navigation.SendMessage
import com.iota.campusX.Navigation.Setting
import com.iota.campusX.Navigation.SignIn
import com.iota.campusX.Navigation.Society
import com.iota.campusX.Navigation.SocietyHub
import com.iota.campusX.Navigation.SocietyInfo
import com.iota.campusX.Navigation.ViewProfile
import com.iota.campusX.Feature.Society.presentation.SocietyHubScreen
import com.iota.campusX.Feature.Society.presentation.SocietyScreen
import androidx.navigation.NavDestination.Companion.hasRoute
import com.iota.campusX.Navigation.Register
import com.iota.campusX.Navigation.CreateProfile
import com.iota.campusX.Navigation.Verification
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.iota.campusX.Navigation.navScreen
import com.iota.campusX.Navigation.shouldShowBottomBar
import com.iota.campusX.NetworkMonitor.ConnectivityUiState
import com.iota.campusX.NetworkMonitor.ConnectivityViewModel
import com.iota.campusX.Screens.Home.MainScreen
import com.iota.campusX.Screens.Home.PostViewScreen
import com.iota.campusX.Screens.Home.VideoViewScreen
import com.iota.campusX.Screens.Home.PdfViewScreen
import com.iota.campusX.Screens.Register.SignInScreen
import com.iota.campusX.Screens.Setting.SettingScreen
import com.iota.campusX.Screens.ShowcaseScreen
import com.iota.campusX.Screens.ShowcaseType
import com.iota.campusX.Utils.UiState
import com.iota.campusX.realtime.manager.RealtimeManager
import com.iota.campusX.ui.UIComponents.AlertDialogWidget
import com.iota.campusX.ui.theme.AppTheme
import com.jetpack.observeliveconnectivity.connectivityState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var appUpdateManager: AppUpdateManager

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        actionBar?.hide()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        checkForUpdate()
        
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )


        setContent {

            val realtimeManager = koinInject<RealtimeManager>()
            LaunchedEffect(Unit) {
                realtimeManager.start()
            }

            val authViewModel = koinInject<GoogleSignInViewModel>()
            val isLoggedIn by authViewModel.isLoggedIn.collectAsStateWithLifecycle()
            val viewModel: ConnectivityViewModel = koinViewModel()

            val snackBarHostState = remember { SnackbarHostState() }
            val navHostController = rememberNavController()
            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val connectivityState = connectivityState()
            val state by viewModel.uiState.collectAsState()
            val profileViewModel = koinInject<UserProfileViewModel>()
            val auraViewModel: AuraViewModel = koinViewModel()
            val updateFcmTokenUseCase = koinInject<UpdateFcmTokenUseCase>()
            val menuController: MenuController = koinInject()
            val menuActionViewModel: MenuActionViewModel = koinInject()

            var showCheckInDialog by remember { mutableStateOf(false) }
            var showAlreadyClaimedDialog by remember { mutableStateOf(false) }
            var checkInData by remember { mutableStateOf<CheckInEvent.Success?>(null) }

            LaunchedEffect(connectivityState.value) {
                when (state) {
                    ConnectivityUiState.Offline ->
                        snackBarHostState.showSnackbar(
                            "No network connection",
                            withDismissAction = true
                        )

                    ConnectivityUiState.ConnectedNoInternet ->
                        snackBarHostState.showSnackbar(
                            "Wi-Fi connected but no internet",
                            withDismissAction = true
                        )

                    ConnectivityUiState.CaptivePortal ->
                        snackBarHostState.showSnackbar("Captive portal — sign in required")

                    ConnectivityUiState.Online -> {}
                }
            }

            LaunchedEffect(navBackStackEntry, isLoggedIn) {
                val destination = navBackStackEntry?.destination

                if (destination != null) {
                    val isAuthRoute = destination.hasRoute(SignIn::class) || 
                                     destination.hasRoute(Register::class) || 
                                     destination.hasRoute(CreateProfile::class) || 
                                     destination.hasRoute(Verification::class) ||
                                     destination.hasRoute(AuthGraph::class)
                    
                    if (isLoggedIn == false && !isAuthRoute) {
                        navHostController.navigate(SignIn) {
                            popUpTo(navHostController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    } else if (isLoggedIn == true && isAuthRoute) {
                        navHostController.navigate(Home()) {
                            popUpTo(navHostController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            }

            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn == true) {
                    // Sequentially sync profile first, then claim daily check-in.
                    // This prevents the sync logic from overwriting the newly awarded aura points.
                    profileViewModel.syncProfileSuspending()
                    auraViewModel.claimDailyCheckIn(showAlreadyClaimedMessage = false)
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            task.result?.let { token ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    updateFcmTokenUseCase(token)
                                }
                            }
                        }
                    }
                }
            }

            LaunchedEffect(Unit) {
                auraViewModel.checkInEvent.collect { event ->
                    when (event) {
                        is CheckInEvent.Success -> {
                        }
                        is CheckInEvent.AlreadyClaimed -> {
                            // showAlreadyClaimedDialog = true
                        }
                        is CheckInEvent.Error -> {
                            android.util.Log.e("Aura", "Check-in failed: ${event.message}")
                        }
                    }
                }
            }

            AppTheme {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    NavHost(modifier = Modifier.fillMaxSize(), navController = navHostController, startDestination = if (authViewModel.getCurrentUser()) MainGraph else AuthGraph) {
                        navigation<AuthGraph>(
                            startDestination = SignIn,
                        ) {
                            composable<SignIn> {
                                SignInScreen(
                                    navHostController = navHostController,
                                )
                            }
                        }

                        navigation<MainGraph>(
                            startDestination = Home(),
                        ) {
                            navScreen<Home>(
                                deepLinks = listOf(
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/post/{postId}" },
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/post/{postId}/" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/post/{postId}" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/post/{postId}/" },
                                    navDeepLink { uriPattern = "https://collabbit.in/post/{postId}" },
                                    navDeepLink { uriPattern = "https://collabbit.in/post/{postId}/" },
                                    navDeepLink { uriPattern = "finder://post/{postId}" }
                                )
                            ) { backStackEntry ->
                                val args = backStackEntry.toRoute<Home>()
                                val notificationViewModel = koinInject<NotificationViewModel>()
                                MainScreen(
                                    navHostController,
                                    profileViewModel = koinInject(),
                                    menuController = menuController,
                                    menuActionViewModel = menuActionViewModel,
                                    notificationViewModel = notificationViewModel,
                                    targetPostId = args.postId
                                )
                            }

                            composable<Collab> {
                                CollabExploreScreen(
                                    navController = navHostController
                                )
                            }

                            composable<CollabDetail> { backStackEntry ->
                                val args = backStackEntry.toRoute<CollabDetail>()
                                CollabDetailScreen(
                                    collabId = args.collabId,
                                    navController = navHostController,
                                    viewModel = koinViewModel()
                                )
                            }

                            composable<CreateCollab> {
                                CreateCollabScreen(
                                    navController = navHostController,
                                    viewModel = koinViewModel(),
                                    createCollabViewModel = koinViewModel()
                                )
                            }


                            composable<Notification> {
                                NotificationScreen(
                                    navController = navHostController
                                )
                            }

                            navScreen<ChatList> {
                                val chatsViewModel = koinViewModel<ChatsViewModel>()
                                ChatScreen(
                                    navHostController = navHostController,
                                    chatsViewModel = chatsViewModel
                                )
                            }

                            navScreen<CommunityChat>(
                                deepLinks = listOf(
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/society/{id}" },
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/society/{id}/" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/society/{id}" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/society/{id}/" },
                                    navDeepLink { uriPattern = "https://collabbit.in/society/{id}" },
                                    navDeepLink { uriPattern = "https://collabbit.in/society/{id}/" },
                                    navDeepLink { uriPattern = "finder://society/{id}" }
                                )
                            ) { backStackEntry ->
                                val args = backStackEntry.toRoute<CommunityChat>()
                                SocietyChatScreen(
                                    societyId = args.id,
                                    navHostController = navHostController
                                )
                            }

                            navScreen<SendMessage> {
                                SendMessageScreen(
                                    navHostController = navHostController,
                                )
                            }
                            composable<Profile> { backStackEntry->
                                val args = backStackEntry.toRoute<Profile>()
                                val viewModel: UserProfileViewModel = koinViewModel()
                                AppUserProfile(
                                    userId = args.userId,
                                    navHostController = navHostController,
                                    navigationViewModel = koinInject(),
                                    profileViewModel = viewModel,
                                    menuController = menuController,
                                    menuActionViewModel = menuActionViewModel
                                )
                            }

                            composable<ViewProfile> { backStackEntry->
                                val args = backStackEntry.toRoute<ViewProfile>()
                                val viewModel: UserProfileViewModel = koinViewModel()
                                AppUserProfile(
                                    userId = args.userId,
                                    navHostController = navHostController,
                                    navigationViewModel = koinInject(),
                                    profileViewModel = viewModel,
                                    menuController = menuController,
                                    menuActionViewModel = menuActionViewModel
                                )
                            }

                            navScreen<EditProfile> { backStackEntry ->
                                val args = backStackEntry.toRoute<EditProfile>()
                                EditProfileScreen(
                                    navController = navHostController,
                                    editProfileViewModel = koinViewModel()
                                )
                            }

                            navScreen<Setting> {
                                SettingScreen(
                                    navController = navHostController,
                                    userProfileViewModel = koinInject()
                                )
                            }

                            navScreen<CreatePost> {
                                val postCreationViewModel: PostCreationViewModel = koinViewModel()
                                val profileViewModel = koinInject<UserProfileViewModel>()

                                CreatePostScreen(
                                    navHostController = navHostController,
                                    userProfileViewModel = profileViewModel,
                                    postCreationViewModel = postCreationViewModel,
                                )
                            }

                            navScreen<EditPost> { backStackEntry ->
                                val args = backStackEntry.toRoute<EditPost>()
                                EditPostScreen(
                                    id = args.id,
                                    type = args.type,
                                    navHostController = navHostController,
                                    userProfileViewModel = koinInject()
                                )
                            }

                            navScreen<ReplyPost> {
                            }

                            navScreen<PostView> { backStackEntry ->
                                val args = backStackEntry.toRoute<PostView>()
                                PostViewScreen(args, navHostController)
                            }

                            navScreen<VideoView> {
                                VideoViewScreen(navHostController)
                            }

                            navScreen<PdfView> {
                                PdfViewScreen(navHostController)
                            }

                            composable<Opportunities> { backStackEntry ->
                                val parentEntry = remember(backStackEntry) {
                                    navHostController.getBackStackEntry<MainGraph>()
                                }
                                OpportunitiesScreen(
                                    navController = navHostController,
                                    viewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                                )
                            }

                            composable<OpportunityDetail>(
                                deepLinks = listOf(
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/opportunity/{opportunityId}" },
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/opportunity/{opportunityId}/" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/opportunity/{opportunityId}" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/opportunity/{opportunityId}/" },
                                    navDeepLink { uriPattern = "https://collabbit.in/opportunity/{opportunityId}" },
                                    navDeepLink { uriPattern = "https://collabbit.in/opportunity/{opportunityId}/" },
                                    navDeepLink { uriPattern = "finder://opportunity/{opportunityId}" }
                                )
                            ) { backStackEntry ->
                                val parentEntry = remember(backStackEntry) {
                                    navHostController.getBackStackEntry<MainGraph>()
                                }
                                val args = backStackEntry.toRoute<OpportunityDetail>()
                                OpportunityDetailScreen(
                                    opportunityId = args.opportunityId,
                                    navController = navHostController,
                                    viewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                                )
                            }

                            composable<CourseDetail>(
                                deepLinks = listOf(
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/course/{courseId}" },
                                    navDeepLink { uriPattern = "https://www.campuscircle.in/course/{courseId}/" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/course/{courseId}" },
                                    navDeepLink { uriPattern = "https://campuscircle.in/course/{courseId}/" },
                                    navDeepLink { uriPattern = "https://collabbit.in/course/{courseId}" },
                                    navDeepLink { uriPattern = "https://collabbit.in/course/{courseId}/" },
                                    navDeepLink { uriPattern = "https://collabbit.in/session/{courseId}" },
                                    navDeepLink { uriPattern = "https://collabbit.in/session/{courseId}/" },
                                    navDeepLink { uriPattern = "finder://course/{courseId}" }
                                )
                            ) { backStackEntry ->
                                val parentEntry = remember(backStackEntry) {
                                    navHostController.getBackStackEntry<MainGraph>()
                                }
                                val args = backStackEntry.toRoute<CourseDetail>()
                                CourseDetailScreen(
                                    courseId = args.courseId,
                                    navController = navHostController,
                                    viewModel = koinViewModel(viewModelStoreOwner = parentEntry)
                                )
                            }

                            composable<SocietyHub> {
                                SocietyHubScreen(navHostController = navHostController)
                            }

                            composable<Society> {
                                SocietyScreen(navHostController = navHostController)
                            }

                            composable<Connection> {
                                ShowcaseScreen(
                                    title = "Connections",
                                    navHostController = navHostController,
                                    type = ShowcaseType.COLLABORATIONS // placeholder type
                                )
                            }

                            composable<Courses> {
                                ShowcaseScreen(
                                    title = "Courses",
                                    navHostController = navHostController,
                                    type = ShowcaseType.COURSES
                                )
                            }

                            composable<Collaborations> {
                                ShowcaseScreen(
                                    title = "Collaborations",
                                    navHostController = navHostController,
                                    type = ShowcaseType.COLLABORATIONS
                                )
                            }

                            navScreen<SocietyInfo> { backStackEntry ->
                                val args = backStackEntry.toRoute<SocietyInfo>()
                                SocietyInfoScreen(
                                    societyId = args.id,
                                    openJoinSheet = args.openJoinSheet,
                                    navHostController = navHostController
                                )
                            }
                        }
                    }

                    BottomBar(
                        showBottomBar = shouldShowBottomBar(currentDestination),
                        navigationViewModel = koinInject(),
                        navHostController = navHostController
                    )

                    SnackbarHost(
                        hostState = snackBarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = if (shouldShowBottomBar(currentDestination)) 72.dp else 0.dp)
                            .padding(16.dp)
                    )


                    val menuBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    val menuOptions = menuController.options
                    val deleteState by menuActionViewModel.deleteState.collectAsState()

                    LaunchedEffect(deleteState) {
                        when (deleteState) {
                            is UiState.Success -> {
                                menuController.dismissDialog()
                                menuController.dismiss()
                                menuActionViewModel.resetDeleteState()
                            }
                            is UiState.Error -> {
                                snackBarHostState.showSnackbar((deleteState as UiState.Error).message)
                                menuController.dismissDialog()
                                menuController.dismiss()
                                menuActionViewModel.resetDeleteState()
                            }
                            else -> {}
                        }
                    }


                    menuController.context?.let {context ->
                        MenuBottomSheet(
                            onDismiss = {
                                menuController.dismiss()
                            },
                            sheetState = menuBottomSheetState,
                            menuOptions = menuOptions,
                            pendingAction = {
                                when (it) {
                                    is MenuItem.Delete -> {
                                        menuController.context?.let { context ->
                                            menuController.showDialog(context = context)
                                            menuController.dismiss()
                                        }
                                    }
                                    is MenuItem.Report -> {
                                        menuController.dismiss()
                                    }
                                    is MenuItem.Edit -> {
                                        menuController.context?.let { context ->
                                            navHostController.navigate(
                                                EditPost(
                                                    id = context.id,
                                                    type = context.type.name
                                                )
                                            )
                                            menuController.dismiss()
                                        }
                                    }

                                }
                            }
                        )
                    }


                    menuController.dialogContext?.let {

                        AlertDialogWidget(
                            onDismiss = {
                                menuController.dismissDialog()
                            },
                            title = "Delete",
                            description = "Are you sure you want to delete this post?",
                            positiveButtonText = "Delete",
                            negativeButtonText = "Cancel",
                            onPositiveClick = {
                                menuController.dialogContext?.let {menuContext ->
                                    menuActionViewModel.onMenuActionEvent(
                                        MenuAction.Delete(
                                            context = menuContext
                                        )
                                    )
                                }

                            },
                            showLoading = deleteState is UiState.Loading
                        )

                    }



                }
                UpdateSnackBar(
                    snackHostState = snackBarHostState,
                    appUpdateManager = appUpdateManager
                )

                if (showCheckInDialog && checkInData != null) {
                    DailyAuraCheckInDialog(
                        pointsEarned = checkInData!!.pointsEarned,
                        totalPoints = checkInData!!.totalPoints,
                        level = checkInData!!.level,
                        // streakDays = 0, // Default is 0
                        onDismiss = {
                            showCheckInDialog = false
                            checkInData = null
                        }
                    )
                }

                if (showAlreadyClaimedDialog) {
                    AlreadyClaimedDialog(
                        totalPoints = 0, // Should be fetched if needed
                        onDismiss = { showAlreadyClaimedDialog = false }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun checkForUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this)
        val updateInfoTask = appUpdateManager.appUpdateInfo

        updateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                if (result.resultCode != RESULT_OK) {
                    // handle callback
                }
            }
    }


    @Composable
    fun BottomBar(
        showBottomBar: Boolean,
        navigationViewModel: NavigationViewModel,
        navHostController: NavHostController
    ) {

        val isBottomBarVisible = navigationViewModel.isBottomBarVisible.collectAsState()

        if (showBottomBar) {
            AnimatedVisibility(
                visible = isBottomBarVisible.value,
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                ),
                exit = slideOutVertically(
                    targetOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                )
            ) {
                BottomAppBar(
                    navController = navHostController,
                    profileViewModel = koinInject()
                )
            }
        }
    }


    @Composable
    fun UpdateSnackBar(
        snackHostState: SnackbarHostState,
        appUpdateManager: AppUpdateManager
    ) {
        // Collect update status
        val context = LocalContext.current

        DisposableEffect(Unit) {
            val listener = InstallStateUpdatedListener { state ->
                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val result = snackHostState.showSnackbar(
                            message = "An update has just been downloaded.",
                            actionLabel = "RESTART",
                            duration = SnackbarDuration.Indefinite
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            appUpdateManager.completeUpdate()
                        }
                    }
                }
            }

            appUpdateManager.registerListener(listener)

            onDispose {
                appUpdateManager.unregisterListener(listener)
            }
        }
    }

}
