package com.iota.campusX.Navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for the Collabbit app.
 */

@Serializable
sealed interface Route

// --- Authentication Graph ---
@Serializable
data object AuthGraph : Route

@Serializable
data object SignIn : Route

@Serializable
data object Register : Route

@Serializable
data object CreateProfile : Route

@Serializable
data object Verification : Route


// --- Main Application Graph ---
@Serializable
data object MainGraph : Route

@Serializable
data class Home(val postId: String? = null) : Route

@Serializable
data object Collab : Route

@Serializable
data object Notification : Route

@Serializable
data object ChatList : Route

@Serializable
data class CommunityChat(val id: String) : Route

@Serializable
data class SocietyInfo(val id: String, val openJoinSheet: Boolean = false) : Route

@Serializable
data class SendMessage(
    val userId: String,
    val userName: String,
    val userImage: String? = null,
) : Route

/**
 * Navigates to a user's profile. 
 * If [userId] is null, it typically refers to the current user's profile.
 */
@Serializable
data class Profile(val userId: String? = null) : Route


@Serializable
data class ViewProfile(val userId: String? = null) : Route

@Serializable
data class EditProfile(val editType: String? = null) : Route

@Serializable
data class Connections(val userId: String) : Route

@Serializable
data class Followers(val userId: String) : Route

@Serializable
data object CreatePost : Route

@Serializable
data class EditPost(val id: String, val type: String = "POST") : Route

@Serializable
data object ReplyPost : Route

@Serializable
data object Setting : Route

@Serializable
data object Society : Route

@Serializable
data object CreateSociety : Route

@Serializable
data object SocietyHub : Route

@Serializable
data object JoinSociety : Route

@Serializable
data object Connection : Route

@Serializable
data object Opportunities : Route

@Serializable
data class OpportunityDetail(val opportunityId: String) : Route

@Serializable
data class CourseDetail(val courseId: String) : Route

@Serializable
data object Courses : Route

@Serializable
data object Collaborations : Route

@Serializable
data object CreateCollab : Route

@Serializable
data class CollabDetail(val collabId: String) : Route

@Serializable
data class CollabRequests(val collabId: String) : Route

@Serializable
data class PostView(
    val postId: String? = null,
    val postImage: String? = null,
    val initialIndex: Int = 0
) : Route

@Serializable
data class VideoView(val videoUrl: String, val thumbnailUrl: String? = null) : Route

@Serializable
data class PdfView(
    val pdfUrl: String,
    val fileName: String? = null,
    val thumbnailUrl: String? = null
) : Route

// --- Helper for Bottom Bar ---
val bottomBarRoutes = listOf(
    Home::class,
    Collab::class,
    Connection::class,
    Profile::class,
    Society::class,
    Opportunities::class
)

fun shouldShowBottomBar(destination: NavDestination?): Boolean {
    return bottomBarRoutes.any { destination?.hasRoute(it) == true }
}
