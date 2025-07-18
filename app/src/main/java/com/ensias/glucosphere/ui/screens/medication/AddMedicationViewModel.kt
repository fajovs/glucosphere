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

data class AddMedicationUiState(
    val medicationName: String = "",
    val dosage: String = "",
    val instructions: String = "",
    val scheduleTimes: List<LocalTime> = emptyList(),
    val isLoading: Boolean = false,
    val isMedicationSaved: Boolean = false,
    val errorMessage: String = "",
    val isFormValid: Boolean = false
)

@HiltViewModel
class AddMedicationViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository,
    private val reminderManager: MedicationReminderManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddMedicationUiState())
    val uiState: StateFlow<AddMedicationUiState> = _uiState.asStateFlow()

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
    fun saveMedication() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = "")

            try {
                val medication = Medication(
                    name = state.medicationName,
                    dosage = state.dosage,
                    instructions = state.instructions
                )

                val medicationId = medicationRepository.insertMedication(medication)
                Log.d("AddMedication", "Saved medication with ID: $medicationId")

                // Save schedules and set up reminders
                state.scheduleTimes.forEach { time ->
                    val schedule = MedicationSchedule(
                        medicationId = medicationId,
                        timeHour = time.hour,
                        timeMinute = time.minute,
                        reminderEnabled = true
                    )

                    val scheduleId = medicationRepository.insertSchedule(schedule)
                    Log.d("AddMedication", "Saved schedule with ID: $scheduleId at ${time.hour}:${time.minute}")

                    // Schedule reminder
                    val savedSchedule = schedule.copy(id = scheduleId)
                    reminderManager.scheduleReminder(medication.copy(id = medicationId), savedSchedule)
                    Log.d("AddMedication", "Scheduled reminder for ${medication.name}")
                }

                _uiState.value = state.copy(isLoading = false, isMedicationSaved = true)
            } catch (e: Exception) {
                Log.e("AddMedication", "Error saving medication", e)
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to save medication: ${e.message}"
                )
            }
        }
    }

    // Test function to show notification immediately
    fun testNotification() {
        reminderManager.showTestNotification()
    }
}
