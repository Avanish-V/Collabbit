package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocietyOptionsViewModel(
    val repository: SocietyOptionsInterface,
    val societyInterface: SocietyInterface
): ViewModel() {

    private val _menuOptions = MutableStateFlow<List<SocietyMenuOptions>>(emptyList())
    val menuOptions: StateFlow<List<SocietyMenuOptions>> = _menuOptions

    private val _hasSubscribed : MutableStateFlow<UiState<Boolean>> = MutableStateFlow(UiState.Idle)
    val hasSubscribed : StateFlow<UiState<Boolean>> = _hasSubscribed.asStateFlow()

    private val _showMenu : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showMenu : StateFlow<Boolean> = _showMenu.asStateFlow()

    private var _actionResult =  MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val actionResult : StateFlow<UiState<Unit>> = _actionResult.asStateFlow()



    fun loadMenu(content: SocietyData) {
        viewModelScope.launch {
            val options = repository.getMenuOptions(content)
            _menuOptions.value = options
        }
    }

    fun onActionSelected(action: SocietyMenuOptions,content: SocietyData,hasAlreadySubscribe: Boolean? = null) {
        viewModelScope.launch {

            _actionResult.emit(UiState.Loading)

            val result = repository.executeAction(action,content,hasAlreadySubscribe)

            _actionResult.value =  result.fold(
                onSuccess = {
                    showMenu(false)
                    UiState.Success(Unit)
                },
                onFailure = {
                    UiState.Error(it.message.toString())
                }
            )
            delay(10000)
            _actionResult.value = UiState.Idle

        }
    }

    fun showMenu(value: Boolean){
        _showMenu.value = value
    }



    suspend fun hasSubscribed(roomId: String) {

            _hasSubscribed.value = UiState.Loading
            val result = societyInterface.hasSubscribed(roomId)
            _hasSubscribed.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message.toString()) }
            )


    }

}
