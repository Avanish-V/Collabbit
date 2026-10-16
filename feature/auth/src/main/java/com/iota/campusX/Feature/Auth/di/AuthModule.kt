package com.iota.campusX.Feature.Auth.di

import com.iota.campusX.Feature.Auth.data.datasource.AuthCredentialDataSource
import com.iota.campusX.Feature.Auth.data.datasource.CredentialAuthDataSourceImpl
import com.iota.campusX.Feature.Auth.data.repository.AuthRepositoryImpl
import com.iota.campusX.Feature.Auth.domain.repository.AuthRepository
import com.iota.campusX.Feature.Auth.domain.usecase.CheckAuthStateUseCase
import com.iota.campusX.Feature.Auth.domain.usecase.SignInWithGoogleUseCase
import com.iota.campusX.Feature.Auth.domain.usecase.SignOutUseCase
import com.iota.campusX.Feature.Auth.presentation.GoogleSignInViewModel
import com.iota.campusX.Feature.Auth.presentation.session.SessionViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val authModule = module {
    single<AuthCredentialDataSource> { CredentialAuthDataSourceImpl(context = androidContext()) }
    single<AuthRepository> {
        AuthRepositoryImpl(
            dataSource = get(),
            firebaseAuth = get(),
            httpClient = get(),
            userProfileDao = get()
        )
    }
    factory { SignInWithGoogleUseCase(repository = get()) }
    factory { SignOutUseCase(repository = get()) }
    factory { CheckAuthStateUseCase(repository = get()) }
    viewModel {
        GoogleSignInViewModel(
            signInWithGoogleUseCase = get(),
            signOutUseCase = get()
        )
    }
    single {
        SessionViewModel(
            checkAuthStateUseCase = get()
        )
    }
}
