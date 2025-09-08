package com.iota.campusX.ui.UIComponents

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.iota.campusX.R

@Composable
fun ReportSuccess(modifier: Modifier = Modifier) {

    Column (modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally){

        Column (horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)){
            AnimatedStatus(
                modifier = Modifier.size(100  .dp),
                file = R.raw.sent_email,
                description = "Submitted"
            )
            Text("Report Submitted", style = MaterialTheme.typography.headlineLarge)

            Text("Thank you for helping keep our community safe", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)){

            Text("What happens next?", style = MaterialTheme.typography.headlineLarge)

            Text(
                text = "● Our moderation team will review your report within 24-48 hours.\n" +
                        "● We'll take appropriate action based on our community guidelines.\n" +
                        "● You may receive an update on the outcome via notification.",
                style = MaterialTheme.typography.bodyMedium
            )

            Box(modifier = Modifier.fillMaxWidth().border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center){
                Text(text = "Report ID: RPT-2024-071-8847", modifier = Modifier.padding(12.dp))
            }

            Box(modifier = Modifier.fillMaxWidth().background(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(12.dp)).border(width = 1.dp, color = MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center){
                Text(text = "Reports are confidential. The user won't know you reported their content unless action is taken.", modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center)
            }

        }




    }


}