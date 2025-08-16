package com.iota.campusX.Feature.Search.Presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchViewModel(private val repo: SearchRepository) : ViewModel() {


    private val _searchResults = MutableStateFlow<UiState<List<UserSearchDTO>>>(UiState.Idle)
    val searchResults: StateFlow<UiState<List<UserSearchDTO>>> = _searchResults.asStateFlow()

    private val searchQuery = MutableStateFlow("")

    init {
        observeQueryWithDebounce()
    }

    /**
     * Called from UI layer as user types
     */
    fun onSearchQuery(input: String) {
        searchQuery.value = input
    }

    @OptIn(FlowPreview::class)
    private fun observeQueryWithDebounce() {
        try {
            viewModelScope.launch {
                searchQuery
                    .filter { it.isNotEmpty() || it.length < 3 }
                    .distinctUntilChanged()
                    .debounce(1000) // 300ms wait after user stops typing
                    .map { it.trim() }
                    .collectLatest { query ->
                        // Show loading state
                        _searchResults.value = UiState.Loading

                        // Fetch from repository
                        repo.userSearch(query)
                            .stateIn(viewModelScope)
                            .collect { result ->
                            _searchResults.value = result.fold(
                                onSuccess = {
                                    UiState.Success(it)
                                },
                                onFailure = {
                                    UiState.Error(it.message ?: "Something went wrong")
                                }
                            )
                        }
                    }
            }
        } catch (e: CancellationException) {
        // ❗️Absolutely required to prevent coroutine crash
        Log.d("SearchViewModel", "Flow cancelled safely.")
    } catch (e: Exception) {
        _searchResults.value = UiState.Error(e.message ?: "Unexpected error")
    }

    }
}
