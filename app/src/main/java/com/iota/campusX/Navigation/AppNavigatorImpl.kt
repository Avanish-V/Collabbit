package com.iota.campusX.Navigation

import androidx.navigation.NavHostController

class AppNavigatorImpl(
    private val navController: NavHostController
) : AppNavigator {

    override fun navigateToViewUserProfile(userId: String) {
        navController.navigate(Profile(userId = userId))
    }

    override fun navigateToViewPostVisualContent(imageUrl: String?) {
        navController.navigate(PostView(postImage = imageUrl))
    }

    override fun navigateToPostDetail(postId: String) {
        // Assuming ReplyPost might need postId in the future, 
        // but for now it's just an object in Routes.kt
        navController.navigate(ReplyPost)
    }

    override fun navigateToEditPost(postId: String) {
        navController.navigate(EditPost(postId = postId))
    }

    override fun navigateToOwnerProfile() {
        navController.navigate(Profile())
    }

    override fun goBack() {
        navController.popBackStack()
    }
}
