package com.iota.campusX.Feature.Search.Data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.cancellation.CancellationException

class SearchRepositoryImpl(private val firestore: FirebaseFirestore): SearchRepository {

    override fun userSearch(query: String): Flow<Result<List<UserSearchDTO>>> = callbackFlow {
        if (query.isBlank()) {
            trySend(Result.success(emptyList()))
            close()
            return@callbackFlow

        }

        try {
            Log.d("SearchRepositoryImpl", "Search triggered with query: $query")

            val snapshot = firestore.collection("Users")
                .orderBy("userName")
                .startAt(query)
                .endAt(query + '\uf8ff')
                .get()
                .addOnSuccessListener {snapshot ->
                    val users = snapshot.documents.mapNotNull { doc ->
                        val user = doc.toObject(UserSearchDTO::class.java)
                        Log.d("SearchRepositoryImpl", "Fetched user: $user")
                        user
                    }
                    trySend(Result.success(users))


                }.addOnFailureListener {
                    trySend(Result.failure(it))

                }

        } catch (e: Exception) {
            Log.e("SearchRepositoryImpl", "Search failed: ${e.message}", e)
            if (e is CancellationException) {
                // ✅ Don't log or emit anything — just rethrow
                throw e
            }
        }
        awaitClose {
            close()
        }

    }



}