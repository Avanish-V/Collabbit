package com.iota.campusX.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.UIComponents.CircularLoading
import com.iota.campusX.ui.theme.LightTheme_Gray
import com.iota.campusX.ui.theme.White
import com.iota.campusX.ui.theme.LightTheme_Blue

@Composable
fun LoadingUI(isLoading:Boolean?=null,modifier: Modifier = Modifier) {

    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularLoading(
            MaterialTheme.colorScheme.primary
        )
    }

}


@Composable
fun StatusScreen(text:String,image:Int?= null,modifier: Modifier = Modifier) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){
            if (image != null){
                Image(
                    modifier = Modifier.size(120.dp),
                    painter = painterResource(image),
                    contentDescription = null
                )
            }

            Text(text = text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

        }

    }

}


@Composable
fun ConnectingLoadingUI(isLoading:Boolean) {

    if (isLoading){

        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White), contentAlignment = Alignment.Center){

            Column(
                modifier = Modifier.size(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = LightTheme_Blue
                )
                Text("Finding match...")
            }

        }


    }

}

