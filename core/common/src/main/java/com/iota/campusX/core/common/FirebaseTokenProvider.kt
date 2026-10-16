package com.iota.campusX.core.common

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class FirebaseTokenProvider(
    private val auth: FirebaseAuth
) {
    suspend fun getIdToken(): String? = suspendCancellableCoroutine { cont ->
        val user = auth.currentUser
        if (user == null) {
            cont.resumeWith(Result.success(null))
            return@suspendCancellableCoroutine
        }

        user.getIdToken(false) // false = use cached if valid
            .addOnSuccessListener { result ->
                cont.resumeWith(Result.success(result.token))
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}
