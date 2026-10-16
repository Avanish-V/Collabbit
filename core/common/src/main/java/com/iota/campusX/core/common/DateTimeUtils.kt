package com.iota.campusX.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun timeMillsToString(timeMills: Long): String {
    return try {
        val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        val date = Date(timeMills)
        dateFormat.format(date)
    } catch (e: Exception) {
        "Invalid date"
    }
}

fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "$minutes m"
        hours < 24 -> "$hours h"
        days < 7 -> "$days d"
        else -> {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}
