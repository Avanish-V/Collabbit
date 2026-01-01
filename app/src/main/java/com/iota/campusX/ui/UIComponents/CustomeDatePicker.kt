package com.iota.campusX.ui.UIComponents

import android.app.DatePickerDialog
import android.util.Log
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Duration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CourseDurationPicker(
    initialDuration: Duration? = null, // pass data from DB here
    onDurationSelected: (Duration) -> Unit = {}
) {
    Log.d("CourseDurationPicker", "initialDuration: $initialDuration")
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    // State holders (not tied permanently to initialDuration)
    var startDate by remember { mutableStateOf<Date?>(null) }
    var endDate by remember { mutableStateOf<Date?>(null) }
    var isCurrentlyStudying by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }
    var selectedDuration by remember { mutableStateOf(Duration()) }

    // 🔹 Sync state with initialDuration whenever it changes
    LaunchedEffect(initialDuration) {
        if (initialDuration != null) {
            startDate = initialDuration.startTimestamp?.let { Date(it) }
            endDate = initialDuration.endTimestamp?.let { Date(it) }
            isCurrentlyStudying = initialDuration.isCurrent
            selectedDuration = initialDuration
        }
    }

    fun updateDuration() {
        selectedDuration = Duration(
            courseStart = startDate?.let { dateFormat.format(it) } ?: "",
            startTimestamp = startDate?.time,
            courseEnd = if (isCurrentlyStudying) null else endDate?.let { dateFormat.format(it) },
            endTimestamp = if (isCurrentlyStudying) null else endDate?.time,
            isCurrent = isCurrentlyStudying
        )
        onDurationSelected(selectedDuration)
    }

    fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, _: Int ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                onDateSelected(calendar.time)

                validationError = validateDates(startDate, endDate, isCurrentlyStudying)
                updateDuration()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Column {
        // Start Date Picker
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surface
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.small
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = startDate?.let { "Start: ${dateFormat.format(it)}" } ?: "Select Start Date",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (startDate != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            IconButton(onClick = { showDatePicker { startDate = it } }) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pick Start Date")
            }
        }

        // Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isCurrentlyStudying,
                onCheckedChange = {
                    isCurrentlyStudying = it
                    if (it) endDate = null
                    validationError = validateDates(startDate, endDate, isCurrentlyStudying)
                    updateDuration()
                }
            )
            Text("I currently study here")
        }

        // End Date Picker
        if (!isCurrentlyStudying) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = MaterialTheme.shapes.small
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = endDate?.let { "End: ${dateFormat.format(it)}" }
                            ?: "Select End Date",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = if (endDate != null) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                IconButton(onClick = { showDatePicker { endDate = it } }) {
                    Icon(imageVector = Icons.Default.DateRange, contentDescription = "Pick End Date")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Validation error
        validationError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

private fun validateDates(startDate: Date?, endDate: Date?, isCurrentlyStudying: Boolean): String? {
    return when {
        startDate == null -> "Please select a start date"
        !isCurrentlyStudying && endDate == null -> "Please select an end date"
        !isCurrentlyStudying && startDate.after(endDate) ->
            "Start date cannot be after end date"
        else -> null
    }
}




