package com.iota.campusX.Feature.UserProfile.ui.screens.EditProfile.EditSkills

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iota.campusX.Feature.UserProfile.data.remote.response.SkillResponse
import com.iota.campusX.Feature.UserProfile.domain.repository.UniversityRepository
import com.iota.campusX.Feature.UserProfile.domain.repository.UserProfileRepository
import com.iota.campusX.Utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class EditSkillsViewModel(
    private val universityRepository: UniversityRepository,
    private val profileRepository: UserProfileRepository
): ViewModel() {

    private val _skill: MutableStateFlow<List<SkillResponse>> = MutableStateFlow(emptyList())
    val skills: StateFlow<List<SkillResponse>> = _skill.asStateFlow()

    fun setInitialSkills(skills: List<SkillResponse>){
        _skill.value = skills
    }
    fun addSkill(skill: SkillResponse){
        _skill.value += skill
    }
    fun removeSkill(skill: SkillResponse){
        _skill.value -= skill
    }

    private val skillQuery = MutableStateFlow("")
    private val _skillsQueryState = MutableStateFlow<UiState<List<SkillResponse>>>(UiState.Idle)
    val skillsQueryState: StateFlow<UiState<List<SkillResponse>>> = _skillsQueryState.asStateFlow()

    private var isSelectionFromDropdown = false

    init {
        viewModelScope.launch {
            skillQuery
                .debounce(500.milliseconds)
                .filter { query ->
                    val shouldSearch = query.isNotBlank() && query.length > 2 && !isSelectionFromDropdown
                    isSelectionFromDropdown = false
                    shouldSearch
                }
                .distinctUntilChanged()
                .onEach { _skillsQueryState.value = UiState.Loading }
                .flatMapLatest { query ->
                    universityRepository.searchKeySkills(query)
                        .catch { e ->
                            Log.e("SkillSearch", "searchQuery flow error: $e")
                            _skillsQueryState.value = UiState.Error("Unexpected error: ${e.localizedMessage ?: "Unknown"}")
                            emit(emptyList())
                        }
                }
                .collect { result ->
                    Log.d("SkillSearch", "searchQuery results: $result")
                    _skillsQueryState.value = UiState.Success(result)
                }
        }
    }

    fun onSkillQueryChanged(query: String) {
        isSelectionFromDropdown = false
        skillQuery.value = query
    }

    fun selectSkillFromDropdown(skill: SkillResponse) {
        isSelectionFromDropdown = true
        skillQuery.value = "" // Clear query after selection
        _skillsQueryState.value = UiState.Idle
        addSkill(skill)
    }

    private val _uiState = MutableStateFlow<UiState<Boolean>>(UiState.Idle)
    val uiState: StateFlow<UiState<Boolean>> = _uiState.asStateFlow()



    fun updateSkills() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            val skillNames = _skill.value
            val result = profileRepository.updateSkills(skillNames)
            result.fold(
                onSuccess = {
                    _uiState.value = UiState.Success(true)
                },
                onFailure = {
                    _uiState.value = UiState.Error(it.message ?: "Failed to update skills")
                }
            )
        }
    }


}