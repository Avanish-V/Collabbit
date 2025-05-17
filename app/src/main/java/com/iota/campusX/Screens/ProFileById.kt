package com.iota.campusX.Screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Feature.Post.domain.User
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.Routes
import com.iota.campusX.Screens.Profile.HorizontalTabComponent
import com.iota.campusX.Screens.Profile.PostScreenComponent
import com.iota.campusX.Screens.Profile.ProfileHeader
import com.iota.campusX.Screens.Profile.UserAbout
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileByID(
    navHostController: NavHostController,
    userProfileViewModel: UserProfileViewModel,
    authViewModel: AuthViewModel,
    postViewModel: PostViewModel,
) {

    val createrId = remember { navHostController.currentBackStackEntry?.savedStateHandle?.get<String>("USER_ID") }
    val user = userProfileViewModel.userById.collectAsState().value
    val currentUser = authViewModel.userId()
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        userProfileViewModel.getUserById(createrId.toString())
    }

    Scaffold (
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Profile")
                },
                navigationIcon = {
                    IconButton(onClick = {navHostController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = ""
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackBarHostState){
                Snackbar(snackbarData = it)
            }
        }
    ){ innerPadding ->


        LoadingUI(user.isLoading)


        if (user.joinedUser != null){

            Column(modifier = Modifier.padding(innerPadding)) {

                ProfileHeader(
                    navHostController=  navHostController,
                    user = User(
                        userName = user.joinedUser?.userName ?: "",
                        userImage = user.joinedUser?.userImage ?: "",
                        _id = user.joinedUser?._id.toString()
                    ),
                    currentUser = currentUser,
                    onLinkUpRequestClick = {
                        scope.launch {
                            userProfileViewModel.sendLinkUpRequest(
                                requestUserId = createrId.toString(),
                                currentState = user.joinedUser.isRequestSent
                            ).collect {
                                when(it){
                                    is ResultState.Success -> {
                                        userProfileViewModel.getUserById(createrId.toString())
                                        snackBarHostState.showSnackbar("Request Sent")
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
                    isLinkUpRequestSent = user.joinedUser?.isRequestSent
                )

                Spacer(modifier = Modifier.height(12.dp))

                HorizontalTabComponent {page->

                    when (page) {
                        0 -> {
                            UserAbout(
                                userBasicProfileDTO = user.joinedUser
                            )
                        }

                        1 -> {
                            PostScreenComponent(
                                navHostController,
                                postViewModel,
                                currentUser = createrId.toString(),
                                campusId = user.joinedUser?.campus?.campusCode ?: ""
                            )
                        }

                    }
                }

            }

        }



    }


}