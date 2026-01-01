package com.iota.campusX.Feature.UserProfile.ui.viewmodels



import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Campus
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.Gender
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.University
import com.iota.campusX.Feature.UserProfile.data.remote.dtos.UpdateProfileDTO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditProfileViewModel: ViewModel() {

    private val _updateProfile = MutableStateFlow(UpdateProfileDTO())
    val updateProfile = _updateProfile.asStateFlow()

    fun updateProfileFields(newData: UpdateProfileDTO) {
        _updateProfile.update { current ->
            current.copy(
                updateUserName = newData.updateUserName ?: current.updateUserName,
                updateAbout = newData.updateAbout ?: current.updateAbout,
                updateTagline = newData.updateTagline ?: current.updateTagline,
                updateGender = newData.updateGender ?: current.updateGender
            )
        }
    }



    private val _editType = MutableStateFlow<EditProfileType?>(null)
    val editType: StateFlow<EditProfileType?> = _editType.asStateFlow()



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

    private val _tagline: MutableState<String> = mutableStateOf("")
    val tagline: MutableState<String> = _tagline

    private val _gender: MutableState<Gender> = mutableStateOf(Gender.UNSPECIFIED)
    val gender: MutableState<Gender> = _gender

    private val _campus: MutableState<Campus> = mutableStateOf(Campus())
    val campus: MutableState<Campus> = _campus


    private val _interests: MutableState<List<String>> = mutableStateOf(emptyList())
    var interests: MutableState<List<String>> = _interests


    fun editGender(newGender: Gender?) {
        if (newGender != null) {
            _gender.value = newGender
        }
    }

    fun editAbout(newAbout: String) {
        _about.value = newAbout

    }
    fun editTagline(newAbout: String) {
        _tagline.value = newAbout

    }
    fun editName(newName: String) {
        _name.value = newName
    }

    fun editCampus(campus: Campus){
        _campus.value = campus
    }
    fun editUniversity(university: University?){
        _campus.value = _campus.value.copy(university = university?.university)
        _campus.value = _campus.value.copy(logo = university?.logo)
    }
    fun editCollege(collegeName: String){
        _campus.value = _campus.value.copy(collegeName = collegeName)
    }
    fun editCampusCode(campusCode: String){
        _campus.value = _campus.value.copy(code = campusCode)
    }
    fun editFieldOfStudy(fieldOfStudy: String){
        _campus.value = _campus.value.copy(fieldOfStudy = fieldOfStudy)
    }

    fun editDegree(degree: String){
        _campus.value = _campus.value.copy(degree = degree)
    }

    fun duration(
        courseStart: String? = null,
        courseEnd: String? = null,
        startTimestamp: Long? = null,
        endTimestamp: Long? = null,
        isCurrent: Boolean? = null
    ){
        courseStart?.let { courseStart ->
            _campus.value = _campus.value.copy(courseStart = courseStart)
        }
        courseEnd?.let { courseEnd ->
            _campus.value = _campus.value.copy(courseEnd = courseEnd)
        }
        startTimestamp?.let { startTimestamp ->
            _campus.value = _campus.value.copy(startTimestamp = startTimestamp)
        }
        endTimestamp?.let { endTimestamp ->
            _campus.value = _campus.value.copy(endTimestamp = endTimestamp)
        }
        isCurrent?.let { isCurrent ->
            _campus.value = _campus.value.copy(isCurrent = isCurrent)
        }
    }


    fun editType(editType: EditProfileType) {
        _editType.value = editType
    }


    fun editTypeSetNull(){
        _editType.value = null
    }

}

sealed class EditProfileType {
    data class UserName (val name: String): EditProfileType()
    data class UserGender(val gender: Gender): EditProfileType()
    data class UserImage(val imageUri: Uri): EditProfileType()
}


