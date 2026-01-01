package com.iota.campusX.Feature.Post.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.appcheck.internal.NetworkClient
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.Feature.Post.domain.models.PostResponse
import com.iota.campusX.Koin.END_POINT
import io.ktor.client.HttpClient
import io.ktor.client.call.*
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
import java.io.InputStream
import kotlin.text.substringBefore

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

        val uploadJobs = uris.map { uri ->
            async(Dispatchers.IO) {
                try {
                    val fileName = generateFileName(uri)
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



    suspend fun UploadImageToS3(uri: Uri): String?{

        val token = try {
            auth.currentUser?.getIdToken(true)?.await()?.token
                ?: throw IllegalStateException("Failed to obtain auth token")
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Token fetch failed: ${e.message}")
            return null
        }

        try {
            val fileName = generateFileName(uri)
            val presignedUrl = getPresignedUrl(fileName, token)
            if (presignedUrl != null) {
                val success = uploadToS3(uri, presignedUrl)
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


    // Get presigned URL from backend (now takes pre-fetched token)
     suspend fun  getPresignedUrl(fileName: String, token: String): String? {
        return try {
            val response: HttpResponse = httpClient.get("$END_POINT/api/media/presign") {
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
     suspend fun uploadToS3(uri: Uri, presignedUrl: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                val bytes = inputStream?.readBytes() ?: return@withContext false
                val response = httpClient.put(presignedUrl) {
                    setBody(bytes)
                }
                response.status.value in 200..299
            }
        } catch (e: Exception) {
            Log.e("IMAGE_UPLOAD", "Upload error: ${e.message}")
            false
        }
    }

     fun generateFileName(uri: Uri): String {
        val time = System.currentTimeMillis()
        return "post_images/$time.jpg"
    }
}




