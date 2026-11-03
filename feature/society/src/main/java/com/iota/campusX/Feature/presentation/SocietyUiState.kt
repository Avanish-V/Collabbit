package com.iota.campusX.Feature.Society.presentation

sealed class SocietyUiState {
    object Idle : SocietyUiState()
    object Loading : SocietyUiState()
    object Success : SocietyUiState()
    data class Error(val message: String) : SocietyUiState()
}

sealed class SocietyUiEvent {
    object JoinSuccess : SocietyUiEvent()
    data class Error(val message: String) : SocietyUiEvent()
}
