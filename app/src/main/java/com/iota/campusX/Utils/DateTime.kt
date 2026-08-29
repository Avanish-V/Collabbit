package com.iota.campusX.Utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialExample(
    isTimePickerActive : Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: (Boolean) -> Unit,
) {
    val currentTime = Calendar.getInstance()

    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.get(Calendar.HOUR_OF_DAY),
        initialMinute = currentTime.get(Calendar.MINUTE),
        is24Hour = false,
    )

    if (isTimePickerActive){
        Column {

            DatePickerDialog(
                onDismissRequest = {onDismiss(false) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val selectedHour = timePickerState.hour
                            val selectedMinute = timePickerState.minute

                            // Convert hour to 12-hour format
                            val isPM = selectedHour >= 12
                            val formattedHour = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                            val formattedMinute = String.format("%02d", selectedMinute)
                            val amPm = if (isPM) "pm" else "am"

                            val formattedTime = "$formattedHour:$formattedMinute$amPm"
                            onConfirm(formattedTime)
                        },

                        ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onDismiss(false)
                        }
                    ) {
                        Text("Close")
                    }
                },
                shape = RoundedCornerShape(20.dp),
                tonalElevation = 5.dp,
            ) {

                Box(modifier = Modifier.fillMaxWidth().padding(top = 20.dp), contentAlignment = Alignment.Center){
                    TimePicker(state = timePickerState)
                }

            }

        }
    }

}

fun timeMillsToString(timeMills: Long): String {
    return try {
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val date = Date(timeMills)
        dateFormat.format(date)
    } catch (e: Exception) {
        "Invalid date"
    }
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes m${if (minutes != 1L) "" else ""}"
        hours < 24 -> "$hours h${if (hours != 1L) "" else ""}"
        days < 7 -> "$days d${if (days != 1L) "" else ""}"
        else -> {
            val sdf = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}
