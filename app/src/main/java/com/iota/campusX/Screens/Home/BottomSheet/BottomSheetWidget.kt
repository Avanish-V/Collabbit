package com.iota.campusX.Screens.Home.BottomSheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.secondary
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDotOptionBottomSheet(
    isBottomSheet: Boolean,
    bottomSheetViewModel: BottomSheetSharedViewModel,
    postFeedViewModel: PostFeedViewModel,
    onDismiss: () -> Unit,
    isCurrentUser: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onHideBottomSheet: (Boolean) -> Unit
) {

    val replyViewModel = koinInject<ReplyViewModel>()


    bottomSheetViewModel.modificationRequest.collectAsState().value
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value

    val editPostState = postFeedViewModel.editPostState
    val editReplyState = replyViewModel.editReplyState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    LocalContext.current
    LocalSoftwareKeyboardController.current

    LaunchedEffect(bottomSheetData) {
        replyText = bottomSheetData.content.text
    }

    LaunchedEffect(bottomSheetData.sheetType) {
        if (bottomSheetData.sheetType == SheetType.EDIT_POST || bottomSheetData.sheetType == SheetType.EDIT_REPLY) {
            focusRequester.requestFocus()
        }
    }

    LaunchedEffect(editPostState) {
        when (editPostState) {
            is UiState.Loading -> {
                isLoading = true
            }

            is UiState.Success -> {
                isLoading = false
                replyText = ""
                bottomSheetViewModel.dismissBottomSheet()
            }

            is UiState.Error -> {
                isLoading = false
            }

            else -> {
                isLoading = false
            }
        }
    }

    LaunchedEffect(editReplyState.value) {
        when (editReplyState.value) {
            is UiState.Loading -> {
                isLoading = true
            }

            is UiState.Success -> {
                isLoading = false
                replyText = ""
                bottomSheetViewModel.dismissBottomSheet()
            }

            is UiState.Error,
            UiState.Idle -> {
                isLoading = false
            }
        }
    }




    if (isBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
        ) {

            when (bottomSheetData.sheetType) {

                SheetType.MENU_LIST -> {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {

                        if (isCurrentUser) {

                            MenuItem(
                                icon = R.drawable.edit,
                                text = "Edit",
                                onClick = {

                                    if (bottomSheetData.contentType == ContentType.POST) {
                                        bottomSheetViewModel.updateBottomSheetState(sheetType = SheetType.EDIT_POST)
                                    }

                                    if (bottomSheetData.contentType == ContentType.REPLY) {
                                        bottomSheetViewModel.updateBottomSheetState(sheetType = SheetType.EDIT_REPLY)

                                    }

                                }
                            )

                            MenuItem(
                                icon = R.drawable.trash,
                                text = "Delete",
                                tint = Color.Red,
                                onClick = {
                                    onDeleteClick.invoke()
                                }
                            )

                        }

                        HorizontalDivider(color = White400)

                        MenuItem(
                            icon = R.drawable.warning_2,
                            text = "Report",
                            tint = Color.Red,
                            onClick = {
                                bottomSheetViewModel.updateBottomSheetState(sheetType = SheetType.REPORT)
                            }
                        )
                    }
                }

                SheetType.EDIT_POST -> {
                    EditTextSection(
                        title = "Edit Post",
                        replyText = replyText,
                        isLoading = isLoading,
                        onTextChange = { replyText = it },
                        onSendClick = {

                            if (replyText.isEmpty()) return@EditTextSection

                            scope.launch {
                                postFeedViewModel.editPost(
                                    postId = bottomSheetData.content.postId,
                                    isCampus = bottomSheetData.campusId.isNullOrEmpty(),
                                    newText = replyText
                                )
                            }
                        },
                        focusRequester = focusRequester
                    )
                }

                SheetType.EDIT_REPLY -> {
                    EditTextSection(
                        title = "Edit Reply",
                        replyText = replyText,
                        isLoading = isLoading,
                        onTextChange = { replyText = it },
                        onSendClick = {
                            if (replyText.isNotEmpty()) {
                                scope.launch {
                                    replyViewModel.editReply(
                                        postId = bottomSheetData.content.postId,
                                        replyId = bottomSheetData.content.replyId,
                                        campusId = bottomSheetData.campusId.toString(),
                                        content = replyText
                                    )
                                }
                            }
                        },
                        focusRequester = focusRequester
                    )
                }

                SheetType.CONSENT -> { /* Not yet implemented */
                }

                SheetType.REPORT -> {

                    ReportContent()

                }

            }
        }
    }
}

@Composable
fun ReportContent(modifier: Modifier = Modifier) {

    Column(modifier = Modifier.padding(12.dp)) {

        LazyColumn {
            items(reportReasons) {
                ReportSingleItem(it)
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 20.dp),
            onClick = {},
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Submit")
        }


    }


}

@Composable
fun ReportSingleItem(reportReason: ReportReason) {

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = false,
            onClick = {}
        )
        Column {
            Text(reportReason.description.toString())
            HorizontalDivider()
        }

    }

}


data class ReportReason(
    val type: ReportType,
    val title: String,
    val description: String
)

val reportReasons = listOf(
    ReportReason(
        ReportType.SPAM,
        "Spam or misleading",
        "This content is unwanted promotional or misleading."
    ),
    ReportReason(
        ReportType.HATE_SPEECH,
        "Hate speech or abuse",
        "Offensive or threatening content."
    ),
    ReportReason(
        ReportType.HARASSMENT,
        "Harassment or bullying",
        "Targeted insults, threats, or unwanted contact."
    ),
    ReportReason(
        ReportType.VIOLENCE,
        "Violence or harmful acts",
        "Promotes violence or self-harm."
    ),
    ReportReason(ReportType.SEXUAL_CONTENT, "Sexual content", "Inappropriate or explicit content."),
    ReportReason(
        ReportType.MISINFORMATION,
        "False information",
        "Contains false or misleading facts."
    ),
    ReportReason(ReportType.OTHER, "Other", "Does not fall under a specific category.")
)


enum class ReportType {
    SPAM,
    HATE_SPEECH,
    HARASSMENT,
    VIOLENCE,
    SEXUAL_CONTENT,
    MISINFORMATION,
    OTHER
}


@Composable
fun MenuItem(
    icon: Int,
    text: String,
    tint: Color = Color.Unspecified,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth()
            .padding(start = 10.dp)
            .background(color = secondary, shape = RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = if (tint == Color.Unspecified) LocalContentColor.current else tint,
            modifier = Modifier.size(22.dp)
        )
        Text(text)
    }
}

@Composable
fun EditTextSection(
    title: String,
    replyText: String,
    isLoading: Boolean,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    focusRequester: FocusRequester
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(title, color = primary, fontWeight = FontWeight.Bold)
        }

        HorizontalDivider(color = Black500)

        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .focusRequester(focusRequester),
            value = replyText,
            onValueChange = onTextChange,
            placeholder = { Text("Write your comment...") },
            trailingIcon = {
                SendButton(isLoading = isLoading, onClick = onSendClick)
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = White900,
                unfocusedContainerColor = White900,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = primary
            )
        )
    }
}

@Composable
fun SendButton(isLoading: Boolean, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                trackColor = secondary,
                color = primary
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.send_2),
                contentDescription = "Send",
                tint = primary
            )
        }
    }
}

