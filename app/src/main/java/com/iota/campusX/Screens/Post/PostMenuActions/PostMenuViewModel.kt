package com.iota.campusX.Screens.Post.PostMenuActions

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ReportReason
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PostMenuViewModel(
    private val repository: PostMenuRepository,
) : ViewModel() {

    private val _menuOptions = MutableStateFlow<List<MenuAction>>(emptyList())
    val menuOptions: StateFlow<List<MenuAction>> = _menuOptions

    private var _actionResult =  MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val actionResult : StateFlow<UiState<Unit>> = _actionResult.asStateFlow()

    fun loadMenu(content: FeedContent) {
        viewModelScope.launch {
            val options = repository.getMenuOptions(content)
            _menuOptions.value = options
        }
    }


    fun onActionSelected(action: MenuAction,content: FeedContent,reportReason: ReportReason? = null) {
        viewModelScope.launch {

            _actionResult.emit(UiState.Loading)

            val result = repository.executeAction(action,content,reportReason)

           _actionResult.value =  result.fold(
                onSuccess = {

                  UiState.Success(Unit)
                },
                onFailure = {
                    UiState.Error(it.message.toString())
                }
            )

        }
    }
}

sealed class ActionResult {
    data object Idle : ActionResult()
    data object Loading : ActionResult()
    data class Success(val message: String) : ActionResult()
    data class Error(val error: Throwable) : ActionResult()
}


