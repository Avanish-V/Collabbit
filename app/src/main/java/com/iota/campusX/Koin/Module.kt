package com.iota.campusX.Koin

import ConsentAgreeViewModel
import SendPushNotification
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.AuthViewModel
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleAuthRepo
import com.iota.campusX.Authentication.GoogleAuthentication.GoogleAuthentication.GoogleAuthUiClient
import com.iota.campusX.Feature.Chats.data.ChatImpl
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Notification.data.NotificationImpl
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Notification.presentation.NotificationViewModel
import com.iota.campusX.Feature.Post.data.PostRepoImpl
import com.iota.campusX.Feature.Post.domain.PostRepository
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetCampusPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.VotePollUseCase
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ReplyViewModel
import com.iota.campusX.Feature.UserProfile.data.UserProfileImpl
import com.iota.campusX.Feature.UserProfile.domain.UserProfileRepo
import com.iota.campusX.Feature.UserProfile.presentation.UserProfileViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.Screens.Home.HomeViewModel
import com.iota.campusX.Screens.Home.dataStore
import com.iota.campusX.Screens.Post.PollViewModel
import com.iota.campusX.Screens.Post.PostScreenViewModel
import com.iota.campusX.Utils.ServerTimeStampViewModel
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

@RequiresApi(Build.VERSION_CODES.O)
val appModule = module {

    // -------------------------------
    // HttpClient Setup
    // -------------------------------
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
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
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
                // Base URL can go here if needed
            }
        }
    }

    // -------------------------------
    // Firebase Services
    // -------------------------------
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }
    single { FirebaseStorage.getInstance() }

    // -------------------------------
    // Repositories
    // -------------------------------
    single<UserProfileRepo> {
        UserProfileImpl(
            sendPushNotification = get(),
            firestore = get(),
            auth = get(),
            firebaseStorage = get(),
            httpClient = get(),
        )
    }
    single<GoogleAuthRepo> {
        GoogleAuthUiClient(androidContext(), get(), get())
    }
    single<PostRepository> { PostRepoImpl(get(), get(), get()) }
    single<ChatRepository> { ChatImpl(get(), get(), get(), get()) }
    single<NotificationRepository> { NotificationImpl(get(), get(), get()) }

    single<DataStore<Preferences>> {
        androidContext().dataStore
    }

    // -------------------------------
    // Push Notification
    // -------------------------------
    single { SendPushNotification(get(), get()) }

    // -------------------------------
    // UseCases for Post Feature
    // -------------------------------
    single { CreatePollUseCase(get()) }
    single { CreatePostUseCase(get()) }
    single { CreateReplyUseCase(get()) }
    single { DeletePostUseCase(get()) }
    single { EditPostUseCase(get()) }
    single { GetPostByIdUseCase(get()) }
    single { GetRepliesUseCase(get()) }
    single { VotePollUseCase(get()) }

    // GetPostsUseCase was missing
    single { GetPostsUseCase(get()) }
    single { GetCampusPostsUseCase(get()) }

    // -------------------------------
    // ViewModels
    // -------------------------------

    viewModel {
        ReplyViewModel(
            getRepliesUseCase = get(),
            createReplyUseCase = get(),
            postRepository = get()
        )
    }
    viewModel {
        PostFeedViewModel(
            postRepository = get(),
            deletePostUseCase = get(),
            getPostsUseCase = get(),
            editPostUseCase = get(),
            getCampusPostsUseCase = get(),
            votePollUseCase = get(),
            postByIdUseCase = get()
        )
    }

    viewModel {
        PostCreationViewModel(
            createPostUseCase = get(),
            createPollUseCase = get(),
            feedViewModel = get()
        )
    }

    viewModel { AuthViewModel(get()) }
    viewModel { UserProfileViewModel(get()) }
    viewModel { ChatsViewModel(get()) }
    viewModel { NavigationViewModel() }
    viewModel { NotificationViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { ServerTimeStampViewModel(get()) }
    viewModel { PollViewModel() }
    viewModel { PostScreenViewModel() }
    viewModel { ConsentAgreeViewModel(get()) }
}


// Prefer using suspend or callback to avoid blocking
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
