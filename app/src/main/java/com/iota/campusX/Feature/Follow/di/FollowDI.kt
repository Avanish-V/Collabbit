package com.iota.campusX.Feature.Follow.di

import androidx.lifecycle.viewmodel.compose.viewModel
import com.iota.campusX.Feature.Follow.data.FollowRepositoryImpl
import com.iota.campusX.Feature.Follow.domain.FollowRepositoryInterface
import com.iota.campusX.Feature.Follow.presentation.FollowersViewModel
import com.iota.campusX.Feature.Reply.ReplyViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val followModule = module {
    single <FollowRepositoryInterface>{ FollowRepositoryImpl(get(), get()) }
    viewModel { FollowersViewModel(get()) }
}