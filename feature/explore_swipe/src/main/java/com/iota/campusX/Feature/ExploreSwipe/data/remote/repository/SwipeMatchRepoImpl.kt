package com.iota.campusX.Feature.ExploreSwipe.data.remote.repository

import android.util.Log
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
                feedResponse.cards.mapNotNull { cardDto ->
                    try {
                        cardDto.profile?.let { profile ->
                            SwipeItem(
                                id = cardDto.id,
                                matchPercentage = cardDto.matchPercentage,
                                profile = profile
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing card ${cardDto.id}: ${e.message}")
                        null
                    }
                }
            } else {
                emptyList()
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
            if (action.equals("CONNECT", ignoreCase = true) || action.equals("SUPER_LIKE", ignoreCase = true)) {
                try {
                    val userResponse = httpClient.get("users/me")
                    if (userResponse.status.isSuccess()) {
                        val profile = userResponse.body<com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse>()
                        val score = com.iota.campusX.Feature.UserProfile.utils.ProfileScoreCalculator.calculateScore(profile)
                        if (score < 80) {
                            throw Exception("First complete your profile (at least 80%) to send notes & connect.")
                        }
                    }
                } catch (e: Exception) {
                    if (e.message?.contains("at least 80%") == true) {
                        throw e
                    }
                }
            }

            val response: HttpResponse = httpClient.post("explore/swipe-match/action") {
                contentType(ContentType.Application.Json)
                setBody(
                    SwipeActionRequest(
                        cardId = cardId,
                        type = SwipeCardType.PROFILE,
                        action = action,
                        note = note
                    )
                )
            }

            if (response.status.isSuccess()) {
                response.body<SwipeActionResult>()
            } else {
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
        }.onFailure {
            Log.e(TAG, "Error performing swipe action: ${it.message}", it)
        }
    }
}
