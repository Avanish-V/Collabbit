package com.iota.campusX.Feature.UserProfile.ui.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.UserProfile.domain.Model.Gender

@Composable
fun GenderSelector(
    enabled: Boolean,
    selectedGender: Gender,
    onSelect: (Gender) -> Unit
) {

    val genders = listOf(Gender.MALE, Gender.FEMALE)

    FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        genders.forEach {
            InputChip(
                enabled = enabled,
                selected = selectedGender == it,
                onClick = { onSelect(it) },
                label = {
                    Text(modifier = Modifier.padding(12.dp), text = it.name.lowercase().replaceFirstChar { it.uppercase() })
                },
            )

        }
    }

}
