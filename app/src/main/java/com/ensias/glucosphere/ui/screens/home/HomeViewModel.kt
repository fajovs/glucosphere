package com.ensias.glucosphere.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import com.ensias.glucosphere.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.ensias.glucosphere.data.database.entity.Medication
import com.ensias.glucosphere.data.database.entity.MedicationLog
import com.ensias.glucosphere.data.repository.MedicationRepository
import java.util.Date
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Locale

data class MedicationScheduleInfo(
    val medication: Medication,
    val nextScheduleTime: String,
    val sortTime: Int // For sorting purposes (hour * 60 + minute)
)

data class HomeUiState(
    val username: String = "",
    val userProfile: UserProfile? = null,
    val recentReadings: List<GlucoseReading> = emptyList(),
    val todaysMedicationsWithSchedules: List<MedicationScheduleInfo> = emptyList(),
    val medicationsTakenToday: Set<Long> = emptySet(),
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val glucoseReadingRepository: GlucoseReadingRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                userProfileRepository.getUserProfile(),
                glucoseReadingRepository.getRecentReadings(),
                medicationRepository.getMedicationsWithSchedules(),
                medicationRepository.getRecentMedicationLogs()
            ) { profile, readings, medicationsWithSchedules, medicationLogs ->

                // Get today's date for comparison
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val tomorrow = Calendar.getInstance().apply {
                    time = today
                    add(Calendar.DAY_OF_MONTH, 1)
                }.time

                // Find medications taken today
                val medicationsTakenToday = medicationLogs
                    .filter { log ->
                        log.taken && log.actualTime >= today && log.actualTime < tomorrow
                    }
                    .map { it.medicationId }
                    .toSet()

                // Process medications with their schedule times
                val medicationsWithScheduleInfo = medicationsWithSchedules
                    .filter { it.medication.isActive && it.schedules.isNotEmpty() }
                    .map { medicationWithSchedules ->
                        val activeSchedules = medicationWithSchedules.schedules.filter { it.isActive }

                        if (activeSchedules.isNotEmpty()) {
                            // Get the next upcoming schedule time or the first one for today
                            val currentTime = Calendar.getInstance()
                            val currentHour = currentTime.get(Calendar.HOUR_OF_DAY)
                            val currentMinute = currentTime.get(Calendar.MINUTE)
                            val currentTimeInMinutes = currentHour * 60 + currentMinute

                            // Find next schedule or first schedule of the day
                            val nextSchedule = activeSchedules
                                .map { schedule ->
                                    val scheduleTimeInMinutes = schedule.timeHour * 60 + schedule.timeMinute
                                    Pair(schedule, scheduleTimeInMinutes)
                                }
                                .sortedBy { it.second }
                                .find { it.second >= currentTimeInMinutes }
                                ?: activeSchedules
                                    .map { schedule ->
                                        val scheduleTimeInMinutes = schedule.timeHour * 60 + schedule.timeMinute
                                        Pair(schedule, scheduleTimeInMinutes)
                                    }
                                    .minByOrNull { it.second }

                            nextSchedule?.let { (schedule, timeInMinutes) ->
                                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                                val calendar = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                                    set(Calendar.MINUTE, schedule.timeMinute)
                                }

                                MedicationScheduleInfo(
                                    medication = medicationWithSchedules.medication,
                                    nextScheduleTime = timeFormat.format(calendar.time),
                                    sortTime = timeInMinutes
                                )
                            }
                        } else {
                            null
                        }
                    }
                    .filterNotNull()
                    .sortedBy { it.sortTime } // Sort by time

                _uiState.value = _uiState.value.copy(
                    username = profile?.username ?: "",
                    userProfile = profile,
                    recentReadings = readings,
                    todaysMedicationsWithSchedules = medicationsWithScheduleInfo,
                    medicationsTakenToday = medicationsTakenToday,
                    isLoading = false
                )
            }.collect()
        }
    }

    fun markMedicationAsTaken(medicationId: Long) {
        viewModelScope.launch {
            val now = Date()
            val log = MedicationLog(
                medicationId = medicationId,
                scheduledTime = now,
                actualTime = now,
                taken = true,
                notes = "Marked from home screen"
            )
            medicationRepository.insertMedicationLog(log)
        }
    }
}
