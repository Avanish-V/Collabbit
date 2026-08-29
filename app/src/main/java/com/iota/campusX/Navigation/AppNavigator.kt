package com.iota.campusX.Navigation

interface AppNavigator {
    fun navigateToOwnerProfile()
    fun navigateToViewUserProfile(userId: String)
    fun navigateToViewPostVisualContent(imageUrl: String?)
    fun navigateToPostDetail(postId: String)
    fun navigateToEditPost(postId: String)
    fun goBack()
}
