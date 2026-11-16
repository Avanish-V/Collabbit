
package com.iota.campusX.Feature.Chats.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Chats.data.local.ChatDao
import com.iota.campusX.Feature.Chats.data.local.ChatMessageEntity
import com.iota.campusX.Feature.Chats.data.local.ChatRoomEntity
import com.iota.campusX.Feature.Chats.data.local.LastMessageEntity
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.core.common.ResultState
import com.iota.campusX.core.network.websocket.model.SocketEvent
import com.iota.campusX.core.network.websocket.model.SocketEventType
import com.iota.campusX.core.network.websocket.socket.RealtimeSocketManager
import com.iota.campusX.Feature.Chats.domain.ChatSendMessagePayload
import com.iota.campusX.Feature.Chats.domain.MessagesReadPayload
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID

class ChatImpl(
    private val httpClient: HttpClient,
    private val auth: FirebaseAuth,
    private val socketManager: RealtimeSocketManager,
    private val chatDao: ChatDao
) : ChatRepository {

    private val scope = CoroutineScope(Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun sendMessage(message: String, receiverId: String, roomId: String): Result<Boolean> {

        val currentUserId = auth.currentUser?.uid ?: ""

        val timestamp = System.currentTimeMillis()

        val clientMessageId = UUID.randomUUID().toString()

        val localMsg = ChatMessageEntity(
            messageId = clientMessageId,
            roomId = roomId,
            senderId = currentUserId,
            text = message,
            attachmentUrl = null,
            timestamp = timestamp,
            read = false,
            isPending = true,
            isFailed = false
        )

        chatDao.insertMessage(localMsg)

        val payload = ChatSendMessagePayload(
            messageId = clientMessageId,
            recipientId = receiverId,
            roomId = roomId,
            text = message
        )

        val event = SocketEvent(
            eventType = SocketEventType.CHAT_MESSAGE,
            payload = Json.encodeToJsonElement(payload)
        )

        try {

            socketManager.send(event)

        } catch (e: Exception) {
            chatDao.updateMessageStatus(messageId = clientMessageId, isPending = false, isFailed = true)
            return Result.failure(e)
        }

        return Result.success(true)

    }

    override fun receiveMessage(participantId: String, roomId: String): Flow<ResultState<List<ChatMessage>>> = callbackFlow  {

        socketManager.connect()

        val dbJob = launch {
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

        launch {
            try {
                val response: HttpResponse = httpClient.get("chat/rooms/$roomId/messages?page=0&size=30")
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

        val wsJob = scope.launch {
            socketManager.incomingEvents.collect { event ->
                when (event.eventType) {
                    SocketEventType.CHAT_MESSAGE -> {
                        try {
                            val newMsg = json.decodeFromJsonElement<ChatMessageCreatedPayload>(event.payload)
                            if (newMsg.roomId == roomId) {

                                val entity = ChatMessageEntity(
                                    messageId = newMsg.messageId,
                                    roomId = roomId,
                                    senderId = newMsg.senderUid,
                                    text = newMsg.text ?: "",
                                    attachmentUrl = newMsg.attachmentUrl,
                                    timestamp = newMsg.timestamp,
                                    read = newMsg.read,
                                    isPending = false,
                                    isFailed = false
                                )
                                Log.e("ChatImpl", "Incoming CHAT_MESSAGE $entity")
                                chatDao.insertMessage(entity)

                                if(!newMsg.read){
                                    markMessagesAsReed(newMsg.senderUid,newMsg.roomId)
                                }

                            }
                        } catch (e: Exception) {
                            Log.e("ChatImpl", "Incoming CHAT_MESSAGE processing failed", e)
                        }
                    }
                    SocketEventType.MESSAGE_SENT -> {
                        try {
                            val ack = json.decodeFromJsonElement<MessageSentAck>(event.payload)
                            Log.e("ChatImpl", "ACK CHAT_MESSAGE $ack")
                            chatDao.updateMessageStatus(
                                messageId = ack.messageId,
                                isPending = false,
                                isFailed = false
                            )
                        } catch (e: Exception) {
                            Log.e("ChatImpl", "Incoming MESSAGE_SENT processing failed", e)
                        }
                    }

                    SocketEventType.MESSAGES_READ -> {
                        try {
                            val payload =
                                json.decodeFromJsonElement<MessagesReadPayload>(
                                    event.payload
                                )

                            if (payload.roomId == roomId) {
                                chatDao.markMessagesAsRead(
                                    messageIds = payload.messageIds
                                )
                            }

                            Log.e("ChatImpl", "READ CHAT_MESSAGE $payload")

                        } catch (e: Exception) {
                            Log.e("ChatImpl", "Read receipt failed", e)
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
    override suspend fun markMessagesAsReed(participantId: String, roomId: String){
        try {
            val currentUserId = auth.currentUser?.uid ?: return
            // 1. Zero room-level unread counter in Room DB immediately → badge updates
            chatDao.clearUnreadCountForRoom(roomId)
            // 2. Mark all messages in this room as read locally → chat_messages.read = 1
            //    This ensures observeUnreadMessagesCount() also returns 0 immediately,
            //    without waiting for the MESSAGES_READ WebSocket round-trip.
            chatDao.markRoomMessagesAsRead(roomId, currentUserId)
            // 3. Notify backend (which will also publish MESSAGES_READ WS event to sender)
            httpClient.post("chat/rooms/$roomId/read")
        } catch (e: Exception) {
            Log.e("ChatImpl", "Failed to mark read: ${e.message}")
        }
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

    override suspend fun getRoomId(participantId: String): Result<String> {
        return fetchChatRoomId(participantId)
    }

    override suspend fun deleteChat(chatId: String, roomId: String): Result<Boolean> {
        return try {
            val response: HttpResponse = httpClient.delete("chat/messages/$chatId")
            if (response.status.isSuccess()) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to delete: ${response.status.value}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun fetchChatRoomId(userId: String): Result<String> {
        val localRoomId = chatDao.getRoomIdByReceiverId(userId)
        if (!localRoomId.isNullOrBlank()) {
            return Result.success(localRoomId)
        }

        return try {
            val response: HttpResponse = httpClient.get("chat/rooms/private/$userId")
            if (response.status.isSuccess()) {
                val body = response.body<Map<String, String>>()
                val roomId = body["roomId"] ?: ""
                if (roomId.isNotEmpty()) {
                    Result.success(roomId)
                } else {
                    Result.failure(Exception("Empty roomId"))
                }
            } else {
                if (!localRoomId.isNullOrBlank()) {
                    Result.success(localRoomId)
                } else {
                    Result.failure(Exception("Server error: ${response.status.value}"))
                }
            }
        } catch (e: Exception) {
            if (!localRoomId.isNullOrBlank()) {
                Result.success(localRoomId)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun fetchAndSaveIncomingMessage(messageId: String, roomId: String) {
        try {
            val response: HttpResponse = httpClient.get("chat/messages/$messageId")
            if (response.status.isSuccess()) {
                val msg = response.body<ChatMessageNetworkDto>()
                val resolvedSenderId = if (msg.senderId.isNotEmpty()) msg.senderId else msg.senderUid
                val entity = ChatMessageEntity(
                    messageId = msg.messageId.ifEmpty { messageId },
                    roomId = msg.roomId.ifEmpty { roomId },
                    senderId = resolvedSenderId,
                    text = msg.text,
                    attachmentUrl = msg.attachmentUrl,
                    timestamp = if (msg.timestamp > 0) msg.timestamp else System.currentTimeMillis(),
                    read = msg.read,
                    isPending = false,
                    isFailed = false
                )
                val effectiveRoomId = entity.roomId
                // Insert only if not already present (IGNORE strategy)
                val currentUserId = auth.currentUser?.uid ?: ""
                val isIncoming = resolvedSenderId != currentUserId
                chatDao.upsertIncomingMessage(entity)
                // Bump the room unread count so the badge updates immediately,
                // even when the chat screen is not open.
                // Only increment for messages not sent by us.
                if (isIncoming && !msg.read) {
                    chatDao.incrementUnreadCount(effectiveRoomId)
                }
            } else {
                Log.w("ChatImpl", "fetchAndSaveIncomingMessage: HTTP ${response.status.value} for messageId=$messageId")
            }
        } catch (e: Exception) {
            Log.e("ChatImpl", "fetchAndSaveIncomingMessage failed for messageId=$messageId", e)
        }
    }

    override fun observeUnreadCount(): Flow<Int> {
        val currentUserId = auth.currentUser?.uid ?: ""
        // Primary source: sum of unreadCount across all chat_rooms.
        // This is set from the backend API (chat/rooms) and cleared by markMessagesAsReed().
        // Secondary source: count of chat_messages where read=0 and senderId != me.
        // We take the MAX so that:
        //   - Incoming messages saved globally (via ChatMessageEventHandler) bump the per-room
        //     unreadCount via incrementUnreadCount(), which propagates to badge immediately.
        //   - After marking read, both sources drop to 0.
        return kotlinx.coroutines.flow.combine(
            chatDao.observeUnreadMessagesCount(currentUserId),
            chatDao.observeRoomsUnreadCount()
        ) { msgCount, roomCount ->
            maxOf(msgCount, roomCount)
        }
    }
}

@Serializable
data class ChatMessageNetworkDto(
    val messageId: String = "",
    val roomId: String = "",
    val senderId: String = "",
    val senderUid: String = "",
    val text: String = "",
    val attachmentUrl: String? = null,
    val timestamp: Long = 0L,
    val read: Boolean = false
)

@Serializable
data class SendMessageRequest(
    val messageId: String,
    val text: String,
    val attachmentUrl: String?
)

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
    val messageId: String = "",
    val roomId: String = "",
    val senderUid: String = "",
    val text: String? = "",
    val attachmentUrl: String? = null,
    val timestamp: Long = 0L,
    val read: Boolean = false
)

@Serializable
data class MessageSentAck(
    val localId: String,
    val messageId: String
)