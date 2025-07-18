package com.ensias.glucosphere.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileSetupUiState(
    val username: String = "",
    val age: String = "",
    val targetMin: String = "",
    val targetMax: String = "",
    val isLoading: Boolean = false,
    val isProfileSaved: Boolean = false,
    val errorMessage: String = "",
    val isFormValid: Boolean = false
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        loadExistingProfile()
        observeFormValidity()
    }

    private fun loadExistingProfile() {
        viewModelScope.launch {
            userProfileRepository.getUserProfile().collect { profile ->
                profile?.let {
                    _uiState.value = _uiState.value.copy(
                        username = it.username,
                        age = it.age.toString(),
                        targetMin = it.targetGlucoseMin.toString(),
                        targetMax = it.targetGlucoseMax.toString()
                    )
                }
            }
        }
    }

    private fun observeFormValidity() {
        viewModelScope.launch {
            _uiState.collect { state ->
                val isValid = state.username.isNotBlank() &&
                        state.age.isNotBlank() && state.age.toIntOrNull() != null &&
                        state.targetMin.isNotBlank() && state.targetMin.toIntOrNull() != null &&
                        state.targetMax.isNotBlank() && state.targetMax.toIntOrNull() != null &&
                        (state.targetMin.toIntOrNull() ?: 0) < (state.targetMax.toIntOrNull() ?: 0)

                _uiState.value = state.copy(isFormValid = isValid)
            }
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = "")
    }

    fun updateAge(age: String) {
        _uiState.value = _uiState.value.copy(age = age, errorMessage = "")
    }

    fun updateTargetMin(targetMin: String) {
        _uiState.value = _uiState.value.copy(targetMin = targetMin, errorMessage = "")
    }

    fun updateTargetMax(targetMax: String) {
        _uiState.value = _uiState.value.copy(targetMax = targetMax, errorMessage = "")
    }

    fun saveProfile() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = "")

            try {
                val userProfile = UserProfile(
                    username = state.username,
                    age = state.age.toInt(),
                    targetGlucoseMin = state.targetMin.toInt(),
                    targetGlucoseMax = state.targetMax.toInt()
                )

                userProfileRepository.insertUserProfile(userProfile)
                _uiState.value = state.copy(isLoading = false, isProfileSaved = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to save profile: ${e.message}"
                )
            }
        }
    }
}
