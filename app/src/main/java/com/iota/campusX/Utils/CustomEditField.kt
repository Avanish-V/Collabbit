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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iota.campusX.ui.theme.Black400
import com.iota.campusX.ui.theme.Black500
import com.iota.campusX.ui.theme.Black800

@Composable
fun CustomTextField(
    modifier: Modifier,
    value: String,
    onValueChange: (Any) -> Unit,
    label:String,
    placeHolder:String,
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

        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = Black800,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
        OutlinedTextField(
            modifier = modifier,
            value = value,
            onValueChange = {
                onValueChange(it)
            },
            placeholder = {
                Text(
                    text = placeHolder,
                    color = Black400
                )
            },
            trailingIcon = {
                trailingIcon?.invoke()
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = Color.LightGray,
            ),
            shape = RoundedCornerShape(8.dp),
            textStyle = TextStyle(
                color = Black800,
                fontWeight = FontWeight.Bold
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
    }




}
