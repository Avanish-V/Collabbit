package com.iota.campusX.Feature.Collab.di

import androidx.lifecycle.viewmodel.compose.viewModel
import com.iota.campusX.Feature.Collab.data.remote.repository.CollabRepoImpl
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Feature.Collab.presentation.CollabViewModel
import com.iota.campusX.Feature.Collab.presentation.CreateCollabViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collabModule = module {


    single<CollabRepository> { CollabRepoImpl(get()) }

    single { CreateCollabUseCase(get()) }

    viewModel { CollabViewModel(get(), get()) }

    viewModel { CreateCollabViewModel() }

}
