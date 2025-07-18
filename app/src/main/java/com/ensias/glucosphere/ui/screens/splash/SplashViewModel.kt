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
                // Get all medications and cancel their reminders
                medicationRepository.getMedicationsWithSchedules().collect { medicationsWithSchedules ->
                    medicationsWithSchedules.forEach { medicationWithSchedules ->
                        medicationWithSchedules.schedules.forEach { schedule ->
                            // Cancel reminders would need to be implemented
                            // reminderManager.cancelReminder(schedule.id)
                        }
                        // Delete medication (cascading delete will handle schedules and logs)
                        medicationRepository.deleteMedication(medicationWithSchedules.medication)
                    }
                }

                // Get all glucose readings and delete them
                glucoseReadingRepository.getAllReadings().collect { readings ->
                    readings.forEach { reading ->
                        glucoseReadingRepository.deleteReading(reading)
                    }
                }

                // Delete user profile
                userProfileRepository.getUserProfile().collect { profile ->
                    profile?.let {
                        // Since we don't have a delete method, we can create a dummy profile
                        // or implement a proper delete method in the repository
                    }
                }

                _isUserProfileExists.value = false
            } catch (e: Exception) {
                // Handle error - for now just set to false to allow re-setup
                _isUserProfileExists.value = false
            }
        }
    }
}
