package com.iota.campusX.Feature.ExploreSwipe.domain.repository

import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeActionResult
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeCardType
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem

interface SwipeMatchRepository {
    suspend fun getSwipeFeed(page: Int = 0, size: Int = 50): Result<List<SwipeItem>>
    suspend fun swipeAction(cardId: String, type: SwipeCardType, action: String, note: String? = null): Result<SwipeActionResult>
}
