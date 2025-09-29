package com.iota.campusX.Utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {

    Box(modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        CircularLoading(color = MaterialTheme.colorScheme.primary)
    }

}

@Composable
fun CircularLoading(color: Color) {

    CircularProgressIndicator(
        modifier = Modifier.size(24.dp),
        strokeWidth = 4.dp,
        color = color
    )
}


@Composable
fun StatusScreen(
    text:String,
    description:String? = null,
    image:Int?= null,
    modifier: Modifier = Modifier,

    buttonText: String? = null,
    onClick: () -> Unit = {}
) {

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ){

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (image != null){
                    Image(
                        modifier = Modifier.size(100.dp),
                        painter = painterResource(image),
                        contentDescription = null
                    )
                }

                Text(
                    modifier = Modifier.padding(start = 16.dp),
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (description != null){
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }


            if (buttonText != null){
                OutlinedButton(
                    onClick = {onClick.invoke()},
                ) {
                    Text(buttonText)
                }
            }


        }

    }

}



