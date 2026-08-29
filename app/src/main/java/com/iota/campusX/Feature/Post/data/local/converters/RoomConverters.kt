package com.iota.campusX.Feature.Post.data.local.converters

import androidx.room.TypeConverter
import com.google.android.gms.auth.api.Auth
import com.iota.campusX.Feature.Post.domain.models.AuthorDetails
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class RoomConverters {

    @TypeConverter
    fun authorToJson(author: AuthorDetails): String {

        return Json.encodeToString(author)

    }

    @TypeConverter
    fun jsonToAuthor(json: String): AuthorDetails {

        return Json.decodeFromString(json)

    }

}