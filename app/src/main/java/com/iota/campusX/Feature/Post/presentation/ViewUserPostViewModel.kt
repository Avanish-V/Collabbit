package com.iota.campusX.Feature.Post.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Post.domain.repository.PostRepository
import com.iota.campusX.Feature.Post.domain.repository.PostRepositoryInterface
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ViewUserPostViewModel(
    private val postRepository: PostRepository
) : ViewModel() {

    val viewUserPost: StateFlow<PagingData<GetPostDTO>> = postRepository.viewUserPost

    fun fetchViewUserPosts(userId: String) = viewModelScope.launch { postRepository.getViewUserPost(userId,viewModelScope) }

}
