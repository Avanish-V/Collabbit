package com.iota.campusX.Screens.Home

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

// Extension for DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "FEED_MODE")
class HomeViewModel(private val context: Context) : ViewModel() {

    private val _mode = MutableStateFlow<UiState<FeedMode>>(UiState.Idle)
    val mode: StateFlow<UiState<FeedMode>> = _mode.asStateFlow()

    companion object {
        private val SWITCH_PREF_KEY = stringPreferencesKey("switch_state")
    }

    init {
        loadSwitchState()
    }

    private fun loadSwitchState() {
        viewModelScope.launch {
            _mode.value = UiState.Loading
            val result = runCatching {
                context.dataStore.data
                    .map { preferences ->
                        val name = preferences[SWITCH_PREF_KEY] ?: FeedMode.GLOBAL.name
                        FeedMode.valueOf(name)
                    }
                    .first()
            }

            _mode.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "Failed to load mode") }
            )
        }
    }

    fun saveSwitchState(mode: FeedMode) {
        viewModelScope.launch {
            context.dataStore.edit { settings ->
                settings[SWITCH_PREF_KEY] = mode.name
            }
            _mode.value = UiState.Success(mode) // Optionally update immediately
        }
    }
}

