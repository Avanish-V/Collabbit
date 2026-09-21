package com.iota.campusX.Koin


import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.VerifyUserRepoImpl
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.CredentialAuthDataSource
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.VerifyUserRepository
import com.iota.campusX.Feature.Chats.data.ChatImpl

import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import androidx.room.Room
import com.iota.campusX.Feature.Chats.data.local.ChatDatabase
import com.iota.campusX.Feature.Chats.data.local.ChatDao
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
import com.iota.campusX.realtime.socket.RealtimeSocketManager
import com.iota.campusX.Feature.Reply.presentation.ReplyViewModel
import com.iota.campusX.Feature.Report.data.ReportRepoImpl
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.NetworkCapability.AndroidConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityViewModel
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Utils.ThemeMode.ThemePreference
import com.iota.campusX.Utils.ThemeMode.dataStore
import com.iota.campusX.Utils.TokenProvider
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
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.Society.presentation.SocietyViewModel
import org.koin.dsl.module
import com.iota.campusX.Feature.Society.data.local.SocietyDatabase
import com.iota.campusX.Feature.Society.data.remote.FirestoreCommunityDataSource
import com.iota.campusX.Feature.Society.data.repository.CommunityRepositoryImpl
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.usecase.CreateCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCommunitiesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.JoinCommunityUseCase


val coreModule = module {

    single {
        Log.i("networkModule", "Creating HttpClient")
        HttpClient(OkHttp) {
            expectSuccess = true
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
                url(AppConstants.BASE_URL)
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
                        val token = currentUser.getIdToken(true).await().token
                        if (token != null) {
                            Log.d("networkModule", "Adding Token to ${context.url.buildString()}: ${token.take(10)}...")
                            context.headers["Authorization"] = "Bearer $token"
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

    // Firebase services
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { TokenProvider(get()) }

    // DataStore
    single<DataStore<Preferences>> { androidContext().dataStore }

    // Connectivity
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
}

val authModule = module {
    single<VerifyUserRepository> { VerifyUserRepoImpl( firebaseAuth = get(), httpClint = get(),get()) }
    single { CredentialAuthDataSource(context = get()) }
    viewModel { GoogleSignInViewModel(
        dataSource = get(),
        verifyUserRepository = get(),
        chatDatabase = get<ChatDatabase>(),
        campusDatabase = get<CampusDatabase>(),
        appDatabase = get<AppDatabase>()
    ) }
}

val replyModule = module {
    viewModel { ReplyViewModel(replyRepository = get()) }
}

val chatModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            ChatDatabase::class.java,
            "chats.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }
    single { get<ChatDatabase>().chatDao() }
    single<ChatRepository> { 
        ChatImpl(
            httpClient = get<HttpClient>(), 
            auth = get<FirebaseAuth>(), 
            socketManager = get<RealtimeSocketManager>(),
            chatDao = get<ChatDao>()
        ) 
    }
    viewModel { ChatsViewModel(chatRepository = get()) }
}


val reportModule = module {
    single<ReportRepository> { ReportRepoImpl(firestore = get(), auth = get()) }
    single { ReportViewModel(reportRepository = get()) }
}

val navigationModule = module {
    single { NavigationViewModel() }
    viewModel { HomeViewModel(get(), get()) }
    viewModel { ConnectivityViewModel(connectivityObserver = get()) }
}

val societyModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            SocietyDatabase::class.java,
            "society_database"
        ).fallbackToDestructiveMigration().build()
    }
    single { get<SocietyDatabase>().communityDao() }
    single { get<SocietyDatabase>().userCacheDao() }
    single { FirestoreCommunityDataSource(get(), get()) }
    single<CommunityRepository> { CommunityRepositoryImpl(get(), get(), get(), get(), get()) }
    
    factory { CreateCommunityUseCase(get()) }
    factory { GetCommunitiesUseCase(get()) }
    factory { JoinCommunityUseCase(get()) }

    viewModel { SocietyViewModel(get(), get(), get(), get(), get(), get()) }
}

