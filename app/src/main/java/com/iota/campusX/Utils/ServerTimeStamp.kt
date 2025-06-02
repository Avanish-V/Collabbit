package com.iota.campusX.Utils

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
class ServerTimeStampViewModel(
    private val httpClient: HttpClient
) : ViewModel() {

    private val _timeStamp = MutableStateFlow<Response?>(null)
    val timeStamp: StateFlow<Response?> = _timeStamp.asStateFlow()

    @Serializable
    data class TimeApiResponse(
        @SerialName("dateTime") val dateTime: String
    )

    data class Response(
        val dateTime: Long,
        val statusCode: Int,
        val error: String? = null
    )

    fun getServerTimestamp() {
        viewModelScope.launch {
            val result = runCatching {
                val response: HttpResponse = httpClient.get(
                    "https://www.timeapi.io/api/Time/current/coordinate?latitude=22.5726&longitude=88.3639"
                )

                val body = response.bodyAsText()
                Log.d("ServerTime", "Raw response: $body")

                val parsed = Json { ignoreUnknownKeys = true }
                    .decodeFromString<TimeApiResponse>(body)

                val localDateTime = LocalDateTime.parse(parsed.dateTime)
                val zoneId = ZoneId.of("Asia/Kolkata")
                val zonedDateTime = localDateTime.atZone(zoneId)
                val epochMillis = zonedDateTime.toInstant().toEpochMilli()

                Response(
                    dateTime = epochMillis,
                    statusCode = response.status.value,
                    error = null
                )
            }

            _timeStamp.value = result.getOrElse { exception ->
                Log.e("ServerTime", "Error: ${exception.message}", exception)
                Response(
                    dateTime = 0L,
                    statusCode = 500,
                    error = exception.localizedMessage
                )
            }
        }
    }
}
