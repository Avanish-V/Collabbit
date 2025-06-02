package com.iota.campusX

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.PushNotification.PushNotificationService
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Koin.appModule
import com.iota.campusX.Screens.Register.SignInScreen
import com.iota.campusX.Navigation.BottomAppBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Chat.ChatScreen
import com.iota.campusX.Screens.Chat.SendMessageScreen
import com.iota.campusX.Screens.ConnectionsScreen
import com.iota.campusX.Screens.Post.CreatePostScreen
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Home.MainScreen
import com.iota.campusX.Screens.Home.PostViewScreen
import com.iota.campusX.Screens.NotificationScreen
import com.iota.campusX.Screens.PostReplyScreen
import com.iota.campusX.Screens.Profile.EditProfileScreen
import com.iota.campusX.Screens.Profile.ProfileScreen
import com.iota.campusX.Screens.Setting.SettingScreen
import com.iota.campusX.Screens.VoxciScreen
import com.iota.campusX.Utils.initCloudinary
import com.iota.campusX.ui.theme.CampusXTheme
import com.iota.campusX.ui.theme.background
import com.voxcii.voxcii.Screens.SearchFlow.SearchScreen
import org.koin.android.ext.koin.androidContext
import org.koin.compose.koinInject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.context.stopKoin
import kotlin.collections.contains

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
                        .update("token",token)
                }
            }
        }


        setContent {

            val scope = rememberCoroutineScope()


            val navHostController = rememberNavController()
            val userProfileViewModel = koinInject<UserProfileViewModel>()
            val googleAuthViewModel = koinInject<AuthViewModel>()
            val postViewModel = koinInject<PostViewModel>()
            val navigationViewModel = koinInject<NavigationViewModel>()
            val homeViewModel = koinInject<HomeViewModel>()
            val notificationViewModel = koinInject<NotificationViewModel>()


            val navBackStackEntry by navHostController.currentBackStackEntryAsState()
            val destination = navBackStackEntry?.destination?.route
            val isBottomBarVisible = navigationViewModel.isBottomBarVisible.collectAsState()

            val uploadProgress = postViewModel.uploadingProgress.collectAsState().value

            installSplashScreen().setKeepOnScreenCondition {
                homeViewModel.switchState.value.isLoad
            }

            val showBottomBar by remember {
                mutableStateOf(
                    listOf(
                        Routes.Main.Home.routes,
                        Routes.Main.Search.routes,
                        Routes.Main.Voxci.routes,
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

                        AnimatedVisibility(visible = uploadProgress.status == "PROGRESS" || uploadProgress.status == "ERROR") {

                            Column(modifier = Modifier.padding(WindowInsets.statusBars.asPaddingValues())) {

                                Text(text = if (uploadProgress.status == "COMPLETED") "Completed" else if (uploadProgress.status == "ERROR") "Upload Failed" else "Uploading...")

                                Row (
                                    modifier = Modifier.height(32.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ){

                                    Row (verticalAlignment = Alignment.CenterVertically){

                                        AnimatedContent(
                                            targetState = uploadProgress.progress,
                                            transitionSpec = {
                                                slideInVertically { height -> height } + fadeIn() togetherWith
                                                        slideOutVertically { height -> -height } + fadeOut()
                                            },
                                            label = "LikeCountAnimation"
                                        ) { progress ->
                                            Text(
                                                text = "${progress}"
                                            )
                                        }
                                        Text("%")
                                    }


                                    LinearProgressIndicator(
                                        progress = { (uploadProgress.progress?.toFloat()?.div(100)) ?: 0f },
                                        modifier = Modifier.weight(1f),
                                        trackColor = background)

                                    IconButton(onClick = {postViewModel.cancelUpload()}) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null
                                        )
                                    }
                                }


                            }

                        }

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
                                            postViewModel = postViewModel,
                                            navigationViewModel = navigationViewModel,
                                            profileViewModel = userProfileViewModel,
                                            homeViewModel = homeViewModel,
                                            notificationViewModel = notificationViewModel

                                        )
                                    }

                                    composable(route = Routes.Main.ProfileByID.routes) {

                                        val postViewModel = koinInject<PostViewModel>()

                                        ProfileScreen(
                                            navHostController,
                                            postViewModel = postViewModel,
                                            profileViewModel = userProfileViewModel,
                                            googleSignInViewModel = googleAuthViewModel,
                                            navigationViewModel = navigationViewModel
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

                                    navScreen( Routes.Main.ChatList.routes) {

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
                                        val postViewModel = koinInject<PostViewModel>()
                                        ProfileScreen(
                                            navHostController,
                                            postViewModel = postViewModel,
                                            profileViewModel = userProfileViewModel,
                                            googleSignInViewModel = googleAuthViewModel,
                                            navigationViewModel = navigationViewModel
                                        )
                                    }

                                    navScreen(Routes.Main.EditProfile.routes) {
                                        EditProfileScreen(
                                            navController = navHostController,
                                            userProfileViewModel = userProfileViewModel
                                        )
                                    }

                                    navScreen(Routes.Main.Setting.routes) {

                                        val userProfileViewModel = koinInject<UserProfileViewModel>()

                                        SettingScreen(
                                            navController = navHostController,
                                            userProfileViewModel = userProfileViewModel
                                        )
                                    }

                                    navScreen(route = Routes.Main.CreatePost.routes) {

                                        CreatePostScreen(
                                            navHostController = navHostController,
                                            userProfileViewModel = userProfileViewModel,
                                            postViewModel = postViewModel,
                                            authViewModel = googleAuthViewModel,
                                            homeViewModel = homeViewModel
                                        )

                                    }

                                    navScreen(
                                        route = Routes.Main.ReplyPost.routes
                                    ) {
                                        PostReplyScreen(
                                            navHostController,
                                            userProfileViewModel,
                                            postViewModel,

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

fun NavGraphBuilder.navScreen(
    route: String,
    content: @Composable () -> Unit
){

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

