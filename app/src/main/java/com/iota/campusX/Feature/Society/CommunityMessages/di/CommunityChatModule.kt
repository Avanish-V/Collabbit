package com.iota.campusX.Feature.Society.CommunityMessages.di

import com.iota.campusX.Feature.Society.CommunityMessages.data.DataSource
import com.iota.campusX.Feature.Society.CommunityMessages.data.Repository.CommunityChatImpl
import com.iota.campusX.Feature.Society.CommunityMessages.domain.GroupChatInterface
import com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase.ObserveEnrichedMessagesUseCase
import com.iota.campusX.Feature.Society.CommunityMessages.domain.UseCase.SendMessageUseCase
import com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels.CommunityChatMenuViewModel
import com.iota.campusX.Feature.Society.CommunityMessages.presentation.ViewModels.CommunityChatViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val communityChatModule = module {
    single { SendMessageUseCase(get()) }
    single { ObserveEnrichedMessagesUseCase(get(), get()) }
    single { DataSource(get()) }
    single <GroupChatInterface>{ CommunityChatImpl(get(),get()) }
    viewModel {CommunityChatViewModel(get(),get(),get()) }
    viewModel { CommunityChatMenuViewModel() }

}