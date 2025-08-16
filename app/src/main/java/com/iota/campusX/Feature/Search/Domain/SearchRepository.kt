package com.iota.campusX.Feature.Search.Domain

import com.iota.campusX.Feature.Search.Domain.Models.UserSearchDTO
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    fun userSearch(query: String): Flow<Result<List<UserSearchDTO>>>

}