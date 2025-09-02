package com.iota.campusX.Feature.Follow.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Follow.domain.FollowRepositoryInterface
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO
import kotlinx.coroutines.tasks.await

class FollowRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
): FollowRepositoryInterface {

    override suspend fun follow(userId: String): Result<Boolean> {
        return try {

            if (auth.currentUser == null)
               return Result.failure(Exception("User not authenticated"))

            firestore.collection("Users")
                .document(userId)
                .collection("Followers")
                .document(auth.currentUser?.uid ?: "")
                .set(
                    mapOf(
                        "createdAt" to FieldValue.serverTimestamp(),
                        "userId" to auth.currentUser?.uid
                    )
                )
                .await()
            Result.success(true)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun unfollow(userId: String): Result<Boolean> {
        return try {

            if (auth.currentUser == null) return Result.failure(Exception("User not authenticated"))

            firestore.collection("Users")
                .document(userId)
                .collection("Followers")
                .document(auth.currentUser?.uid ?: "")
                .delete()
                .await()
            Result.success(true)

        }catch (e: Exception){
            Result.failure(e)
        }
    }

    override suspend fun getFollowers(userId: String): Result<List<ConnectionsDTO>> {
         return try {
             if (auth.currentUser == null) return Result.failure(Exception("User not authenticated"))

             val followers = firestore.collection("Users")
                 .document(userId)
                 .collection("Followers")
                 .get()
                 .await()

             val followersList = followers.map {

                val userId =  it.get("userId") as String

                val userData =  firestore.collection("Users")
                     .document(userId)
                     .get()
                     .await()
                     .toObject(BaseProfileDTO::class.java)

                 ConnectionsDTO(
                     user = UserBasicDetail(
                         id = userData?.id ?: "",
                         userName = userData?.userName ?: "",
                         userImage = userData?.userImage ?: "",
                         userBio = userData?.userBio ?: "",
                         isCurrentUser = auth.currentUser?.uid == userId
                     )
                 )

             }

             Result.success(followersList)


         }catch (e:Exception){
             Result.failure(e)
         }
    }

}