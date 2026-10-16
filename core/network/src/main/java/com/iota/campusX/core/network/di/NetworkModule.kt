package com.iota.campusX.core.network.di

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.iota.campusX.core.network.NetworkConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpSendPipeline
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val networkModule = module {
    single {
        val networkConfig = get<NetworkConfig>()
        Log.i("networkModule", "Creating HttpClient with baseUrl: ${networkConfig.baseUrl}")
        HttpClient(OkHttp) {
            expectSuccess = false
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.i("Ktor", message)
                    }
                }
                level = LogLevel.ALL
            }
            install(WebSockets)
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            defaultRequest {
                url(networkConfig.baseUrl)
            }
        }.also { client ->
            client.sendPipeline.intercept(HttpSendPipeline.State) {
                // DO NOT add Authorization header to S3/AWS requests
                val host = context.url.host
                if (host.contains("amazonaws.com") || host.contains("s3")) {
                    proceed()
                    return@intercept
                }

                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    try {
                        var token = currentUser.getIdToken(false).await().token
                        if (token == null) {
                            token = currentUser.getIdToken(true).await().token
                        }
                        if (token != null) {
                            Log.d("networkModule", "Adding Token to ${context.url.buildString()}: ${token.take(10)}...")
                            context.headers["Authorization"] = "Bearer $token"
                        } else {
                            Log.e("networkModule", "Token is null for ${context.url.buildString()}")
                        }
                    } catch (e: Exception) {
                        Log.e("networkModule", "Token fetch failed", e)
                    }
                } else {
                    Log.e("networkModule", "Current user is null! Cannot attach Authorization header for ${context.url.buildString()}")
                }
                proceed()
            }
        }
    }

    // Network Monitoring
    single(named("pingerClient")) { HttpClient(OkHttp) { expectSuccess = false } }
    single<com.iota.campusX.core.network.monitor.InternetPinger> {
        com.iota.campusX.core.network.monitor.KtorInternetPinger(get(named("pingerClient")))
    }
    single {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob() + kotlinx.coroutines.Dispatchers.Default)
    }
    single {
        com.iota.campusX.core.network.monitor.NetworkMonitor(androidContext(), get(), get())
    }
    single<com.iota.campusX.core.network.monitor.ConnectivityRepository> {
        com.iota.campusX.core.network.monitor.ConnectivityRepositoryImpl(get())
    }
    viewModel {
        com.iota.campusX.core.network.monitor.ConnectivityViewModel(get())
    }
}
