package com.ensias.glucosphere.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.repository.UserProfileRepository
import com.ensias.glucosphere.data.repository.MedicationRepository
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val medicationRepository: MedicationRepository,
    private val glucoseReadingRepository: GlucoseReadingRepository
) : ViewModel() {

    private val _isUserProfileExists = MutableStateFlow<Boolean?>(null)
    val isUserProfileExists: StateFlow<Boolean?> = _isUserProfileExists

    init {
        checkUserProfile()
    }

    private fun checkUserProfile() {
        viewModelScope.launch {
            _isUserProfileExists.value = userProfileRepository.userProfileExists()
        }
    }

    fun clearUserData() {
        viewModelScope.launch {
            try {
                // Just logout the current user instead of deleting all data
                userProfileRepository.logout()
                _isUserProfileExists.value = false
            } catch (e: Exception) {
                // Handle error - for now just set to false to allow re-setup
                _isUserProfileExists.value = false
            }
        }
    }
}
