package com.iota.campusX

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Follow.di.followModule
import com.iota.campusX.Feature.Post.DI.postModule
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.Post.presentation.ViewUserPostViewModel
import com.iota.campusX.Feature.Post.presentation.ViewUserReplyViewModel
import com.iota.campusX.Feature.PushNotification.PushNotificationService
import com.iota.campusX.Navigation.BottomAppBar
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Chat.ChatScreen
import com.iota.campusX.Screens.Chat.SendMessageScreen
import com.iota.campusX.Screens.ConnectionsScreen
import com.iota.campusX.Screens.Home.MainScreen
import com.iota.campusX.Screens.Home.PostViewScreen
import com.iota.campusX.Screens.NotificationScreen
import com.iota.campusX.Screens.Post.CreatePostScreen
import com.iota.campusX.Screens.PostReplyScreen
import com.iota.campusX.Screens.Profile.EditProfileScreen
import com.iota.campusX.Screens.Profile.AppUserProfile
import com.iota.campusX.Screens.Register.SignInScreen
import com.iota.campusX.Screens.Setting.SettingScreen
import com.iota.campusX.Feature.Society.presentation.Screens.CreateSociety
import com.iota.campusX.Feature.Society.presentation.Screens.JoinSocietyScreen
import com.iota.campusX.Feature.Society.presentation.Screens.SocietyScreen
import com.iota.campusX.Feature.UserProfile.presentation.ViewProfileViewModel
import com.iota.campusX.Koin.authModule
import com.iota.campusX.Koin.chatModule
import com.iota.campusX.Koin.cloudinaryModule
import com.iota.campusX.Koin.coreModule
import com.iota.campusX.Koin.firebaseModule
import com.iota.campusX.Koin.navigationModule
import com.iota.campusX.Koin.notificationModule
import com.iota.campusX.Koin.profileModule
import com.iota.campusX.Koin.replyModule
import com.iota.campusX.Koin.reportModule
import com.iota.campusX.Koin.searchModule
import com.iota.campusX.Koin.societyModule
import com.iota.campusX.Koin.themeMode
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.navScreen
import com.iota.campusX.Navigation.shouldShowBottomBar
import com.iota.campusX.Feature.Follow.presentation.Followers
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuSheet
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuViewModel
import com.iota.campusX.Screens.Profile.ViewProfile
import com.iota.campusX.Screens.Setting.ThemeMode
import com.iota.campusX.Screens.VoxciScreen
import com.iota.campusX.Utils.ThemeMode.ThemePreference
import com.iota.campusX.Utils.initCloudinary
import com.iota.campusX.ui.theme.AppTheme
import com.jetpack.observeliveconnectivity.ConnectionState
import com.jetpack.observeliveconnectivity.connectivityState
import com.voxcii.voxcii.Screens.SearchFlow.SearchScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

class MainActivity : ComponentActivity() {

    private lateinit var activityResultLauncher: ActivityResultLauncher<IntentSenderRequest>
    private lateinit var appUpdateManager: AppUpdateManager

