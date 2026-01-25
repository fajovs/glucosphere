package com.ensias.glucosphere.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.MedicationLog
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import com.ensias.glucosphere.data.repository.MedicationRepository
import com.ensias.glucosphere.data.repository.UserProfileRepository
import javax.inject.Inject
import java.util.*

data class HistoryUiState(
    val isLoading: Boolean = true,
    val glucoseReadings: List<GlucoseReading> = emptyList(),
    val medicationLogs: List<MedicationLog> = emptyList(),
    val medications: Map<Long, Medication> = emptyMap(),
    val userProfile: UserProfile? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val filteredGlucoseReadings: List<GlucoseReading> = emptyList(),
    val filteredMedicationLogs: List<MedicationLog> = emptyList()
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val glucoseReadingRepository: GlucoseReadingRepository,
    private val medicationRepository: MedicationRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadHistoryData()
    }

    private fun loadHistoryData() {
        viewModelScope.launch {
            userProfileRepository.getUserProfile().collectLatest { userProfile ->
                if (userProfile != null) {
                    combine(
                        glucoseReadingRepository.getAllReadings(),
                        medicationRepository.getMedicationLogsForUser(userProfile.id),
                        medicationRepository.getActiveMedications(userProfile.id)
                    ) { readings, logs, medications ->
                        Triple(readings, logs, medications)
                    }.collectLatest { (readings, logs, medications) ->
                        val currentState = _uiState.value
                        val filteredReadings = filterByMonth(readings, currentState.selectedMonth, currentState.selectedYear)
                        val filteredLogs = filterByMonth(logs, currentState.selectedMonth, currentState.selectedYear)

                        _uiState.value = HistoryUiState(
                            isLoading = false,
                            glucoseReadings = readings,
                            medicationLogs = logs,
                            medications = medications.associateBy { it.id },
                            userProfile = userProfile,
                            selectedMonth = currentState.selectedMonth,
                            selectedYear = currentState.selectedYear,
                            filteredGlucoseReadings = filteredReadings,
                            filteredMedicationLogs = filteredLogs
                        )
                    }
                } else {
                    _uiState.value = HistoryUiState(isLoading = false)
                }
            }
        }


    }

    fun updateSelectedMonth(month: Int, year: Int) {
        val currentState = _uiState.value
        val filteredReadings = filterByMonth(currentState.glucoseReadings, month, year)
        val filteredLogs = filterByMonth(currentState.medicationLogs, month, year)

        _uiState.value = currentState.copy(
            selectedMonth = month,
            selectedYear = year,
            filteredGlucoseReadings = filteredReadings,
            filteredMedicationLogs = filteredLogs
        )
    }

    private fun <T> filterByMonth(items: List<T>, month: Int, year: Int): List<T> {
        val calendar = Calendar.getInstance()
        return items.filter { item ->
            val itemDate = when (item) {
                is GlucoseReading -> item.timestamp
                is MedicationLog -> item.scheduledTime
                else -> return@filter false
            }
            calendar.time = itemDate as Date
            calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.YEAR) == year
        }
    }
}
