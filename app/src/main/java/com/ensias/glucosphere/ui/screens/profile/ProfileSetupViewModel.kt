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
    val currentStep: Int = 0, // 0: Age, 1: Health Status (if 65+), 2: Insulin, 3: Review Targets
    val username: String = "",
    val age: String = "",
    val isOver65: Boolean = false,
    val healthStatus: String? = null,
    val insulinUser: Boolean = false,
    val fastingMin: String = "",
    val fastingMax: String = "",
    val preMealMin: String = "",
    val preMealMax: String = "",
    val postMealMax: String = "",
    val randomMax: String = "",
    val useRecommendedTargets: Boolean = true,
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
                    val age = it.age
                    val isOver65 = age >= 65
                    _uiState.value = _uiState.value.copy(
                        username = it.username,
                        age = it.age.toString(),
                        isOver65 = isOver65,
                        healthStatus = it.healthStatus,
                        insulinUser = it.insulinUser,
                        fastingMin = it.fastingMin?.toString() ?: "",
                        fastingMax = it.fastingMax?.toString() ?: "",
                        preMealMin = it.preMealMin?.toString() ?: "",
                        preMealMax = it.preMealMax?.toString() ?: "",
                        postMealMax = it.postMealMax?.toString() ?: "",
                        randomMax = it.randomMax?.toString() ?: ""
                    )
                }
            }
        }
    }

    private fun observeFormValidity() {
        viewModelScope.launch {
            _uiState.collect { state ->
                val isValid = when (state.currentStep) {
                    0 -> state.username.isNotBlank() && state.age.isNotBlank() && state.age.toIntOrNull() != null
                    1 -> !state.isOver65 || state.healthStatus != null // Only validate health status if over 65
                    2 -> true // Insulin question always proceeds (both yes/no are valid)
                    3 -> validateTargets(state)
                    else -> false
                }
                _uiState.value = state.copy(isFormValid = isValid)
            }
        }
    }

    private fun validateTargets(state: ProfileSetupUiState): Boolean {
        return if (state.useRecommendedTargets) {
            true
        } else {
            state.fastingMin.toIntOrNull() != null && state.fastingMax.toIntOrNull() != null &&
                    state.preMealMin.toIntOrNull() != null && state.preMealMax.toIntOrNull() != null &&
                    state.postMealMax.toIntOrNull() != null && state.randomMax.toIntOrNull() != null &&
                    (state.fastingMin.toIntOrNull() ?: 0) < (state.fastingMax.toIntOrNull() ?: 0)
        }
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username, errorMessage = "")
    }

    fun updateAge(age: String) {
        val ageInt = age.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(
            age = age,
            isOver65 = ageInt >= 65,
            errorMessage = ""
        )
    }

    fun updateHealthStatus(status: String) {
        _uiState.value = _uiState.value.copy(healthStatus = status, errorMessage = "")
    }

    fun updateInsulinUser(insulinUser: Boolean) {
        _uiState.value = _uiState.value.copy(insulinUser = insulinUser, errorMessage = "")
    }

    fun updateFastingMin(value: String) {
        _uiState.value = _uiState.value.copy(fastingMin = value, errorMessage = "")
    }

    fun updateFastingMax(value: String) {
        _uiState.value = _uiState.value.copy(fastingMax = value, errorMessage = "")
    }

    fun updatePreMealMin(value: String) {
        _uiState.value = _uiState.value.copy(preMealMin = value, errorMessage = "")
    }

    fun updatePreMealMax(value: String) {
        _uiState.value = _uiState.value.copy(preMealMax = value, errorMessage = "")
    }

    fun updatePostMealMax(value: String) {
        _uiState.value = _uiState.value.copy(postMealMax = value, errorMessage = "")
    }

    fun updateRandomMax(value: String) {
        _uiState.value = _uiState.value.copy(randomMax = value, errorMessage = "")
    }

    fun toggleUseRecommendedTargets(use: Boolean) {
        _uiState.value = _uiState.value.copy(useRecommendedTargets = use)
        if (use) {
            applyRecommendedTargets()
        }
    }

    private fun applyRecommendedTargets() {
        val state = _uiState.value
        val (fMin, fMax, pMin, pMax, postMax, randMax) = calculateTargetRanges(
            age = state.age.toIntOrNull() ?: 18,
            healthStatus = state.healthStatus,
            insulinUser = state.insulinUser
        )

        _uiState.value = state.copy(
            fastingMin = fMin.toString(),
            fastingMax = fMax.toString(),
            preMealMin = pMin.toString(),
            preMealMax = pMax.toString(),
            postMealMax = postMax.toString(),
            randomMax = randMax.toString()
        )
    }

    private fun calculateTargetRanges(
        age: Int,
        healthStatus: String?,
        insulinUser: Boolean
    ): Tuple6<Int, Int, Int, Int, Int, Int> {
        return when {
            age < 65 -> {
                // Standard Adult (18-64)
                Tuple6(80, 130, 80, 130, 180, 180)
            }
            healthStatus == "healthy" -> {
                // Healthy / Independent
                Tuple6(80, 130, 80, 130, 180, 180)
            }
            healthStatus == "multiple_conditions" -> {
                // Multiple Illnesses / Mild Cognitive Issues
                Tuple6(90, 150, 90, 150, 200, 200)
            }
            healthStatus == "frail" -> {
                // Frail / Limited Life Expectancy
                Tuple6(100, 180, 100, 180, 220, 220)
            }
            else -> {
                // Default fallback
                Tuple6(80, 130, 80, 130, 180, 180)
            }
        }
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.currentStep < 3) {
            var nextStep = state.currentStep + 1
            if (nextStep == 1 && !state.isOver65) {
                nextStep = 2 // Skip health status step
            }
            _uiState.value = state.copy(currentStep = nextStep)
        }
    }

    fun previousStep() {
        val state = _uiState.value
        if (state.currentStep > 0) {
            var prevStep = state.currentStep - 1
            if (prevStep == 1 && !state.isOver65) {
                prevStep = 0 // Skip health status step
            }
            _uiState.value = state.copy(currentStep = prevStep)
        }
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
                    healthStatus = state.healthStatus,
                    insulinUser = state.insulinUser,
                    fastingMin = state.fastingMin.toIntOrNull(),
                    fastingMax = state.fastingMax.toIntOrNull(),
                    preMealMin = state.preMealMin.toIntOrNull(),
                    preMealMax = state.preMealMax.toIntOrNull(),
                    postMealMax = state.postMealMax.toIntOrNull(),
                    randomMax = state.randomMax.toIntOrNull(),
                    targetGlucoseMin = state.fastingMin.toIntOrNull() ?: 80,
                    targetGlucoseMax = state.fastingMax.toIntOrNull() ?: 130
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

data class Tuple6<A, B, C, D, E, F>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
    val sixth: F
)
