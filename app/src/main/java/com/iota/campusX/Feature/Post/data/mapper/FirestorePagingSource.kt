package com.iota.campusX.Feature.Post.data.mapper


import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.FeedMode
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.remote.PostApi

class PostsPagingSource(
    private val api: PostApi,
    private val feedMode: FeedMode?= null,
    private val campusId: String?= null,
    private val userId: String?= null,
) : PagingSource<Int, GetPostDTO>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GetPostDTO> {

        val fixedPageSize = 10

        val page = params.key ?: 0

        return try {


            val result = if (feedMode != null){
                api.getPosts(feedMode, campusId, page, fixedPageSize)
            }else{
                api.getPostByUserId(userId, page,fixedPageSize)
            }

            result.fold(
                onSuccess = { pageResponse ->
                    val data = pageResponse.content.map { post ->
                        // Convert to GetPostDTO (your existing mapping logic)
                        GetPostDTO(
                            postId = post.postId.toString(),
                            creatorDetail = CreatorDetail(
                                profile = UserBasicDetail(
                                    id = post.authorDetails?.authorId.orEmpty(),
                                    name = post.authorDetails?.authorName.orEmpty(),
                                    tagline = post.authorDetails?.authorTagline.orEmpty(),
                                    image = post.authorDetails?.authorImage.orEmpty()
                                ),
                                isCurrentUser = post.authorDetails?.isCurrentUser ?:false
                            ),
                            createdAt = post.createdAt,
                            visibilityMode = post.visibility,
                            type = post.postType,
                            feedMode = post.feedMode,
                            postContent = PostContent(
                                postText = post.text,
                                postImage = post.mediaPost
                            ),
                            postActions = PostActions(
                                isLiked = post.isLiked,
                                likesCount = post.likes,
                                replyCount = post.comments
                            ),
                        )
                    }

                    LoadResult.Page(
                        data = data,
                        prevKey = if (page == 0) null else page - 1,
                        nextKey = if (pageResponse.last) null else page + 1
                    )
                },
                onFailure = { LoadResult.Error(it) }
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, GetPostDTO>): Int? {
        return state.anchorPosition?.let { position ->
            val anchorPage = state.closestPageToPosition(position)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}



