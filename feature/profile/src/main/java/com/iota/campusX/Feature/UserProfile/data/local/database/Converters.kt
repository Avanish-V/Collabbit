package com.iota.campusX.Feature.UserProfile.data.local.database

import androidx.room.TypeConverter
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Contact
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import com.iota.campusX.Feature.UserProfile.domain.Model.Gender
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



    // Campus
    @TypeConverter
    fun fromCampus(education: Education?): String? =
        education?.let { json.encodeToString(it) }

    @TypeConverter
    fun toCampus(value: String?): Education? =
        value?.takeIf { it.isNotEmpty() }?.let { json.decodeFromString<Education>(it) }

    @TypeConverter
    fun fromBaseProfile(baseProfile: BaseProfile?): String =
        json.encodeToString(baseProfile)

    fun toBaseProfile(value: String?): BaseProfile? =
        value?.takeIf { it.isNotEmpty() }?.let { json.decodeFromString<BaseProfile>(it) }

    @TypeConverter
    fun fromContact(contact: Contact?): String =
        json.encodeToString(contact)

    fun toContact(value: String?): Contact? =
        value?.takeIf { it.isNotEmpty() }?.let { json.decodeFromString<Contact>(it) }


    @TypeConverter
    fun fromGender(value: Gender?): String = value?.name ?: Gender.UNSPECIFIED.name

    @TypeConverter
    fun toGender(value: String?): Gender = value?.let { Gender.valueOf(it) } ?: Gender.UNSPECIFIED
}