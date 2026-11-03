package com.iota.campusX.core.common

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

/**
 * Standard share utility for posts.
 */
suspend fun sharePost(context: Context, postId: String, postCaption: String, imageUrl: String? = null) {
    val deepLink = "https://collabbit.in/post/$postId"

    val shareText = buildString {
        if (postCaption.isNotBlank()) {
            append(postCaption)
            append("\n\n")
        }
        append("Read more on Collabbit: ")
        append(deepLink)
    }

    val imageUri = imageUrl?.let { downloadImageToCache(context, it) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "Post Image", imageUri)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (!imageUrl.isNullOrBlank()) {
                try {
                    clipData = ClipData.newRawUri("Post Image", imageUrl.toUri())
                } catch (_: Exception) {}
            }
        }

        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Post")
        val previewTitle = postCaption.take(60).ifBlank { "Collabbit Post" }
        putExtra(Intent.EXTRA_TITLE, previewTitle)
    }

    val chooser = Intent.createChooser(shareIntent, "Share Post").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (imageUri != null) {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(chooser)
}

/**
 * Standard share utility for skill courses.
 */
suspend fun shareCourse(context: Context, courseId: String, courseTitle: String, imageUrl: String? = null) {
    val deepLink = "https://collabbit.in/course/$courseId"

    val shareText = "Hey! I found this interesting course on Collabbit: $courseTitle\n\nCheck it out here: $deepLink"

    val imageUri = imageUrl?.let { downloadImageToCache(context, it) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "Course Thumbnail", imageUri)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (!imageUrl.isNullOrBlank()) {
                try {
                    clipData = ClipData.newRawUri("Course Thumbnail", imageUrl.toUri())
                } catch (_: Exception) {}
            }
        }

        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Course: $courseTitle")
        putExtra(Intent.EXTRA_TITLE, "Collabbit Course: $courseTitle")
    }

    val chooser = Intent.createChooser(shareIntent, "Share Course").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (imageUri != null) {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(chooser)
}

/**
 * Standard share utility for opportunities.
 */
suspend fun shareOpportunity(context: Context, opportunityId: String, title: String, imageUrl: String? = null) {
    val deepLink = "https://collabbit.in/opportunity/$opportunityId"

    val shareText = "Hey! I found this interesting opportunity on Collabbit: $title\n\nApply here: $deepLink"

    val imageUri = imageUrl?.let { downloadImageToCache(context, it) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "Company Logo", imageUri)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (!imageUrl.isNullOrBlank()) {
                try {
                    clipData = ClipData.newRawUri("Company Logo", imageUrl.toUri())
                } catch (_: Exception) {}
            }
        }

        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Opportunity: $title")
        putExtra(Intent.EXTRA_TITLE, "Collabbit Opportunity: $title")
    }

    val chooser = Intent.createChooser(shareIntent, "Share Opportunity").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (imageUri != null) {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(chooser)
}

/**
 * Standard share utility for societies/communities.
 */
suspend fun shareSociety(context: Context, societyId: String, societyName: String, imageUrl: String? = null) {
    val deepLink = "https://collabbit.in/society/$societyId"

    val shareText = "Hey! Join the \"$societyName\" society on Collabbit to connect, chat, and collaborate with others.\n\nJoin here: $deepLink"

    val imageUri = imageUrl?.let { downloadImageToCache(context, it) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            clipData = ClipData.newUri(context.contentResolver, "Society Logo", imageUri)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            if (!imageUrl.isNullOrBlank()) {
                try {
                    clipData = ClipData.newRawUri("Society Logo", imageUrl.toUri())
                } catch (_: Exception) {}
            }
        }

        putExtra(Intent.EXTRA_SUBJECT, "Join Society: $societyName")
        putExtra(Intent.EXTRA_TITLE, "Join Society: $societyName")
    }

    val chooser = Intent.createChooser(shareIntent, "Share Society").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (imageUri != null) {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(chooser)
}

/**
 * Downloads an image from a URL to the app's cache and returns a content URI.
 */
suspend fun downloadImageToCache(context: Context, imageUrl: String): Uri? = withContext(Dispatchers.IO) {
    return@withContext try {
        val url = URL(imageUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 10; Mobile)")
        connection.instanceFollowRedirects = true

        val inputStream = connection.inputStream

        val cacheDir = File(context.cacheDir, "shared_images").apply { mkdirs() }
        val file = File(cacheDir, "share_${System.currentTimeMillis()}.jpg")

        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
