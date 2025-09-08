// di/NetworkModule.kt
package com.iota.campusX.NetworkMonitor

import android.content.Context
import com.example.connectivity.network.NetworkMonitor
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val networkModule = module {

    single { HttpClient(CIO) { expectSuccess = false } }

    single<InternetPinger> { KtorInternetPinger(get()) }

    single {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }

    single {
        NetworkMonitor(androidContext(), get(), get())
    }

    single<ConnectivityRepository> { ConnectivityRepositoryImpl(get()) }

    viewModel { ConnectivityViewModel(get()) }
}
