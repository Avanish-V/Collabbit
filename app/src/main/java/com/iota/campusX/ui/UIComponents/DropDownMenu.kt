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
import com.iota.campusX.Feature.Post.data.model.FeedMode
//import com.iota.campusX.ui.theme.secondary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce

@OptIn(FlowPreview::class)
@Composable
fun AutoCompleteFieldOfStudyDropdown(
    modifier: Modifier = Modifier,
    fieldOptions: List<String>,
    selectedField: String,
    onFieldChange: (String) -> Unit,
    label: String = "Field of Study"
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var query by remember { mutableStateOf(selectedField) }
    var expanded by remember { mutableStateOf(false) }
    var filteredSuggestions by remember { mutableStateOf(emptyList<String>()) }

    // Debounce logic with coroutine
    LaunchedEffect(query) {
        snapshotFlow { query }
            .debounce(300) // 300ms debounce
            .collectLatest { typedText ->
                if (typedText.length > 6 || typedText.isEmpty()) return@collectLatest
                filteredSuggestions = fieldOptions.filter {
                    it.contains(typedText, ignoreCase = true)
                }.take(5)
                expanded = filteredSuggestions.isNotEmpty()
            }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
        )

        OutlinedTextField(
            value = selectedField,
            onValueChange = {
                query = it
                onFieldChange(it)
            },
            trailingIcon = {
                IconButton(
                    onClick = { expanded = !expanded },
                    enabled = query.isNotEmpty()
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Toggle dropdown"
                    )
                }
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.small
                ),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            ),
            placeholder = {
                Text(
                    text = "Ex-Computer Science & Engineering",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), // ensure no external padding
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            filteredSuggestions.forEach { suggestion ->
                DropdownMenuItem(
                    onClick = {
                        query = suggestion
                        onFieldChange(suggestion)
                        expanded = false
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    text = { Text(suggestion) },
                    contentPadding = PaddingValues(0.dp) // removes item padding
                )
            }
        }

    }
}


@OptIn(FlowPreview::class)
@Composable
fun  SimpleDropDown(
    modifier: Modifier = Modifier,
    fieldOptions: List<String>,
    currentMode: FeedMode,
    selectedField: String,
    onFieldChange: (String) -> Unit,
    label: String = "Field of Study"
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var expanded by remember { mutableStateOf(false) }


    var currentMode by remember {
        mutableStateOf(currentMode.name.lowercase().replaceFirstChar { it.uppercase() })
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

