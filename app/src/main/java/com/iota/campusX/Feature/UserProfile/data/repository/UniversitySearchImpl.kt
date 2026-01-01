package com.iota.campusX.Feature.UserProfile.data.repository

import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UniversityDTO
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Utils.UiState
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

class UniversitySearchImpl(private val httpClient: HttpClient) : UniversityRepository{

    override fun updateUniversity(title: String): Flow<UiState<List<UniversityDTO>>> = flow {

        emit(UiState.Loading)

        try {

            val response: HttpResponse = httpClient.get(
                "https://autocomplete.clearbit.com/v1/companies/suggest?query=$title"
            ) {
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }

            val bodyText = response.bodyAsText()

            val universities = Json.Default.decodeFromString<List<UniversityDTO>>(bodyText)

            emit(UiState.Success(universities))

        } catch (e: Exception) {

            emit(UiState.Error("Parsing error: ${e.localizedMessage}"))

        }

    }
}