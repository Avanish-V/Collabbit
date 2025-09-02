package com.iota.campusX.Feature.Follow.di

import com.iota.campusX.Feature.Follow.data.FollowRepositoryImpl
import com.iota.campusX.Feature.Follow.domain.FollowRepositoryInterface
import com.iota.campusX.Feature.Follow.presentation.FollowersViewModel
import org.koin.dsl.module

val followModule = module {
    single <FollowRepositoryInterface>{ FollowRepositoryImpl(get(), get()) }
    single { FollowersViewModel(get()) }
}