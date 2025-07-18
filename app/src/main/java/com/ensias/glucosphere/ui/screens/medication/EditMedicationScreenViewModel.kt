package com.ensias.glucosphere.ui.screens.medication

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.database.entity.MedicationSchedule
import com.ensias.glucosphere.data.repository.MedicationRepository
import com.ensias.glucosphere.notification.MedicationReminderManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject
import android.util.Log
import androidx.annotation.RequiresApi

data class EditMedicationUiState(
    val medicationName: String = "",
    val dosage: String = "",
    val instructions: String = "",
    val scheduleTimes: List<LocalTime> = emptyList(),
    val isLoading: Boolean = true,
    val isMedicationUpdated: Boolean = false,
    val errorMessage: String = "",
    val isFormValid: Boolean = false
)

@HiltViewModel
class EditMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderManager: MedicationReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditMedicationUiState())
    val uiState: StateFlow<EditMedicationUiState> = _uiState.asStateFlow()

    private var currentMedicationId: Long = -1
    private var existingScheduleIds: List<Long> = emptyList()

    init {
        observeFormValidity()
    }

    private fun observeFormValidity() {
        viewModelScope.launch {
            _uiState.collect { state ->
                val isValid = state.medicationName.isNotBlank() &&
                        state.dosage.isNotBlank()

                _uiState.value = state.copy(isFormValid = isValid)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun loadMedication(medicationId: Long) {
        currentMedicationId = medicationId
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = "")

            try {
                // Get medication details
                val medication = medicationRepository.getMedicationById(medicationId)
                if (medication != null) {
                    // Get existing schedules
                    medicationRepository.getSchedulesForMedication(medicationId).collect { schedules ->
                        existingScheduleIds = schedules.map { it.id }

                        val scheduleTimes = schedules
                            .filter { it.isActive }
                            .map { LocalTime.of(it.timeHour, it.timeMinute) }
                            .sorted()

                        _uiState.value = _uiState.value.copy(
                            medicationName = medication.name,
                            dosage = medication.dosage,
                            instructions = medication.instructions,
                            scheduleTimes = scheduleTimes,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Medication not found"
                    )
                }
            } catch (e: Exception) {
                Log.e("EditMedication", "Error loading medication", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load medication: ${e.message}"
                )
            }
        }
    }

    fun updateMedicationName(name: String) {
        _uiState.value = _uiState.value.copy(medicationName = name, errorMessage = "")
    }

    fun updateDosage(dosage: String) {
        _uiState.value = _uiState.value.copy(dosage = dosage, errorMessage = "")
    }

    fun updateInstructions(instructions: String) {
        _uiState.value = _uiState.value.copy(instructions = instructions)
    }

    fun addScheduleTime(time: LocalTime) {
        val currentTimes = _uiState.value.scheduleTimes.toMutableList()
        if (!currentTimes.contains(time)) {
            currentTimes.add(time)
            currentTimes.sort()
            _uiState.value = _uiState.value.copy(scheduleTimes = currentTimes)
        }
    }

    fun removeScheduleTime(index: Int) {
        val currentTimes = _uiState.value.scheduleTimes.toMutableList()
        if (index in currentTimes.indices) {
            currentTimes.removeAt(index)
            _uiState.value = _uiState.value.copy(scheduleTimes = currentTimes)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateMedication() {
        val state = _uiState.value
        if (!state.isFormValid || currentMedicationId == -1L) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = "")

            try {
                // Update medication
                val medication = Medication(
                    id = currentMedicationId,
                    name = state.medicationName,
                    dosage = state.dosage,
                    instructions = state.instructions,
                    isActive = true
                )

                medicationRepository.updateMedication(medication)

                // Cancel existing reminders
                existingScheduleIds.forEach { scheduleId ->
                    reminderManager.cancelReminder(scheduleId)
                }

                // Delete existing schedules
                medicationRepository.deleteSchedulesForMedication(currentMedicationId)

                // Add new schedules and set up reminders
                state.scheduleTimes.forEach { time ->
                    val schedule = MedicationSchedule(
                        medicationId = currentMedicationId,
                        timeHour = time.hour,
                        timeMinute = time.minute,
                        reminderEnabled = true
                    )

                    val scheduleId = medicationRepository.insertSchedule(schedule)

                    // Schedule reminder
                    val savedSchedule = schedule.copy(id = scheduleId)
                    reminderManager.scheduleReminder(medication, savedSchedule)
                }

                _uiState.value = state.copy(isLoading = false, isMedicationUpdated = true)
            } catch (e: Exception) {
                Log.e("EditMedication", "Error updating medication", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to update medication: ${e.message}"
                )
            }
        }
    }
}
