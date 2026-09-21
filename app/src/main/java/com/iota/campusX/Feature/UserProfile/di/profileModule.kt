package com.iota.campusX.Feature.UserProfile.di

import androidx.room.Room
import com.google.firebase.firestore.FirebaseFirestore
import com.iota.campusX.Feature.UserProfile.data.local.database.AppDatabase
import com.iota.campusX.Feature.UserProfile.data.local.database.MIGRATION_3_4
import com.iota.campusX.Feature.UserProfile.data.local.database.MIGRATION_4_5
import com.iota.campusX.Feature.UserProfile.data.local.database.MIGRATION_6_7
import com.iota.campusX.Feature.UserProfile.data.local.database.MIGRATION_7_8
import com.iota.campusX.Feature.UserProfile.data.remote.repository.AuraRepositoryImpl
import com.iota.campusX.Feature.UserProfile.data.remote.repository.UniversitySearchImpl
import com.iota.campusX.Feature.UserProfile.data.remote.repository.UserProfileImpl
import com.iota.campusX.Feature.UserProfile.domain.repository.AuraRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Feature.UserProfile.domain.useCases.EditBaseProfileUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.GetProfileUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.ObserveProfileUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.RecordCheckInUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.UpdateFcmTokenUseCase
import com.iota.campusX.Feature.UserProfile.domain.useCases.UpdateOpenToUseCase
import com.iota.campusX.Feature.UserProfile.domain.usecase.ClaimDailyAuraUseCase
import com.iota.campusX.Feature.UserProfile.domain.usecase.GetAuraInfoUseCase
import com.iota.campusX.Feature.UserProfile.domain.usecase.ObserveAuraTransactionsUseCase
import com.iota.campusX.Feature.UserProfile.presentation.AuraViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditBaseProfile.EditBaseProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditEducation.EditEducationViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditOpenTo.EditOpenToViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSkills.EditSkillsViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSummary.EditSummaryViewModel
import com.iota.campusX.Feature.UserProfile.ui.screens.ProfileMain.UserProfileViewModel
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
            .addMigrations(MIGRATION_3_4, MIGRATION_4_5, MIGRATION_6_7, MIGRATION_7_8)
            .fallbackToDestructiveMigration()
            .build()
    }

    single { UpdateFcmTokenUseCase(get()) }

    // DAO
    single { get<AppDatabase>().userProfileDao() }
    single { get<AppDatabase>().auraTransactionDao() }

    // Firebase Firestore
    single { FirebaseFirestore.getInstance() }

    // Kotlinx Serialization Json
    single { Json { ignoreUnknownKeys = true } }

    // Repository bindings
    single<UserProfileRepository> {
        UserProfileImpl(
            firestore      = get(),
            auth           = get(),
            httpClient     = get(),
            userProfileDao = get()
        )
    }

    single<AuraRepository> {
        AuraRepositoryImpl(
            httpClient     = get(),
            userProfileDao = get(),
            auraTransactionDao = get()
        )
    }

    single<UniversityRepository> { UniversitySearchImpl(httpClient = get()) }

    // Use cases
    single { ObserveProfileUseCase(get()) }
    single { GetProfileUseCase(get()) }
    single { EditBaseProfileUseCase(get(), get()) }
    single { UpdateOpenToUseCase(get()) }
    single { RecordCheckInUseCase(get()) }
    single { ClaimDailyAuraUseCase(get()) }
    single { GetAuraInfoUseCase(get()) }
    single { ObserveAuraTransactionsUseCase(get()) }

    // ViewModels
    viewModel {
        UserProfileViewModel(
            observeProfile    = get(),
            getProfileUseCase = get(),
            profileRepository = get()
        )
    }

    viewModel { 
        AuraViewModel(
            claimDailyAuraUseCase = get(),
            getAuraInfoUseCase = get(),
            observeAuraTransactionsUseCase = get(),
            observeProfile = get()
        )
    }

    single { EditProfileViewModel() }

    viewModel { EditEducationViewModel(get(), get()) }
    viewModel { EditBaseProfileViewModel(get()) }
    viewModel { EditSkillsViewModel(get(), get()) }
    viewModel { EditOpenToViewModel(get()) }
    viewModel { EditSummaryViewModel(get()) }
}
