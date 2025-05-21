package com.iota.campusX.Feature.Chats.data

import android.util.Log
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.UserProfile.data.UserBasicProfileDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable
import java.util.UUID

class ChatImpl(
    private val participantId: String,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun sendMessage(message: String, messageId: String, receiverId: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            val senderId = auth.currentUser?.uid ?: return@callbackFlow

            if(receiverId.isEmpty()) return@callbackFlow

            val roomId = senderId+receiverId
            val timeStamp = System.currentTimeMillis().toString()
            val key = messageId

            firestore.collection("Chats")
                .document(senderId)
                .collection("Messages")
                .document(receiverId)
                .set(
                    mapOf(
                        "_id" to receiverId,
                        "type" to "SEND",
                        "roomId" to roomId
                    )
                )
                .addOnSuccessListener {

                    // ✅ Set message metadata for receiver
                    firestore.collection("Chats")
                        .document(receiverId)
                        .collection("Messages")
                        .document(senderId)
                        .set(
                            mapOf(
                                "_id" to senderId,
                                "type" to "RECEIVE",
                                "roomId" to roomId
                            )
                        )
                        .addOnSuccessListener {

                            // ✅ Write message to Realtime Database
                            database.getReference("ChatRoom")
                                .child(roomId)
                                .addListenerForSingleValueEvent(object : ValueEventListener {

                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        val message = ChatMessage(
                                            messageId = key!!,
                                            senderId = senderId,
                                            text = message,
                                            timestamp = timeStamp.toLong(),
                                            read = false
                                        )

                                        database.getReference("ChatRoom")
                                            .child(roomId)
                                            .child("messages")
                                            .child(key)
                                            .setValue(message)
                                            .addOnSuccessListener {
                                                trySend(ResultState.Success(true))
                                            }
                                            .addOnFailureListener {
                                                trySend(ResultState.Error(it.message ?: "Something went wrong"))
                                            }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        trySend(ResultState.Error(error.message))
                                    }
                                })
                        }
                        .addOnFailureListener {
                            trySend(ResultState.Error(it.message ?: "Failed to write receiver message metadata"))
                        }

                }
                .addOnFailureListener {
                    trySend(ResultState.Error(it.message ?: "Failed to write sender message metadata"))
                }

            awaitClose { close() }
        }
    }

    override fun getChats(): Flow<ResultState<List<UserChatsDTO>>> = callbackFlow {


        val currentUserId = auth.currentUser?.uid

        if (currentUserId == null) {
            trySend(ResultState.Error("User not authenticated"))
            close()
            return@callbackFlow
        }

        trySend(ResultState.Loading)

        val chatList = mutableListOf<UserChatsDTO>()
        val userChatsRef = firestore.collection("Chats").document(currentUserId).collection("Messages")

        userChatsRef.get()
            .addOnSuccessListener { messageDocs ->
                val chatCount = messageDocs.size()
                if (chatCount == 0) {
                    trySend(ResultState.Success(emptyList()))
                    close()
                    return@addOnSuccessListener
                }

                var processedChats = 0

                for (doc in messageDocs) {
                    val idData = doc.toObject(ID::class.java)
                    val receiverId = idData._id
                    val roomId = idData.roomId

                    if (receiverId.isEmpty() || roomId.isEmpty()) {
                        processedChats++
                        if (processedChats == chatCount) {
                            trySend(ResultState.Success(chatList))
                            close()
                        }
                        continue
                    }

                    firestore.collection("Users").document(receiverId).get()
                        .addOnSuccessListener { userSnapshot ->
                            val userData = userSnapshot.toObject(UserBasicProfileDTO::class.java)

                            if (userData == null) {
                                processedChats++
                                if (processedChats == chatCount) {
                                    trySend(ResultState.Success(chatList))
                                    close()
                                }
                                return@addOnSuccessListener
                            }

                            val messagesRef = database.getReference("ChatRoom").child(roomId).child("messages")

                            // Fetch both unread count and last message together
                            messagesRef.get().addOnSuccessListener { snapshot ->
                                var unreadCount = 0
                                var lastMessage: ChatMessage? = null

                                for (messageSnap in snapshot.children) {
                                    val message = messageSnap.getValue(ChatMessage::class.java)
                                    if (message != null) {
                                        if (message.senderId != auth.currentUser!!.uid && message.read == false) {
                                            unreadCount++
                                        }
                                        if (lastMessage == null || message.timestamp.toLong() > lastMessage.timestamp.toLong()
                                        ) {
                                            lastMessage = message
                                        }
                                    }
                                }

                                chatList.add(
                                    UserChatsDTO(
                                        roomId = roomId,
                                        receiverId = receiverId,
                                        userName = userData.userName,
                                        userImage = userData.userImage,
                                        lastMessage = LastMessage(
                                            lastMessage = lastMessage?.text ?: "",
                                            timeStamp = lastMessage?.timestamp?.toLong() ?: 0L,
                                            unreadCount = unreadCount
                                        )
                                    )
                                )

                                processedChats++
                                if (processedChats == chatCount) {
                                    trySend(ResultState.Success(chatList.sortedByDescending { it.lastMessage.timeStamp }))
                                    close()
                                }
                            }.addOnFailureListener {
                                processedChats++
                                if (processedChats == chatCount) {
                                    trySend(ResultState.Success(chatList.sortedByDescending { it.lastMessage.timeStamp }))
                                    close()
                                }
                            }
                        }
                        .addOnFailureListener {
                            processedChats++
                            if (processedChats == chatCount) {
                                trySend(ResultState.Success(chatList))
                                close()
                            }
                        }
                }
            }
            .addOnFailureListener {
                trySend(ResultState.Error(it.message ?: "Failed to fetch chats"))
                close()
            }

        awaitClose()
    }

    override fun updateIsUserActive(isActive: Boolean,participantId: String) {

        val roomId = auth.currentUser!!.uid+participantId

        database.getReference("ChatRoom").child(roomId)
            .child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        database.getReference("ChatRoom").child(roomId)
                            .child(auth.currentUser!!.uid)
                            .updateChildren(
                                mapOf(
                                    "isActive" to isActive,
                                )
                            )
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }

            )

    }

    override fun getIsUserActive( receiverId: String): Flow<Boolean> {
        return callbackFlow {

            val roomId = auth.currentUser!!.uid+receiverId

            database.getReference("ChatRoom").child(roomId)
                .child(receiverId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.hasChild("isActive")) {
                            val isActive = snapshot.child("isActive").value as? Boolean
                            trySend(isActive as Boolean)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            awaitClose { close() }
        }
    }

    override fun updateIsUserTyping(isActive: Boolean,participantId: String) {

        val roomId = auth.currentUser!!.uid+participantId

        database.getReference("ChatRoom").child(roomId)
            .child(auth.currentUser!!.uid)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        database.getReference("ChatRoom").child(roomId).child(auth.currentUser!!.uid)
                            .updateChildren(
                                mapOf(
                                    "isTyping" to isActive,
                                )
                            )
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                }

            )


    }

    override fun getIsUserTyping(participantId: String): Flow<Boolean>{
        return callbackFlow {

            val roomId = auth.currentUser!!.uid+participantId

            database.getReference("ChatRoom").child(roomId)
                .child(participantId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists() && snapshot.hasChild("isTyping")) {
                            val isTyping = snapshot.child("isTyping").value as? Boolean
                            trySend(isTyping as Boolean)
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            awaitClose { close() }
        }
    }

    override fun receiveMessage(participantId: String): Flow<ResultState<List<ChatMessage>>> = callbackFlow {
        trySend(ResultState.Loading)

        val roomId = auth.currentUser!!.uid+participantId

        val roomRef = database.getReference("ChatRoom").child(roomId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    trySend(ResultState.Error("ChatRoom does not exist"))
                    return
                }

                val messageList = mutableListOf<ChatMessage>()
                val lastSeenMap = mutableMapOf<String, Long>()

                // Extract messages
                val messagesSnapshot = snapshot.child("messages")
                for (messageSnap in messagesSnapshot.children) {
                    val message = messageSnap.getValue(ChatMessage::class.java)
                    if (message != null) {
                        messageList.add(message)
                    } else {
                        Log.e("ChatImpl", "Invalid message format: ${messageSnap.value}")
                    }
                }

                // Extract LastSeen map
                val lastSeenSnapshot = snapshot.child("LastSeen")
                for (child in lastSeenSnapshot.children) {
                    val userId = child.key
                    val seenTime = child.getValue(Long::class.java)
                    if (userId != null && seenTime != null) {
                        lastSeenMap[userId] = seenTime
                    }
                }

                val chatRoomData = messageList

                trySend(ResultState.Success(chatRoomData))
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(ResultState.Error(error.message))
            }
        }

        roomRef.addValueEventListener(listener)

        awaitClose {
            roomRef.removeEventListener(listener)
        }
    }

    override fun markMessagesAsReed(participantId: String) {

        val roomId = auth.currentUser!!.uid+participantId

        val roomRef = database.getReference("ChatRoom").child(roomId)

        roomRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val messagesSnapshot = snapshot.child("messages")
                for (messageSnap in messagesSnapshot.children) {
                    val message = messageSnap.getValue(ChatMessage::class.java) ?: continue
                    val senderId = message.senderId
                    val isRead = message.read

                    if (senderId == participantId && !isRead) {
                        messageSnap.ref.child("read").setValue(true)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed (e.g., log it)
            }
        })
    }

    override fun getRoomId(participantId: String): Flow<ResultState<String>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Chats").document(participantId).collection("Messages")
                    .document(auth.currentUser!!.uid)

            }catch (e: Exception){

            }

        }
    }

}

@Serializable
data class ID(
    val _id : String = "",
    val type : String = "",
    val roomId: String = ""
)