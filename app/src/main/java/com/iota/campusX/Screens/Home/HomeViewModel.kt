package com.iota.campusX.Screens.Home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Extension for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "FEED_MODE")

class HomeViewModel(private val context: Context) : ViewModel() {

    private val _switchState = MutableStateFlow(SwitchState())
    val switchState: StateFlow<SwitchState> = _switchState.asStateFlow()

    companion object {
        private val SWITCH_PREF_KEY = intPreferencesKey("switch_state")
    }

    fun saveSwitchState(index: Int) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SWITCH_PREF_KEY] = index
            }
        }
    }

    init {
        viewModelScope.launch {
            _switchState.value = SwitchState(isLoad = true)

            val result = runCatching {
                context.dataStore.data
                    .catch { ex ->
                        throw ex
                    }
                    .map { it[SWITCH_PREF_KEY] ?: 0 }
                    .first()
            }

            _switchState.value = result.fold(
                onSuccess = { SwitchState(isLoad = false, savedIndex = it) },
                onFailure = { SwitchState(isLoad = false, error = it.message ?: "Unknown error") }
            )
        }
    }
}

data class SwitchState(
    val isLoad: Boolean = false,
    val savedIndex: Int = 0,
    val error: String = ""
)
