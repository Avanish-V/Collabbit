package com.iota.campusX.Utils

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.iota.campusX.ui.theme.primary
import java.util.regex.Pattern

fun buildAnnotatedAutoLinkText(text: String): AnnotatedString {
    val linkPattern = Pattern.compile("(https?://[\\w-]+(\\.[\\w-]+)+(:\\d+)?(/[\\w-./?%&=]*)?)")
    val matcher = linkPattern.matcher(text)

    return buildAnnotatedString {
        var lastIndex = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val url = matcher.group()

            append(text.substring(lastIndex, start))

            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(SpanStyle(color = primary)) {
                append(url)
            }
            pop()

            lastIndex = end
        }
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

