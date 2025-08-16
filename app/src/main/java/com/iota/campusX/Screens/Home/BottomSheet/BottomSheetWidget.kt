package com.iota.campusX.Screens.Home.BottomSheet

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.R
import com.iota.campusX.Utils.LoadingUI
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.AnimatedStatus
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.PrimaryButton
import com.iota.campusX.ui.theme.LightTheme_Blue
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDotOptionBottomSheet(
    isBottomSheet: Boolean,
    bottomSheetViewModel: SharedBottomSheetViewModel,
    postFeedViewModel: PostFeedViewModel,
    replyViewModel: ReplyViewModel,
    onDismiss: () -> Unit,
    isCurrentUser: Boolean,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit,
    onHideBottomSheet: (Boolean) -> Unit
) {


    val reportViewModel = koinInject<ReportViewModel>()
    bottomSheetViewModel.modificationRequest.collectAsState().value
    val bottomSheetData = bottomSheetViewModel.bottomSheetState.collectAsState().value

    val editPostState = postFeedViewModel.editPostState
    val editReplyState = replyViewModel.editReplyState.collectAsState()
    val reportPostState = reportViewModel.submitReportState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    var replyText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }


    val scope = rememberCoroutineScope()
    LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
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
                snackbarHostState.showSnackbar(editPostState.message)
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

            is UiState.Error->{
                isLoading = false
                Log.d("ERROR", "PostDotOptionBottomSheet: ${editReplyState.value}")
            }
            else -> {}
        }
    }



    if (isBottomSheet) {
        ModalBottomSheet(
            modifier = Modifier.padding(horizontal = 8.dp),
            onDismissRequest = { onDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.background,
        ) {

            when (bottomSheetData.sheetType) {

                SheetType.MENU_LIST -> {

                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp, horizontal = 16.dp),
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

                            Divider()

                            MenuItem(
                                icon = R.drawable.trash,
                                text = "Delete",
                                tint = Color.Red,
                                onClick = {
                                    onDeleteClick.invoke()
                                }
                            )

                            Divider()

                        }


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
                        title = "@Edit Post",
                        replyText = replyText,
                        isLoading = isLoading,
                        onTextChange = { replyText = it },
                        onSendClick = {

                            if (replyText.isEmpty()) return@EditTextSection

                            scope.launch {
                                postFeedViewModel.editPost(
                                    postId = bottomSheetData.content.postId,
                                    feedMode = bottomSheetData.feedMode,
                                    campusId = bottomSheetData.campusId,
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
                                        campusId = bottomSheetData.campusId,
                                        feedMode = bottomSheetData.feedMode,
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

                    when(reportPostState.value){
                        is UiState.Idle->{

                            ReportContent(
                                onSubmitClick = {
                                    reportViewModel.submitReport(
                                        reportReason = it,
                                        postId = bottomSheetData.content.postId,
                                        campusId = bottomSheetData.campusId
                                    )
                                }
                            )

                        }
                        is UiState.Loading->{

                            LoadingUI(isLoading = true)
                        }
                        is UiState.Success->{

                            Column (modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally){

                                Column (horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)){
                                    AnimatedStatus(
                                        modifier = Modifier.size(100  .dp),
                                        file = R.raw.sent_email,
                                        description = "Submitted"
                                    )
                                    Text("Report Submitted", style = MaterialTheme.typography.headlineLarge)

                                    Text("Thank you for helping keep our community safe", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)){

                                    Text("What happens next?", style = MaterialTheme.typography.headlineLarge)

                                    Text(
                                        text = "● Our moderation team will review your report within 24-48 hours.\n" +
                                            "● We'll take appropriate action based on our community guidelines.\n" +
                                            "● You may receive an update on the outcome via notification.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )

                                    Box(modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center){
                                        Text(text = "Report ID: RPT-2024-071-8847", modifier = Modifier.padding(12.dp))
                                    }

                                    Box(modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)).border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center){
                                        Text(text = "Reports are confidential. The user won't know you reported their content unless action is taken.", modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
                                    }

                                }




                            }


                        }
                        is UiState.Error -> {

                            ReportContent(
                                onSubmitClick = {
                                    reportViewModel.submitReport(
                                        reportReason = it,
                                        postId = bottomSheetData.content.postId,
                                        campusId = bottomSheetData.campusId
                                    )
                                }
                            )

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReportContent(
    onSubmitClick:(ReportReason)-> Unit
) {

    var reportReason by remember { mutableStateOf(ReportReason())}

    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        LazyColumn (verticalArrangement = Arrangement.spacedBy(12.dp)){
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    Text("Report", style = MaterialTheme.typography.headlineLarge)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)){
                        Divider()
                        Text(text = "CampusX protects your identity",style = MaterialTheme.typography.headlineMedium)
                        Text(text = "When reporting a post, your identity remains confidential. Your concerns are addressed without revealing your name or information to ensure anonymity" +
                                "and maintain privacy throughout the process.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider()

                    }
                }
            }
            items(reportReasons) {
                ReportSingleItem(
                    reportReason = it,
                    selectedReason = reportReason,
                    onReasonSelect = {
                        reportReason = it
                    }
                )
            }
        }


        PrimaryButton (
            buttonText = "Submit",
            onClick = {
                onSubmitClick.invoke(reportReason)
            }
        )

    }


}

@Composable
fun ReportSingleItem(
    reportReason: ReportReason,
    selectedReason: ReportReason,
    onReasonSelect:(ReportReason)-> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = if (selectedReason == reportReason) true else false,
            onClick = {
                isSelected = !isSelected
                onReasonSelect(reportReason)
            }
        )
        Text(reportReason.description.toString())
    }

}


data class ReportReason(
    val type: ReportType = ReportType.NONE,
    val title: String = "",
    val description: String = ""
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
    NONE,
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
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
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
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

        }

        Divider()

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
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTrailingIconColor = LightTheme_Blue
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
                trackColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                modifier = Modifier.size(22.dp),
                painter = painterResource(R.drawable.send_solid),
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

