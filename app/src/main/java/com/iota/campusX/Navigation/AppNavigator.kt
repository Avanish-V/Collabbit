package com.iota.campusX.Navigation

interface AppNavigator {
    fun navigateToOwnerProfile()
    fun navigateToViewUserProfile(userId: String)
    fun navigateToPostDetail(postId: String)
    fun goBack()
}
