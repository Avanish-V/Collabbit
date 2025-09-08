package com.iota.campusX.Screens.Post.PostMenuActions

import android.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.R
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Utils.UiState
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostMenuSheet(
    viewModel: PostMenuViewModel = koinInject(),
    content: FeedContent,
    onDismiss: () -> Unit,
    snackBarHostState: SnackbarHostState
) {
    val menuOptions by viewModel.menuOptions.collectAsState()
    val actionResult = viewModel.actionResult.collectAsState()
    var pendingAction by remember { mutableStateOf<MenuAction?>(null) }

    LaunchedEffect( content.isOwner) {
        viewModel.loadMenu(content)
    }

    LaunchedEffect(Unit) {

        when (actionResult.value) {

            is UiState.Error ->{
                snackBarHostState.showSnackbar((actionResult.value as UiState.Error).message)
            }
            UiState.Loading -> {
                // Show loading state if needed
            }
            UiState.Idle -> {
                // Handle idle state if needed
            }
            is UiState.Success<*> -> {
                snackBarHostState.showSnackbar("Done")
            }

        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    MenuBottomSheet(
        onDismiss = { onDismiss() },
        sheetState = sheetState,
        menuOptions = menuOptions,
        pendingAction = { action ->
            pendingAction = action
        }
    )

    // Show confirmation dialog if needed
    pendingAction?.let { action ->
        ActionHandler(
            action = action,
            content = content,
            viewModel = viewModel,
            onDismiss = { pendingAction = null },
            snackBar = snackBarHostState
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuBottomSheet(onDismiss: () -> Unit,sheetState: SheetState,menuOptions: List<MenuAction>,pendingAction: (MenuAction)-> Unit) {

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding( 12.dp)
                .clip(MaterialTheme.shapes.medium),
        ) {
            menuOptions.forEach { action ->
                Row (
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.background)
                        .clickable { pendingAction(action)}
                        .padding(horizontal = 12.dp),

                    verticalAlignment = Alignment.CenterVertically
                ){
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInHorizontally(
                            initialOffsetX = { it }, // starts from right side
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium, // adjust bounce
                                dampingRatio = Spring.DampingRatioMediumBouncy
                            )
                        ),
                        exit = slideOutHorizontally(
                            targetOffsetX = { it }, // slides out to right side
                            animationSpec = spring(
                                stiffness = Spring.StiffnessMedium,
                                dampingRatio = Spring.DampingRatioNoBouncy
                            )
                        )
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(action.icon),
                            contentDescription = action.label,
                            tint = if (action.label == "Delete") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Text(
                        text = action.label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Spacer(modifier = Modifier.height(1.dp))

            }
        }

        Spacer(Modifier.height(12.dp))
    }

}

