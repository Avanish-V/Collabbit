package com.iota.campusX.Navigation

interface AppNavigator {
    fun navigateToOwnerProfile()
    fun navigateToViewUserProfile(userId: String)
    fun navigateToViewPostVisualContent(imageUrl: String?)
    fun navigateToPostDetail(postId: String)
    fun navigateToEditPost(id: String, type: String = "POST")
    fun goBack()
}
