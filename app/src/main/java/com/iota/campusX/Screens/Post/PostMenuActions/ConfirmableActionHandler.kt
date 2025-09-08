package com.iota.campusX.Screens.Post.PostMenuActions

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ReportContent
import com.iota.campusX.ui.UIComponents.ReportSuccess
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionHandler(
    content: FeedContent,
    action: MenuAction?,
    viewModel: PostMenuViewModel,
    onDismiss: () -> Unit,
    snackBar : SnackbarHostState,
    postMenuState: PostMenuState = koinInject(),
    reportViewModel: ReportViewModel = koinInject()
) {
    when (action) {
        MenuAction.Delete -> {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Delete Post") },
                text = { Text("Are you sure you want to delete this post?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onActionSelected(action, content)
                        onDismiss()
                        postMenuState.close()
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                },
                shape = MaterialTheme.shapes.small
            )
        }
        MenuAction.Edit -> {

            var text by remember { mutableStateOf("") }

            LaunchedEffect(Unit) {
                text = content.text
            }

            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    when(content.id){
                       is ContentId.Post -> Text("Edit Post")
                       is ContentId.Reply -> Text("Edit Comment")
                    }
                },
                text = {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Edit your post") },
                        maxLines = 6
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onActionSelected(action, FeedContent(content.id, text, isOwner = content.isOwner, type = content.type))
                        onDismiss()
                        postMenuState.close()
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                },
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
        MenuAction.Report -> {

            ModalBottomSheet(onDismissRequest = onDismiss) {
                ReportContent {
                    viewModel.onActionSelected(action, content,reportReason = it)
                    onDismiss()
                }
            }

            val state by  viewModel.actionResult.collectAsState()

            when(state){
                is UiState.Success<*> ->{
                    ModalBottomSheet(onDismissRequest = onDismiss) {
                        Log.d("PostMenuViewModel", "onActionSelected: Success")
                        ReportSuccess()

                    }
                }
                else -> {}
            }

        }

        null -> {}
    }
}

