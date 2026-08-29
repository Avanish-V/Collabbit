package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.Utils.CircularLoading
import com.iota.campusX.Utils.ProfileEdit
import com.iota.campusX.ui.UIComponents.SubmitButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPage(
    onCancelClick: (ProfileEdit) -> Unit,
    onSubmitClick: () -> Unit,
    isLoading: Boolean,
    topBarTitle: String,
    snackBarHostState: SnackbarHostState,
    content: @Composable () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = topBarTitle, style = MaterialTheme.typography.titleLarge)
                },
                navigationIcon = {
                    IconButton(onClick = { onCancelClick(ProfileEdit.PROFILE_SCREEN) }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier.padding(end = 12.dp).height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoading) {
                            CircularLoading(MaterialTheme.colorScheme.primary)
                        } else {
                            SubmitButton { onSubmitClick.invoke() }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
        // 👇 important to handle keyboard properly
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding() // ✅ pushes content above keyboard
                .padding(padding)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            content()
        }
    }
}