    @OptIn(ExperimentalCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        //WindowCompat.setDecorFitsSystemWindows(window, true)

        initCloudinary(this)

        startKoin {
            androidContext(this@MainActivity)
            modules(
                coreModule,
                authModule,
                postModule,
                firebaseModule,
                chatModule,
                notificationModule,
                profileModule,
                societyModule,
                searchModule,
                reportModule,
                navigationModule,
                replyModule,
                cloudinaryModule,
                themeMode,
                followModule
            )
        }
//
//        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
//            if (task.isSuccessful) {
//                val token = task.result
//                FirebaseAuth.getInstance().currentUser?.let {
//                    FirebaseFirestore.getInstance().collection("Users")
//                        .document(it.uid)
//                        .update("token", token)
//                }
//            }
//        }
        setContent {

            val themeMode by ThemePreference.getThemeMode(this).collectAsState(initial = ThemeMode.LIGHT)

            val postMenuState: PostMenuState = koinInject()
            val authViewModel = koinInject<GoogleSignInViewModel>()

            val snackBarHostState = remember { SnackbarHostState() }
            val navHostController = rememberNavController()
            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val destination = navBackStackEntry?.destination?.route
            val connectivityState = connectivityState()
            val userProfileViewModel: UserProfileViewModel = koinInject()

            LaunchedEffect(connectivityState.value) {
                if (connectivityState.value == ConnectionState.Unavailable) {
                    snackBarHostState.showSnackbar(
                        "No Internet Connection",
                        actionLabel = "OK",
                        withDismissAction = true
                    )
                }else{
                    userProfileViewModel.refreshProfile()
                }
            }

            AppTheme(themeMode = themeMode) {

                Column {

                    UploadProgressUI(
                        postCreationViewModel = koinInject(),
                    )

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {

                        NavHost(
                            modifier = Modifier.fillMaxSize(),
                            navController = navHostController,
                            startDestination = if (authViewModel.getCurrentUser()) Routes.Main.routes else Routes.Register.routes
                        ) {

                            navigation(
                                startDestination = Routes.Register.SignIn.routes,
                                route = Routes.Register.routes
                            ) {
                                composable(route = Routes.Register.SignIn.routes) {

                                    SignInScreen(
                                        navHostController = navHostController,
                                    )

                                }
                            }

                            navigation(
                                startDestination = Routes.Main.Home.routes,
                                route = Routes.Main.routes
                            ) {
                                composable(route = Routes.Main.Home.routes) {

                                    MainScreen(
                                        navHostController,
                                        navigationViewModel = koinInject(),
                                        profileViewModel = koinInject(),
                                        homeViewModel = koinInject(),
                                        notificationViewModel = koinInject()

                                    )
                                }
                                navScreen(route = Routes.Main.ProfileByID.routes) {
                                    val viewProfileViewModel = koinInject<ViewProfileViewModel>()
                                    val viewUserPostViewModel = koinInject<ViewUserPostViewModel>()
                                    val viewUserReplyViewModel = koinInject<ViewUserReplyViewModel>()
                                    ViewProfile(
                                        navHostController,
                                        viewUserPostViewModel = viewUserPostViewModel,
                                        viewProfileViewModel = viewProfileViewModel,
                                        navigationViewModel = koinInject(),
                                        viewUserReplyViewModel = viewUserReplyViewModel,
                                    )

                                }
                                navScreen(route = Routes.Main.Connections.routes) {

                                    ConnectionsScreen(
                                        navHostController = navHostController
                                    )

                                }
                                navScreen(route = Routes.Main.Followers.routes) {

                                    Followers(
                                        navHostController = navHostController
                                    )

                                }
                                composable(route = Routes.Main.Search.routes) {
                                    SearchScreen(
                                        navHostController = navHostController
                                    )
                                }
                                composable(route = Routes.Main.Society.routes) {
                                    SocietyScreen(
                                        navHostController = navHostController,
                                    )
                                }
                                navScreen(route = Routes.Main.CreateSociety.routes) {
                                    CreateSociety(navHostController)
                                }
                                composable(route = Routes.Main.JoinSociety.routes) {
                                    JoinSocietyScreen(
                                        navController = navHostController,
                                        userProfileViewModel = koinInject(),
                                    )
                                }
                                composable(route = Routes.Main.Voxci.routes) {
                                    VoxciScreen()
                                }
                                composable(route = Routes.Main.Notification.routes) {
                                    NotificationScreen(
                                        navigationViewModel = koinInject(),
                                        userProfileViewModel = koinInject(),
                                        navHostController = navHostController
                                    )
                                }

                                navScreen(Routes.Main.ChatList.routes) {

                                    val chatsViewModel = koinInject<ChatsViewModel>()
                                    ChatScreen(
                                        navHostController = navHostController,
                                        chatsViewModel = chatsViewModel
                                    )

                                }

                                navScreen(Routes.Main.SendMessage.routes) {
                                    SendMessageScreen(
                                        navHostController = navHostController,
                                    )
                                }

                                composable(route = Routes.Main.Profile.routes) {
                                    AppUserProfile(
                                        navHostController,
                                        navigationViewModel = koinInject(),
                                    )
                                }

                                navScreen(Routes.Main.EditProfile.routes) {
                                    EditProfileScreen(
                                        navController = navHostController,
                                        userProfileViewModel = koinInject()
                                    )
                                }

                                navScreen(Routes.Main.Setting.routes) {
                                    SettingScreen(
                                        navController = navHostController,
                                        userProfileViewModel = koinInject()
                                    )
                                }

                                navScreen(route = Routes.Main.CreatePost.routes) {

                                    CreatePostScreen(
                                        navHostController = navHostController,
                                        userProfileViewModel = koinInject(),
                                        postCreationViewModel = koinInject(),
                                        homeViewModel = koinInject(),
                                    )

                                }

                                navScreen(
                                    route = Routes.Main.ReplyPost.routes
                                ) {
                                    PostReplyScreen(
                                        navHostController =  navHostController,
                                        profileViewModel = koinInject(),
                                        postViewModel = koinInject(),
                                        homeViewModel = koinInject()
                                    )
                                }

                                navScreen(
                                    route = Routes.Main.PostViewScreen.routes
                                ) {
                                    PostViewScreen(navHostController)
                                }

                            }
                        }


                        BottomBar(
                            showBottomBar = shouldShowBottomBar(destination),
                            navigationViewModel = koinInject(),
                            navHostController = navHostController
                        )

                        SnackbarHost(
                            hostState = snackBarHostState,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp) // margin from bottom
                        )

                    }

                    if (postMenuState.showSheet) {
                        postMenuState.currentContent?.let {
                            PostMenuSheet(
                                viewModel = koinInject<PostMenuViewModel>(),
                                content = it,
                                onDismiss = { postMenuState.close() },
                                snackBarHostState = snackBarHostState,
                            )
                        }
                    }
                }
            }
            UpdateSnackBar(
                snackHostState = snackBarHostState,
                appUpdateManager = appUpdateManager
            )
        }
        checkForUpdate()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopKoin()
    }

    override fun onResume() {
        super.onResume()
        PushNotificationService().setIsAppRunning(
            isRunning = true
        )
    }

    private fun checkForUpdate() {

        appUpdateManager = AppUpdateManagerFactory.create(this)
        val updateInfoTask = appUpdateManager.appUpdateInfo

        updateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                // This example applies an immediate update. To apply a flexible update
                // instead, pass in AppUpdateType.FLEXIBLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                appUpdateManager.startUpdateFlowForResult(
                    // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,
                    // an activity result launcher registered via registerForActivityResult
                    activityResultLauncher,
                    // Or pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                    // flexible updates.
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
                // handle callback
                if (result.resultCode != RESULT_OK) {
                    //log("Update flow failed! Result code: " + result.resultCode);
                    // If the update is canceled or fails,
                    // you can request to start the update again.
                }
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
                initialOffsetY = { fullHeight -> fullHeight }, // slide up from bottom
                animationSpec = tween(600)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight }, // slide down to bottom
                animationSpec = tween(600)
            )
        ) {
            BottomAppBar(
                navController = navHostController,
                notificationViewModel = koinInject()
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
                // Show Compose Snackbar when download completed
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


@Composable
fun UploadProgressUI(
    postCreationViewModel: PostCreationViewModel,
) {
    val uploadProgress = postCreationViewModel.uploadState.collectAsState().value
    val progress = (uploadProgress as? UploadState.Progress)?.progress ?: 0
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "AnimatedUploadProgress"
    )

    AnimatedVisibility(
        visible = uploadProgress is UploadState.Progress || uploadProgress is UploadState.MediaUploadError,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 })
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp).fillMaxWidth(),
        ) {

            val statusText = when (uploadProgress) {
                is UploadState.Error -> "Upload Failed"
                is UploadState.Progress -> if (progress >= 100) "Completed" else "Uploading..."
                else -> ""
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    AnimatedContent(
                        targetState = progress,
                        transitionSpec = {
                            slideInVertically { height -> height } + fadeIn() togetherWith
                                    slideOutVertically { height -> -height } + fadeOut()
                        },
                        label = "ProgressPercentage"
                    ) { value ->
                        Text(
                            text = "$value",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Text(
                        text = "%",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .height(4.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(50)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surface,
                    strokeCap = StrokeCap.Round,
                    drawStopIndicator = {
                        false
                    }
                )

                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(
                            onClick = {
                                postCreationViewModel.cancelUpload()
                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel Upload",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}




