import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.VerifyUserRepository
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.MetaData
import kotlinx.coroutines.tasks.await

class VerifyUserRepoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : VerifyUserRepository {

    override suspend fun verifyUser(userId: String, userToken: String): Result<Unit> {
        return try {

            val snapshot = firestore.collection("Users")
                .document(userId)
                .get()
                .await() // suspend until finished

            if (snapshot.exists()) {

                Result.success(Unit)

            } else {

                val currentUser = firebaseAuth.currentUser ?: return Result.failure(
                    IllegalStateException("No authenticated user")
                )

                val profile = BaseProfileDTO(
                    id = currentUser.uid,
                    token = userToken,
                    userName = currentUser.displayName?.replaceFirstChar { it.uppercase() } ?: "Unknown",
                    userImage = currentUser.photoUrl?.toString() ?: "",
                    userEmail = currentUser.email ?: "",
                    metaData = MetaData(
                        premium = false,
                        firstUser = true,
                        createdAt = System.currentTimeMillis()
                    ),
                )

                firestore.collection("Users")
                    .document(userId)
                    .set(profile)
                    .await() // suspend until finished

                Result.success(Unit)
            }
        } catch (e: Exception) {

            firebaseAuth.signOut() // optional: log out on failure

            Result.failure(e)

        }
    }

}
