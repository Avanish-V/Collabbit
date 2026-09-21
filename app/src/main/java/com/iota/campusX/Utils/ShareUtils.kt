package com.iota.campusX.Utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
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
    
    val shareIntent = Intent(if (imageUri != null) Intent.ACTION_SEND else Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            // Some apps need the text in EXTRA_TEXT even for images
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Post")
        
        // Android 10+ Rich Preview (for the share sheet itself)
        val previewTitle = postCaption.take(60).ifBlank { "Collabbit Post" }
        putExtra(Intent.EXTRA_TITLE, previewTitle)
        
        if (imageUri != null) {
            clipData = ClipData.newRawUri("Post Image", imageUri)
        } else if (!imageUrl.isNullOrBlank()) {
            try {
                clipData = ClipData.newRawUri("Post Image", imageUrl.toUri())
            } catch (_: Exception) {}
        }
    }

    val chooser = Intent.createChooser(shareIntent, "Share Post")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shareIntent.clipData != null) {
        chooser.clipData = shareIntent.clipData
        chooser.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(chooser)
}

/**
 * Standard share utility for skill courses.
 */
suspend fun shareCourse(context: Context, courseId: String, courseTitle: String, imageUrl: String? = null) {
    val deepLink = "https://collabbit.in/course/$courseId"
    
    // Put the link at the top to encourage link previews in apps like WhatsApp
    val shareText = "Hey! I found this interesting course on Collabbit: $courseTitle\n\nCheck it out here: $deepLink"

    val imageUri = imageUrl?.let { downloadImageToCache(context, it) }

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        if (imageUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, imageUri)
            putExtra(Intent.EXTRA_TEXT, shareText)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Course: $courseTitle")
        putExtra(Intent.EXTRA_TITLE, "Collabbit Course: $courseTitle")
        
        if (imageUri != null) {
            clipData = ClipData.newRawUri("Course Thumbnail", imageUri)
        } else if (!imageUrl.isNullOrBlank()) {
            try {
                clipData = ClipData.newRawUri("Course Thumbnail", imageUrl.toUri())
            } catch (_: Exception) {}
        }
    }

    val chooser = Intent.createChooser(shareIntent, "Share Course")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shareIntent.clipData != null) {
        chooser.clipData = shareIntent.clipData
        chooser.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
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
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        putExtra(Intent.EXTRA_SUBJECT, "Collabbit Opportunity: $title")
        putExtra(Intent.EXTRA_TITLE, "Collabbit Opportunity: $title")
        
        if (imageUri != null) {
            clipData = ClipData.newRawUri("Company Logo", imageUri)
        } else if (!imageUrl.isNullOrBlank()) {
            try {
                clipData = ClipData.newRawUri("Company Logo", imageUrl.toUri())
            } catch (_: Exception) {}
        }
    }

    val chooser = Intent.createChooser(shareIntent, "Share Opportunity")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shareIntent.clipData != null) {
        chooser.clipData = shareIntent.clipData
        chooser.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
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
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        
        putExtra(Intent.EXTRA_SUBJECT, "Join Society: $societyName")
        putExtra(Intent.EXTRA_TITLE, "Join Society: $societyName")
        
        if (imageUri != null) {
            clipData = ClipData.newRawUri("Society Logo", imageUri)
        } else if (!imageUrl.isNullOrBlank()) {
            try {
                clipData = ClipData.newRawUri("Society Logo", imageUrl.toUri())
            } catch (_: Exception) {}
        }
    }

    val chooser = Intent.createChooser(shareIntent, "Share Society")
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && shareIntent.clipData != null) {
        chooser.clipData = shareIntent.clipData
        chooser.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(chooser)
}

/**
 * Downloads an image from a URL to the app's cache and returns a content URI.
 */
private suspend fun downloadImageToCache(context: Context, urlString: String): Uri? = withContext(Dispatchers.IO) {
    try {
        val url = URL(urlString)
        val connection = url.openConnection()
        connection.connect()
        
        val inputStream = connection.getInputStream()
        val cacheFile = File(context.cacheDir, "shared_image_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(cacheFile)
        
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
