package com.ensias.glucosphere.ui.screens.log

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.ReadingType
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class LogGlucoseUiState(
    val glucoseLevel: String = "",
    val selectedReadingType: ReadingType = ReadingType.RANDOM,
    val notes: String = "",
    val isLoading: Boolean = false,
    val isReadingSaved: Boolean = false,
    val errorMessage: String = "",
    val isFormValid: Boolean = false
)

@HiltViewModel
class LogGlucoseViewModel @Inject constructor(
    private val glucoseReadingRepository: GlucoseReadingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogGlucoseUiState())
    val uiState: StateFlow<LogGlucoseUiState> = _uiState.asStateFlow()

    init {
        observeFormValidity()
    }

    private fun observeFormValidity() {
        viewModelScope.launch {
            _uiState.collect { state ->
                val isValid = state.glucoseLevel.isNotBlank() &&
                        state.glucoseLevel.toIntOrNull() != null &&
                        (state.glucoseLevel.toIntOrNull() ?: 0) > 0

                _uiState.value = state.copy(isFormValid = isValid)
            }
        }
    }

    fun updateGlucoseLevel(glucoseLevel: String) {
        _uiState.value = _uiState.value.copy(glucoseLevel = glucoseLevel, errorMessage = "")
    }

    fun updateReadingType(readingType: ReadingType) {
        _uiState.value = _uiState.value.copy(selectedReadingType = readingType)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun saveReading() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true, errorMessage = "")

            try {
                val reading = GlucoseReading(
                    glucoseLevel = state.glucoseLevel.toInt(),
                    timestamp = Date(),
                    notes = state.notes,
                    readingType = state.selectedReadingType
                )

                glucoseReadingRepository.insertReading(reading)
                _uiState.value = state.copy(isLoading = false, isReadingSaved = true)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    errorMessage = "Failed to save reading: ${e.message}"
                )
            }
        }
    }
}
