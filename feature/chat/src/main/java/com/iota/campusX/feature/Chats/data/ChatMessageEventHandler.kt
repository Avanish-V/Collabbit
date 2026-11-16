package com.iota.campusX.Feature.Chats.data

import android.util.Log
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.core.network.websocket.handler.SocketEventHandler
import com.iota.campusX.core.network.websocket.model.SocketEvent
import com.iota.campusX.core.network.websocket.model.SocketEventType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class ChatMessageEventHandler(
    private val chatRepositoryLazy: Lazy<ChatRepository>,
    private val json: Json
) : SocketEventHandler {

    private val chatRepository get() = chatRepositoryLazy.value

    override val eventType = SocketEventType.CHAT_MESSAGE

    override suspend fun handle(event: SocketEvent) {
        try {
            val payload = json.decodeFromJsonElement<ChatMessageCreatedPayload>(event.payload)
            chatRepository.fetchAndSaveIncomingMessage(
                messageId = payload.messageId,
                roomId = payload.roomId
            )
        } catch (e: Exception) {
            Log.e("ChatMessageEventHandler", "Failed to handle CHAT_MESSAGE event", e)
        }
    }
}
