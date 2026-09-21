package com.iota.campusX.Feature.ExploreSwipe.di

import com.iota.campusX.Feature.ExploreSwipe.data.remote.repository.SwipeMatchRepoImpl
import com.iota.campusX.Feature.ExploreSwipe.domain.repository.SwipeMatchRepository
import com.iota.campusX.Feature.ExploreSwipe.presentation.ExploreSwipeViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val exploreSwipeModule = module {
    single<SwipeMatchRepository> {
        SwipeMatchRepoImpl(httpClient = get())
    }

    viewModel {
        ExploreSwipeViewModel(
            repository = get(),
            userProfileRepository = get()
        )
    }
}
