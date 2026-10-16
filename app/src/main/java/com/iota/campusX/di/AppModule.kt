package com.iota.campusX.Koin

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.iota.campusX.Feature.Chats.data.ChatImpl
import com.iota.campusX.Feature.Chats.data.local.ChatDao
import com.iota.campusX.Feature.Chats.data.local.ChatDatabase
import com.iota.campusX.Feature.Chats.domain.ChatRepository
import com.iota.campusX.Feature.Chats.presentation.ChatsViewModel
import com.iota.campusX.Feature.Reply.presentation.ReplyViewModel
import com.iota.campusX.Feature.Report.data.ReportRepoImpl
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Feature.Report.presentation.ReportViewModel
import com.iota.campusX.Feature.Society.data.local.SocietyDatabase
import com.iota.campusX.Feature.Society.data.remote.FirestoreCommunityDataSource
import com.iota.campusX.Feature.Society.data.repository.CommunityRepositoryImpl
import com.iota.campusX.Feature.Society.domain.repository.CommunityRepository
import com.iota.campusX.Feature.Society.domain.usecase.CreateCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.DeleteCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCommunitiesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.GetCurrentUserIdUseCase
import com.iota.campusX.Feature.Society.domain.usecase.JoinCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.LeaveCommunityUseCase
import com.iota.campusX.Feature.Society.domain.usecase.ListenToMessagesUseCase
import com.iota.campusX.Feature.Society.domain.usecase.MessageOperationsUseCase
import com.iota.campusX.Feature.Society.domain.usecase.PresenceUseCase
import com.iota.campusX.Feature.Society.domain.usecase.ResolveUserUseCase
import com.iota.campusX.Feature.Society.domain.usecase.SendSocietyMessageUseCase
import com.iota.campusX.Feature.Society.domain.usecase.UpdateCommunityUseCase
import com.iota.campusX.Feature.Society.presentation.SocietyChatViewModel
import com.iota.campusX.Feature.Society.presentation.SocietyHubViewModel
import com.iota.campusX.Feature.Society.presentation.SocietyInfoViewModel
import com.iota.campusX.Navigation.NavigationViewModel
import com.iota.campusX.core.network.websocket.dispatcher.SocketEventDispatcher
import com.iota.campusX.core.network.websocket.dispatcher.SocketEventDispatcherImpl
import com.iota.campusX.core.network.websocket.handler.SocketEventHandler
import com.iota.campusX.core.network.websocket.manager.RealtimeManager
import com.iota.campusX.core.network.websocket.manager.RealtimeManagerImpl
import com.iota.campusX.core.network.websocket.socket.RealtimeSocketManager
import com.iota.campusX.core.network.websocket.socket.RealtimeSocketManagerImpl
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


val coreModule = module {
    single {
        com.iota.campusX.core.network.NetworkConfig(
            baseUrl = com.iota.campusX.BuildConfig.BASE_URL,
            opportunitiesBaseUrl = com.iota.campusX.BuildConfig.OPPORTUNITIES_BASE_URL,
            coursesBaseUrl = com.iota.campusX.BuildConfig.COURSES_BASE_URL,
            isProduction = com.iota.campusX.BuildConfig.IS_PRODUCTION
        )
    }
    includes(com.iota.campusX.core.network.di.networkModule)

    // Firebase services
    single { FirebaseFirestore.getInstance() }
    single { FirebaseDatabase.getInstance() }
    single { FirebaseAuth.getInstance() }


    // DataStore
    single<DataStore<Preferences>> { androidContext().dataStore }
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
    single { FirestoreCommunityDataSource(get(), get(), get()) }
    single<CommunityRepository> { CommunityRepositoryImpl(get(), get(), get(), get(), get()) }
    
    factory { CreateCommunityUseCase(get()) }
    factory { GetCommunitiesUseCase(get()) }
    factory { JoinCommunityUseCase(get()) }
    factory { GetCurrentUserIdUseCase(get()) }
    factory { UpdateCommunityUseCase(get()) }
    factory { LeaveCommunityUseCase(get()) }
    factory { DeleteCommunityUseCase(get()) }
    factory { ListenToMessagesUseCase(get()) }
    factory { SendSocietyMessageUseCase(get()) }
    factory { MessageOperationsUseCase(get()) }
    factory { PresenceUseCase(get()) }
    factory { ResolveUserUseCase(get()) }

    viewModel { SocietyHubViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { SocietyChatViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    viewModel { SocietyInfoViewModel(get(), get(), get(), get(), get(), get(), get()) }
}

val firebaseModule = module {

    single<FirebaseFirestore> {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false) // 🔹 Disable offline persistence
            .build()
        firestore.firestoreSettings = settings
        firestore
    }
}

val realtimeModule = module {
    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
        }
    }

    single<com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager> {
        com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManagerImpl(get())
    }

    single<SocketEventHandler> {
        com.iota.campusX.core.notifications.websocket.NotificationEventHandler(
            syncManager = get(),
            json = get()
        )
    }

    single<SocketEventDispatcher> {
        SocketEventDispatcherImpl(
            handlers = getAll()
        )
    }

    single<RealtimeSocketManager> {
        RealtimeSocketManagerImpl(
            client = get(),
            dispatcher = get(),
            json = get(),
            auth = get()
        )
    }

    single<RealtimeManager> {
        RealtimeManagerImpl(
            socketManager = get()
        )
    }
}
