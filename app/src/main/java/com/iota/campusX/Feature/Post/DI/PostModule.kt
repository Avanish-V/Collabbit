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
import com.iota.campusX.Feature.Post.data.remote.PostApi
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val postModule = module {
    single { PostValidator() }

    single { S3Uploader(context = get(), httpClient = get(), auth = get()) }
    // Repositories
    factory<PostRepositoryInterface> { PostRemoteDataSource(sendPushNotification = get(), auth = get(), firestore = get(), validator = get(), httpClient = get(), s3Uploader = get()) }
    single<ReplyRepositoryInterface> { ReplyRepoImpl(sendPushNotification = get(), notificationRepository = get(), auth = get(), firestore = get(), firebaseStorage = get(), httpClient = get(), s3Uploader = get()) }
    single {
        PostRepository(getPostsUseCase = get(), getCampusPostsUseCase = get(), postByIdUseCase = get(), deletePostUseCase = get(), editPostUseCase = get(), likeUseCase = get(), votePollUseCase = get(), postRepository = get())
    }

    single { PostApi(client = get(), auth = get()) }

    // Use cases
    single { CreatePollUseCase(pollRepository = get()) }
    single { CreatePostUseCase(repository = get()) }
    single { CreateReplyUseCase(repository = get()) }
    single { DeletePostUseCase(repository = get()) }
    single { EditPostUseCase(repository = get()) }
    single { GetSinglePostByIdUseCase(repository = get()) }
    single { GetPostByIdUseCase(repository = get()) }
    single { GetRepliesUseCase(repository = get()) }
    single { VotePollUseCase(pollRepository = get()) }
    single { ToggleLikeUseCase(repository = get()) }
    single { GetPostsUseCase(repository = get()) }
    single { GetCampusPostsUseCase(repository = get()) }

    // ViewModels
    single { PostCreationViewModel(createPostUseCase = get(), postRepository = get(), mediaManager = get(), sendPushNotification = get()) }
    factory { ViewUserPostViewModel(postRepository = get()) }
    viewModel { ViewUserReplyViewModel(getRepliesUseCase = get(), createReplyUseCase = get(), replyRepository = get()) }
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
    single<PostMenuRepository> { FakePostMenuRepository(postRepository = get(), replyRepository = get(), reportRepository = get()) }
    single { PostMenuState() }
    single { PostMenuViewModel(get()) }
    single { SharedVisualContentViewModel() }

    viewModel { LinkPreviewViewModel() }
}