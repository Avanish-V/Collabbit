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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.primary
import com.iota.campusX.ui.theme.White900

@Composable
fun LoadingUI(isLoading:Boolean) {

    if (isLoading){

        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center){
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                color = primary
            )
        }


    }

}

@Composable
fun StatusScreen(isActive:Boolean,text:String,image:Int?= null) {

    if (isActive){

        Box(modifier = Modifier.fillMaxSize().background(White900), contentAlignment = Alignment.Center){
            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                if (image != null){
                    Image(
                        modifier = Modifier.size(150.dp),
                        painter = painterResource(image),
                        contentDescription = null
                    )
                }

                Text(text = text, style = MaterialTheme.typography.titleMedium, color = Black500)

            }

        }


    }

}


@Composable
fun ConnectingLoadingUI(isLoading:Boolean) {

    if (isLoading){

        Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center){

            Column(
                modifier = Modifier.size(120.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = primary
                )
                Text("Finding match...")
            }

        }


    }

}

