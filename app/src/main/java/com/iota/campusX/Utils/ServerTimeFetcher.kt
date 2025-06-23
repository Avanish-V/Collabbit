package com.iota.campusX.Utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

@RequiresApi(Build.VERSION_CODES.O)
class ServerTimeFetcher(private val httpClient: HttpClient) {

    companion object {
        private const val TAG = "ServerTimeFetcher"
        private const val API_URL =
            "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
        private val ZONE_ID: ZoneId = ZoneId.of("Asia/Kolkata")
    }

    @Serializable
    data class TimeApiResponse(
        @SerialName("dateTime") val dateTime: String
    )

    @Serializable
    data class Response(
        @SerialName("dateTime") val dateTime: Long,
        @SerialName("statusCode") val statusCode: Int,
        @SerialName("error") val error: String? = null
    )

    suspend fun fetchServerTimestamp(): Response {
        return try {
            val httpResponse = httpClient.get(API_URL)
            val body = httpResponse.bodyAsText()

            Log.d(TAG, "Raw response: $body")

            val parsed = Json {
                ignoreUnknownKeys = true
                isLenient = true
            }.decodeFromString(TimeApiResponse.serializer(), body)

            val localDateTime = LocalDateTime.parse(parsed.dateTime)
            val zonedDateTime = localDateTime.atZone(ZONE_ID)
            val epochMillis = zonedDateTime.toInstant().toEpochMilli()

            Response(
                dateTime = epochMillis,
                statusCode = httpResponse.status.value
            )
        } catch (e: DateTimeParseException) {
            Log.e(TAG, "Invalid date format", e)
            Response(
                dateTime = 0L,
                statusCode = HttpStatusCode.InternalServerError.value,
                error = "Invalid date format from server"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching timestamp", e)
            Response(
                dateTime = 0L,
                statusCode = HttpStatusCode.InternalServerError.value,
                error = e.localizedMessage ?: "Unknown error"
            )
        }
    }
}
