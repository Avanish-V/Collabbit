package com.iota.campusX.Utils

import kotlinx.serialization.Serializable
import java.time.Instant


sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}


@Serializable
data class ErrorResponse(
    val status: Int,
    val message: String,
    val timestamp: String
)

fun Throwable.extractReason(default: String = "An unexpected error occurred"): String {
    return this.message?.let { msg ->
        if (msg.contains("\"message\"") || msg.contains("message")) {
            try {
                val regex = "\"message\"\\s*:\\s*\"([^\"]*)\"".toRegex()
                val match = regex.find(msg)
                if (match != null) {
                    match.groupValues[1]
                } else {
                    msg.substringAfter("message\": \"").substringBefore("\"")
                        .takeIf { it != msg } ?: msg
                }
            } catch (e: Exception) {
                msg
            }
        } else {
            msg
        }
    } ?: default
}
