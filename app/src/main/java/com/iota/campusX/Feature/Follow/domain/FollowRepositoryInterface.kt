package com.iota.campusX.Feature.Follow.domain

import com.iota.campusX.Feature.UserProfile.data.ConnectionsDTO

interface FollowRepositoryInterface {
    suspend fun follow(userId: String): Result<Boolean>
    suspend fun unfollow(userId: String): Result<Boolean>
    suspend fun getFollowers(userId: String): Result<List<ConnectionsDTO>>
}