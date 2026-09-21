package com.iota.campusX.Feature.UserProfile.data.remote.repository

import android.util.Log
import com.iota.campusX.Feature.UserProfile.data.remote.response.College
import com.iota.campusX.Feature.UserProfile.data.remote.response.CollegeResponse
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class UniversitySearchImpl(private val httpClient: HttpClient) : UniversityRepository {

    override fun updateUniversity(title: String): Flow<CollegeResponse> = flow {
        try {
            Log.d("UniversitySearch", "updateUniversity: $title")

            val response: HttpResponse = httpClient.get(
                "https://autocomplete.clearbit.com/v1/companies/suggest?query=$title"
            ) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }

            Log.d("UniversitySearch", "updateUniversity: ${response.status}")

            // Clearbit API returns a JSON array [{}, {}]
            val colleges = response.body<List<College>>()
            Log.d("UniversitySearch", "updateUniversity success: $colleges")

            emit(CollegeResponse(college = colleges))

        } catch (e: Exception) {
            Log.d("UniversitySearch", "updateUniversity: $e")
            throw e // Let the VM's catch operator handle it
        }
    }

    override fun searchKeySkills(query: String): Flow<List<SkillResponse>> = flow {
        try {
            val response: HttpResponse = httpClient.get("skills/search") {
                parameter("query", query)
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }
            val skills = response.body<List<SkillResponse>>()
            emit(skills)
        } catch (e: Exception) {
            throw e
        }
    }
}
