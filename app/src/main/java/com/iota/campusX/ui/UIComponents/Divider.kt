package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.theme.LightTheme_Blue

@Composable
fun Divider(modifier: Modifier = Modifier) {

    HorizontalDivider(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.outline,
        thickness = 0.5.dp
    )

}

@Composable
fun CircularLoading(modifier: Modifier = Modifier) {

    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        color = MaterialTheme.colorScheme.primary,
        strokeWidth = 2.dp
    )

}