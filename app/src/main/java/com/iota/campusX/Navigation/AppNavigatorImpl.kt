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

    override fun navigateToEditPost(id: String, type: String) {
        navController.navigate(EditPost(id = id, type = type))
    }

    override fun navigateToOwnerProfile() {
        navController.navigate(Profile())
    }

    override fun goBack() {
        navController.popBackStack()
    }
}
