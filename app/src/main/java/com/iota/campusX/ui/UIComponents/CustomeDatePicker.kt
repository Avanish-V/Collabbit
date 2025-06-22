package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.serialization.Serializable

@Serializable
data class CourseDuration(
    val month: String = "",
    val year: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDatePicker(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (Pair<String, String>) -> Unit
) {

    if (!isVisible) return

    BasicAlertDialog(
        onDismissRequest = {
            onDismiss.invoke()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        var selectedMonth: String by remember { mutableStateOf("0") }
        var selectedYear: String by remember { mutableStateOf("2025") }

        Column(
            modifier = Modifier
                .background(color = Color.White, shape = RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp)),
            horizontalAlignment = Alignment.End
        ) {


            MonthYearPicker(
                selectedMonth = selectedMonth.toString(),
                selectedYear = selectedYear.toString(),
                onMonthChange = { selectedMonth = it },
                onYearChange = { selectedYear = it }
            )

            Spacer(modifier = Modifier.height(12.dp))


            TextButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                onClick = {
                    onDateSelected(Pair(first = selectedMonth, second = selectedYear))
                },
                shape = RoundedCornerShape(0.dp)
            ) {
                Text("Set")
            }


        }
    }


}



@Composable
fun MonthYearPicker(
    selectedMonth: String,
    selectedYear: String,
    onMonthChange: (String) -> Unit,
    onYearChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )
    val years = (2000..2050).toList()



    Box(modifier = Modifier
        .fillMaxWidth()
        .height(150.dp)){

        Row(
            modifier = modifier
                .height(150.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Month Picker
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(months) { index, month ->
                    Text(
                        text = month,
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable(
                                onClick = { onMonthChange(month) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        color = if (month == selectedMonth) Color.Black else Color.Gray,
                        fontSize = if (month == selectedMonth) 20.sp else 16.sp,
                        fontWeight = if (month == selectedMonth) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            // Year Picker
            LazyColumn(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                itemsIndexed(years) { index, year ->
                    Text(
                        text = year.toString(),
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable(
                                onClick = { onYearChange(year.toString()) },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ),
                        color = if (year.toString() == selectedYear) Color.Black else Color.Gray,
                        fontSize = if (year.toString() == selectedYear) 20.sp else 16.sp,
                        fontWeight = if (year.toString() == selectedYear) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Column(verticalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color.Gray.copy(alpha = 0.2f), Color.Transparent)
                        )
                    )

            )
        }

    }




}