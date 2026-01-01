import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.VerifyUserRepository
import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText

class VerifyUserRepoImpl(
    private val firebaseAuth: FirebaseAuth,
    private val httpClint: HttpClient
) : VerifyUserRepository {

    override suspend fun verifyUser(userToken: String): Result<UserProfileEntity> {
        return try {
            val response = httpClint.get("$END_POINT/users/me") {
                header("Authorization", "Bearer $userToken")
            }

            Log.d("VerifyUserRepoImpl", "Status: ${response.status}")
            val body = response.bodyAsText()
            Log.d("VerifyUserRepoImpl", "Body: $body")
            Log.d("VerifyUserRepoImpl", "Sending token: $userToken")

            when (response.status.value) {
                200 -> {
                    val profileDTO = Gson().fromJson(body, UserProfileEntity::class.java)
                    Log.d("VerifyUserRepoImpl", "ProfileDTO: $profileDTO")
                    Result.success(profileDTO)
                }
                401, 403 -> {
                    // Invalid/expired token
                    FirebaseAuth.getInstance().signOut()
                    Result.failure(Exception("Unauthorized: ${response.status}"))
                }
                else -> {
                    // Other server errors
                    Result.failure(Exception("Server error: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            // Network failure, timeout, etc.
            FirebaseAuth.getInstance().signOut()
            Log.e("VerifyUserRepoImpl", "Network error: ${e.localizedMessage}", e)
            Result.failure(e)
        }
    }


}
