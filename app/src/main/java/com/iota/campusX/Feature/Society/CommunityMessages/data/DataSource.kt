package com.iota.campusX.Feature.Society.CommunityMessages.data

import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Society.CommunityMessages.data.model.MessageDto
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class DataSource(
    private val firestore: FirebaseFirestore
) {


    suspend fun sendMessage(groupId:String, message: MessageDto) {
       val reference =  firestore.collection("CommunityChats")
            .document(groupId)
            .collection("messages")
       reference.document(message.messageId).set(message).await()
    }



    fun observeMessages(groupId: String): Flow<List<MessageDto>> = callbackFlow {
        val listener = firestore.collection("CommunityChats")
            .document(groupId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val dtos = snapshot?.documents?.mapNotNull { it.toObject(MessageDto::class.java) } ?: emptyList()
                trySend(dtos)
            }
        awaitClose { listener.remove() }
    }



}