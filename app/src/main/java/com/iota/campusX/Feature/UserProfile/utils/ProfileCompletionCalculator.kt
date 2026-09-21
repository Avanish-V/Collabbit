package com.iota.campusX.Feature.UserProfile.utils

import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse
import com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents.EditProfileActions
import com.iota.campusX.R

enum class ProfileStrengthLevel(
    val label: String,
    val description: String,
    val badgeIconRes: Int
) {
    ALL_STAR(
        label = "All-Star Profile",
        description = "Your profile is fully optimized for maximum discoverability & match accuracy.",
        badgeIconRes = R.drawable.trophy_star
    ),
    STRONG(
        label = "Strong Profile",
        description = "Almost there! Complete the remaining steps to unlock top match.",
        badgeIconRes = R.drawable.fire_flame_curved
    ),
    INTERMEDIATE(
        label = "Intermediate Profile",
        description = "Good progress! Add more details to receive 2x more collaboration invites.",
        badgeIconRes = R.drawable.bullseye_arrow
    ),
    BEGINNER(
        label = "Getting Started",
        description = "Complete your profile to connect with student peers and projects.",
        badgeIconRes = R.drawable.lightbulb_on
    )
}

data class ProfileCompletionItem(
    val id: String,
    val title: String,
    val description: String,
    val weight: Int,
    val isCompleted: Boolean,
    val iconRes: Int,
    val action: EditProfileActions
)

data class ProfileCompletionScore(
    val score: Int,
    val completedCount: Int,
    val totalCount: Int,
    val level: ProfileStrengthLevel,
    val items: List<ProfileCompletionItem>,
    val pendingItems: List<ProfileCompletionItem>,
    val completedItems: List<ProfileCompletionItem>
)

object ProfileCompletionCalculator {

    fun calculate(profile: ProfileResponse): ProfileCompletionScore {
        val hasPhoto = !profile.baseProfile.image.isNullOrBlank()
        val hasTagline = profile.baseProfile.tagline.isNotBlank()
        val hasSummary = profile.baseProfile.summary.isNotBlank()
        val hasEducation = profile.education != null && 
                (profile.education.college.isNotBlank() || profile.education.course.isNotBlank())
        val hasSkills = !profile.skills.isNullOrEmpty()
        val hasPreferences = !profile.matchPreferences.isNullOrEmpty()

        val items = listOf(
            ProfileCompletionItem(
                id = "photo",
                title = "Profile Photo",
                description = "Add a photo so peers recognize you",
                weight = 15,
                isCompleted = hasPhoto,
                iconRes = R.drawable.user,
                action = EditProfileActions.EditBasicDetails(profile.baseProfile)
            ),
            ProfileCompletionItem(
                id = "tagline",
                title = "Headline / Tagline",
                description = "Highlight your role or ambition in a line",
                weight = 15,
                isCompleted = hasTagline,
                iconRes = R.drawable.pencil,
                action = EditProfileActions.EditBasicDetails(profile.baseProfile)
            ),
            ProfileCompletionItem(
                id = "summary",
                title = "About / Bio",
                description = "Share your journey, interests & goals",
                weight = 15,
                isCompleted = hasSummary,
                iconRes = R.drawable.write,
                action = EditProfileActions.EditSummary(profile.baseProfile.summary)
            ),
            ProfileCompletionItem(
                id = "education",
                title = "Campus & Education",
                description = "Add your college, course & year",
                weight = 20,
                isCompleted = hasEducation,
                iconRes = R.drawable.user_graduate,
                action = EditProfileActions.EditEducation(profile.education)
            ),
            ProfileCompletionItem(
                id = "skills",
                title = "Key Skills",
                description = "List languages, frameworks & tools",
                weight = 20,
                isCompleted = hasSkills,
                iconRes = R.drawable.code_simple,
                action = EditProfileActions.EditSkills(profile.skills)
            ),
            ProfileCompletionItem(
                id = "preferences",
                title = "Collaboration Goals",
                description = "Choose what you're open to (e.g. Hackathons)",
                weight = 15,
                isCompleted = hasPreferences,
                iconRes = R.drawable.heart_partner_handshake,
                action = EditProfileActions.EditOpenTo(profile.matchPreferences)
            )
        )

        val completedItems = items.filter { it.isCompleted }
        val pendingItems = items.filter { !it.isCompleted }
        val rawScore = completedItems.sumOf { it.weight }
        val finalScore = rawScore.coerceIn(0, 100)

        val level = when {
            finalScore >= 100 -> ProfileStrengthLevel.ALL_STAR
            finalScore >= 75 -> ProfileStrengthLevel.STRONG
            finalScore >= 50 -> ProfileStrengthLevel.INTERMEDIATE
            else -> ProfileStrengthLevel.BEGINNER
        }

        return ProfileCompletionScore(
            score = finalScore,
            completedCount = completedItems.size,
            totalCount = items.size,
            level = level,
            items = items,
            pendingItems = pendingItems,
            completedItems = completedItems
        )
    }
}
