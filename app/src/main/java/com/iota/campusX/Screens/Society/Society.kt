package com.iota.campusX.Screens.Society

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Society(modifier: Modifier = Modifier) {

    Scaffold(
        topBar = {
            TopAppBar(
                title ={
                    Text(text = "Society")
                },
                actions = {
                    TextButton(onClick = {}, shape = RoundedCornerShape(2.dp)) {
                        Text(text = "Create Society")
                    }
                }
            )
        },
    ) {

        LazyColumn(modifier = Modifier.padding(it)) {




        }


    }

}