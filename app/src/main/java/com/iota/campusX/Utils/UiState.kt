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