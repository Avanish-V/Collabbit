package com.iota.campusX.Screens.Profile


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.University


class EditProfileViewModel: ViewModel() {

    private val _name: MutableState<String> = mutableStateOf("")
    val name: MutableState<String> = _name

    private val _about: MutableState<String> = mutableStateOf("")
    val about: MutableState<String> = _about

    private val _gender: MutableState<String> = mutableStateOf("")
    val gender: MutableState<String> = _gender

    private val _campus: MutableState<Campus> = mutableStateOf(Campus())
    val campus: MutableState<Campus> = _campus

    private val _interests: MutableState<List<String>> = mutableStateOf(emptyList())
    val interests: MutableState<List<String>> = _interests




    fun editAbout(newAbout: String) {
        _about.value = newAbout

    }
    fun editName(newName: String) {
        _name.value = newName
    }

    fun editCampus(campus: Campus){
        _campus.value = campus
    }
    fun editUniversity(university: University){
        _campus.value = _campus.value.copy(university = university)
    }
    fun editCollege(collegeName: String){
        _campus.value = _campus.value.copy(collegeName = collegeName)
    }
    fun editCampusCode(campusCode: String){
        _campus.value = _campus.value.copy(campusCode = campusCode)
    }
    fun editFieldOfStudy(fieldOfStudy: String){
        _campus.value = _campus.value.copy(fieldOfStudy = fieldOfStudy)
    }

    fun editCourseStart(courseStart: Long){
        _campus.value = _campus.value.copy(courseStart = courseStart)
    }
    fun editCourseEnd(courseEnd: Long){
        _campus.value = _campus.value.copy(courseEnd = courseEnd)
    }


}

