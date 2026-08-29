package com.iota.campusX.Feature.Reply.presentation.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.iota.campusX.Feature.Reply.domain.model.MentionBuilder

@Composable
fun ReplyText(text: String, mentionedUser: MentionBuilder? = null, onMentionClick: (String) -> Unit = {}) {
    val color = MaterialTheme.colorScheme.primary
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val annotatedText = remember(text, mentionedUser) {
        buildAnnotatedString {
            if (mentionedUser != null) {
                pushStringAnnotation(tag = "MENTION", annotation = mentionedUser.mentionUserName)
                withStyle(style = SpanStyle(color = color)) { append("@${mentionedUser.mentionUserName}") }
                pop()
                append(" ")
            }
            append(text)
        }
    }
    Text(
        text = annotatedText,
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 4.dp).pointerInput(annotatedText) {
            detectTapGestures { tapOffset ->
                layoutResult?.let { layout ->
                    val position = layout.getOffsetForPosition(tapOffset)
                    annotatedText.getStringAnnotations("MENTION", position, position).firstOrNull()?.let { onMentionClick(it.item) }
                }
            }
        },
        onTextLayout = { layoutResult = it }
    )
}