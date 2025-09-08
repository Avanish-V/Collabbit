package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SocietyOptionsViewModel(val repository: SocietyOptionsInterface): ViewModel() {

    private val _menuOptions = MutableStateFlow<List<SocietyMenuOptions>>(emptyList())
    val menuOptions: StateFlow<List<SocietyMenuOptions>> = _menuOptions


    fun loadMenu(content: SocietyData) {
        viewModelScope.launch {
            val options = repository.getMenuOptions(content)
            _menuOptions.value = options
        }
    }

    fun onActionSelected(action: SocietyMenuOptions,content: SocietyData) {
        viewModelScope.launch {
            repository.executeAction(action,content)
        }
    }

}