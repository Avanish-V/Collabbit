package com.iota.campusX

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.Post.presentation.UploadState
import com.iota.campusX.Feature.PushNotification.PushNotificationService
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Koin.appModule
import com.iota.campusX.Navigation.BottomAppBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Chat.ChatScreen
import com.iota.campusX.Screens.Chat.SendMessageScreen
import com.iota.campusX.Screens.ConnectionsScreen
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Home.MainScreen
import com.iota.campusX.Screens.Home.PostViewScreen
import com.iota.campusX.Screens.NotificationScreen
import com.iota.campusX.Screens.Post.CreatePostScreen
import com.iota.campusX.Screens.PostReplyScreen
import com.iota.campusX.Screens.Profile.EditProfileScreen
import com.iota.campusX.Screens.Profile.ProfileScreen
import com.iota.campusX.Screens.Profile.ProfilyTypeViewModel
import com.iota.campusX.Screens.Register.SignInScreen
import com.iota.campusX.Screens.Setting.SettingScreen
import com.iota.campusX.Screens.Society.Society
import com.iota.campusX.Screens.VoxciScreen
import com.iota.campusX.Utils.UiState
import com.iota.campusX.Utils.initCloudinary
import com.iota.campusX.ui.theme.Black800
import com.iota.campusX.ui.theme.CampusXTheme
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import com.iota.campusX.ui.theme.typography
import com.voxcii.voxcii.Screens.SearchFlow.SearchScreen
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin

