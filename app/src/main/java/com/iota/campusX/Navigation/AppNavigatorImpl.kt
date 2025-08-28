package com.iota.campusX.Navigation

import androidx.navigation.NavHostController

class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {

    override fun navigateToViewUserProfile(userId: String) {
        navController.navigate(Routes.Main.ProfileByID.routes).apply {
            navController.currentBackStackEntry?.savedStateHandle?.set("USER_ID", userId)
        }
    }

    override fun navigateToViewPostVisualContent(imageUrl: String?) {
        navController.navigate(Routes.Main.PostViewScreen.routes).apply {
            navController.currentBackStackEntry?.savedStateHandle?.set("POST_IMAGE", imageUrl)
        }
    }

    override fun navigateToPostDetail(postId: String) {
        navController.navigate(Routes.Main.ReplyPost.routes).apply {
            navController.currentBackStackEntry?.savedStateHandle?.set("POST_ID", postId)
        }
    }

    override fun navigateToOwnerProfile() {
        navController.navigate(Routes.Main.Profile.routes)
    }

    override fun goBack() {
        navController.popBackStack()
    }
}
