package com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GetTokenResult
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.UserProfile.data.MetaData
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import com.iota.campusX.Utils.ResultState
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.CancellationException


class GoogleAuthUiClient(
    private val context: Context,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
):GoogleAuthRepo {

    private val oneTapClient:SignInClient = Identity.getSignInClient(context)

    private val auth= FirebaseAuth.getInstance()

    override suspend fun signIn():IntentSender?{
        val result= try {
            oneTapClient.beginSignIn(buildSignInRequest()).await()
        }catch (e:Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            null

        }
        return result?.pendingIntent?.intentSender
    }

    override suspend fun signInWithIntent(intent:Intent):SignInResult{

        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken,null)

        return try {

            val user = auth.signInWithCredential(googleCredentials).await().user

            SignInResult(
                status = user.run { true },
                userId = user?.uid ?: "",
                userToken = googleIdToken.toString(),
                errorMessage = null
            )

        }catch (e:Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
            SignInResult(
                status = null,
                errorMessage = e.message
            )
        }
    }

    private fun buildSignInRequest():BeginSignInRequest{
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.Builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("1083272757839-jgnafarj06d7e4em1jo4ba69df0na3mg.apps.googleusercontent.com")
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()
    }

    suspend fun signOut(){
        try {
            auth.signOut()
        }catch (e:Exception){
            e.printStackTrace()
            if (e is CancellationException) throw e
        }
    }

    override fun getCurrentUser():Boolean{
        return FirebaseAuth.getInstance().currentUser?.uid != null
    }


    override fun verifyUser(userId: String,userToken: String): Flow<ResultState<UserResponse>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            firestore.collection("Users").document(userId)
                .get()
                .addOnSuccessListener {
                    if (it.exists()){
                        trySend(
                            ResultState.Success(
                                UserResponse(
                                    status = true,
                                    message = "User Already Exists"
                                )
                            )
                        )
                    }else{
                        firestore.collection("Users").document(userId)
                            .set(

                                UserBasicProfileDTO(
                                    _id = firebaseAuth.currentUser!!.uid,
                                    token = userToken,
                                    userName = firebaseAuth.currentUser!!.displayName?.replaceFirstChar { it.uppercase() } ?: "",
                                    userImage = firebaseAuth.currentUser!!.photoUrl.toString(),
                                    userEmail = firebaseAuth.currentUser!!.email.toString(),
                                    social = "",
                                    metaData = MetaData(
                                        isFirstUser = true
                                    ),
                                )

                            ).addOnSuccessListener {
                                trySend(
                                    ResultState.Success(UserResponse(
                                        status = true,
                                        message = "User Created Successfully"
                                    ))
                                )
                            }
                            .addOnCanceledListener{
                                FirebaseAuth.getInstance().signOut()
                            }
                            .addOnFailureListener {
                                FirebaseAuth.getInstance().signOut()
                            }

                    }
                }
                .addOnFailureListener {
                    trySend(
                        ResultState.Error("false")
                    )
                }

            awaitClose {
                close()
            }
        }
    }

}