package com.iota.campusX

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animation
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Koin.appModule
import com.iota.campusX.Screens.Register.SignInScreen
import com.iota.campusX.Navigation.BottomAppBar
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Chat.ChatScreen
import com.iota.campusX.Screens.Chat.SendMessageScreen
import com.iota.campusX.Screens.CreatePostScreen
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
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.CampusXTheme
import com.iota.campusX.ui.theme.background
import com.iota.campusX.ui.theme.typography
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

        startKoin {
            androidContext(this@MainActivity)
            modules(appModule)
        }

        initCloudinary(this@MainActivity)

        setContent {

            val navHostController = rememberNavController()
            val userProfileViewModel = koinInject<UserProfileViewModel>()
            val googleAuthViewModel = koinInject<AuthViewModel>()
            val postViewModel = koinInject<PostViewModel>()
            val navigationViewModel = koinInject<NavigationViewModel>()
            val homeViewModel = koinInject<HomeViewModel>()


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

                            Column(modifier = Modifier.padding(horizontal = 12.dp)) {

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
                                            homeViewModel = homeViewModel
                                        )
                                    }

                                    composable(route = Routes.Main.ProfileByID.routes) {

//                                        ProfileByID(
//                                            navHostController = navHostController,
//                                            userProfileViewModel = userProfileViewModel,
//                                            postViewModel = postViewModel,
//                                            authViewModel = googleAuthViewModel
//                                        )
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
                                            userProfileViewModel = userProfileViewModel
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
                                    BottomAppBar(navController = navHostController)
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

