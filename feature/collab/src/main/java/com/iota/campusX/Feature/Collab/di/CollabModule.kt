package com.iota.campusX.Feature.Collab.di

import com.iota.campusX.Feature.Collab.data.remote.repository.CollabRepoImpl
import com.iota.campusX.Feature.Collab.domain.repository.CollabRepository
import com.iota.campusX.Feature.Collab.domain.usecase.CreateCollabUseCase
import com.iota.campusX.Feature.Collab.presentation.CollabViewModel
import com.iota.campusX.Feature.Collab.presentation.CreateCollabViewModel
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val collabModule = module {

    single<CollabRepository> { CollabRepoImpl(get()) }

    single { CreateCollabUseCase(get()) }

    viewModel { CollabViewModel(get<CollabRepository>(), get<CreateCollabUseCase>(), get<S3Uploader>()) }

    viewModel { CreateCollabViewModel(get<CreateCollabUseCase>(), get<S3Uploader>()) }
}
