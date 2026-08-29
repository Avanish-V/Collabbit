package com.iota.campusX.ui.UIComponents

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

//import com.iota.campusX.ui.theme.secondary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce




@OptIn(FlowPreview::class)
@Composable
fun  SimpleDropDown(
    modifier: Modifier = Modifier,
    fieldOptions: List<String>,
    currentMode: String,
    selectedField: String,
    onFieldChange: (String) -> Unit,
    label: String = "Field of Study"
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var expanded by remember { mutableStateOf(false) }

    var currentMode by remember {
        mutableStateOf(currentMode.lowercase().replaceFirstChar { it.uppercase() })
    }
    LaunchedEffect(selectedField) {
        if (selectedField.isNotEmpty()){
            currentMode = selectedField
        }
    }


    Column(modifier = modifier) {

        Row (
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(5.dp)
                ).
                    background(
                        color = MaterialTheme.colorScheme.surface
                    )
                .padding( horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(currentMode)
            Icon(
                modifier = Modifier.clickable(
                    onClick = { expanded = !expanded},
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ),
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Toggle dropdown"

            )
        }

        Spacer(Modifier.height(12.dp))

        DropdownMenu(
            modifier= Modifier.padding(end = 12.dp),
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            fieldOptions.forEachIndexed { index,suggestion ->
                DropdownMenuItem(
                    onClick = {
                        onFieldChange(suggestion)
                        expanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    text = {
                        Text(suggestion)
                    }

                )
                if (index == 0){
                    Divider()
                }

            }
        }
    }
}

