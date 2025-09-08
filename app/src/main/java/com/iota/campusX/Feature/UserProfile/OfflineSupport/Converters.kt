package com.iota.campusX.Feature.UserProfile.OfflineSupport

import androidx.room.TypeConverter
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Counts
import com.iota.campusX.Feature.UserProfile.data.MetaData
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    // List<String>
    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String): List<String> =
        json.decodeFromString(value)

    // MetaData
    @TypeConverter
    fun fromMetaData(meta: MetaData?): String =
        meta?.let { json.encodeToString(it) } ?: ""

    @TypeConverter
    fun toMetaData(value: String): MetaData =
        if (value.isNotEmpty()) json.decodeFromString(value) else MetaData()

    // Campus
    @TypeConverter
    fun fromCampus(campus: Campus?): String =
        campus?.let { json.encodeToString(it) } ?: ""

    @TypeConverter
    fun toCampus(value: String): Campus? =
        if (value.isNotEmpty()) json.decodeFromString(value) else null

    // Counts
    @TypeConverter
    fun fromCounts(counts: Counts?): String =
        counts?.let { json.encodeToString(it) } ?: ""

    @TypeConverter
    fun toCounts(value: String): Counts? =
        if (value.isNotEmpty()) json.decodeFromString(value) else null
}
