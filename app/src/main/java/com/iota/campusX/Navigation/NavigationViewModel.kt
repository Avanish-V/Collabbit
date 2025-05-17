package com.iota.campusX.Navigation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NavigationViewModel: ViewModel() {

    private val _isBottomBarVisible: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isBottomBarVisible : StateFlow<Boolean> = _isBottomBarVisible.asStateFlow()


    fun isBottomBarVisible(isVisible: Boolean) {
        _isBottomBarVisible.value = isVisible


    }

}