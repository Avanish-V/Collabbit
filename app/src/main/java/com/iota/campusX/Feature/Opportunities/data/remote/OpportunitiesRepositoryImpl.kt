package com.iota.campusX.Feature.Opportunities.data.remote

 import android.util.Log
 import com.iota.campusX.Feature.Opportunities.data.model.CourseEnrollmentRequest
import com.iota.campusX.Feature.Opportunities.data.model.CourseResponse
import com.iota.campusX.Feature.Opportunities.data.model.ModuleResponse
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityApplicationRequest
import com.iota.campusX.Feature.Opportunities.data.model.OpportunityResponse
import com.iota.campusX.Feature.Opportunities.domain.repository.OpportunitiesRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import com.iota.campusX.Koin.AppConstants
 import com.iota.campusX.Utils.ErrorResponse
 import io.ktor.client.plugins.ClientRequestException
 import io.ktor.client.statement.bodyAsText

class OpportunitiesRepositoryImpl(
    private val httpClient: HttpClient
) : OpportunitiesRepository {

    private val baseUrl = AppConstants.OPPORTUNITIES_BASE_URL
    private val coursesUrl = AppConstants.COURSES_BASE_URL


    override suspend fun getOpportunities(): Result<List<OpportunityResponse>> {
        return try {
            val response = httpClient.get(baseUrl)

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch opportunities: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getOpportunityById(id: String): Result<OpportunityResponse> {
        return try {
            val response = httpClient.get("$baseUrl/$id") {

            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch opportunity details: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourses(): Result<List<CourseResponse>> {
        return try {
            val response = httpClient.get(coursesUrl)
            if (response.status == HttpStatusCode.OK) {
                Log.e("OpportunitiesRepositoryImpl", "Failed to fetch courses: ${response.bodyAsText()}")
                Result.success(response.body())
            } else {

                Result.failure(Exception("Failed to fetch courses: ${response.status}"))
            }
        } catch (e: Exception) {
            Log.e("OpportunitiesRepositoryImpl", "Failed to fetch courses: ${e}")
            Result.failure(e)
        }
    }

    override suspend fun getCourseById(id: String): Result<CourseResponse> {
        return try {
            val response = httpClient.get("$coursesUrl/$id") {

            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch course details: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCourseModules(courseId: String): Result<List<ModuleResponse>> {
        return try {
            val response = httpClient.get("$coursesUrl/$courseId/modules") {

            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body())
            } else {
                Result.failure(Exception("Failed to fetch course modules: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun enrollInCourse(courseId: String, request: CourseEnrollmentRequest): Result<Unit> {
        return try {
            val response = httpClient.post("$coursesUrl/$courseId/enroll") {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (response.status == HttpStatusCode.OK || response.status == HttpStatusCode.Created) {
                Log.d("OpportunitiesRepositoryImpl", "Enrollment successful: ${response.status} $request")
                Result.success(Unit)
            } else {
                Log.e("OpportunitiesRepositoryImpl", "Enrollment failed: ${response.status } $request")
                Result.failure(Exception("Enrollment failed: ${response.status}"))
            }
        }
        catch (e: ClientRequestException) {

            val errorResponse = e.response.body<ErrorResponse>()

            Result.failure(
                Exception(errorResponse.message)
            )
        }catch (e: Exception) {
            Log.e("OpportunitiesRepositoryImpl", "Enrollment failed: ${e.message} $request")
            Result.failure(e)
        }
    }

    override suspend fun applyForOpportunity(
        opportunityId: String,
        request: OpportunityApplicationRequest
    ): Result<Unit> {

        return try {

            val response = httpClient.post(
                "$baseUrl/$opportunityId/apply-guest"
            ) {
                contentType(ContentType.Application.Json)
                setBody(request)
            }

            if (
                response.status == HttpStatusCode.OK ||
                response.status == HttpStatusCode.Created
            ) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("Application failed: ${response.status}")
                )
            }

        } catch (e: ClientRequestException) {

            val errorResponse = e.response.body<ErrorResponse>()

            Result.failure(
                Exception(errorResponse.message)
            )

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}
