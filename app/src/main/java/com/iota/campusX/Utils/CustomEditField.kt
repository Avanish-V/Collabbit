package com.iota.campusX.Utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (label.isNotEmpty()){
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )
        }

        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = {
                Text(
                    text = placeHolder,
                    style = MaterialTheme.typography.bodyMedium,
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
                focusedIndicatorColor = MaterialTheme.colorScheme.outline,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
//                disabledContainerColor = secondary
            ),
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }




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

    Column(

        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        if (label.isNotEmpty()){
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = {
                Text(
                    text = placeHolder,
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            enabled = enabled ?:true,
            leadingIcon = {
                leadingIcon?.invoke()
            },
            trailingIcon = {
                trailingIcon?.invoke()
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.outline,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
//                disabledContainerColor = secondary
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }




}