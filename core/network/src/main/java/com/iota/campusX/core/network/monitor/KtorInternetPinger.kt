package com.iota.campusX.core.network.monitor

import android.net.Network
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
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
