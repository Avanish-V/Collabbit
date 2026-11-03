package com.iota.campusX.Feature.Notificattion.di

import androidx.room.Room
import com.iota.campusX.Feature.Notificattion.data.api.NotificationApi
import com.iota.campusX.Feature.Notificattion.data.local.NotificationDatabase
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationDao
import com.iota.campusX.Feature.Notificattion.data.local.dao.NotificationSyncDao
import com.iota.campusX.Feature.Notificattion.data.repository.NotificationRepositoryImpl
import com.iota.campusX.Feature.Notificattion.domain.repository.NotificationRepository
import com.iota.campusX.Feature.Notificattion.domain.usecase.*
import com.iota.campusX.Feature.Notificattion.presentation.NotificationViewModel
import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManager
import com.iota.campusX.Feature.Notificattion.sync.NotificationSyncManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val notificationModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            NotificationDatabase::class.java,
            "notifications.db"
        ).fallbackToDestructiveMigration().build()
    }

    single { get<NotificationDatabase>().notificationDao() }
    single { get<NotificationDatabase>().notificationSyncDao() }

    single { NotificationApi(get()) }

    single<NotificationRepository> {
        NotificationRepositoryImpl(
            api = get<NotificationApi>(),
            dao = get<NotificationDao>(),
            syncDao = get<NotificationSyncDao>(),
            database = get<NotificationDatabase>()
        )
    }

    single<NotificationSyncManager> {
        NotificationSyncManagerImpl(repository = get<NotificationRepository>())
    }

    factory { GetNotificationsUseCase(get()) }
    factory { GetUnreadCountUseCase(get()) }
    factory { MarkNotificationReadUseCase(get()) }
    factory { MarkAllNotificationReadUseCase(get()) }

    factory {
        NotificationUseCases(
            get(),
            get(),
            get(),
            get()
        )
    }

    viewModel {
        NotificationViewModel(
            useCases = get(),
            repository = get()
        )
    }
}
