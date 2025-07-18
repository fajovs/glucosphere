package com.ensias.glucosphere.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ensias.glucosphere.data.database.entity.GlucoseReading
import com.ensias.glucosphere.data.database.entity.UserProfile
import com.ensias.glucosphere.data.repository.GlucoseReadingRepository
import com.ensias.glucosphere.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class AnalysisUiState(
    val userProfile: UserProfile? = null,
    val totalReadings: Int = 0,
    val averageGlucose: Int = 0,
    val inTargetCount: Int = 0,
    val belowTargetCount: Int = 0,
    val aboveTargetCount: Int = 0,
    val inTargetPercentage: Int = 0,
    val belowTargetPercentage: Int = 0,
    val aboveTargetPercentage: Int = 0,
    val insights: List<String> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val glucoseReadingRepository: GlucoseReadingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisData()
    }

    private fun loadAnalysisData() {
        viewModelScope.launch {
            val thirtyDaysAgo = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))

            combine(
                userProfileRepository.getUserProfile(),
                glucoseReadingRepository.getReadingsFromDate(thirtyDaysAgo)
            ) { profile, readings ->
                if (profile != null) {
                    calculateAnalysis(profile, readings)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }.collect()
        }
    }

    private fun calculateAnalysis(profile: UserProfile, readings: List<GlucoseReading>) {
        val totalReadings = readings.size
        val averageGlucose = if (readings.isNotEmpty()) {
            readings.map { it.glucoseLevel }.average().toInt()
        } else 0

        val inTargetCount = readings.count {
            it.glucoseLevel in profile.targetGlucoseMin..profile.targetGlucoseMax
        }
        val belowTargetCount = readings.count { it.glucoseLevel < profile.targetGlucoseMin }
        val aboveTargetCount = readings.count { it.glucoseLevel > profile.targetGlucoseMax }

        val inTargetPercentage = if (totalReadings > 0) (inTargetCount * 100) / totalReadings else 0
        val belowTargetPercentage = if (totalReadings > 0) (belowTargetCount * 100) / totalReadings else 0
        val aboveTargetPercentage = if (totalReadings > 0) (aboveTargetCount * 100) / totalReadings else 0

        val insights = generateInsights(
            profile,
            totalReadings,
            inTargetPercentage,
            averageGlucose,
            readings
        )

        _uiState.value = AnalysisUiState(
            userProfile = profile,
            totalReadings = totalReadings,
            averageGlucose = averageGlucose,
            inTargetCount = inTargetCount,
            belowTargetCount = belowTargetCount,
            aboveTargetCount = aboveTargetCount,
            inTargetPercentage = inTargetPercentage,
            belowTargetPercentage = belowTargetPercentage,
            aboveTargetPercentage = aboveTargetPercentage,
            insights = insights,
            isLoading = false
        )
    }

    private fun generateInsights(
        profile: UserProfile,
        totalReadings: Int,
        inTargetPercentage: Int,
        averageGlucose: Int,
        readings: List<GlucoseReading>
    ): List<String> {
        val insights = mutableListOf<String>()

        if (totalReadings == 0) {
            return listOf("Start logging your glucose readings to see personalized insights!")
        }

        // Target range insights
        when {
            inTargetPercentage >= 80 -> {
                insights.add("Excellent! You stayed within your target range $inTargetPercentage% of the time.")
            }
            inTargetPercentage >= 60 -> {
                insights.add("Good progress! You stayed within your target range $inTargetPercentage% of the time.")
            }
            else -> {
                insights.add("You stayed within your target range $inTargetPercentage% of the time. Consider consulting with your healthcare provider.")
            }
        }

        // Average glucose insights
        val targetMid = (profile.targetGlucoseMin + profile.targetGlucoseMax) / 2
        when {
            averageGlucose in profile.targetGlucoseMin..profile.targetGlucoseMax -> {
                insights.add("Your average glucose level ($averageGlucose mg/dL) is within your target range.")
            }
            averageGlucose > profile.targetGlucoseMax -> {
                insights.add("Your average glucose level ($averageGlucose mg/dL) is above your target range.")
            }
            else -> {
                insights.add("Your average glucose level ($averageGlucose mg/dL) is below your target range.")
            }
        }

        // Reading frequency insights
        if (totalReadings < 10) {
            insights.add("Try to log more readings for better insights and tracking.")
        } else if (totalReadings >= 30) {
            insights.add("Great job maintaining consistent glucose monitoring!")
        }

        return insights
    }
}
