package com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions

import android.util.Log
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import com.iota.campusX.Screens.Post.PostMenuActions.MenuAction
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuViewModel
import com.iota.campusX.Utils.UiState
import com.iota.campusX.ui.UIComponents.ReportContent
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocietyConfirmationHandler(
    content: SocietyData,
    action: SocietyMenuOptions,
    viewModel: SocietyOptionsViewModel,
    onDismiss: () -> Unit,

) {
    when (action) {
        SocietyMenuOptions.Delete -> {
            Log.d("ConfirmationHandler", "Showing delete confirmation dialog")
            AlertDialog(
                onDismissRequest = onDismiss,
                title = { Text("Delete Society") },
                text = { Text("Are you sure you want to delete this Society?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.onActionSelected(action, content)
                        onDismiss()
                    }) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                },
                shape = MaterialTheme.shapes.small,
                containerColor = MaterialTheme.colorScheme.background
            )
        }
        else  -> {} // nothing selected
    }
}

