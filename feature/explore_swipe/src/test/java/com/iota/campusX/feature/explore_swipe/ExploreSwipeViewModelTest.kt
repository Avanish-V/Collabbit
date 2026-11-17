package com.iota.campusX.feature.explore_swipe

import com.iota.campusX.Feature.Chats.domain.SwipeActionRequest
import com.iota.campusX.Feature.Chats.domain.SwipeActionResponse
import org.junit.Assert.assertEquals
import org.junit.Test

class ExploreSwipeViewModelTest {

    @Test
    fun swipeActionRequest_createsValidModelWithoutCardType() {
        val request = SwipeActionRequest(
            targetUserId = "user1",
            action = "CONNECT",
            roomId = "room1",
            note = "Hello"
        )
        assertEquals("user1", request.targetUserId)
        assertEquals("CONNECT", request.action)
        assertEquals("room1", request.roomId)
        assertEquals("Hello", request.note)
    }

    @Test
    fun swipeActionResponse_createsValidModel() {
        val response = SwipeActionResponse(
            status = "ACCEPTED",
            roomId = "room1"
        )
        assertEquals("ACCEPTED", response.status)
        assertEquals("room1", response.roomId)
    }
}
