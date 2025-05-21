package com.iota.campusX.Koin

import android.util.Log
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleAuthRepo
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleAuthUiClient
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Feature.Chats.data.ChatImpl
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notification.data.NotificationImpl
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.PostRepoImpl
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.presentation.PostViewModel
import com.iota.campusX.Feature.PushNotification.PushNotificationService
import com.iota.campusX.Feature.UserProfile.data.UserProfileImpl
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Screens.Home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val appModule = module {

    single {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 15000
                socketTimeoutMillis = 15000
            }

            install(io.ktor.client.plugins.logging.Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        println("Error is$message")
                    }
                }
            }
//            install(DefaultRequest) {
//                header(HttpHeaders.Authorization, "Bearer ${getFirebaseToken()}")
//            }

            defaultRequest {
               // url("http://192.168.80.189:8080") // Base URL
                headers.append(HttpHeaders.Accept, "application/json")
            }
        }
    }

    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }
    single <FirebaseDatabase>{ FirebaseDatabase.getInstance() }
    single<FirebaseAuth> { FirebaseAuth.getInstance() }
    single<FirebaseStorage> { FirebaseStorage.getInstance() }


    single<UserProfileRepo> { UserProfileImpl(get(),get(),get(),get()) }
    single<GoogleAuthRepo> { GoogleAuthUiClient(androidContext(), get(),get()) }

    single<PostRepository> { PostRepoImpl(getFCMToken(),get(),get()) }
    single <ChatRepository>{ ChatImpl("",get(),get(),get()) }
    single <NotificationRepository>{ NotificationImpl(get(),get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { UserProfileViewModel(get()) }
    viewModel { PostViewModel(get()) }
    viewModel { ChatsViewModel(get()) }
    viewModel { NavigationViewModel() }
    viewModel { NotificationViewModel(get()) }
    viewModel { HomeViewModel(get()) }

}

fun getFirebaseToken(): String? {
    return runBlocking {
        FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token
    }
}

fun getFCMToken(): String {
    var token: String = ""
    FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
        if (task.isSuccessful) {
             token = task.result
        }
    }
    return token
}