package com.iota.campusX.core.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle

fun extractUrlFromText(text: String): String? {
    val regex = "(https?://[\\w\\d:#@%/;\$()~_?\\+-=\\.\\&]+)".toRegex()
    return regex.find(text)?.value
}

fun normalizeUrl(url: String): String {
    return if (!url.startsWith("http://") && !url.startsWith("https://")) {
        "https://$url"
    } else {
        url
    }
}

fun buildAnnotatedAutoLinkText(text: String): AnnotatedString {
    val urlRegex = "(https?://[\\w\\d:#@%/;\$()~_?\\+-=\\.\\&]+)".toRegex()
    return buildAnnotatedString {
        var lastIndex = 0
        urlRegex.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val url = matchResult.value
            pushStringAnnotation(tag = "URL", annotation = url)
            withStyle(
                style = SpanStyle(
                    color = Color(0xFF2563EB),
                    textDecoration = TextDecoration.Underline
                )
            ) {
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
