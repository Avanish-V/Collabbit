package com.iota.campusX.Feature.Opportunities.di

import com.iota.campusX.Feature.Opportunities.data.remote.OpportunitiesRepositoryImpl
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository
import com.iota.campusX.Feature.Opportunities.domain.usecase.ApplyForOpportunityUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.EnrollInCourseUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCourseDetailUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCourseModulesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetCoursesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunitiesUseCase
import com.iota.campusX.Feature.Opportunities.domain.usecase.GetOpportunityDetailUseCase
import com.iota.campusX.Feature.Opportunities.presentation.CourseDetailViewModel
import com.iota.campusX.Feature.Opportunities.presentation.OpportunitiesViewModel
import com.iota.campusX.Feature.Opportunities.presentation.OpportunityDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val opportunitiesModule = module {
    single<OpportunitiesRepository> { OpportunitiesRepositoryImpl(get()) }
    single { GetOpportunitiesUseCase(get()) }
    single { GetOpportunityDetailUseCase(get()) }
    single { GetCoursesUseCase(get()) }
    single { GetCourseDetailUseCase(get()) }
    single { GetCourseModulesUseCase(get()) }
    single { EnrollInCourseUseCase(get()) }
    single { ApplyForOpportunityUseCase(get()) }

    viewModel {
        OpportunitiesViewModel(
            getOpportunitiesUseCase = get(),
            getCoursesUseCase = get()
        )
    }

    viewModel {
        OpportunityDetailViewModel(
            getOpportunityDetailUseCase = get(),
            applyForOpportunityUseCase = get(),
            userProfileRepository = get()
        )
    }

    viewModel {
        CourseDetailViewModel(
            getCourseDetailUseCase = get(),
            getCourseModulesUseCase = get(),
            enrollInCourseUseCase = get(),
            userProfileRepository = get()
        )
    }
}
