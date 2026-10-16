package com.iota.campusX.Feature.Post.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.onUpload
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream

@Serializable
data class UploadUrlResponse(val url: String)

class S3Uploader(
    private val context: Context,
    private val httpClient: HttpClient,
    private val auth: FirebaseAuth
) {

    suspend fun uploadImages(uris: List<Uri>): List<String> = coroutineScope {
        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Token fetch failed: ${e.message}")
            return@coroutineScope emptyList()
        }

        val uploadJobs = uris.mapIndexed { index, uri ->
            async(Dispatchers.IO) {
                try {
                    val fileName = generateFileName(uri, index)
                    val presignedUrl = getPresignedUrl(fileName, token)
                    if (presignedUrl != null) {
                        val success = uploadToS3(uri, presignedUrl)
                        if (success) presignedUrl.substringBefore("?") else null
                    } else {
                        Log.e("IMAGE_UPLOAD", "Failed to get presigned URL for: $fileName")
                        null
                    }
                } catch (e: Exception) {
                    Log.e("IMAGE_UPLOAD", "Error uploading $uri: ${e.message}")
                    null
                }
            }
        }

        val uploadedUrls = uploadJobs.awaitAll().filterNotNull()
        uploadedUrls
    }

    suspend fun UploadImageToS3(
        uri: Uri,
        folder: String = "post_images",
        shouldCompress: Boolean = true,
        onProgress: ((Float) -> Unit)? = null
    ): String? {
        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Token fetch failed: ${e.message}")
            return null
        }

        try {
            val fileName = generateFileName(uri, folder = folder)
            val presignedUrl = getPresignedUrl(fileName, token)
            if (presignedUrl != null) {
                val success = uploadToS3(uri, presignedUrl, onProgress, shouldCompress)
                return if (success) presignedUrl.substringBefore("?") else null
            } else {
                Log.e("IMAGE_UPLOAD", "Failed to get presigned URL for: $fileName")
                return null
            }
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error uploading $uri: ${e.message}")
            return null
        }
    }

    suspend fun uploadVideoWithThumbnail(
        videoUri: Uri,
        folder: String = "society_messages",
        onProgress: ((Float) -> Unit)? = null
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Token fetch failed: ${e.message}")
            return@withContext null to null
        }

        val videoJob = async {
            try {
                val fileName = generateFileName(videoUri, folder = folder)
                val presignedUrl = getPresignedUrl(fileName, token)
                if (presignedUrl != null) {
                    val success = uploadToS3(videoUri, presignedUrl, onProgress)
                    if (success) presignedUrl.substringBefore("?") else null
                } else null
            } catch (e: Exception) { null }
        }

        val thumbnailJob = async {
            try {
                val bitmap = generateVideoThumbnail(videoUri)
                if (bitmap != null) {
                    val fileName = "${folder}/thumbnails/${System.currentTimeMillis()}.jpg"
                    val presignedUrl = getPresignedUrl(fileName, token)
                    if (presignedUrl != null) {
                        val success = uploadBitmapToS3(bitmap, presignedUrl)
                        if (success) presignedUrl.substringBefore("?") else null
                    } else null
                } else null
            } catch (e: Exception) { null }
        }

        videoJob.await() to thumbnailJob.await()
    }

    suspend fun uploadDocumentWithThumbnail(
        documentUri: Uri,
        folder: String = "post_documents",
        onProgress: ((Float) -> Unit)? = null
    ): Pair<String?, String?> = withContext(Dispatchers.IO) {
        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Token fetch failed: ${e.message}")
            return@withContext null to null
        }

        val documentJob = async {
            try {
                val fileName = generateFileName(documentUri, folder = folder)
                val presignedUrl = getPresignedUrl(fileName, token)
                if (presignedUrl != null) {
                    val success = uploadToS3(documentUri, presignedUrl, onProgress)
                    if (success) presignedUrl.substringBefore("?") else null
                } else null
            } catch (e: Exception) { null }
        }

        val thumbnailJob = async {
            try {
                val bitmap = generatePdfThumbnail(documentUri)
                if (bitmap != null) {
                    val fileName = "${folder}/${System.currentTimeMillis()}_thumb.jpg"
                    val presignedUrl = getPresignedUrl(fileName, token)
                    if (presignedUrl != null) {
                        val success = uploadBitmapToS3(bitmap, presignedUrl)
                        if (success) presignedUrl.substringBefore("?") else null
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }

        documentJob.await() to thumbnailJob.await()
    }

    private fun generatePdfThumbnail(uri: Uri): Bitmap? {
        try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = android.graphics.pdf.PdfRenderer(fileDescriptor)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                return bitmap
            }
            renderer.close()
            fileDescriptor.close()
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error generating PDF thumbnail: ${e.message}")
        }
        return null
    }

    private fun generateVideoThumbnail(videoUri: Uri): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, videoUri)
            retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
        } catch (e: Exception) {
            null
        } finally {
            retriever.release()
        }
    }

    private suspend fun uploadBitmapToS3(bitmap: Bitmap, presignedUrl: String): Boolean = withContext(Dispatchers.IO) {
        var s3Client: HttpClient? = null
        try {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
            val bytes = stream.toByteArray()

            s3Client = HttpClient(OkHttp) {
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 60_000
                }
            }

            val response: HttpResponse = s3Client.put(presignedUrl) {
                setBody(bytes)
                header(HttpHeaders.ContentType, "image/jpeg")
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        } finally {
            s3Client?.close()
        }
    }

    suspend fun getPresignedUrl(fileName: String, token: String): String? {
        return try {
            val response: HttpResponse = httpClient.get("media/presign") {
                parameter("fileName", fileName)
                header("Authorization", "Bearer $token")
            }

            val urlResponse: UploadUrlResponse = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())
            urlResponse.url
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error generating presigned URL: ${e.message}")
            null
        }
    }

    suspend fun uploadToS3(
        uri: Uri,
        presignedUrl: String,
        onProgress: ((Float) -> Unit)? = null,
        shouldCompress: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        var s3Client: HttpClient? = null
        try {
            val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            val isImage = mimeType.startsWith("image/")

            val bytes = if (shouldCompress && isImage) {
                compressImage(uri) ?: context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext false
            } else {
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return@withContext false
            }

            if (bytes.isEmpty()) return@withContext false

            s3Client = HttpClient(OkHttp) {
                install(io.ktor.client.plugins.HttpTimeout) {
                    requestTimeoutMillis = 90_000
                    connectTimeoutMillis = 30_000
                }
            }

            val response: HttpResponse = s3Client.put(presignedUrl) {
                setBody(bytes)
                header(HttpHeaders.ContentType, if (shouldCompress && isImage) "image/jpeg" else mimeType)
                onUpload { bytesSentTotal, contentLength ->
                    if (contentLength != null && contentLength > 0) {
                        onProgress?.invoke(bytesSentTotal.toFloat() / contentLength)
                    }
                }
            }

            response.status.isSuccess()
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Upload error for $uri: ${e.message}", e)
            false
        } finally {
            s3Client?.close()
        }
    }

    private fun generateFileName(uri: Uri, index: Int = 0, folder: String = "posts"): String {
        return "${folder}/${System.currentTimeMillis()}_${index}.jpg"
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            val outputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }
}
