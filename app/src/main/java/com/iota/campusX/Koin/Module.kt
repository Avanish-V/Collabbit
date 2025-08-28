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
import com.iota.campusX.Feature.Reply.AppUserReplyViewModel
import com.iota.campusX.Feature.Reply.ReplyViewModel
import com.iota.campusX.Feature.Report.data.ReportRepoImpl
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.Feature.Search.Data.SearchRepositoryImpl
import com.iota.campusX.Feature.Search.Domain.SearchRepository
import com.iota.campusX.Feature.Search.Presentation.SearchViewModel
import com.iota.campusX.Feature.Society.data.SocietyImplementation
import com.iota.campusX.Feature.Society.data.StreamImplementation
import com.iota.campusX.Feature.Society.domain.repository.SocietyRepository
import com.iota.campusX.Feature.Society.domain.repository.StreamRepository
import com.iota.campusX.Feature.Society.presentation.ViewModels.SocietyViewModel
import com.iota.campusX.Feature.Society.presentation.ViewModels.StreamViewModel
import com.iota.campusX.Feature.UserProfile.data.UserProfileImpl
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Feature.UserProfile.presentation.ViewProfileViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.NetworkCapability.AndroidConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityObserver
import com.iota.campusX.NetworkCapability.ConnectivityViewModel
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Home.dataStore
import com.iota.campusX.Utils.ThemeMode.ThemePreference
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val coreModule = module {
    // Http client
    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = false
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 5000
                connectTimeoutMillis = 5000
                socketTimeoutMillis = 5000
            }
            install(io.ktor.client.plugins.logging.Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Log.d("HttpClient", message)
                    }
                }
            }
            defaultRequest {
                headers.append(HttpHeaders.Accept, "application/json")
            }
        }
    }

    // Firebase services
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { FirebaseStorage.getInstance() }

    // DataStore
    single<DataStore<Preferences>> { androidContext().dataStore }

    // Connectivity
    single<ConnectivityObserver> { AndroidConnectivityObserver(get()) }
}

val authModule = module {
    single<VerifyUserRepository> { VerifyUserRepoImpl( get(), get()) }
    viewModel { ConsentAgreeViewModel(get()) }
    single { CredentialAuthDataSource(get()) }
    viewModel { GoogleSignInViewModel(get(),get()) }
}



val replyModule = module {
    single { com.iota.campusX.Feature.Reply.ReplyRepository(get(), get(), get()) }
    viewModel { ReplyViewModel(get()) }
    single { AppUserReplyViewModel(get()) }
}

val chatModule = module {
    single<ChatRepository> { ChatImpl(get(), get(), get(), get()) }
    viewModel { ChatsViewModel(get()) }
}

val notificationModule = module {
    single<NotificationRepository> { NotificationImpl(get(), get(), get()) }
    viewModel { NotificationViewModel(get()) }
    single { SendPushNotification(get(), get()) }
}

val profileModule = module {
    single<UserProfileRepo> { UserProfileImpl(get(), get(), get(), get(), get()) }
    single { UserProfileViewModel(get(),get()) }
    single { UserProfileRepository(get()) }
    viewModel { ViewProfileViewModel(get()) }
}

val societyModule = module {
    single<SocietyRepository> { SocietyImplementation(get(), get()) }
    single<StreamRepository> { StreamImplementation() }
    viewModel { SocietyViewModel(get()) }
    viewModel { StreamViewModel(get()) }
}

val searchModule = module {
    single<SearchRepository> { SearchRepositoryImpl(get()) }
    viewModel { SearchViewModel(get()) }
}

val reportModule = module {
    single<ReportRepository> { ReportRepoImpl(get(), get()) }
    single { ReportViewModel(get()) }
}

val navigationModule = module {
    single { NavigationViewModel() }
    viewModel { HomeViewModel(get()) }
    viewModel { ConnectivityViewModel(get()) }
}
val themeMode = module {
    single { ThemePreference }
}

val cloudinaryModule = module {
    single {
        MediaManager.get()
    }
}


suspend fun getFirebaseToken(): String? {
    val user = FirebaseAuth.getInstance().currentUser ?: return null
    return user.getIdToken(false).await().token
}

// Callback-based FCM token fetch
fun getFCMToken(onTokenReceived: (String?) -> Unit) {
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
            onTokenReceived(task.result)
        } else {
            Log.e("FCM", "Token fetch failed", task.exception)
            onTokenReceived(null)
        }
    }
}

fun disableOfflineSync(firestore: FirebaseFirestore) {
    firestore.firestoreSettings = FirebaseFirestoreSettings
        .Builder()
        .setPersistenceEnabled(false)
        .build()
}