package com.iota.campusX.Screens.Home

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BottomSheetSharedViewModel: ViewModel(){

    private val _bottomSheetState : MutableStateFlow<PassBottomSheetData> = MutableStateFlow(PassBottomSheetData())
    val bottomSheetState: StateFlow<PassBottomSheetData> = _bottomSheetState.asStateFlow()

    private val _modificationRequest : MutableStateFlow<String> = MutableStateFlow("")
    val modificationRequest: StateFlow<String> = _modificationRequest.asStateFlow()

    private val _alertDialog : MutableStateFlow<String> = MutableStateFlow("")
    val alertDialog: StateFlow<String> = _alertDialog.asStateFlow()

    fun setBottomSheetState(
        state: Boolean,
        type: String,
        isCurrentUser: Boolean = false,
        postId: String,
        postText: String? = "",
        replyId: String ?= null,
        replyText : String?= "",
        campusId: String
    ){
       _bottomSheetState.value = PassBottomSheetData(
           isBottomSheet = state,
           type = type,
           isCurrentUser = isCurrentUser,
           postId = postId,
           postText = postText.toString(),
           campusId = campusId,
           replyId = replyId ?: "",
           replyText = replyText
       )

    }

    fun hideBottomSheet(hide: Boolean){
        _bottomSheetState.value = PassBottomSheetData(
            isBottomSheet = hide
        )
    }

    fun setModificationRequest(request: String){
        _modificationRequest.value = request
    }

    fun setAlertDialog(request: String){
        _alertDialog.value = request
    }

    fun AlertDialogText(): AlertDialogData? {

        return when {
            modificationRequest.value == "DELETE" && bottomSheetState.value.type == "POST" -> {
                AlertDialogData(
                    action = "DELETE_POST",
                    titleText = "Delete Post",
                    positiveButtonText = "Delete",
                    descriptionText = "Are you sure you want to delete this post?",
                )
            }

            modificationRequest.value == "DELETE" && bottomSheetState.value.type == "REPLY" -> {
                AlertDialogData(
                    action = "DELETE_REPLY",
                    titleText = "Delete Reply",
                    positiveButtonText = "Delete",
                    descriptionText = "Are you sure you want to delete this reply?",
                )
            }

            modificationRequest.value == "EDIT" && bottomSheetState.value.type == "REPLY" -> {
                AlertDialogData(
                    action = "EDIT_REPLY",
                    titleText = "Discard Changes",
                    positiveButtonText = "Edit",
                    descriptionText = "Are you sure you want to discard changes?",
                )
            }

            else -> null // Or handle differently if you expect a value always
        }
    }




}

data class PassBottomSheetData(
    var isBottomSheet: Boolean = false,
    val postId: String = "",
    val type: String = "",
    val postText: String = "",
    val replyId: String ?= "",
    val replyText: String ?= "",
    val isCurrentUser: Boolean = false,
    val campusId: String? = null
)

data class AlertDialogData(
    var action: String = "",
    val titleText: String = "",
    val descriptionText: String = "",
    val positiveButtonText: String = "",
)

data class Modification(
    val type: String = "",
    val event: String = ""

)
