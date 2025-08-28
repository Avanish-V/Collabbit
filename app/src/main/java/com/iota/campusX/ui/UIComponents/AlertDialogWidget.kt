package com.iota.campusX.ui.UIComponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.iota.campusX.ui.theme.LightTheme_Blue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertDialogWidget(
    onDismiss: () -> Unit,
    title: String,
    description: String,
    positiveButtonText: String,
    negativeButtonText: String,
    onPositiveClick:()-> Unit,
    showLoading: Boolean,
) {

        BasicAlertDialog(
            onDismissRequest = {onDismiss},
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp)
            ) {
                Column {

                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(title,fontWeight = FontWeight.Bold)
                        Text(description, textAlign = TextAlign.Center)
                    }

                    Column {
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Box(Modifier
                                .weight(1f)
                                .clickable(
                                    onClick = { onDismiss.invoke() },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }),contentAlignment = Alignment.Center){
                                Text(negativeButtonText, modifier = Modifier.padding(16.dp))
                            }

                            VerticalDivider(
                                modifier = Modifier.height(48.dp)

                            )

                            Box(
                                Modifier
                                    .weight(1f)
                                    .clickable(
                                        onClick = {
                                            onPositiveClick.invoke()
                                        },
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ),
                                contentAlignment = Alignment.Center
                            ){
                                if (showLoading)
                                    CircularProgressIndicator(color = LightTheme_Blue, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                                else
                                    Text(positiveButtonText, modifier = Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }

}