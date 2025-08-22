package com.iota.campusX.ui.UIComponents

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

@Composable
fun ReportContent(
    onSubmitClick:(ReportReason)-> Unit
) {

    var reportReason by remember { mutableStateOf(ReportReason())}

    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

        LazyColumn (verticalArrangement = Arrangement.spacedBy(12.dp)){
            item {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(24.dp)) {

                    Text("Report", style = MaterialTheme.typography.titleLarge)

                    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)){
                        Divider()
                        Text(text = "CampusX protects your identity",style = MaterialTheme.typography.titleMedium)
                        Text(text = "When reporting a post, your identity remains confidential. Your concerns are addressed without revealing your name or information to ensure anonymity" +
                                "and maintain privacy throughout the process.",
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

        PrimaryButton (
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
    onReasonSelect:(ReportReason)-> Unit
) {
    var isSelected by remember { mutableStateOf(false) }

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(
            selected = if (selectedReason == reportReason) true else false,
            onClick = {
                isSelected = !isSelected
                onReasonSelect(reportReason)
            }
        )
        Text(reportReason.description.toString())
    }

}


data class ReportReason(
    val type: ReportType = ReportType.NONE,
    val title: String = "",
    val description: String = ""
)

val reportReasons = listOf(
    ReportReason(
        ReportType.SPAM,
        "Spam or misleading",
        "This content is unwanted promotional or misleading."
    ),
    ReportReason(
        ReportType.HATE_SPEECH,
        "Hate speech or abuse",
        "Offensive or threatening content."
    ),
    ReportReason(
        ReportType.HARASSMENT,
        "Harassment or bullying",
        "Targeted insults, threats, or unwanted contact."
    ),
    ReportReason(
        ReportType.VIOLENCE,
        "Violence or harmful acts",
        "Promotes violence or self-harm."
    ),
    ReportReason(ReportType.SEXUAL_CONTENT, "Sexual content", "Inappropriate or explicit content."),
    ReportReason(
        ReportType.MISINFORMATION,
        "False information",
        "Contains false or misleading facts."
    ),
    ReportReason(ReportType.OTHER, "Other", "Does not fall under a specific category.")
)


enum class ReportType {
    NONE,
    SPAM,
    HATE_SPEECH,
    HARASSMENT,
    VIOLENCE,
    SEXUAL_CONTENT,
    MISINFORMATION,
    OTHER
}


