package com.iota.campusX.Utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.iota.campusX.ui.theme.LightTheme_Blue
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
            withStyle(SpanStyle(color = LightTheme_Blue)) {
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

