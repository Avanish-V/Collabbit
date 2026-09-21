package com.iota.campusX.Feature.ExploreSwipe.data.remote.repository

import android.util.Log
import com.iota.campusX.Feature.Collab.data.remote.response.CollabPageResponse
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeActionRequest
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeActionResult
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeCardType
import com.iota.campusX.Feature.ExploreSwipe.data.model.SwipeMatchFeedResponse
import com.iota.campusX.Feature.ExploreSwipe.domain.model.SwipeItem
import com.iota.campusX.Feature.ExploreSwipe.domain.repository.SwipeMatchRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SwipeMatchRepoImpl(
    private val httpClient: HttpClient
) : SwipeMatchRepository {

    private val TAG = "SwipeMatchRepoImpl"

    override suspend fun getSwipeFeed(page: Int, size: Int): Result<List<SwipeItem>> = withContext(Dispatchers.IO) {
        runCatching {
            val response: HttpResponse = httpClient.get("explore/swipe-match/cards") {
                parameter("page", page)
                parameter("size", size)
            }

            if (response.status.isSuccess()) {
                val feedResponse = response.body<SwipeMatchFeedResponse>()
                val items = feedResponse.cards.mapNotNull { cardDto ->
                    when (cardDto.type) {
                        SwipeCardType.COLLABORATION -> {
                            cardDto.collab?.let {
                                SwipeItem.CollabItem(
                                    id = cardDto.id,
                                    matchPercentage = cardDto.matchPercentage,
                                    collab = it
                                )
                            }
                        }
                        SwipeCardType.PROFILE -> {
                            cardDto.profile?.let { profile ->
                                val completionScore = com.iota.campusX.Feature.UserProfile.utils.ProfileCompletionCalculator.calculate(profile).score
                                if (completionScore > 60) {
                                    SwipeItem.ProfileItem(
                                        id = cardDto.id,
                                        matchPercentage = cardDto.matchPercentage,
                                        profile = profile
                                    )
                                } else {
                                    null
                                }
                            }
                        }
                    }
                }
                items
            } else {
                // Fallback: fetch directly from collabs if dedicated endpoint is unavailable
                Log.w(TAG, "Swipe feed endpoint returned ${response.status}, attempting fallback to collabs")
                val collabFallbackResponse = httpClient.get("collabs") {
                    parameter("type", "All")
                    parameter("page", page)
                    parameter("size", size)
                }
                if (collabFallbackResponse.status.isSuccess()) {
                    val collabPage = collabFallbackResponse.body<CollabPageResponse>()
                    collabPage.content.mapIndexed { index, collab ->
                        val score = 80 + ((collab.id.hashCode() + index) % 18).let { if (it < 0) -it else it }
                        SwipeItem.CollabItem(
                            id = collab.id,
                            matchPercentage = minOf(score, 99),
                            collab = collab
                        )
                    }
                } else {
                    val errorText = response.bodyAsText()
                    throw Exception("Failed to fetch swipe feed: ${response.status} - $errorText")
                }
            }
        }.onFailure {
            Log.e(TAG, "Error fetching swipe feed: ${it.message}", it)
        }
    }

    override suspend fun swipeAction(
        cardId: String,
        type: SwipeCardType,
        action: String,
        note: String?
    ): Result<SwipeActionResult> {
        return runCatching {
            val response: HttpResponse = httpClient.post("explore/swipe-match/action") {
                contentType(ContentType.Application.Json)
                setBody(
                    SwipeActionRequest(
                        cardId = cardId,
                        type = type,
                        action = action,
                        note = note
                    )
                )
            }

            if (response.status.isSuccess()) {
                response.body<SwipeActionResult>()
            } else {
                // Fallback to direct actions if needed
                when (type) {
                    SwipeCardType.COLLABORATION -> {
                        if (action.equals("CONNECT", ignoreCase = true) || action.equals("SUPER_LIKE", ignoreCase = true)) {
                            httpClient.post("collabs/$cardId/connect") {
                                contentType(ContentType.Application.Json)
                            }
                        }
                        SwipeActionResult(true, "Collab action completed")
                    }
                    SwipeCardType.PROFILE -> {
                        if (action.equals("CONNECT", ignoreCase = true) || action.equals("SUPER_LIKE", ignoreCase = true)) {
                            httpClient.post("connections/request/$cardId") {
                                contentType(ContentType.Application.Json)
                                if (!note.isNullOrBlank()) {
                                    setBody(mapOf("note" to note))
                                }
                            }
                        }
                        SwipeActionResult(true, "Profile action completed")
                    }
                }
            }
        }.onFailure {
            Log.e(TAG, "Error performing swipe action: ${it.message}", it)
        }
    }
}
