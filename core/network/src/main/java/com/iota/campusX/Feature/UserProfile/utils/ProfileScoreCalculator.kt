package com.iota.campusX.Feature.UserProfile.utils

import com.iota.campusX.Feature.UserProfile.data.remote.response.ProfileResponse

object ProfileScoreCalculator {
    fun calculateScore(profile: ProfileResponse?): Int {
        if (profile == null) return 0
        var score = 0
        if (!profile.baseProfile.image.isNullOrBlank()) score += 15
        if (!profile.baseProfile.tagline.isNullOrBlank()) score += 15
        if (!profile.baseProfile.summary.isNullOrBlank()) score += 15
        if (profile.education != null && (!profile.education.college.isNullOrBlank() || !profile.education.course.isNullOrBlank())) score += 20
        if (!profile.skills.isNullOrEmpty()) score += 20
        if (!profile.matchPreferences.isNullOrEmpty()) score += 15
        return score.coerceIn(0, 100)
    }
}