class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        actionBar?.hide()

        initCloudinary(this@MainActivity)

        WindowCompat.setDecorFitsSystemWindows(window, true)

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                FirebaseAuth.getInstance().currentUser?.let {
                    FirebaseFirestore.getInstance().collection("Users")
                        .document(it.uid)
                        .update("token", token)
                }
            }
        }


        setContent {

            val scope = rememberCoroutineScope()


            val navHostController = rememberNavController()
            val userProfileViewModel = koinInject<UserProfileViewModel>()
            val googleAuthViewModel = koinInject<AuthViewModel>()
            val postCreationViewModel = koinInject<PostCreationViewModel>()
            val postFeedViewModel = koinInject<PostFeedViewModel>()
            val navigationViewModel = koinInject<NavigationViewModel>()
            val homeViewModel = koinInject<HomeViewModel>()
            val notificationViewModel = koinInject<NotificationViewModel>()
            val replyViewModel = koinInject<ReplyViewModel>()
            val profileTypeViewModel = koinInject<ProfilyTypeViewModel>()


            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val destination = navBackStackEntry?.destination?.route
            val isBottomBarVisible = navigationViewModel.isBottomBarVisible.collectAsState()

            val uploadProgress = postCreationViewModel.uploadingProgress.collectAsState().value
            val mode = homeViewModel.mode.collectAsState().value

            Log.d("MODE", mode.toString())

            when(mode){
                is UiState.Loading -> installSplashScreen().setKeepOnScreenCondition { false }
                is UiState.Success<*> -> false
                else -> false
            }

            val showBottomBar by remember {
                mutableStateOf(
                    listOf(
                        Routes.Main.Home.routes,
                        Routes.Main.Search.routes,
                        Routes.Main.Society.routes,
                        Routes.Main.Notification.routes,
                        Routes.Main.Profile.routes
                    )
                )
            }
            LaunchedEffect(Unit) {
                navigationViewModel.isBottomBarVisible(
                    showBottomBar.contains(destination)
                )
            }

            CampusXTheme {
                Surface(

                ) {


                    Column {

                        UploadProgressUI(
                            uploadProgress = uploadProgress,
                            onCancel = {
                                postCreationViewModel.clearUpload()
                            }
                        )

                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.BottomCenter
                        ) {


                            NavHost(
                                modifier = Modifier.fillMaxSize(),
                                navController = navHostController,
                                startDestination = if (googleAuthViewModel.currentUser()) Routes.Main.routes else Routes.Register.routes
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
                                            postViewModel = postFeedViewModel,
                                            navigationViewModel = navigationViewModel,
                                            profileViewModel = userProfileViewModel,
                                            homeViewModel = homeViewModel,
                                            notificationViewModel = notificationViewModel

                                        )
                                    }

                                    composable(route = Routes.Main.ProfileByID.routes) {

                                        ProfileScreen(
                                            navHostController,
                                            postViewModel = postFeedViewModel,
                                            profileViewModel = userProfileViewModel,
                                            googleSignInViewModel = googleAuthViewModel,
                                            navigationViewModel = navigationViewModel,
                                            replyViewModel = replyViewModel,
                                            profileTypeViewModel = profileTypeViewModel
                                        )

                                    }
                                    composable(route = Routes.Main.Connections.routes) {

                                        ConnectionsScreen(
                                            navHostController = navHostController
                                        )

                                    }

                                    composable(route = Routes.Main.Search.routes) {
                                        SearchScreen(
                                            navHostController = navHostController
                                        )
                                    }
                                    composable (route = Routes.Main.Society.routes){
                                        Society()
                                    }
                                    composable(route = Routes.Main.Voxci.routes) {
                                        VoxciScreen()
                                    }
                                    composable(route = Routes.Main.Notification.routes) {
                                        NotificationScreen(
                                            navigationViewModel = navigationViewModel,
                                            userProfileViewModel = userProfileViewModel,
                                            navHostController
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
                                        val chatsViewModel = koinInject<ChatsViewModel>()
                                        SendMessageScreen(
                                            navHostController = navHostController,

                                            )
                                    }

                                    composable(route = Routes.Main.Profile.routes) {

                                        ProfileScreen(
                                            navHostController,
                                            postViewModel = postFeedViewModel,
                                            profileViewModel = userProfileViewModel,
                                            googleSignInViewModel = googleAuthViewModel,
                                            navigationViewModel = navigationViewModel,
                                            replyViewModel = replyViewModel,
                                            profileTypeViewModel = profileTypeViewModel

                                        )
                                    }

                                    navScreen(Routes.Main.EditProfile.routes) {
                                        EditProfileScreen(
                                            navController = navHostController,
                                            userProfileViewModel = userProfileViewModel
                                        )
                                    }

                                    navScreen(Routes.Main.Setting.routes) {

                                        val userProfileViewModel =
                                            koinInject<UserProfileViewModel>()

                                        SettingScreen(
                                            navController = navHostController,
                                            userProfileViewModel = userProfileViewModel
                                        )
                                    }

                                    navScreen(route = Routes.Main.CreatePost.routes) {

                                        CreatePostScreen(
                                            navHostController = navHostController,
                                            userProfileViewModel = userProfileViewModel,
                                            postCreationViewModel = postCreationViewModel,
                                            authViewModel = googleAuthViewModel,
                                            homeViewModel = homeViewModel,
                                            feedViewModel = postFeedViewModel
                                        )

                                    }

                                    navScreen(
                                        route = Routes.Main.ReplyPost.routes
                                    ) {
                                        PostReplyScreen(
                                            navHostController,
                                            userProfileViewModel,
                                            postFeedViewModel,
                                            homeViewModel,
                                            replyViewModel
                                        )
                                    }

                                    navScreen(
                                        route = Routes.Main.PostViewScreen.routes
                                    ) {
                                        PostViewScreen(navHostController)
                                    }

                                }
                            }

                            if (showBottomBar.contains(destination)) {
                                this@Column.AnimatedVisibility(
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
                                        notificationViewModel = notificationViewModel
                                    )
                                }
                            }

                        }

                    }


                }
            }
        }

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
}

@Composable
fun UploadProgressUI(
    uploadProgress: UploadState,
    onCancel: () -> Unit
) {
    val progress = (uploadProgress as? UploadState.Progress)?.progress ?: 0
    val animatedProgress by animateFloatAsState(
        targetValue = progress / 100f,
        animationSpec = tween(durationMillis = 500),
        label = "AnimatedUploadProgress"
    )

    AnimatedVisibility(
        visible = uploadProgress is UploadState.Progress || uploadProgress is UploadState.Error,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -40 })
    ) {
        Surface(
            color = White900,
            modifier = Modifier.fillMaxWidth()

        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
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

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                style = typography.headingRegular,
                                color = Black800
                            )
                        }
                        Text(
                            text = "%",
                            style = typography.headingRegular,
                            color = Black800
                        )
                    }

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .height(8.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(50)),
                        color = primary,
                        trackColor = secondary
                    )

                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Upload",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}


fun NavGraphBuilder.navScreen(
    route: String,
    content: @Composable () -> Unit
) {

    composable(
        route = route,
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                tween(500)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(500)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                tween(500)
            )
        }
    ) {
        content()
    }

}

