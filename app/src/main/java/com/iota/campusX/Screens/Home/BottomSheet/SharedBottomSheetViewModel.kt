package com.iota.campusX.Screens.Home.BottomSheet

import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.Post.domain.Models.FeedMode
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

    private val _isLoading : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()


    fun setBottomSheetState(
        state: Boolean = false,
        isCurrentUser: Boolean = false,
        campusId: String? = null,
        feedMode: FeedMode = FeedMode.GLOBAL,
        content: Content,
        contentType: ContentType,
        sheetType: SheetType
    ){
       _bottomSheetState.value = PassBottomSheetData(
           isBottomSheet = state,
           isCurrentUser = isCurrentUser,
           campusId = campusId,
           sheetType = sheetType,
           content = content,
           contentType = contentType,
           feedMode = feedMode

       )

    }

    fun updateBottomSheetState(sheetType: SheetType) {
        _bottomSheetState.value = _bottomSheetState.value.copy(
            sheetType = sheetType
        )
    }

    fun dismissBottomSheet() {
        _bottomSheetState.value = _bottomSheetState.value.copy(
            isBottomSheet = false
        )
    }

    fun isLoading(isLoading: Boolean){
        _isLoading.value = isLoading
    }


    fun setAlertDialog(request: String){
        _alertDialog.value = request
    }

    fun AlertDialogText(): AlertDialogData? {

        return when {
            bottomSheetState.value.contentType == ContentType.POST -> {
                AlertDialogData(
                    action = "DELETE_POST",
                    titleText = "Delete Post",
                    positiveButtonText = "Delete",
                    descriptionText = "Are you sure you want to delete this post?",
                )
            }

            bottomSheetState.value.contentType == ContentType.POST -> {
                AlertDialogData(
                    action = "DELETE_REPLY",
                    titleText = "Delete Reply",
                    positiveButtonText = "Delete",
                    descriptionText = "Are you sure you want to delete this reply?",
                )
            }

            else -> null // Or handle differently if you expect a value always
        }
    }




}

data class PassBottomSheetData(
    var isBottomSheet: Boolean = false,
    val content: Content = Content(),
    val contentType: ContentType = ContentType.NONE,
    val isCurrentUser: Boolean = false,
    val feedMode: FeedMode = FeedMode.GLOBAL,
    val campusId: String? = null,
    val sheetType: SheetType = SheetType.MENU_LIST,
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

enum class ContentType {
    POST,
    REPLY,
    CONSENT,
    NONE
}

enum class SheetType {
    MENU_LIST,
    CONSENT,
    EDIT_POST,
    EDIT_REPLY,
    REPORT
}


data class Content(
    val postId: String = "",
    val text: String = "",
    val replyId: String = ""
)


