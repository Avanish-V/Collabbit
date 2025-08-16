package com.iota.campusX.Feature.Report.presentation

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Screens.Home.BottomSheet.ReportReason
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ReportViewModel (private val reportRepository: ReportRepository): ViewModel() {

    private val _submitReportState =  MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val submitReportState : StateFlow<UiState<Boolean>> = _submitReportState.asStateFlow()

    fun submitReport(reportReason: ReportReason,postId:String,campusId: String?){
        viewModelScope.launch {
            _submitReportState.value = UiState.Loading
            val result = reportRepository.createReportOnPost(
                reportReason = reportReason,
                postId = postId,
                campusId = campusId
            )
            result.fold(
                onSuccess = {
                    _submitReportState.value = UiState.Success(it)
                },
                onFailure = {
                    _submitReportState.value = UiState.Error(it.message.toString())
                }
            )
        }
    }
}