package com.iota.campusX.Utils

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
//import com.iota.campusX.ui.theme.secondary

@Composable
fun CustomTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (Any) -> Unit,
    label: String,
    enabled: Boolean? = null,
    maxLines: Int = 1,
    placeHolder: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions =  KeyboardOptions.Default.copy(
        imeAction = ImeAction.Done // Ensure "Done" action is set
    ),
    keyboardActions: KeyboardActions = KeyboardActions(
        onDone = {
            // Hide the keyboard after submission
        }
    )
) {

    TextField(
        modifier = modifier,
        value = value,
        maxLines = maxLines,
        onValueChange = {
            onValueChange(it)
        },
        label = {
            if (label.isNotEmpty()) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        placeholder = {
            Text(
                text = placeHolder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        textStyle = MaterialTheme.typography.bodyMedium,
        enabled = enabled ?:true,
        trailingIcon = {
            trailingIcon?.invoke()
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        shape = RoundedCornerShape(8.dp),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )

}


@Composable
fun CustomTextFieldWithLeadingIcon(
    modifier: Modifier,
    value: String,
    onValueChange: (Any) -> Unit,
    label:String,
    enabled:Boolean? = null,
    placeHolder:String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions =  KeyboardOptions.Default.copy(
        imeAction = ImeAction.Done // Ensure "Done" action is set
    ),
    keyboardActions: KeyboardActions = KeyboardActions(
        onDone = {
            // Hide the keyboard after submission
        }
    )
) {

    TextField(
        modifier = modifier,
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        placeholder = {
            Text(
                text = placeHolder,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        enabled = enabled ?:true,
        trailingIcon = {
            trailingIcon?.invoke()
        },
        colors = TextFieldDefaults.colors(
            unfocusedContainerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface,
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        maxLines = 1,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions
    )

}