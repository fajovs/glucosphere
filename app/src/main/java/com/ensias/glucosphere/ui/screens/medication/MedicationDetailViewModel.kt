package com.ensias.glucosphere.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.MedicationWithSchedules
import com.ensias.glucosphere.data.database.entity.MedicationLog
import com.ensias.glucosphere.data.repository.MedicationRepository
import com.ensias.glucosphere.notification.MedicationReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

data class MedicationDetailUiState(
    val medicationWithSchedules: MedicationWithSchedules? = null,
    val recentLogs: List<MedicationLog> = emptyList(),
    val isLoading: Boolean = true,
    val isMedicationDeleted: Boolean = false,
    val errorMessage: String = ""
)

@HiltViewModel
class MedicationDetailViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderManager: MedicationReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationDetailUiState())
    val uiState: StateFlow<MedicationDetailUiState> = _uiState.asStateFlow()

    private var currentMedicationId: Long = -1

    fun loadMedication(medicationId: Long) {
        currentMedicationId = medicationId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                // Get medication with schedules
                medicationRepository.getMedicationsWithSchedules().collect { allMedications ->
                    val medicationWithSchedules = allMedications.find { it.medication.id == medicationId }

                    if (medicationWithSchedules != null) {
                        // Get recent logs for this medication (increased to 10 for scrollable view)
                        medicationRepository.getRecentMedicationLogs().collect { allLogs ->
                            val medicationLogs = allLogs
                                .filter { it.medicationId == medicationId }
                                .sortedByDescending { it.actualTime }
                                .take(10)

                            _uiState.value = _uiState.value.copy(
                                medicationWithSchedules = medicationWithSchedules,
                                recentLogs = medicationLogs,
                                isLoading = false
                            )
                        }
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Medication not found"
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("MedicationDetail", "Error loading medication", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load medication: ${e.message}"
                )
            }
        }
    }

    fun deleteMedication() {
        if (currentMedicationId == -1L) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                val medicationWithSchedules = _uiState.value.medicationWithSchedules

                if (medicationWithSchedules != null) {
                    // Cancel all reminders for this medication
                    medicationWithSchedules.schedules.forEach { schedule ->
                        reminderManager.cancelReminder(schedule.id)
                    }

                    // Delete the medication (cascading delete will handle schedules and logs)
                    medicationRepository.deleteMedication(medicationWithSchedules.medication)

                    Log.d("MedicationDetail", "Successfully deleted medication: ${medicationWithSchedules.medication.name}")

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isMedicationDeleted = true
                    )
                }
            } catch (e: Exception) {
                Log.e("MedicationDetail", "Error deleting medication", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete medication: ${e.message}"
                )
            }
        }
    }
}
