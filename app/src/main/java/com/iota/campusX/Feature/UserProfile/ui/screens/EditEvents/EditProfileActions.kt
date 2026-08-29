package com.iota.campusX.Feature.UserProfile.ui.screens.EditEvents

import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.Model.BaseProfile
import com.iota.campusX.Feature.UserProfile.domain.Model.Education

sealed class EditProfileActions{
    data class EditBasicDetails(val basicDetails: BaseProfile): EditProfileActions()
    data class EditEducation(val education: Education?):EditProfileActions()
    data class EditSummary(val summary: String?):EditProfileActions()
    data class EditSkills(val skills: List<SkillResponse>?):EditProfileActions()
}