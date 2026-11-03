
package com.iota.campusX.Feature.UserProfile.utils

import com.iota.campusX.Feature.UserProfile.data.local.entities.UserProfileEntity
import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Education
import org.junit.Assert.assertEquals
import org.junit.Test

class ProfileCompletionCalculatorTest {

    private fun createEntity(
        baseProfile: String = "",
        tagline: String = "",
        education: String? = null,
        skills: String? = null,
        matchPreferences: String? = null
    ) = UserProfileEntity(
        uid = "test-user",
        baseProfile = baseProfile,
        contact = "",
        education = education,
        skills = skills,
        matchPreferences = matchPreferences,
        isCurrentUser = true,
        tagline = tagline
    )

    @Test
    fun calculateScoreFromEntity_withNullEntity_returnsZero() {
        val result = ProfileCompletionCalculator.calculateScoreFromEntity(null)

        assertEquals(0, result)
    }

    @Test
    fun calculateScoreFromEntity_withEmptyEntity_returnsZero() {
        val entity = createEntity()

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(0, result)
    }

    @Test
    fun calculateScoreFromEntity_withBaseProfile_returns20() {
        val entity = createEntity(baseProfile = "{}")

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(20, result)
    }

    @Test
    fun calculateScoreFromEntity_withTagline_returns15() {
        val entity = createEntity(tagline = "Android Developer")

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(15, result)
    }

    @Test
    fun calculateScoreFromEntity_withEducation_returns25() {
        val entity = createEntity(education = """["BTech"]""")

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(25, result)
    }

    @Test
    fun calculateScoreFromEntity_withSkills_returns25() {
        val entity = createEntity(skills = """["Kotlin"]""")

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(25, result)
    }

    @Test
    fun calculateScoreFromEntity_withMatchPreferences_returns15() {
        val entity = createEntity(matchPreferences = """["Hackathon"]""")

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(15, result)
    }

    @Test
    fun calculateScoreFromEntity_withAllFieldsCompleted_returns100() {
        val entity = createEntity(
            baseProfile = "{}",
            tagline = "Android Developer",
            education = """["BTech"]""",
            skills = """["Kotlin"]""",
            matchPreferences = """["Hackathon"]"""
        )

        val result = ProfileCompletionCalculator.calculateScoreFromEntity(entity)

        assertEquals(100, result)
    }


    @Test
    fun calculate_withNullProfile_returnsBeginner() {

        val result = ProfileCompletionCalculator.calculate(null)

        assertEquals(0, result.score)
        assertEquals(0, result.completedCount)
        assertEquals(6, result.totalCount)
        assertEquals(ProfileStrengthLevel.BEGINNER, result.level)
    }

    @Test
    fun calculate_withEmptyProfile_returnsZeroScore() {

        val profile = ProfileResponse()

        val result = ProfileCompletionCalculator.calculate(profile)

        assertEquals(0, result.score)
        assertEquals(0, result.completedCount)
        assertEquals(6, result.totalCount)
        assertEquals(ProfileStrengthLevel.BEGINNER, result.level)
        assertEquals(6, result.pendingItems.size)
    }

    @Test
    fun calculate_withPhotoTaglineAndEducation_returns50Percent() {

        val profile = ProfileResponse(
            baseProfile = BaseProfile(
                name = "Avanish",
                image = "profile.jpg",
                tagline = "Android Developer"
            ),
            education = Education(
                college = "ABC Engineering College",
                course = "BTech",
                specialization = "Computer Science"
            )
        )

        val result = ProfileCompletionCalculator.calculate(profile)

        assertEquals(50, result.score)
        assertEquals(3, result.completedCount)
        assertEquals(6, result.totalCount)
        assertEquals(ProfileStrengthLevel.INTERMEDIATE, result.level)
    }

}
