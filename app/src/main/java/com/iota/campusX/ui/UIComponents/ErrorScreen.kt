package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.White900

@Composable
fun ErrorScreen(isActive:Boolean,text:String,image:Int?= null,onReTry:()-> Unit) {

    if (isActive){

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
            Column (
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                if (image != null){
                    Image(
                        modifier = Modifier.size(150.dp),
                        painter = painterResource(image),
                        contentDescription = null
                    )
                }

                Text(text = text, style = MaterialTheme.typography.titleMedium, color = Black500, textAlign = TextAlign.Center)

                OutlinedButton(
                    onClick = {onReTry.invoke()},
                    border = _root_ide_package_.androidx.compose.foundation.BorderStroke(color = White400, width = 1.dp)
                ) {
                    Text("Try Again")
                }
            }

        }


    }

}