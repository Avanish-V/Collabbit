package com.iota.campusX.Feature.Post.di

import androidx.room.Room
import com.iota.campusX.Feature.Post.Savers.LinkPreviewViewModel
import com.iota.campusX.Feature.Post.data.remote.repository.PostRemoteDataSource
import com.iota.campusX.Feature.Reply.data.remote.repository.ReplyRepoImpl
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePollUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreatePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.CreateReplyUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.DeletePostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.EditPostUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetChildRepliesUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostByIdUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetPostsUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.GetRepliesUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.ToggleLikeUseCase
import com.iota.campusX.Feature.Post.domain.UseCases.VotePollUseCase
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.Feature.Reply.domain.repository.ReplyRepository
import com.iota.campusX.Feature.Post.presentation.create.PostCreationViewModel
import com.iota.campusX.Feature.Post.presentation.edit.EditPostViewModel
import com.iota.campusX.Feature.Post.presentation.feed.PostFeedViewModel
import com.iota.campusX.Feature.Reply.ViewUserReplyViewModel
import com.iota.campusX.Feature.Post.data.local.database.CampusDatabase
import com.iota.campusX.Feature.Post.data.remote.api.PostApi
import com.iota.campusX.Feature.Post.data.remote.S3Uploader
import com.iota.campusX.Feature.Post.domain.UseCases.GetSinglePostByIdUseCase
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentHandler
import com.iota.campusX.Feature.Post.domain.attachment.AttachmentProcessor
import com.iota.campusX.Feature.Post.domain.attachment.ImageAttachmentHandler
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuActionViewModel
import com.iota.campusX.Feature.Post.presentation.feedmenu.MenuController
import com.iota.campusX.Feature.Post.presentation.feedmenu.PostMenuRepository
import com.iota.campusX.Feature.Post.presentation.feedmenu.PostMenuRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val postModule = module {

    single { S3Uploader(context = get(), httpClient = get(), auth = get()) }
    // Repositories
    factory<PostRepositoryInterface> { PostRemoteDataSource(httpClient = get(),get(),get(),get() ) }
    single<ReplyRepository> { ReplyRepoImpl( httpClient = get(), s3Uploader = get()) }


    single { PostApi(client = get()) }

    single { MenuController() }


    single {
        ImageAttachmentHandler(get())
    }

    single<AttachmentHandler> {
        get<ImageAttachmentHandler>()
    }

    single {
        AttachmentProcessor(
            handlers = listOf(
                get<ImageAttachmentHandler>()
            )
        )
    }
    // Use cases
    single { CreatePollUseCase(pollRepository = get()) }
    single { CreatePostUseCase(repository = get(),get(),get(),get()) }
    single { CreateReplyUseCase(repository = get()) }
    single { DeletePostUseCase(repository = get()) }
    single { EditPostUseCase(repository = get()) }
    single { GetSinglePostByIdUseCase(repository = get()) }
    single { GetPostByIdUseCase(repository = get()) }
    single { GetRepliesUseCase(repository = get()) }
    single { GetChildRepliesUseCase(repository = get()) }
    single { VotePollUseCase(pollRepository = get()) }
    single { ToggleLikeUseCase(repository = get()) }
    single { GetPostsUseCase(repository = get()) }

    // ViewModels
    viewModel { PostCreationViewModel(createPostUseCase = get(),get()) }
    viewModel { EditPostViewModel(editPostUseCase = get(), getSinglePostByIdUseCase = get(), postFeedViewModel = get()) }
    viewModel { ViewUserReplyViewModel(getRepliesUseCase = get(), createReplyUseCase = get(), replyRepository = get()) }
    viewModel { MenuActionViewModel(repository = get(), deletePostUseCase = get(), postFeedViewModel = get(), replyRepository = get()) }


    // Stateful "shared" viewmodels (use `single` carefully!)
    single { PostFeedViewModel(get(), get(), get(), get(), get()) }

    // factory { PostActionHandler(get(), get()) }
    // viewModel {
    // PostActionViewModel(get()) }

    // Post menu
    single<PostMenuRepository> { PostMenuRepositoryImpl() }

    single { LinkPreviewViewModel() }

    single {

        Room.databaseBuilder(

            androidContext(),

            CampusDatabase::class.java,

            "campusx.db"

        )
            .fallbackToDestructiveMigration()
            .build()
    }

    single {
        get<CampusDatabase>().postDao()
    }

    single {
        get<CampusDatabase>().remoteKeysDao()
    }
}