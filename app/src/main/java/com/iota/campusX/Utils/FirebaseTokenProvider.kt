package com.iota.campusX.Utils

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class FirebaseTokenProvider(
    private val auth: FirebaseAuth
) {
    suspend fun getIdToken(): String? = suspendCancellableCoroutine { cont ->
        val user = auth.currentUser
        if (user == null) {
            cont.resume(null, null)
            return@suspendCancellableCoroutine
        }

        user.getIdToken(false) // false = use cached if valid
            .addOnSuccessListener { result ->
                cont.resume(result.token, null)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}

