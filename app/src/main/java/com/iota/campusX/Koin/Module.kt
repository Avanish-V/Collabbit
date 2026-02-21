package com.iota.campusX.Koin

import ConsentAgreeViewModel
import SendPushNotification
import VerifyUserRepoImpl
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.cloudinary.android.MediaManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.CredentialAuthDataSource
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleSignInViewModel
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.VerifyUserRepository
import com.iota.campusX.Feature.Chats.data.ChatImpl
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notification.data.NotificationImpl
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.PushNotification.FcmNotificationSender
import com.iota.campusX.Feature.PushNotification.TokenServices
import com.iota.campusX.Feature.Reply.AppUserReplyViewModel
import com.iota.campusX.Feature.Reply.ReplyViewModel
import com.iota.campusX.Feature.Report.data.ReportRepoImpl
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.Feature.Search.Data.SearchRepositoryImpl
import com.iota.campusX.Feature.Search.Data.UserSearchApi
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import com.iota.campusX.Feature.Search.Presentation.SearchViewModel
import com.iota.campusX.Feature.Society.data.SocietyImplementation
import com.iota.campusX.Feature.Society.data.StreamImplementation
import com.iota.campusX.Feature.Society.domain.repository.SocietyInterface
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyOptionRepository
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyOptionsInterface
import com.iota.campusX.Feature.Society.presentation.SocietyMenuOptions.SocietyOptionsViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.AudioRoomViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.NetworkCapability.AndroidConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityViewModel
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Home.dataStore
import com.iota.campusX.Utils.ThemeMode.ThemePreference
import com.iota.campusX.Utils.TokenProvider
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val END_POINT = "http://10.58.100.105:8080"
//val END_POINT = "https://campusappbackend-446123587571.asia-south1.run.app"

val coreModule = module {

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = false   // off in production
                        isLenient = true
                        ignoreUnknownKeys = true
                        explicitNulls = false
                        encodeDefaults = true
                    }
                )
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 120_000  // 2 minutes
                connectTimeoutMillis = 30_000   // 30 seconds
                socketTimeoutMillis = 120_000   // 2 minutes
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("KtorHttp", message)  // 👈 shows in Logcat
                    }
                }
                level = LogLevel.ALL // log everything (headers + body)
            }
            install(DefaultRequest)

            defaultRequest {
                url {
                    protocol = URLProtocol.HTTP
                    host = "10.119.226.105"
                    port = 8080
                }
                contentType(ContentType.Application.Json)
            }
        }
    }

    // Firebase services
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { TokenProvider(get()) }

    // DataStore
    single<DataStore<Preferences>> { androidContext().dataStore }

    // Connectivity
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
}

val authModule = module {
    single<VerifyUserRepository> { VerifyUserRepoImpl( firebaseAuth = get(), httpClint = get()) }
    viewModel { ConsentAgreeViewModel(dataStore = get()) }
    single { CredentialAuthDataSource(context = get()) }
    viewModel { GoogleSignInViewModel(dataSource = get(), verifyUserRepository = get(), userProfileDao = get()) }
}

val replyModule = module {
    single { com.iota.campusX.Feature.Reply.ReplyRepository(getRepliesUseCase = get(), createReplyUseCase = get(), replyRepository = get(), notificationRepository = get()) }
    viewModel { ReplyViewModel(replyRepository = get()) }
    single { AppUserReplyViewModel(replyRepository = get()) }
}

val chatModule = module {
    single<ChatRepository> { ChatImpl(sendPushNotification = get(), database = get(), auth = get(), firestore = get(), userProfileRepository = get()) }
    viewModel { ChatsViewModel(chatRepository = get()) }
}

val notificationModule = module {
    single<NotificationRepository> {
        NotificationImpl(
            firestore = get(),
            auth = get(),
            database = get(),
            getSinglePostByIdUseCase = get(),
            userProfileRepository = get()
        )
    }

    viewModel { NotificationViewModel(notificationRepository = get()) }
    single { TokenServices(context = get()) }
    single { SendPushNotification(auth = get(), firestore = get(), context = get()) }
    single { FcmNotificationSender(context = get()) }
}

val societyModule = module {

    single<SocietyInterface> { SocietyImplementation(fireStore = get(), auth = get(), fireStorage = get(),get()) }
    single<StreamRepository> { StreamImplementation() }
    single { SocietyViewModel(societyRepository = get(), societyInterface = get()) }
    viewModel { StreamViewModel(streamRepository = get(), societyRepository = get(), appContext = get()) }
    viewModel { AudioRoomViewModel(societyRepository = get(), notificationSender = get()) }
    viewModel { SocietyOptionsViewModel(repository = get(), societyInterface = get()) }
    single { SocietyRepository(societyInterface = get(), userProfileInterface = get()) }
    single <SocietyOptionsInterface>{ SocietyOptionRepository(societyRepository = get(), societyInterface = get()) }

}

val searchModule = module {
    single<SearchRepository> { SearchRepositoryImpl(firestore = get(), auth = get(), httpClient = get()) }
    single { UserSearchApi(auth = get(), client = get()) }
    viewModel { SearchViewModel(repo = get()) }
}

val reportModule = module {
    single<ReportRepository> { ReportRepoImpl(firestore = get(), auth = get()) }
    single { ReportViewModel(reportRepository = get()) }
}

val navigationModule = module {
    single { NavigationViewModel() }
    viewModel { HomeViewModel(context = get()) }
    viewModel { ConnectivityViewModel(connectivityObserver = get()) }
}
val themeMode = module {
    single { ThemePreference }
}

val cloudinaryModule = module {
    single {
        MediaManager.get()
    }
}
