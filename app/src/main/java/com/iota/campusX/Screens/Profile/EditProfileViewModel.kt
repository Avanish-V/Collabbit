package com.iota.campusX.Screens.Profile


import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.UserProfile.data.Campus
import com.iota.campusX.Feature.UserProfile.data.Gender
import com.iota.campusX.Feature.UserProfile.data.University
import com.iota.campusX.ui.UIComponents.CourseDuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
class EditProfileViewModel: ViewModel() {

    private val _profileData = MutableStateFlow<ProfileData?>(null)
    val profileData: StateFlow<ProfileData?> = _profileData.asStateFlow()


    private val _editType = MutableStateFlow<EditProfileType>(EditProfileType.NONE)
    val editType: StateFlow<EditProfileType> = _editType.asStateFlow()
    private val _items = MutableStateFlow<List<String>>(emptyList())
    val items: StateFlow<List<String>> = _items.asStateFlow()

    fun addItem(item: String) {
        _items.value = _items.value + item
    }

    fun removeItem(item: String) {
        _items.value = _items.value - item
    }

    fun setAllItems(items: List<String>) {
        _items.value = items
    }

    private val _name: MutableState<String> = mutableStateOf("")
    val name: MutableState<String> = _name

    private val _about: MutableState<String> = mutableStateOf("")
    val about: MutableState<String> = _about

    private val _gender: MutableState<Gender> = mutableStateOf(Gender.UNSPECIFIED)
    val gender: MutableState<Gender> = _gender

    private val _campus: MutableState<Campus> = mutableStateOf(Campus())
    val campus: MutableState<Campus> = _campus


    private val _interests: MutableState<List<String>> = mutableStateOf(emptyList())
    var interests: MutableState<List<String>> = _interests


    fun editGender(newGender: Gender) {
        _gender.value = newGender
    }

    fun editAbout(newAbout: String) {
        _about.value = newAbout

    }
    fun editName(newName: String) {
        _name.value = newName
    }

    fun editCampus(campus: Campus){
        _campus.value = campus
    }
    fun editUniversity(university: University?){
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

    fun editDegree(degree: String){
        _campus.value = _campus.value.copy(degree = degree)
    }

    fun editCourseStart(courseStart: CourseDuration){
        _campus.value = _campus.value.copy(courseStart = courseStart)
    }
    fun editCourseEnd(courseEnd: CourseDuration){
        _campus.value = _campus.value.copy(courseEnd = courseEnd)
    }

    fun getProfileData(profileData: ProfileData) {
         _profileData.value = profileData
    }

    fun editType(){
        Log.d("PROFILE_DATA", "profile: ${profileData.value}")
        Log.d("PROFILE_DATA", "editType: ${name.value}" + " ${gender.value}")
         if (profileData.value?.name != name.value){
            _editType.value = EditProfileType.NAME
        }else if (_profileData.value?.gender == gender.value){
            _editType.value = EditProfileType.GENDER
        } else {
            _editType.value = EditProfileType.NONE
        }
    }

}
data class ProfileData(
    val name: String,
    val gender: Gender = Gender.UNSPECIFIED,
)

enum class EditProfileType {NONE,NAME,GENDER}