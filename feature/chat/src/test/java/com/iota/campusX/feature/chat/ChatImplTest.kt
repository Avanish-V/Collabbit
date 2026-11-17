package com.iota.campusX.feature.chat

import com.iota.campusX.Feature.Chats.data.local.ChatRoomEntity
import com.iota.campusX.Feature.Chats.data.local.LastMessageEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class ChatImplTest {

    @Test
    fun chatRoomEntity_defaultStatusIsAccepted() {
        val room = ChatRoomEntity(
            roomId = "room1",
            receiverId = "user1",
            userName = "User 1",
            userImage = null,
            status = "ACCEPTED",
            lastMessage = LastMessageEntity("Hi", System.currentTimeMillis(), 0, true, true)
        )
        assertEquals("ACCEPTED", room.status)
        assertEquals("room1", room.roomId)
    }

    @Test
    fun chatRoomEntity_pendingStatus() {
        val room = ChatRoomEntity(
            roomId = "room2",
            receiverId = "user2",
            userName = "User 2",
            userImage = null,
            status = "PENDING",
            lastMessage = LastMessageEntity("Hello", System.currentTimeMillis(), 1, false, false)
        )
        assertEquals("PENDING", room.status)
        assertEquals("user2", room.receiverId)
    }
}
