import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.PushNotification.FcmNotificationSender
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class SendPushNotification(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    fun messageNotification(notificationReceiverId: String, notificationType: String) {

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
                        userFcmToken = fcmToken,
                        title = notificationText(notificationType,userNameResponse.userName).title,
                        body = notificationText(notificationType).body
                    )
                    messageNotify.sendNotification()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                // Handle failure
            }
        }
    }

fun notificationText(notificationType: String,userName:String?=null): NotificationTextDTO{

        return when(notificationType){

            "MESSAGE" -> {
                NotificationTextDTO(
                    title = userName.toString(),
                    body = "Sent a message."
                )
            }
            "LIKE_POST" -> {
                NotificationTextDTO(
                    title = "Liked",
                    body = "Someone liked your post."
                )}
            "LIKE_REPLY" -> {
                NotificationTextDTO(
                    title = "Liked",
                    body = "Someone liked your reply."
                )
            }
            "COMMENTED" -> {
                NotificationTextDTO(
                    title = "Comment",
                    body = "Someone commented on your post."
                )
            }
            "REQUEST" -> {
                NotificationTextDTO(
                    title = "Request",
                    body = "Someone sent you a connection request."
                )
            }

            else -> {
                NotificationTextDTO()
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
