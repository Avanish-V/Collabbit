package com.iota.campusX.Feature.Post.data.mapper

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.iota.campusX.Feature.Post.data.model.CreatePostDTO
import com.iota.campusX.Feature.Post.data.model.CreatorDetail
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.data.model.PostActions
import com.iota.campusX.Feature.Post.data.model.PostContent
import com.iota.campusX.Feature.Post.data.model.UserBasicDetail
import com.iota.campusX.Feature.Post.data.remote.visibilityMode
import com.iota.campusX.Feature.UserProfile.data.BaseProfileDTO
import com.iota.campusX.Navigation.isPollExpired
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await


class FirestorePagingSource(
    private val newsQuery: Query,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PagingSource<QuerySnapshot, GetPostDTO>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, GetPostDTO>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, GetPostDTO> {
        return try {
            val currentPage = params.key ?: newsQuery.get().await()

            // Build the list from current snapshot
            val data = fetchPosts(currentPage, firestore, auth)

            // If this page is empty OR less than requested load size → no more data
            val lastVisibleDoc = currentPage.documents.lastOrNull()
            val nextPage =
                if (lastVisibleDoc != null && currentPage.size() > 0) {
                    val nextSnapshot = newsQuery.startAfter(lastVisibleDoc).get().await()
                    if (nextSnapshot.isEmpty) null else nextSnapshot
                } else {
                    null
                }

            LoadResult.Page(
                data = data,
                prevKey = null,
                nextKey = nextPage
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

}

private suspend fun fetchPosts(
    postsSnapshot: QuerySnapshot,
    firestore: FirebaseFirestore,
    auth: FirebaseAuth
): List<GetPostDTO> = coroutineScope {
    postsSnapshot.documents.mapNotNull { doc ->
        async {
            val post = doc.toObject(CreatePostDTO::class.java) ?: return@async null

            val userDeferred = async {
                firestore.collection("Users")
                    .document(post.creatorId)
                    .get()
                    .await()
                    .toObject(BaseProfileDTO::class.java)
            }

            val likesDeferred = async {
                firestore.collection("Posts")
                    .document(post.postId)
                    .collection("Likes")
                    .document(post.postId)
                    .get()
                    .await()
                    .get("likes") as? List<String> ?: emptyList()

            }

            val repliesCountDeferred = async {
                firestore.collection("Posts")
                    .document(post.postId)
                    .collection("Replies")
                    .get()
                    .await()
                    .size() // only count
            }

            val isFollowDeferred = async {
                firestore.collection("Users")
                    .document(post.creatorId)
                    .collection("Followers")
                    .document(auth.currentUser?.uid ?: "")
                    .get()
                    .await()
                    .exists()
            }

            val user = userDeferred.await()
            val likes = likesDeferred.await()
            val repliesCount = repliesCountDeferred.await()
            val isFollow = isFollowDeferred.await()

            val isAlumni = if (!user?.campus?.duration?.start.isNullOrEmpty() && !user.campus.duration.end.isNullOrEmpty()){
                user.campus.duration.endTimestamp?.let { if (it < System.currentTimeMillis()) true else false }
            }else false

            val isLiked = auth.currentUser?.uid in likes
            val isCurrentUser = auth.currentUser?.uid == post.creatorId

            val profile = visibilityMode(
                post.visibilityMode,
                userName = user?.userName ?: "",
                userImage = user?.userImage ?: ""
            )

            val poll = post.poll?.copy(
                hasVoted = post.poll.votes.any { it.userId == auth.currentUser?.uid },
                isActive = isPollExpired(
                    createdAt = post.createdAt.toDate().time,
                ),
                selectedOptionId = post.poll.votes.firstOrNull { it.userId == auth.currentUser?.uid }?.optionId
            )

            GetPostDTO(
                postId = post.postId,
                createdAt = post.createdAt,
                creatorDetail = CreatorDetail(
                    isCurrentUser = isCurrentUser,
                    isVerified = user?.metaData?.verified ?: false,
                    isPremium = user?.metaData?.premium ?: false,
                    isAlumni = isAlumni?:false,
                    isFollow = isFollow,
                    profile = UserBasicDetail(
                        id = post.creatorId,
                        userName = profile.first,
                        userImage = profile.second,
                        userBio = user?.userBio ?: ""
                    )
                ),
                campusId = post.campusId,
                feedMode = post.feedMode,
                reference = post.reference,
                visibilityMode = post.visibilityMode,
                postContent = PostContent(
                    postText = post.postText,
                    postImage = post.image,
                    poll = poll
                ),
                postActions = PostActions(
                    isLiked = isLiked,
                    likesCount = likes.size,
                    replies = emptyList(),
                    replyCount = repliesCount
                ),
                type = post.type,
                mediaType = post.mediaType
            )
        }
    }.awaitAll().filterNotNull()
}

