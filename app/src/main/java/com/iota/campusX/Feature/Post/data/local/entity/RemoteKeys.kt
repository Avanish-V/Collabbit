package com.iota.campusX.Feature.Post.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "remote_keys")
data class RemoteKeys(

    @PrimaryKey
    val id: String = "feed",

    val nextPage: Int?
)