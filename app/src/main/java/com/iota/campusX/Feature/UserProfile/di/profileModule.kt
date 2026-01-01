package com.iota.campusX.Feature.UserProfile.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.Notification.domain.NotificationRepository
import com.iota.campusX.Feature.Post.data.remote.PostApi
import com.iota.campusX.Feature.UserProfile.data.repository.UserProfileImpl
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
import com.iota.campusX.Feature.UserProfile.data.remote.api.ConnectionApi
import com.iota.campusX.Feature.UserProfile.data.repository.UniversitySearchImpl
import com.iota.campusX.Feature.UserProfile.data.repository.UserConnectionImpl
import com.iota.campusX.Feature.UserProfile.data.repository.UserProfileRepositoryData
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserConnectionsRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.domain.useCases.GetProfileUseCase
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.ConnectionRequestViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UpdateProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.UserProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.viewmodels.ViewProfileViewModel
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val profileModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }


    // DAO
    single { get<AppDatabase>().userProfileDao() }

    // Firebase Firestore
    single { FirebaseFirestore.getInstance() }

    // Kotlinx Serialization Json
    single { Json { ignoreUnknownKeys = true } }

    // Bind implementation to interface

    single<UserProfileRepository> {
        UserProfileImpl(
            sendPushNotification = get(),
            notificationRepositoryProvider = { get<NotificationRepository>() },
            firestore = get(),
            auth = get(),
            firebaseStorage = get(),
            httpClient = get(),
            userProfileDao = get()
        )
    }

    single { GetProfileUseCase(get()) }

    single { UserProfileViewModel(userProfileRepo = get(), userProfileRepository = get()) }
    single { UserProfileRepositoryData(userProfileRepo = get(), userProfileDao = get()) }
    single <UserConnectionsRepository>{ UserConnectionImpl(sendPushNotification = get(), notificationRepository = get(), firestore = get(), auth = get(), httpClient = get()) }
    single <UniversityRepository>{ UniversitySearchImpl(httpClient = get()) }
    single { ConnectionApi(client = get(), auth = get()) }
    viewModel { ViewProfileViewModel(userProfileRepo = get(), userProfileRepository = get()) }
    viewModel { ConnectionRequestViewModel(userProfileRepo = get(), notificationRepository = get() )}
    viewModel { UpdateProfileViewModel(userProfileRepo = get(), userProfileRepository = get(), universityRepository = get()) }
}