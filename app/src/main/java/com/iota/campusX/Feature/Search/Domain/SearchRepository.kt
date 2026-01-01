package com.iota.campusX.Feature.Search.Domain

import androidx.paging.PagingData
import com.iota.campusX.Feature.Post.data.model.GetPostDTO
import com.iota.campusX.Feature.Search.Data.SearchResponse
import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun userSearch(query: String): Flow<PagingData<SearchResponse>>

    fun postSearch(query: String): Flow<PagingData<GetPostDTO>>

}