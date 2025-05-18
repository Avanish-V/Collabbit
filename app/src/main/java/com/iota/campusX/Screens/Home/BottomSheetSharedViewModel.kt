package com.iota.campusX.Screens.Home

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetSharedViewModel: ViewModel(){

    private val _bottomSheetState : MutableStateFlow<PassBottomSheetData> = MutableStateFlow(PassBottomSheetData())
    val bottomSheetState: StateFlow<PassBottomSheetData> = _bottomSheetState.asStateFlow()

    fun setBottomSheetState(
        state: Boolean,
        isCurrentUser: Boolean = false,
        postId: String,
        campusId: String
    ){
       _bottomSheetState.value = PassBottomSheetData(
           isBottomSheet = state,
           isCurrentUser = isCurrentUser,
           postId = postId,
           campusId = campusId
       )

    }

    fun hideBottomSheet(hide: Boolean){
        _bottomSheetState.value = PassBottomSheetData(
            isBottomSheet = hide
        )
    }

}

data class PassBottomSheetData(
    var isBottomSheet: Boolean = false,
    val postId: String = "",
    val isCurrentUser: Boolean = false,
    val campusId: String? = null
)