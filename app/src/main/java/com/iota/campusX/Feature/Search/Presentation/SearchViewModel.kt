package com.iota.campusX.Feature.Search.Presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Search.Data.SearchResponse
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class SearchViewModel(private val repo: SearchRepository) : ViewModel() {


    private val _searchResults = MutableStateFlow<PagingData<SearchResponse>>(PagingData.empty())
    val searchResults: StateFlow<PagingData<SearchResponse>> = _searchResults.asStateFlow()

    private val _postSearchResults = MutableStateFlow<PagingData<GetPostDTO>>(PagingData.empty())
    val postSearchResults: StateFlow<PagingData<GetPostDTO>> = _postSearchResults.asStateFlow()

    val searchQuery = MutableStateFlow("")

    var selectedTab = MutableStateFlow(0) // 0 = Users, 1 = Posts
        private set

    init {
        when(selectedTab.value){
            0->{
                searchForUser(query = searchQuery.value)
            }
            1->{
                searchForPost(query = searchQuery.value)
            }
        }
    }

    fun onSearchButtonClick(){
        when(selectedTab.value){
            0->{
                searchForUser(query = searchQuery.value)
            }
            1->{
                searchForPost(query = searchQuery.value)
            }
        }
    }

    /**
     * Called from UI layer as user types
     */
    fun onSearchQuery(input: String) {
        searchQuery.value = input
    }

    fun onTabChanged(index: Int) {
        selectedTab.value = index
    }


    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun searchForUser(query: String){

        try {
            viewModelScope.launch {
                searchQuery
                    .filter { it.isNotEmpty() && it.length > 3 }
                    .distinctUntilChanged()
                    .debounce(1000) // 300ms wait after user stops typing
                    .map { it.trim() }
                    .flatMapLatest { query ->
                        if (query.isBlank()){
                            _searchResults.value = PagingData.empty()
                        }
                        repo.userSearch(query)
                    }
                    .cachedIn(viewModelScope) // ✅ cache once at the end
                    .collectLatest { pagingData ->
                        _searchResults.value = pagingData
                    }
            }
        } catch (e: CancellationException) {
            // ❗️Absolutely required to prevent coroutine crash
        } catch (e: Exception) {

        }

    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    private fun searchForPost(query: String){

        try {
            viewModelScope.launch {
                searchQuery
                    .filter { it.isNotEmpty() && it.length > 3 }
                    .distinctUntilChanged()
                    .debounce(1000) // 300ms wait after user stops typing
                    .map { it.trim() }
                    .flatMapLatest { query ->
                        if (query.isBlank()){
                            _postSearchResults.value = PagingData.empty()
                        }
                        repo.postSearch(query)
                        // returns Flow<PagingData<User>>
                    }
                    .cachedIn(viewModelScope) // ✅ cache once at the end
                    .collectLatest { pagingData ->
                        _postSearchResults.value = pagingData
                    }
            }
        } catch (e: CancellationException) {
            // ❗️Absolutely required to prevent coroutine crash
        } catch (e: Exception) {

        }

    }

    fun clearResults(){
        _searchResults.value = PagingData.empty()
    }

}
