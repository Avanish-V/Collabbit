package com.iota.campusX.Feature.Post.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.exifinterface.media.ExifInterface
import com.google.firebase.auth.FirebaseAuth
import io.ktor.client.HttpClient
import io.ktor.client.call.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
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

        // Wait for all uploads to complete
        val uploadedUrls = uploadJobs.awaitAll().filterNotNull()

        Log.e("IMAGE_UPLOAD", "Uploaded URLs: $uploadedUrls")
        uploadedUrls
    }



    suspend fun UploadImageToS3(
        uri: Uri, 
        folder: String = "post_images",
        shouldCompress: Boolean = true,
        onProgress: ((Float) -> Unit)? = null
    ): String?{

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
                    } else {
                        Log.e("IMAGE_UPLOAD", "Failed to get presigned URL for document thumbnail")
                        null
                    }
                } else {
                    Log.e("IMAGE_UPLOAD", "Failed to generate bitmap for document thumbnail")
                    null
                }
            } catch (e: Exception) {
                Log.e("IMAGE_UPLOAD", "Error in document thumbnail job: ${e.message}")
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
            val success = response.status.isSuccess()
            if (!success) {
                Log.e("IMAGE_UPLOAD", "S3 Bitmap Upload failed: ${response.status.value}")
            }
            success
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error uploading bitmap to S3: ${e.message}")
            false
        } finally {
            s3Client?.close()
        }
    }


    // Get presigned URL from backend (now takes pre-fetched token)
     suspend fun  getPresignedUrl(fileName: String, token: String): String? {
        return try {
            val response: HttpResponse = httpClient.get("media/presign") {
                parameter("fileName", fileName)
                header("Authorization", "Bearer $token")
            }

            val urlResponse: UploadUrlResponse = Json { ignoreUnknownKeys = true }.decodeFromString(response.bodyAsText())

            Log.e("IMAGE_UPLOAD", "Presigned URL generated: ${urlResponse.url}")
            urlResponse.url
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error generating presigned URL: ${e.message}")
            null
        }
    }

    // Upload single file to S3
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
            
            if (bytes.isEmpty()) {
                Log.e("IMAGE_UPLOAD", "File bytes are empty for $uri")
                return@withContext false
            }

            Log.d("IMAGE_UPLOAD", "Starting S3 upload for ${uri.path}, original size might have changed due to compression. Upload size: ${bytes.size} bytes")
            
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
            
            val isSuccess = response.status.isSuccess()
            if (!isSuccess) {
                val errorBody = response.bodyAsText()
                Log.e("IMAGE_UPLOAD", "S3 Upload failed: ${response.status.value} - $errorBody")
            } else {
                Log.d("IMAGE_UPLOAD", "S3 Upload successful for ${uri.path}")
            }
            
            isSuccess
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Upload error for $uri: ${e.message}", e)
            false
        } finally {
            s3Client?.close()
        }
    }

    private fun compressImage(uri: Uri): ByteArray? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            
            // First decode with inJustDecodeBounds=true to check dimensions
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Calculate inSampleSize to scale down if image is too large
            // Target 4K resolution for maximum quality (1MB-1.5MB range)
            val reqWidth = 2160
            val reqHeight = 3840
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
            options.inJustDecodeBounds = false

            // Decode bitmap with inSampleSize set
            val inputStream2 = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, options)
            inputStream2.close()

            if (originalBitmap == null) return null

            // Rotate bitmap based on EXIF orientation
            val rotatedBitmap = handleRotation(uri, originalBitmap)
            val outputStream = ByteArrayOutputStream()
            // Maximum quality compression to target 1MB-1.5MB and avoid blur
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            val bytes = outputStream.toByteArray()
            
            // Clean up
            if (rotatedBitmap != originalBitmap) {
                rotatedBitmap.recycle()
            }
            originalBitmap.recycle()
            
            Log.d("IMAGE_UPLOAD", "Compressed and rotated image from ${options.outWidth}x${options.outHeight} to ${bytes.size} bytes")
            bytes
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Compression failed for $uri: ${e.message}")
            null
        }
    }

    private fun handleRotation(uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return bitmap
            val exifInterface = ExifInterface(inputStream)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            inputStream.close()

            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                else -> return bitmap
            }

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Error handling rotation: ${e.message}")
            bitmap
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

     fun generateFileName(uri: Uri, index: Int = 0, folder: String = "post_images"): String {
        val time = System.currentTimeMillis()
        val extension = getFileExtension(uri)
        return "$folder/${time}_$index.$extension"
    }

    private fun getFileExtension(uri: Uri): String {
        val contentResolver = context.contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri)) ?: "jpg"
    }
}




