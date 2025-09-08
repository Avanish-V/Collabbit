package com.iota.campusX.Feature.Post.DI

import com.iota.campusX.Feature.Post.Savers.LinkPreviewViewModel
import com.iota.campusX.Feature.Post.data.remote.PostRemoteDataSource
import com.iota.campusX.Feature.Post.data.remote.ReplyRepoImpl
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetCampusPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.ToggleLikeUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.VotePollUseCase
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Post.domain.repository.ReplyRepositoryInterface
import com.iota.campusX.Feature.Post.presentation.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.PostFeedViewModel
import com.iota.campusX.Feature.Post.presentation.ViewUserPostViewModel
import com.iota.campusX.Feature.Post.presentation.ViewUserReplyViewModel
import com.iota.campusX.Feature.Reply.ReplyViewModel
import com.iota.campusX.Navigation.AppNavigator
import com.iota.campusX.Screens.Post.PollViewModel
import com.iota.campusX.Screens.Post.PostActions.PostActionHandler
import com.iota.campusX.Screens.Post.PostActions.PostActionViewModel
import com.iota.campusX.Screens.Post.PostMenuActions.FakePostMenuRepository
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuRepository
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuState
import com.iota.campusX.Screens.Post.PostMenuActions.PostMenuViewModel
import com.iota.campusX.Screens.Post.PostScreenViewModel
import com.iota.campusX.Screens.Post.SharedVisualContentViewModel
import com.iota.campusX.Feature.Post.Validators.PostValidator
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val postModule = module {
    single { PostValidator() }
    // Repositories
    single<PostRepositoryInterface> { PostRemoteDataSource(get(), get(), get(), get(),get()) }
    single<ReplyRepositoryInterface> { ReplyRepoImpl(get(), get(), get(),get()) }
    single {
        PostRepository(get(), get(), get(), get(), get(), get(), get(), get())
    }

    // Use cases
    single { CreatePollUseCase(get()) }
    single { CreatePostUseCase(get()) }
    single { CreateReplyUseCase(get()) }
    single { DeletePostUseCase(get()) }
    single { EditPostUseCase(get()) }
    single { GetPostByIdUseCase(get()) }
    single { GetRepliesUseCase(get()) }
    single { VotePollUseCase(get()) }
    single { ToggleLikeUseCase(get()) }
    single { GetPostsUseCase(get()) }
    single { GetCampusPostsUseCase(get()) }

    // ViewModels
    single { PostCreationViewModel(get(), get(), get(),get(),get()) }
    viewModel { ReplyViewModel(get()) }
    factory { ViewUserPostViewModel(get()) }
    viewModel { ViewUserReplyViewModel(get(), get(), get()) }
    viewModel { PollViewModel() }
    viewModel { PostScreenViewModel() }

    // Stateful "shared" viewmodels (use `single` carefully!)
    single { PostFeedViewModel(get()) }

    factory { (appNavigator: AppNavigator) ->
        PostActionHandler(
            context = get(),
            navHostController = appNavigator,
            replyRepository = get(),
            sharedVisualContentViewModel = get(),
            repository = get(),
            followRepositoryInterface = get()
        )
    }

    single { (appNavigator: AppNavigator) ->
        PostActionViewModel(
            PostActionHandler(
                context = get(),
                navHostController = appNavigator,
                replyRepository = get(),
                sharedVisualContentViewModel = get(),
                repository = get(),
                followRepositoryInterface = get()
            )
        )
    }

    // factory { PostActionHandler(get(), get()) }
    // viewModel { PostActionViewModel(get()) }

    // Post menu
    single<PostMenuRepository> { FakePostMenuRepository(get(), get(), get()) }
    single { PostMenuState() }
    single { PostMenuViewModel(get()) }
    single { SharedVisualContentViewModel() }

    viewModel { LinkPreviewViewModel() }
}