package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.iota.campusX.ui.theme.Black500

@Composable
fun ErrorScreen(error: String) {

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
        Text(
            text = error,
            color = Black500
        )
    }

}