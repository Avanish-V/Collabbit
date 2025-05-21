package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.iota.campusX.R
import com.iota.campusX.ui.theme.White400
import com.iota.campusX.ui.theme.secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDotOptionBottomSheet(
    isBottomSheet: Boolean,
    onDismiss: () -> Unit,
    isCurrentUser: Boolean,
    onDeleteClick:()-> Unit,
    onEditClick:()-> Unit,
) {


    if (isBottomSheet) {

        ModalBottomSheet(
            onDismissRequest = { onDismiss.invoke() },
            sheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            ),
            containerColor = Color.White,
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 20.dp, horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (isCurrentUser){

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(48.dp).fillMaxWidth().padding(start = 10.dp)
                            .background(
                                color = secondary,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                onClick = {
                                    onDeleteClick.invoke()
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {

                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.edit),
                            contentDescription = null,
                        )

                        Text("Edit")

                    }


                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.height(48.dp).fillMaxWidth().padding(start = 10.dp)
                            .background(
                                color = secondary,
                                shape = RoundedCornerShape(6.dp)
                            )
                            .clickable(
                                onClick = {
                                    onDeleteClick.invoke()
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            )
                    ) {

                        Icon(
                            modifier = Modifier.size(22.dp),
                            painter = painterResource(R.drawable.trash),
                            contentDescription = null,
                            tint = Color.Red
                        )

                        Text("Delete")

                    }


                }

                HorizontalDivider(
                    color = White400
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .fillMaxWidth()
                        .padding(start = 10.dp)
                        .background(
                            color = secondary,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable(
                            onClick = {

                            },
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )

                ) {

                    Icon(
                        modifier = Modifier.size(22.dp),
                        painter = painterResource(R.drawable.warning_2),
                        contentDescription = null,
                        tint = Color.Red
                    )

                    Text("Report (Work in progress)", color = Color.Red)

                }

            }


        }

    }

}