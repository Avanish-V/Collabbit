package com.iota.campusX.Screens.Home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Extension to create DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "setting")

class HomeViewModel(private val context: Context) : ViewModel() {

    private val _switchState: MutableStateFlow<SwitchState> = MutableStateFlow(SwitchState())
    val switchState: StateFlow<SwitchState> = _switchState.asStateFlow()

    private val SWITCH_PREF_KEY = booleanPreferencesKey("switch_state")

    fun saveSwitchState(isChecked: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SWITCH_PREF_KEY] = isChecked
            }
        }
    }

    init {
        viewModelScope.launch {
            _switchState.value = SwitchState(isLoad = true)

            context.dataStore.data
                .map { preferences ->
                    preferences[SWITCH_PREF_KEY] ?: false
                }
                .collect { isChecked ->
                    _switchState.value = SwitchState(isLoad = false, isActive = isChecked)
                }
        }
    }
}

data class SwitchState(
    val isLoad: Boolean = false,
    val isActive: Boolean = false,
    val error: String = ""
)
