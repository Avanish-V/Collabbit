package com.iota.campusX.Feature.UserProfile.data.local.database

import androidx.room.TypeConverter
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Counts
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Gender
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.MetaData
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
        if (value.isNotEmpty()) json.decodeFromString(value) else emptyList()

    // MetaData
    @TypeConverter
    fun fromMetaData(meta: MetaData?): String =
        json.encodeToString(meta ?: MetaData())

    @TypeConverter
    fun toMetaData(value: String): MetaData =
        if (value.isNotEmpty()) json.decodeFromString(value) else MetaData()

    // Campus
    @TypeConverter
    fun fromCampus(campus: Campus?): String? =
        campus?.let { json.encodeToString(it) }

    @TypeConverter
    fun toCampus(value: String?): Campus? =
        value?.takeIf { it.isNotEmpty() }?.let { json.decodeFromString<Campus>(it) }


    // Counts
    @TypeConverter
    fun fromCounts(counts: Counts?): String =
        json.encodeToString(counts ?: Counts())

    @TypeConverter
    fun toCounts(value: String): Counts =
        if (value.isNotEmpty()) json.decodeFromString(value) else Counts()

    @TypeConverter
    fun fromGender(value: Gender?): String = value?.name ?: Gender.UNSPECIFIED.name

    @TypeConverter
    fun toGender(value: String?): Gender = value?.let { Gender.valueOf(it) } ?: Gender.UNSPECIFIED
}