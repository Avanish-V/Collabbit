package com.iota.campusX.Screens.Post.PostMenuActions

import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.Reply.ReplyRepository
import com.iota.campusX.Feature.Report.domain.ReportRepository
import com.iota.campusX.Screens.Post.DataModel.ContentId
import com.iota.campusX.Screens.Post.DataModel.FeedContent
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import com.iota.campusX.ui.UIComponents.ReportReason
import kotlinx.coroutines.delay

interface PostMenuRepository {
    suspend fun getMenuOptions(content: FeedContent): List<MenuAction>
    suspend fun executeAction(action: MenuAction,content: FeedContent,reportReason: ReportReason?): Result<Unit>
}

class FakePostMenuRepository (
    private val postRepository: PostRepository,
    private val replyRepository: ReplyRepository,
    private val reportRepository: ReportRepository,
): PostMenuRepository {

    override suspend fun getMenuOptions(content: FeedContent): List<MenuAction> {
        return if (content.isOwner) {
            listOf(MenuAction.Edit, MenuAction.Delete,)
        } else {
            listOf(MenuAction.Report,)
        }
    }

    override suspend fun executeAction(action: MenuAction,content: FeedContent,reportReason: ReportReason?): Result<Unit> {
        delay(300) // simulate network latency
        return try {
            when (action) {
                MenuAction.Delete -> {

                  when(val id = content.id){
                      is ContentId.Post -> {
                           postRepository.deletePost(postId = id.postId)
                      }
                      is ContentId.Reply -> {
                          return replyRepository.deleteReply(replyId = id.replyId, postId = id.postId)
                      }
                  }

                }
                MenuAction.Edit -> {

                    when(val id = content.id){
                        is ContentId.Post -> {
                           postRepository.editPost(postId = id.postId, newText = content.text)
                        }
                        is ContentId.Reply -> {
                            return replyRepository.editReply(replyId = id.replyId, postId = id.postId,content = content.text)
                        }
                    }

                }

                MenuAction.Report -> {

                    when(val id = content.id){
                        is ContentId.Post -> {
                            reportReason?.let {
                               val result =  reportRepository.createReportOnPost(
                                    reportReason = it,
                                    postId = id.postId,
                                )
                                return result
                            }
                        }
                        is ContentId.Reply -> {

                        }
                    }

                }

            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
