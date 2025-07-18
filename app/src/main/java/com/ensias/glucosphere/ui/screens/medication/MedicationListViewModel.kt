package com.ensias.glucosphere.ui.screens.medication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.MedicationWithSchedules
import com.ensias.glucosphere.data.repository.MedicationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MedicationListUiState(
    val medicationsWithSchedules: List<MedicationWithSchedules> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class MedicationListViewModel @Inject constructor(
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MedicationListUiState())
    val uiState: StateFlow<MedicationListUiState> = _uiState.asStateFlow()

    init {
        loadMedications()
    }

    private fun loadMedications() {
        viewModelScope.launch {
            medicationRepository.getMedicationsWithSchedules().collect { medications ->
                _uiState.value = _uiState.value.copy(
                    medicationsWithSchedules = medications,
                    isLoading = false
                )
            }
        }
    }
}
