package com.iota.campusX.Utils

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class TokenProvider(private val auth: FirebaseAuth) {
    
    suspend fun getToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }
}