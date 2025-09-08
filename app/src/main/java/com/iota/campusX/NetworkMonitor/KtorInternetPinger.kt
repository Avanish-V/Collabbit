// network/KtorInternetPinger.kt
package com.iota.campusX.NetworkMonitor

import android.net.Network
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class KtorInternetPinger(
    private val client: HttpClient
) : InternetPinger {

    private val validationUrls = listOf(
        "https://clients3.google.com/generate_204",
        "https://www.google.com/generate_204",
        "https://1.1.1.1"
    )


    override suspend fun isInternetAvailable(network: Network?): Boolean =
        withContext(Dispatchers.IO) {
            for (url in validationUrls) {
                try {
                    val response: HttpResponse = client.get(url) {
                        header("User-Agent", "ConnectivityChecker")
                        header("Connection", "close")
                    }
                    val code = response.status.value
                    if (code == 204 || (code in 200..299)) {
                        return@withContext true
                    }
                } catch (_: Exception) {
                    // try next
                }
            }
            false
        }
}
