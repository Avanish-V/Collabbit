package com.iota.campusX.Feature.Chats.data

import SendPushNotification
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseReference.CompletionListener
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Utils.ResultState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.Serializable

class ChatImpl(
    private val sendPushNotification: SendPushNotification,
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ChatRepository {

    override fun sendMessage(message: String, messageId: String, timestamp: Any, receiverId: String, roomId: String): Flow<ResultState<Boolean>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            val senderId = auth.currentUser?.uid ?: return@callbackFlow

            if(receiverId.isEmpty()) return@callbackFlow

            val roomId = if (roomId.isEmpty()){
                senderId+receiverId
            }else{
                roomId
            }

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

                                        val messageData = hashMapOf(
                                            "messageId" to messageId,
                                            "senderId" to senderId,
                                            "text" to message,
                                            "timestamp" to timestamp,
                                            "read" to false
                                        )

                                        database.getReference("ChatRoom")
                                            .child(roomId)
                                            .child("messages")
                                            .child(key)
                                            .setValue(messageData)
                                            .addOnSuccessListener {
                                                trySend(ResultState.Success(true))
                                                sendPushNotification.messageNotification(
                                                    notificationReceiverId = receiverId,
                                                    notificationType = "MESSAGE"
                                                )
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

    override suspend fun getChats(): Result<List<UserChatsDTO>> {
        return try {
            val currentUserId = auth.currentUser?.uid
                ?: return Result.failure(Exception("User not authenticated"))

            val chatList = mutableListOf<UserChatsDTO>()
            val userChatsRef = firestore.collection("Chats").document(currentUserId).collection("Messages")

            val messageDocs = Tasks.await(userChatsRef.get())
            val chatCount = messageDocs.size()
            if (chatCount == 0) {
                return Result.success(emptyList())
            }

            var processedChats = 0
            val deferreds = messageDocs.map { doc ->
                CoroutineScope(Dispatchers.IO).async {
                    val idData = doc.toObject(ID::class.java)
                    val receiverId = idData._id
                    val roomId = idData.roomId

                    if (receiverId.isEmpty() || roomId.isEmpty()) return@async null

                    val userSnapshot = try {
                        Tasks.await(firestore.collection("Users").document(receiverId).get())
                    } catch (e: Exception) {
                        return@async null
                    }

                    val userData = userSnapshot.toObject(BaseProfileDTO::class.java) ?: return@async null

                    val snapshot = try {
                        Tasks.await(database.getReference("ChatRoom").child(roomId).child("messages").get())
                    } catch (e: Exception) {
                        return@async null
                    }

                    var unreadCount = 0
                    var lastMessage: ChatMessage? = null

                    for (messageSnap in snapshot.children) {
                        val message = messageSnap.getValue(ChatMessage::class.java)
                        if (message != null) {
                            if (message.senderId != currentUserId && message.read == false) {
                                unreadCount++
                            }
                            if (lastMessage == null || message.timestamp.toLong() > lastMessage.timestamp.toLong()) {
                                lastMessage = message
                            }
                        }
                    }

                    val isLastMessageReadByReceiver = lastMessage?.senderId == currentUserId && lastMessage.read == true
                    val lastMessageBy = lastMessage?.senderId == currentUserId

                    UserChatsDTO(
                        roomId = roomId,
                        receiverId = receiverId,
                        userName = userData.userName,
                        userImage = userData.userImage,
                        lastMessage = LastMessage(
                            lastMessage = lastMessage?.text ?: "",
                            timeStamp = lastMessage?.timestamp?.toLong() ?: 0L,
                            unreadCount = unreadCount,
                            isRead = isLastMessageReadByReceiver,
                            lastMessageBy = lastMessageBy
                        )
                    )
                }
            }

            val results = deferreds.awaitAll().filterNotNull()
            Result.success(results.sortedByDescending { it.lastMessage.timeStamp })

        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override fun markMessagesAsReed(participantId: String, roomId: String): Flow<Unit> = callbackFlow {

        if (roomId.isEmpty()) {
            close()
            return@callbackFlow
        }

        val roomRef = FirebaseDatabase.getInstance()
            .getReference("ChatRoom")
            .child(roomId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) return

                val messagesSnapshot = snapshot.child("messages")
                for (messageSnap in messagesSnapshot.children) {
                    val message = messageSnap.getValue(ChatMessage::class.java) ?: continue
                    if (message.senderId == participantId && message.read != true) {
                        messageSnap.ref.child("read").setValue(true)
                    }
                }

                trySend(Unit).isSuccess
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        roomRef.addValueEventListener(listener)

        // ✅ FINAL LINE
        awaitClose {
            roomRef.removeEventListener(listener)
        }
    }

    override fun updateIsUserActive(isActive: Boolean,roomId: String) {

        Log.d("ChatImpl", "updateIsUserActive: $isActive $roomId")

        if (roomId.isEmpty()) return

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

    override fun getIsUserActive(receiverId: String, roomId: String): Flow<Boolean> = callbackFlow {
        if (roomId.isEmpty()) {
            close()
            return@callbackFlow
        }

        val roomRef = database.getReference("ChatRoom").child(roomId).child(receiverId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isActive = snapshot.child("isActive").getValue(Boolean::class.java)
                if (isActive != null) {
                    trySend(isActive).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cancel("Firebase cancelled: ${error.message}")
            }
        }

        roomRef.addValueEventListener(listener)

        // ✅ FINAL LINE
        awaitClose {
            roomRef.removeEventListener(listener)
        }
    }

    override fun updateIsUserTyping(isActive: Boolean, roomId: String) {
        if (roomId.isEmpty()) return

        val typingRef = database.getReference("ChatRoom")
            .child(roomId)
            .child(auth.currentUser!!.uid)

        typingRef.child("isTyping").setValue(isActive)
    }

    override fun getIsUserTyping(participantId: String, roomId: String): Flow<Boolean> = callbackFlow {

        if (roomId.isEmpty()) {
            close()
            return@callbackFlow
        }

        val typingRef = database.getReference("ChatRoom").child(roomId).child(participantId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val isTyping = snapshot.child("isTyping").getValue(Boolean::class.java)
                if (isTyping != null) {
                    trySend(isTyping).isSuccess
                }
            }

            override fun onCancelled(error: DatabaseError) {
                cancel("Firebase cancelled: ${error.message}")
            }
        }

        typingRef.addValueEventListener(listener)

        awaitClose {
            typingRef.removeEventListener(listener) // 🛑 Required cleanup
        }
    }

    override fun receiveMessage(participantId: String,roomId: String): Flow<ResultState<List<ChatMessage>>> = callbackFlow {

        if (roomId.isEmpty()) {
            close()
            return@callbackFlow
        }

        trySend(ResultState.Loading)

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
    
    override fun getRoomId(participantId: String): Flow<ResultState<String>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Chats").document(participantId).collection("Messages")
                    .document(auth.currentUser!!.uid)

            }catch (e: Exception){

            }

            awaitClose{
                close()
            }

        }
    }

    override fun deleteChat(chatId: String, roomId: String): Flow<ResultState<Boolean>> = callbackFlow {
        trySend(ResultState.Loading)

        val ref = database.getReference("ChatRoom").child(roomId).child("messages").child(chatId)

        val listener = object : CompletionListener {
            override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Something went wrong"))
                } else {
                    trySend(ResultState.Success(true))
                }
                // Do not close here; awaitClose will handle it.
            }
        }

        ref.removeValue(listener)

        awaitClose {
            // no cleanup needed here for single removal
        }
    }

    override fun fetchChatRoomId(userId: String): Flow<ResultState<String>> {
        return callbackFlow {

            trySend(ResultState.Loading)

            try {

                firestore.collection("Chats").document(auth.currentUser!!.uid)
                    .collection("Messages")
                    .document(userId)
                    .get()
                    .addOnSuccessListener {
                        val roomId = it.getString("roomId")
                        if (roomId != null)
                            trySend(ResultState.Success(roomId))
                    }
                    .addOnFailureListener {
                        trySend(ResultState.Error(it.message.toString()))

                    }

            }catch (e: Exception){
                trySend(ResultState.Error(e.message.toString()))
            }

            awaitClose()

        }
    }
}

@Serializable
data class ID(
    val _id : String = "",
    val type : String = "",
    val roomId: String = ""
)