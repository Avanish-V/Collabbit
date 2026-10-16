package com.iota.campusX.Feature.Report.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Report.domain.model.ReportReason
import com.iota.campusX.Feature.Report.domain.model.reportReasons
import com.iota.campusX.ui.UIComponents.Divider
import com.iota.campusX.ui.UIComponents.PrimaryButton

@Composable
fun ReportContent(
    onSubmitClick: (ReportReason) -> Unit
) {
    var reportReason by remember { mutableStateOf(ReportReason()) }

    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    Text("Report", style = MaterialTheme.typography.titleLarge)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Divider()
                        Text(text = "CampusCircle protects your identity", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "When reporting a post, your identity remains confidential. Your concerns are addressed without revealing your name or information to ensure anonymity and maintain privacy throughout the process.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Divider()
                    }
                }
            }
            items(reportReasons) {
                ReportSingleItem(
                    reportReason = it,
                    selectedReason = reportReason,
                    onReasonSelect = {
                        reportReason = it
                    }
                )
            }
        }

        PrimaryButton(
            buttonText = "Submit",
            onClick = {
                onSubmitClick.invoke(reportReason)
            }
        )
    }
}

@Composable
fun ReportSingleItem(
    reportReason: ReportReason,
    selectedReason: ReportReason,
    onReasonSelect: (ReportReason) -> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = selectedReason == reportReason,
            onClick = {
                isSelected = !isSelected
                onReasonSelect(reportReason)
            }
        )
        Text(reportReason.description)
    }
}
