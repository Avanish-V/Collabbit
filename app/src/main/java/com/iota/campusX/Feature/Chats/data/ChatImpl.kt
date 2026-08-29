
package com.iota.campusX.Feature.Chats.data

import android.util.Log
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Utils.ResultState
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.decodeFromJsonElement

import com.iota.campusX.Feature.Chats.data.local.ChatDao
import com.iota.campusX.Feature.Chats.data.local.ChatMessageEntity
import com.iota.campusX.Feature.Chats.data.local.ChatRoomEntity
import com.iota.campusX.Feature.Chats.data.local.LastMessageEntity
import com.iota.campusX.realtime.socket.RealtimeSocketManager
import com.iota.campusX.realtime.model.SocketEvent
import com.iota.campusX.realtime.model.SocketEventType

class ChatImpl(
    private val httpClient: HttpClient,
    private val auth: FirebaseAuth,
    private val socketManager: RealtimeSocketManager,
    private val chatDao: ChatDao
) : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override fun sendMessage(
        message: String,
        messageId: String,
        timestamp: Any,
        receiverId: String,
        roomId: String
    ): Flow<ResultState<Boolean>> = callbackFlow {
        val currentUserId = auth.currentUser?.uid ?: ""
        val tempMsgId = messageId.ifBlank { "temp_${System.currentTimeMillis()}" }

        // 1. Optimistic local insert
        val localMsg = ChatMessageEntity(
            messageId = tempMsgId,
            roomId = roomId,
            senderId = currentUserId,
            text = message,
            attachmentUrl = null,
            timestamp = System.currentTimeMillis(),
            read = false,
            isPending = true,
            isFailed = false
        )
        chatDao.insertMessage(localMsg)
        trySend(ResultState.Success(true)) // notify UI that optimistic update succeeded

        // 2. Perform network request
        try {
            val response: HttpResponse = httpClient.post("chat/rooms/$roomId/messages") {
                contentType(ContentType.Application.Json)
                setBody(SendMessageRequest(text = message, attachmentUrl = null))
            }
            if (response.status.isSuccess()) {
                val serverMsg = response.body<ChatMessage>()
                // Replace the temporary message with the official message to avoid duplication
                chatDao.deleteMessage(tempMsgId)
                
                val officialEntity = ChatMessageEntity(
                    messageId = serverMsg.messageId,
                    roomId = roomId,
                    senderId = currentUserId,
                    text = serverMsg.text,
                    attachmentUrl = serverMsg.attachmentUrl,
                    timestamp = serverMsg.timestamp,
                    read = serverMsg.read,
                    isPending = false,
                    isFailed = false
                )
                chatDao.insertMessage(officialEntity)
            } else {
                // Mark as failed
                chatDao.updateMessageStatus(tempMsgId, isPending = false, isFailed = true)
            }
        } catch (e: Exception) {
            Log.e("ChatImpl", "Failed to send message: ${e.message}", e)
            // Mark as failed on error
            chatDao.updateMessageStatus(tempMsgId, isPending = false, isFailed = true)
        }
        awaitClose { }
    }

    override fun receiveMessage(participantId: String, roomId: String): Flow<ResultState<List<ChatMessage>>> = callbackFlow {
        // Ensure WebSocket is connected
        socketManager.connect()

        // 1. Collect from local DB flow and emit to UI
        val dbJob = scope.launch {
            chatDao.getMessagesForRoom(roomId).collect { entities ->
                val chatMessages = entities.map { entity ->
                    ChatMessage(
                        messageId = entity.messageId,
                        senderId = entity.senderId,
                        text = entity.text,
                        attachmentUrl = entity.attachmentUrl,
                        timestamp = entity.timestamp,
                        read = entity.read,
                        isPending = entity.isPending,
                        isFailed = entity.isFailed
                    )
                }
                trySend(ResultState.Success(chatMessages))
            }
        }

        // 2. Sync history via REST in background and insert into Room
        scope.launch {
            try {
                val response: HttpResponse = httpClient.get("chat/rooms/$roomId/messages?page=0&size=100")
                if (response.status.isSuccess()) {
                    val history = response.body<List<ChatMessage>>()
                    val entities = history.map { msg ->
                        ChatMessageEntity(
                            messageId = msg.messageId,
                            roomId = roomId,
                            senderId = msg.senderId,
                            text = msg.text,
                            attachmentUrl = msg.attachmentUrl,
                            timestamp = msg.timestamp,
                            read = msg.read,
                            isPending = false,
                            isFailed = false
                        )
                    }
                    chatDao.insertMessages(entities)
                }
            } catch (e: Exception) {
                Log.e("ChatImpl", "History sync failed", e)
            }
        }

        // 3. Listen to WebSocket incoming messages and insert into Room.
        // IMPORTANT: Use collect (not collectLatest) so DB writes are never cancelled mid-flight
        // when a second socket event arrives immediately after (e.g. PING after CHAT_MESSAGE).
        val wsJob = scope.launch {
            socketManager.incomingEvents.collect { event ->
                when (event.eventType) {
                    SocketEventType.CHAT_MESSAGE -> {
                        try {
                            val newMsg = json.decodeFromJsonElement<ChatMessageCreatedPayload>(event.payload)
                            if (newMsg.roomId == roomId) {
                                val currentUserId = auth.currentUser?.uid

                                // Delete matching pending message if it's from us
                                if (newMsg.senderUid == currentUserId) {
                                    chatDao.deletePendingMessage(roomId, newMsg.senderUid, newMsg.text)
                                }

                                // Delete by messageId in case it was already inserted
                                chatDao.deleteMessage(newMsg.messageId)

                                // Insert the official server message
                                val entity = ChatMessageEntity(
                                    messageId = newMsg.messageId,
                                    roomId = roomId,
                                    senderId = newMsg.senderUid,
                                    text = newMsg.text,
                                    attachmentUrl = newMsg.attachmentUrl,
                                    timestamp = newMsg.timestamp,
                                    read = newMsg.read,
                                    isPending = false,
                                    isFailed = false
                                )
                                chatDao.insertMessage(entity)
                            }
                        } catch (e: Exception) {
                            Log.e("ChatImpl", "Incoming CHAT_MESSAGE processing failed", e)
                        }
                    }
                    SocketEventType.MESSAGE_SENT -> {
                        // Server ack: our REST flow already replaced the temp message, but
                        // if the REST response hasn't been processed yet, clean up any lingering
                        // pending messages for this room using the server-assigned messageId.
                        try {
                            val ack = json.decodeFromJsonElement<MessageSentAck>(event.payload)
                            // Remove any pending placeholder that might still exist
                            chatDao.deleteMessage(ack.messageId)
                        } catch (e: Exception) {
                            Log.e("ChatImpl", "Incoming MESSAGE_SENT processing failed", e)
                        }
                    }
                    else -> Unit
                }
            }
        }

        awaitClose {
            dbJob.cancel()
            wsJob.cancel()
        }
    }

    override fun getChats(): Flow<ResultState<List<UserChatsDTO>>> = callbackFlow {
        // 1. Collect from local DB
        val dbJob = scope.launch {
            chatDao.getAllChatRooms().collect { entities ->
                val dtos = entities.map { entity ->
                    UserChatsDTO(
                        roomId = entity.roomId,
                        receiverId = entity.receiverId,
                        userName = entity.userName,
                        userImage = entity.userImage,
                        lastMessage = LastMessage(
                            lastMessage = entity.lastMessage.lastMessageText,
                            timeStamp = entity.lastMessage.timeStamp,
                            unreadCount = entity.lastMessage.unreadCount,
                            isRead = entity.lastMessage.isRead,
                            lastMessageBy = entity.lastMessage.lastMessageBy
                        )
                    )
                }
                trySend(ResultState.Success(dtos))
            }
        }

        // 2. Fetch from network and sync
        scope.launch {
            try {
                val response: HttpResponse = httpClient.get("chat/rooms")
                if (response.status.isSuccess()) {
                    val chats = response.body<List<UserChatsDTO>>()
                    val entities = chats.map { dto ->
                        ChatRoomEntity(
                            roomId = dto.roomId,
                            receiverId = dto.receiverId,
                            userName = dto.userName,
                            userImage = dto.userImage,
                            lastMessage = LastMessageEntity(
                                lastMessageText = dto.lastMessage.lastMessage,
                                timeStamp = dto.lastMessage.timeStamp,
                                unreadCount = dto.lastMessage.unreadCount,
                                isRead = dto.lastMessage.isRead,
                                lastMessageBy = dto.lastMessage.lastMessageBy
                            )
                        )
                    }
                    chatDao.insertChatRooms(entities)
                }
            } catch (e: Exception) {
                Log.e("ChatImpl", "Failed to fetch chats", e)
            }
        }
        awaitClose { dbJob.cancel() }
    }

    override fun markMessagesAsReed(participantId: String, roomId: String): Flow<Unit> = callbackFlow {
        try {
            val response: HttpResponse = httpClient.post("chat/rooms/$roomId/read")
            if (response.status.isSuccess()) {
                trySend(Unit)
            }
        } catch (e: Exception) {
            Log.e("ChatImpl", "Failed to mark read: ${e.message}")
        }
        awaitClose { }
    }

    override fun updateIsUserActive(isActive: Boolean, roomId: String) {
        scope.launch {
            val receiverId = chatDao.getReceiverIdByRoomId(roomId) ?: ""
            val eventType = if (isActive) SocketEventType.USER_ONLINE else SocketEventType.USER_OFFLINE
            val payload = PresencePayload(
                roomId = roomId,
                recipientId = receiverId,
                active = isActive
            )
            val socketEvent = SocketEvent(
                eventType = eventType,
                payload = json.encodeToJsonElement(payload)
            )
            socketManager.send(socketEvent)
        }
    }

    override fun getIsUserActive(receiverId: String, roomId: String): Flow<Boolean> = callbackFlow {
        socketManager.connect()

        val job = scope.launch {
            socketManager.incomingEvents.collect { event ->
                if (event.eventType == SocketEventType.USER_ONLINE || event.eventType == SocketEventType.USER_OFFLINE) {
                    try {
                        val presence = json.decodeFromJsonElement<PresencePayload>(event.payload)
                        if (presence.roomId == roomId) {
                            trySend(presence.active)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatImpl", "Failed parsing presence", e)
                    }
                }
            }
        }
        awaitClose { job.cancel() }
    }

    override fun updateIsUserTyping(isActive: Boolean, roomId: String) {
        scope.launch {
            val receiverId = chatDao.getReceiverIdByRoomId(roomId) ?: ""
            val payload = TypingPayload(
                roomId = roomId,
                recipientId = receiverId,
                typing = isActive
            )
            val socketEvent = SocketEvent(
                eventType = SocketEventType.USER_TYPING,
                payload = json.encodeToJsonElement(payload)
            )
            socketManager.send(socketEvent)
        }
    }

    override fun getIsUserTyping(participantId: String, roomId: String): Flow<Boolean> = callbackFlow {
        socketManager.connect()

        val job = scope.launch {
            socketManager.incomingEvents.collect { event ->
                if (event.eventType == SocketEventType.USER_TYPING) {
                    try {
                        val typingPayload = json.decodeFromJsonElement<TypingPayload>(event.payload)
                        if (typingPayload.roomId == roomId) {
                            trySend(typingPayload.typing)
                        }
                    } catch (e: Exception) {
                        Log.e("ChatImpl", "Failed parsing typing status", e)
                    }
                }
            }
        }
        awaitClose { job.cancel() }
    }

    override fun getRoomId(participantId: String): Flow<ResultState<String>> {
        return fetchChatRoomId(participantId)
    }

    override fun deleteChat(chatId: String, roomId: String): Flow<ResultState<Boolean>> = callbackFlow {
        trySend(ResultState.Loading)
        try {
            val response: HttpResponse = httpClient.delete("chat/messages/$chatId")
            if (response.status.isSuccess()) {
                trySend(ResultState.Success(true))
            } else {
                trySend(ResultState.Error("Failed to delete: ${response.status.value}"))
            }
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Network error"))
        }
        awaitClose { }
    }

    override fun fetchChatRoomId(userId: String): Flow<ResultState<String>> = callbackFlow {
        trySend(ResultState.Loading)
        
        // 1. Check local DB first for instant response
        val localRoomId = chatDao.getRoomIdByReceiverId(userId)
        if (!localRoomId.isNullOrBlank()) {
            trySend(ResultState.Success(localRoomId))
        }

        // 2. Always fetch from network to ensure we have the latest/correct one
        try {
            val response: HttpResponse = httpClient.get("chat/rooms/private/$userId")
            if (response.status.isSuccess()) {
                val body = response.body<Map<String, String>>()
                val roomId = body["roomId"] ?: ""
                if (roomId.isNotEmpty()) {
                    trySend(ResultState.Success(roomId))
                }
            } else {
                // Only emit error if we don't have a local one
                if (localRoomId.isNullOrBlank()) {
                    trySend(ResultState.Error("Server error: ${response.status.value}"))
                }
            }
        } catch (e: Exception) {
            if (localRoomId.isNullOrBlank()) {
                trySend(ResultState.Error(e.message ?: "Unknown error"))
            }
        }
        awaitClose { }
    }
}

@Serializable
data class SendMessageRequest(val text: String, val attachmentUrl: String?)

@Serializable
data class PresencePayload(
    val roomId: String,
    val recipientId: String,
    val active: Boolean
)

@Serializable
data class TypingPayload(
    val roomId: String,
    val recipientId: String,
    val typing: Boolean
)

@Serializable
data class ChatMessageCreatedPayload(
    val messageId: String,
    val roomId: String,
    val senderUid: String,
    val text: String,
    val attachmentUrl: String?,
    val timestamp: Long,
    val read: Boolean
)

@Serializable
data class MessageSentAck(
    val localId: String,    // client-side temp id (empty string if not used)
    val messageId: String   // the server-assigned official message id
)