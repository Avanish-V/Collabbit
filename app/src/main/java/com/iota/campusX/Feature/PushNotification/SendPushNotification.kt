import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Post.data.model.VisibilityMode
import com.iota.campusX.Feature.PushNotification.FcmNotificationSender
import com.iota.campusX.Feature.PushNotification.TokenServices
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SendPushNotification(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val context: Context
) {

    fun messageNotification(
        notificationReceiverId: String,
        notificationType: String,
        visibilityMode: VisibilityMode? = null
    ) {

        if (auth.currentUser == null) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Launch both calls in parallel
                val (userNameResponse, fcmToken) = coroutineScope {
                    val userNameDeferred = async { getUserName(auth.currentUser?.uid ?: "") }
                    val tokenDeferred = async { getFcmToken(notificationReceiverId) }

                    Pair(userNameDeferred.await(), tokenDeferred.await())
                }

                if (fcmToken != null && userNameResponse.status) {
                    val messageNotify = FcmNotificationSender(
                        context = context
                    )
                    messageNotify.sendFcmNotification(
                        userFcmToken = fcmToken,
                        title = notificationText(
                            notificationType =  notificationType,
                            userName = userNameResponse.userName).title,
                        bodyText = notificationText(
                            notificationType = notificationType,
                            visibilityMode = visibilityMode,
                            userName = userNameResponse.userName).body,
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle failure
            }
        }
    }

    fun sendNotificationToSubscriber(topic: String, title: String, body: String){

        val messageNotify = FcmNotificationSender(

            context = context
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                messageNotify.sendToSubscriber(
                    topic = topic,
                    title = title,
                    bodyText = body,
                )
            }catch (e:Exception){
                e.printStackTrace()
            }
        }

    }


fun notificationText(
    notificationType: String,
    userName:String="",
    visibilityMode: VisibilityMode?=null
): NotificationTextDTO{

    return when (notificationType) {
        "MESSAGE" -> {
            NotificationTextDTO(
                title = "New Message",
                body = "$userName sent you a message."
            )
        }
        "LIKE_POST" -> {
            NotificationTextDTO(
                title = "Post Upvote",
                body = "$userName upvoted your post."
            )
        }
        "LIKE_REPLY" -> {
            NotificationTextDTO(
                title = "Reply Upvote",
                body = "$userName upvoted your reply."
            )
        }
        "COMMENTED" -> {
            if (visibilityMode != null){

                if (visibilityMode == VisibilityMode.USER){
                    NotificationTextDTO(
                        title = "New Comment",
                        body = "$userName commented on your post."
                    )
                }else{
                    NotificationTextDTO(
                        title = "New Comment",
                        body = "Anonymous commented on your post."
                    )
                }

            }else{
                NotificationTextDTO(
                    title = "New Comment",
                    body = "$userName commented on your post."
                )
            }

        }
        "REQUEST" -> {
            NotificationTextDTO(
                title = "Connection Request",
                body = "$userName wants to connect with you."
            )
        }
        else -> {
            NotificationTextDTO(
                title = "Notification",
                body = "You have a new update."
            )
        }
    }


}


    // Suspend function to get user name
    private suspend fun getUserName(currentUser: String): GetNameResponse {
        return try {
            val document = firestore.collection("Users").document(currentUser).get().await()
            val userName = document.getString("userName") ?: ""
            GetNameResponse(userName = userName, status = true)
        } catch (e: Exception) {
            GetNameResponse(status = false)
        }
    }

    // Suspend function to get FCM token
    private suspend fun getFcmToken(userId: String): String? {
        return try {
            val document = firestore.collection("Users").document(userId).get().await()
            document.getString("token")
        } catch (e: Exception) {
            null
        }
    }

    data class GetNameResponse(
        val userName: String = "",
        val status: Boolean = false
    )
    data class NotificationTextDTO(
        val title: String = "",
        val body: String = ""
    )
}
