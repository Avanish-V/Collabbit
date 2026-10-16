package com.iota.campusX.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CommonConverters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        return value?.let { json.encodeToString(it) } ?: "[]"
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrEmpty()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String {
        return value?.let { json.encodeToString(it) } ?: "{}"
    }

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> {
        if (value.isNullOrEmpty()) return emptyMap()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
