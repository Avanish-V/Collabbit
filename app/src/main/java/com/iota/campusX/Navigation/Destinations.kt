//package com.iota.campusX.Navigation
//
//import kotlinx.serialization.Serializable
//
//@Serializable
//sealed interface Destination
//
//@Serializable
//data object AuthGraph : Destination
//
//@Serializable
//data object SignIn : Destination
//
//@Serializable
//data object MainGraph : Destination
//
//@Serializable
//data object Home : Destination
//
//@Serializable
//data object Notification : Destination
//
//@Serializable
//data object ChatList : Destination
//
//@Serializable
//data class SendMessage(val chatId: String) : Destination
//
//@Serializable
//data class UserProfile(val userId: String? = null) : Destination
//
//@Serializable
//data object EditProfile : Destination
//
//@Serializable
//data object Setting : Destination
//
//@Serializable
//data object CreatePost : Destination
//
//@Serializable
//data class EditPost(val postId: String) : Destination
//
//@Serializable
//data class PostDetail(val postId: String) : Destination
//
//@Serializable
//data class PostView(val imageUrl: String?) : Destination
//
//@Serializable
//data object Opportunities : Destination
//
//@Serializable
//data object Courses : Destination
//
//@Serializable
//data object Collaborations : Destination
//
//@Serializable
//data object CreateCollab : Destination
